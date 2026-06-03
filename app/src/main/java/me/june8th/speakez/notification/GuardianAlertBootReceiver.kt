package me.june8th.speakez.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import timber.log.Timber

class GuardianAlertBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        runCatching {
            ContextCompat.startForegroundService(
                context,
                GuardianAlertForegroundService.startIntent(context),
            )
        }.onFailure { throwable ->
            Timber.w(throwable, "Failed to restart guardian alert monitoring")
        }
    }
}
