package cc.metapro.openct.university.CMS;

/**
 * Created by jeffrey on 11/24/16.
 */

public class ClassTableInfo {

    public int
            mDailyClasses,
            mNameIndex,
            mTypeIndex,
            mDuringIndex,
            mPlaceIndex,
            mTimeIndex,
            mTeacherIndex,
            mClassStringCount,
            mClassLength;

    public String
            mClassTableID,
            mClassInfoStart;

    // Regular Expressions to parse class infos
    public String
            mNameRE, mTypeRE,
            mDuringRE, mTimeRE,
            mTeacherRE, mPlaceRE;

}
