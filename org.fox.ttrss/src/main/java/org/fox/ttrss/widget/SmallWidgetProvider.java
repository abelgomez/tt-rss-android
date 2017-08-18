package org.fox.ttrss.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.fox.ttrss.OnlineActivity;
import org.fox.ttrss.R;

public class SmallWidgetProvider extends AppWidgetProvider {
	private final String TAG = this.getClass().getSimpleName();

	public static final String ACTION_REQUEST_UPDATE = "org.fox.ttrss.WIDGET_FORCE_UPDATE";
    public static final String ACTION_UPDATE_RESULT = "org.fox.ttrss.WIDGET_UPDATE_RESULT";

    @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        Intent intent = new Intent(context, OnlineActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        views.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

        SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(context);
        String widgetBackground = prefs.getString("widget_background", "WB_LIGHT");

        Log.d(TAG, "widget bg: " + widgetBackground);

        if ("WB_LIGHT".equals(widgetBackground)) {
            views.setViewVisibility(R.id.widget_dark, View.INVISIBLE);
            views.setViewVisibility(R.id.widget_light, View.VISIBLE);
        } else if ("WB_DARK".equals(widgetBackground)) {
            views.setViewVisibility(R.id.widget_dark, View.VISIBLE);
            views.setViewVisibility(R.id.widget_light, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_dark, View.INVISIBLE);
            views.setViewVisibility(R.id.widget_light, View.INVISIBLE);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, views);

        Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        context.startService(serviceIntent);

    }


	@Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SmallWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		if (ACTION_REQUEST_UPDATE.equals(intent.getAction())) {
            Log.d(TAG, "onReceive: got update request");

		    onUpdate(context, appWidgetManager, appWidgetIds);

		} else if (ACTION_UPDATE_RESULT.equals(intent.getAction())) {
            int unread = intent.getIntExtra("unread", -1);
            int resultCode = intent.getIntExtra("resultCode", WidgetUpdateService.UPDATE_RESULT_ERROR_OTHER);

            Log.d(TAG, "onReceive: got update result from service: " + unread + " " + resultCode);

            updateWidgetsText(context, appWidgetManager, appWidgetIds, unread, resultCode);
        } else {
            super.onReceive(context, intent);
        }
	}

    private void updateWidgetsText(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int unread, int resultCode) {

        Intent intent = new Intent(context, OnlineActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        views.setOnClickPendingIntent(R.id.widget_main, pendingIntent);

        String viewText;

        switch (resultCode) {
            case WidgetUpdateService.UPDATE_RESULT_OK:
                viewText = String.valueOf(unread);
                break;
            case WidgetUpdateService.UPDATE_IN_PROGRESS:
                viewText = "...";
                break;
            default:
                viewText = "?";
        }

        views.setTextViewText(R.id.widget_unread_counter, viewText);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

}
