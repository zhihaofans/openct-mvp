package cc.metapro.openct.data;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.Serializable;
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
        String[] classInfos = content.split(Constants.BR_REPLACER + Constants.BR_REPLACER + "+");
        String s = classInfos[0];
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

        // create all subclass infos
        if (classInfos.length > 1) {
            String subContent = "";
            for (int i = 1; i < classInfos.length; i++) {
                if (i < classInfos.length - 1) {
                    subContent += classInfos[i] + Constants.BR_REPLACER + Constants.BR_REPLACER;
                } else {
                    subContent += classInfos[i];
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
        String during = getDuring();
        if (!RE.isEmpty(mTeacher)) sb.append("授课教师: ").append(mTeacher).append("\n\n");
        if (!RE.isEmpty(during)) sb.append("课程周期: ").append(during).append("\n\n");
        if (hasSubClass()) sb.append("\n\n").append(mSubClassInfo.toFullString());

        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        return sb.toString();
    }

    public boolean contains(ClassInfo info) {
        if (hasSubClass()) {
            return getSubClassInfo().contains(info);
        }
        return equals(info);
    }

    public void deactive() {
        mInactive = true;
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }
}
