package cc.metapro.openct.classtable;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.ClassInfo;

/**
 * Created by jeffrey on 16/12/3.
 */

public class TodayClassAdapter extends RecyclerView.Adapter<TodayClassAdapter.ClassViewHolder> {

    private List<ClassInfo> mClassInfos;

    private Context mContext;

    private boolean hasClass = true;

    public TodayClassAdapter(Context context) {
        mContext = context;
        mClassInfos = new ArrayList<>(0);
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        ClassInfo c = mClassInfos.get(position);
        holder.setClassName(c.getName());
        holder.setTimePlace(c.getTime(), c.getPlace());
    }

    @Override
    public int getItemCount() {
        return mClassInfos.size();
    }

    public void setNewTodayClassInfos(List<ClassInfo> classInfos, int week) {
        if (classInfos == null || classInfos.size() == 0) {
            mClassInfos = new ArrayList<>(0);
        } else {
            mClassInfos = classInfos;
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
            for (int i = 0; i < classInfos.size() / 7; i++) {
                ClassInfo c = mClassInfos.get(7 * i + weekDay);
                if (c != null && c.hasClass(week)) {
                    infos.add(c);
                }
            }
            if (infos.size() == 0) {
                hasClass = false;
            }
            mClassInfos = infos;
        }
    }

    public boolean hasClassToday() {
        return hasClass;
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mClassName, mTimePlace;

        public ClassViewHolder(View itemView) {
            super(itemView);
            mClassName = (AppCompatTextView) itemView.findViewById(R.id.class_name);
            mTimePlace = (AppCompatTextView) itemView.findViewById(R.id.class_place_time);
        }

        public void setClassName(String className) {
            mClassName.setText(className);
        }

        public void setTimePlace(String time, String place) {
            String content = "";
            if (!Strings.isNullOrEmpty(time)) content += "今天 " + time + " 节 ";
            if (!Strings.isNullOrEmpty(place)) content += "在 " + place;
            mTimePlace.setText(content);
        }
    }
}
