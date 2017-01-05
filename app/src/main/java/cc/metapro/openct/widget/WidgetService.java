package cc.metapro.openct.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;

public class WidgetService extends RemoteViewsService {

    private static List<ClassInfo> mDailyClasses;

    public WidgetService() {

    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetFactory(getApplicationContext(), intent);
    }

    static class WidgetFactory implements RemoteViewsFactory {

        private Context mContext;

        WidgetFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            DBManger manger = DBManger.getInstance(mContext);
            List<ClassInfo> allClasses = manger.getClassInfos();
            Calendar now = Calendar.getInstance();
            boolean isFirstSunday = (now.getFirstDayOfWeek() == Calendar.SUNDAY);
            int weekDay = now.get(Calendar.DAY_OF_WEEK);
            if (isFirstSunday) {
                weekDay = weekDay - 1;
                if (weekDay == 0) {
                    weekDay = 7;
                }
            }
            weekDay--;
            List<ClassInfo> infos = new ArrayList<>();
            for (int i = 0; i < allClasses.size() / 7; i++) {
                ClassInfo c = allClasses.get(7 * i + weekDay);
                if (c != null && c.hasClass(Loader.getCurrentWeek(mContext))) {
                    infos.add(c);
                }
            }
            mDailyClasses = infos;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mDailyClasses.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            if (i < 0 || i >= getCount()) {
                return null;
            }
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
            ClassInfo classInfo = mDailyClasses.get(i);

//            views.setTextColor(R.id.widget_class_name, mContext.getResources().getColor(R.color.colorPrimary));
//            views.setTextColor(R.id.widget_class_place, mContext.getResources().getColor(R.color.colorAccent));
            views.setTextViewText(R.id.widget_class_name, classInfo.getName());
            views.setTextViewText(R.id.widget_class_place, classInfo.getTime() + " 节, 在 " + classInfo.getPlace());
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
