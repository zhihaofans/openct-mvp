package cc.metapro.openct.classtable;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;

class DailyClassAdapter extends RecyclerView.Adapter<DailyClassAdapter.ClassViewHolder> {

    private List<ClassInfo> mClasses;

    private Context mContext;

    private boolean hasClass = true;

    DailyClassAdapter(Context context) {
        mContext = context;
        mClasses = new ArrayList<>(0);
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        ClassInfo c = mClasses.get(position);
        holder.setClassName(c.getName());
        holder.setTimePlace(c.getTime(), c.getPlace());
    }

    @Override
    public int getItemCount() {
        return mClasses.size();
    }

    void setNewTodayClasses(List<ClassInfo> classes, int week) {
        if (classes == null || classes.size() == 0) {
            mClasses = new ArrayList<>(0);
        } else {
            mClasses = classes;
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
            List<ClassInfo> tmpClasses = new ArrayList<>();
            for (int i = 0; i < classes.size() / 7; i++) {
                ClassInfo c = mClasses.get(7 * i + weekDay);
                if (c != null && c.hasClass(week)) {
                    tmpClasses.add(c);
                }
            }
            if (tmpClasses.size() == 0) {
                hasClass = false;
            }
            mClasses = tmpClasses;
        }
    }

    boolean hasClassToday() {
        return hasClass;
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {

        private TextView mClassName, mTimePlace;

        ClassViewHolder(View itemView) {
            super(itemView);
            mClassName = (TextView) itemView.findViewById(R.id.class_name);
            mTimePlace = (TextView) itemView.findViewById(R.id.class_place_time);
        }

        void setClassName(String className) {
            mClassName.setText(className);
        }

        void setTimePlace(String time, String place) {
            String content = "";
            if (!Strings.isNullOrEmpty(time)) content += "今天 " + time + " 节 ";
            if (!Strings.isNullOrEmpty(place)) content += "在 " + place;
            mTimePlace.setText(content);
        }
    }

}
