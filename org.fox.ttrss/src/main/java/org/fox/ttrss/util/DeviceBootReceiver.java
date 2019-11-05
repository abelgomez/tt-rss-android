package org.fox.ttrss.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.fox.ttrss.CommonActivity;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CommonActivity.requestWidgetUpdate(context);
            CommonActivity.setupWidgetUpdates(context);
        }
    }
}
