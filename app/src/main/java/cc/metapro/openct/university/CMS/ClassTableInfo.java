package cc.metapro.openct.university.CMS;

/**
 * Created by jeffrey on 11/24/16.
 */

public class ClassTableInfo {

    public int
            weekdays, dailyClasses,
            classNameIndex,
            classTypeIndex,
            classPlaceIndex,
            classTimeIndex,
            classTeacherIndex,
            classStringCount,
            classLength;

    public boolean duringFront;
    public String
            classTableID,
            classInfoStart,
            classStringSep,
            timeAndDuringSep,
            teacherRegularExp;
}
