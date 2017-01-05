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

    public static final String DATE_CHANGED = "android.intent.action.DATE_CHANGED";
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String DATE_SET = "android.intent.action.TIME_SET";

    public static final String UPDATE_ITEMS = "cc.metapro.openct.action.UPDATE_ITEMS";

    public static void update(Context context) {
        Intent intent = new Intent(UPDATE_ITEMS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.sendBroadcast(intent);
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        Intent intent = new Intent(context, WidgetService.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.daily_class_widget);
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

        if (UPDATE_ITEMS.equals(action) || BOOT_COMPLETED.equals(action)
                || DATE_CHANGED.equals(action) || DATE_SET.equals(action)) {
            onUpdate(context, manager, ids);
        }
        super.onReceive(context, intent);
    }
}

