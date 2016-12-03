package cc.metapro.openct.data;

import com.google.common.base.Strings;

import java.io.Serializable;

import cc.metapro.openct.utils.RE;

/**
 * Created by jeffrey on 16/10/9.
 */
public class ClassInfo implements Serializable {
    private String className, type, teacher, place;
    private int time_start = -1, time_end = -1;
    private int
            duringStart_1 = -1,
            duringEnd_1 = -1,
            duringStart_2 = -1,
            duringEnd_2 = -1;
    private ClassInfo subClassInfo;
    private String rawInfo;

    public ClassInfo() {
        this.className = "";
        this.type = "";
        this.teacher = "";
        this.place = "";
    }

    public ClassInfo(String classname, String type, String teacher, String place) {
        this.className = classname;
        this.type = type;
        this.teacher = teacher;
        this.place = place;
    }

    public ClassInfo setDuring(int start_1, int end_1, int start_2, int end_2) {
        duringStart_1 = start_1;
        if (end_1 < start_1) {
            end_1 = start_1;
        }
        duringEnd_1 = end_1;
        duringStart_2 = start_2;
        if (end_2 < start_2) {
            end_2 = start_2;
        }
        duringEnd_2 = end_2;
        return this;
    }

    public ClassInfo setTime(int start, int end) {
        time_start = start;
        time_end = end;
        return this;
    }

    public boolean hasClass(int thisWeek) {
        return ((thisWeek >= duringStart_1 && thisWeek <= duringEnd_1)
                || (thisWeek >= duringStart_2 && thisWeek <= duringEnd_2));
    }

    public boolean hasSubClass() {
        return subClassInfo != null;
    }

    public ClassInfo getSubClassInfo() {
        return subClassInfo;
    }

    public void setSubClassInfo(ClassInfo subClassInfo) {
        this.subClassInfo = subClassInfo;
    }

    @Override
    public String toString() {
        return Strings.isNullOrEmpty(className) ? rawInfo : className + "@" + place;
    }

    public String getClassName() {
        return className;
    }

    public String getTime() {
        String time = "";
        if (time_start != -1) {
            time = time_start + " - " + time_end;
        }
        return time;
    }

    public String getPlace() {
        return place;
    }

    private String getDuring() {
        String during = "";
        if (duringStart_1 != -1) {
            if (duringStart_2 != -1) {
                during = "第 " +
                        duringStart_1 + " - " + duringEnd_1 + ", " +
                        duringStart_2 + " - " + duringEnd_2 + " 周";
            } else {
                during = "第 " + duringStart_1 + " - " + duringEnd_1 + " 周";
            }
        }
        return during;
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(className)) sb.append("课程名称: ").append(className).append("\n\n");
        if (!Strings.isNullOrEmpty(type)) sb.append("课程类型: ").append(type).append("\n\n");
        String time = getTime();
        if (!Strings.isNullOrEmpty(time)) sb.append("上课时间: ").append(time).append("\n\n");
        if (!Strings.isNullOrEmpty(place)) sb.append("上课地点: ").append(place).append("\n\n");
        String during = getDuring();
        if (!Strings.isNullOrEmpty(during)) sb.append("课程周期: ").append(during).append("\n\n");
        if (!Strings.isNullOrEmpty(rawInfo)) sb.append("原始信息\n\n").append(rawInfo);
        return sb.toString();
    }

    public ClassInfo setRawInfo(String rawInfo) {
        this.rawInfo = rawInfo;
        return this;
    }

    public boolean isEmpty() {
        return RE.isEmpty(rawInfo);
    }

    public int getClassLength() {
        if (time_start == -1) return -1;
        return time_end - time_start + 1;
    }
}
