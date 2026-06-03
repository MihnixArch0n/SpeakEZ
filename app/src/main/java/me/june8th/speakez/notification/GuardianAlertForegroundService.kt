package me.june8th.speakez.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.june8th.speakez.MainActivity
import me.june8th.speakez.R
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.domain.repository.GuardianRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class GuardianAlertForegroundService : Service() {
    @Inject
    lateinit var guardianRepository: GuardianRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    @Inject
    lateinit var emergencyAlertNotifier: EmergencyAlertNotifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private val notifiedAlertIds by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getStringSet(KEY_NOTIFIED_ALERT_IDS, emptySet()).orEmpty()
            .toMutableSet()
    }
    private var isMonitoring = false
    private var alertJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MONITORING -> {
                stopMonitoringFromNotification()
                return START_NOT_STICKY
            }
            else -> startMonitoring()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        startForeground(
            SERVICE_NOTIFICATION_ID,
            buildServiceNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING,
        )

        if (isMonitoring) return
        isMonitoring = true

        serviceScope.launch {
            combine(
                authRepository.profileState,
                appSettingsRepository.guardianBackgroundMonitoringEnabled,
            ) { profile, backgroundMonitoringEnabled ->
                profile?.uid != null &&
                    profile.isGuest == false &&
                    profile.accountType == AccountType.GUARDIAN &&
                    backgroundMonitoringEnabled
            }
                .distinctUntilChanged()
                .collectLatest { shouldMonitor ->
                    if (shouldMonitor) {
                        startAlertCollection()
                    } else {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
        }
    }

    private fun startAlertCollection() {
        if (alertJob?.isActive == true) return
        alertJob = serviceScope.launch {
            guardianRepository.observeUnreadEmergencyAlerts()
                .catch { throwable ->
                    Timber.w(throwable, "Guardian alert background monitoring failed")
                }
                .collectLatest { alerts ->
                    alerts.forEach { alert ->
                        if (notifiedAlertIds.add(alert.id)) {
                            persistNotifiedAlertIds()
                            emergencyAlertNotifier.show(alert)
                        }
                    }
                }
        }
    }

    private fun stopMonitoringFromNotification() {
        serviceScope.launch {
            runCatching { appSettingsRepository.setGuardianBackgroundMonitoringEnabled(false) }
                .onFailure { Timber.w(it, "Failed to disable guardian background monitoring") }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun persistNotifiedAlertIds() {
        val trimmedIds = notifiedAlertIds.toList().takeLast(MAX_STORED_ALERT_IDS).toSet()
        if (trimmedIds.size != notifiedAlertIds.size) {
            notifiedAlertIds.clear()
            notifiedAlertIds.addAll(trimmedIds)
        }
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_NOTIFIED_ALERT_IDS, trimmedIds)
            .apply()
    }

    private fun ensureChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Theo dõi cảnh báo giám hộ",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Giữ SpeakEZ chạy nền để nhận cảnh báo khẩn cấp"
                setShowBadge(false)
            },
        )
    }

    private fun buildServiceNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_STOP_MONITORING,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SpeakEZ đang theo dõi cảnh báo")
            .setContentText("Người giám hộ sẽ nhận thông báo khi có cảnh báo khẩn cấp.")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(Notification.PRIORITY_LOW)
            .addAction(
                Notification.Action.Builder(
                    R.mipmap.ic_launcher,
                    "Dừng theo dõi",
                    stopPendingIntent,
                ).build(),
            )
            .build()
    }

    companion object {
        private const val ACTION_START_MONITORING = "me.june8th.speakez.action.START_GUARDIAN_ALERT_MONITORING"
        private const val ACTION_STOP_MONITORING = "me.june8th.speakez.action.STOP_GUARDIAN_ALERT_MONITORING"
        private const val SERVICE_CHANNEL_ID = "guardian_alert_background_monitoring"
        private const val SERVICE_NOTIFICATION_ID = 2001
        private const val REQUEST_OPEN_APP = 2101
        private const val REQUEST_STOP_MONITORING = 2102
        private const val PREFS_NAME = "guardian_alert_monitoring"
        private const val KEY_NOTIFIED_ALERT_IDS = "notified_alert_ids"
        private const val MAX_STORED_ALERT_IDS = 200

        fun startIntent(context: Context): Intent {
            return Intent(context, GuardianAlertForegroundService::class.java).apply {
                action = ACTION_START_MONITORING
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, GuardianAlertForegroundService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
        }
    }
}
