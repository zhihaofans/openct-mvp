package cc.metapro.openct.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import cc.metapro.openct.R;

public class DailyClassWidget extends AppWidgetProvider {

    public static final String UPDATE_ITEMS = "cc.metapro.openct.action.UPDATE_ITEMS";

    public static void update(Context context) {
        Intent intent = new Intent(UPDATE_ITEMS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.sendBroadcast(intent);
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {
        Intent intent = new Intent(context, WidgetService.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily_class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, DailyClassWidget.class);
        int[] ids = manager.getAppWidgetIds(component);

        switch (action) {
            case UPDATE_ITEMS:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIME_TICK:
            case Intent.ACTION_USER_PRESENT:
                onUpdate(context, manager, ids);
                break;
        }

        super.onReceive(context, intent);
    }

}

