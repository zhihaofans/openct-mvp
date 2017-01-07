package cc.metapro.openct.data;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.UidGenerator;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.university.CmsFactory;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RE;

public class ClassInfo implements Serializable {

    private final static Pattern oddPattern = Pattern.compile("单周?");
    private final static Pattern evenPattern = Pattern.compile("双周?");

    private String mName, mType, mTime, mDuring, mTeacher, mPlace;
    private ClassInfo mSubClassInfo;
    private boolean mOddWeek, mEvenWeek, mInactive;

    public ClassInfo() {
    }

    public ClassInfo(String content, CmsFactory.ClassTableInfo info) {
        String[] classes = content.split(Constants.BR_REPLACER + Constants.BR_REPLACER + "+");
        String s = classes[0];
        String[] tmp = s.split(Constants.BR_REPLACER);
        if (tmp.length == info.mClassStringCount) {
            mName = infoParser(info.mNameRE, tmp[info.mNameIndex]);
            mType = infoParser(info.mTypeRE, tmp[info.mTypeIndex]);
            mTeacher = infoParser(info.mTeacherRE, tmp[info.mTeacherIndex]);
            mPlace = infoParser(info.mPlaceRE, tmp[info.mPlaceIndex]);
            mTime = infoParser(info.mTimeRE, tmp[info.mTimeIndex]);
            mDuring = infoParser(info.mDuringRE, tmp[info.mDuringIndex]);

            mOddWeek = oddPattern.matcher(tmp[info.mTimeIndex]).find();
            mEvenWeek = evenPattern.matcher(tmp[info.mTimeIndex]).find();
        }

        // create all subclass
        if (classes.length > 1) {
            String subContent = "";
            for (int i = 1; i < classes.length; i++) {
                if (i < classes.length - 1) {
                    subContent += classes[i] + Constants.BR_REPLACER + Constants.BR_REPLACER;
                } else {
                    subContent += classes[i];
                }
            }
            mSubClassInfo = new ClassInfo(subContent, info);
        }
    }

    private String infoParser(String re, String content) {
        if (!Strings.isNullOrEmpty(re)) {
            Pattern pattern = Pattern.compile(re);
            Matcher m = pattern.matcher(content);
            if (m.find()) content = m.group();
        }
        return content;
    }

    public boolean hasClass(int week) {
        if (mInactive) return false;
        if (Strings.isNullOrEmpty(mDuring)) return false;
        int[] startEnd = RE.getStartEnd(mDuring);
        if (week >= startEnd[0] && week <= startEnd[1]) {
            if (mOddWeek && (week % 2 == 1)) return true;
            if (mEvenWeek && (week % 2 == 0)) return true;
            if (!mEvenWeek && !mOddWeek) return true;
        }
        return false;
    }

    public int getLength() {
        if (Strings.isNullOrEmpty(mTime)) return 1;
        int[] startEnd = RE.getStartEnd(mTime);
        try {
            if (startEnd[0] == -1) return Integer.parseInt(mTime);
        } catch (Exception e) {
            return 1;
        }
        return startEnd[1] - startEnd[0] + 1;
    }

    @Nullable
    private String getDuring() {
        return Strings.isNullOrEmpty(mDuring) ? null : mDuring;
    }

    @Nullable
    public String getTime() {
        return Strings.isNullOrEmpty(mTime) ? null : mTime;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mName);
    }

    public boolean hasSubClass() {
        return mSubClassInfo != null;
    }

    public ClassInfo getSubClassInfo() {
        return mSubClassInfo;
    }

    public String getName() {
        return Strings.isNullOrEmpty(mName) ? "" : mName;
    }

    public String getPlace() {
        return Strings.isNullOrEmpty(mPlace) ? "" : mPlace;
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();

        if (!RE.isEmpty(mName)) sb.append("课程名称: ").append(mName).append("\n\n");
        if (!RE.isEmpty(mType)) sb.append("课程类型: ").append(mType).append("\n\n");

        String time = getTime();
        if (!RE.isEmpty(time)) sb.append("上课时间: ").append(time).append("\n\n");

        if (!RE.isEmpty(mPlace)) sb.append("上课地点: ").append(mPlace).append("\n\n");
        if (!RE.isEmpty(mTeacher)) sb.append("授课教师: ").append(mTeacher).append("\n\n");

        String during = getDuring();
        if (!RE.isEmpty(during)) sb.append("课程周期: ").append(during).append("\n\n");

        if (hasSubClass()) sb.append("\n\n").append(mSubClassInfo.toFullString());

        if (sb.length() > 2 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public boolean contains(ClassInfo info) {
        if (hasSubClass()) {
            return getSubClassInfo().contains(info);
        }
        return equals(info);
    }

    public void deActive() {
        mInactive = true;
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

    @Nullable
    public VEvent getEvent(int week, int weekDay) {
        if (isEmpty() || mInactive)
            return null;
        try {
            // set end Date
            Calendar now = Calendar.getInstance();
            int[] startEnd = RE.getStartEnd(mDuring);

            int dayAfter = (now.get(Calendar.WEEK_OF_YEAR) + startEnd[1] - week - 1) * 7;

            // repeat every week until endDate
            Recur recur = new Recur(Recur.WEEKLY,
                    new DateTime(RE.getDateAfter(now.getTime(), dayAfter)));
            recur.setInterval(1);
            RRule rule = new RRule(recur);

            // set event period
            int dayBefore = Math.abs(
                    (now.get(Calendar.WEEK_OF_YEAR) + startEnd[0] - week - 1) * 7);

            Calendar dailyStart = Calendar.getInstance();
            dailyStart.setTime(RE.getDateBefore(now.getTime(), dayBefore));
            dailyStart.set(Calendar.HOUR_OF_DAY, 8);
            dailyStart.set(Calendar.MINUTE, 0);
            dailyStart.set(Calendar.DAY_OF_WEEK, weekDay);
            DateTime start = new DateTime(dailyStart.getTime());

            Calendar dailyEnd = Calendar.getInstance();
            dailyEnd.setTime(RE.getDateBefore(now.getTime(), dayBefore));
            dailyEnd.set(Calendar.HOUR_OF_DAY, 17);
            dailyEnd.set(Calendar.MINUTE, 0);
            dailyEnd.set(Calendar.DAY_OF_WEEK, weekDay);
            DateTime end = new DateTime(dailyEnd.getTime());

            ParameterList paraList = new ParameterList();
            paraList.add(ParameterFactoryImpl.getInstance().createParameter
                    (Value.PERIOD.getName(), Value.PERIOD.getValue()));

            PeriodList periodList = new PeriodList();
            periodList.add(new Period(start, end));
            RDate rdate = new RDate(paraList, periodList);

            // create event, repeat weekly
            VEvent event = new VEvent(start, end, mName + "@" + mPlace + ", " + mTime);

            // set uid
            event.getProperties().add(new Uid(new UidGenerator("OPENCT").generateUid().getValue()));

            // set event
            event.getProperties().add(rdate);
            event.getProperties().add(rule);
            return event;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
