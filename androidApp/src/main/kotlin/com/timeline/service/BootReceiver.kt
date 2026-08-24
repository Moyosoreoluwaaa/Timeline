package com.timeline.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger

/**
 * NEW FILE (this conversation). Nothing in the manifest previously
 * registered BOOT_COMPLETED, so TrackingService did not resume after a
 * device restart until the user manually reopened the app.
 *
 * specialUse (TrackingService's declared foregroundServiceType) is not on
 * Android 15's list of foreground-service types blocked from being started
 * by a BOOT_COMPLETED receiver, so this path should remain open on stock
 * Android. Some OEM skins (MIUI, EMUI, some Oppo/Realme builds) separately
 * gate third-party auto-start behind a manufacturer-specific "Autostart"
 * toggle the user must grant outside this app — BOOT_COMPLETED firing
 * correctly and being allowed to actually start the service are two
 * different permission surfaces on those devices.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.withTag("BootReceiver").d { "Boot completed — restarting TrackingService" }
            val serviceIntent = Intent(context, TrackingService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
