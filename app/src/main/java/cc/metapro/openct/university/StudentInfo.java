package cc.metapro.openct.university;


import java.util.Calendar;
import java.util.Locale;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.utils.Constants;

/**
 * Created by jeffrey on 16/6/11.
 */
public class StudentInfo {
    //    private University university;
    public int currentWeek = 1, weekOfYearWhenSetCurrentWeek;
    public String
            cmsUsername,
            cmsPassword,
            libUsername,
            libPassword;
    private int weekLimit = 30;
    private ClassInfo[][] mClasses;
    private GradeInfo[] mGrades;
    private BorrowInfo[] mBorrows;

    StudentInfo() {
        currentWeek = 1;
        Calendar cal = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
        weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
    }

    void setCurrentWeek(int week) {
        currentWeek = week;
        Calendar cal = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
        weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
    }

    void updateThisWeek() {
        int lastSetWeek = weekOfYearWhenSetCurrentWeek;
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        if (weekOfYearWhenSetCurrentWeek < lastSetWeek && lastSetWeek <= 53) {
            if (lastSetWeek == 53) {
                currentWeek += weekOfYearWhenSetCurrentWeek;
            } else {
                currentWeek += (52 - lastSetWeek) + weekOfYearWhenSetCurrentWeek;
            }
        } else {
            currentWeek += (weekOfYearWhenSetCurrentWeek - lastSetWeek);
        }
        if (currentWeek >= weekLimit) {
            currentWeek = 1;
        }
    }

//    void setMyclasses(List<ClassInfo> infos) {
//        if (infos == null || infos.size() != university.getDailyClasses() * university.getWeekdays())
//            return;
//        mClasses = new ClassInfo[university.getWeekdays()][university.getDailyClasses()];
//        for (int i = 0; i < university.getWeekdays(); i++) {
//            for (int j = 0; j < university.getDailyClasses(); j++) {
//                mClasses[i][j] = infos.get(i + j * university.getWeekdays());
//            }
//        }
//    }

    void setMyGrades(GradeInfo[] infos) {
        mGrades = infos;
    }

    void setMyBorrows(BorrowInfo[] infos) {
        mBorrows = infos;
    }

    boolean hasClassInfos() {
        return mClasses != null;
    }

    GradeInfo[] getGrades() {
        return mGrades;
    }

    ClassInfo getClass(int weekDay, int classSeq) {
        if (mClasses == null || mClasses.length < 2) return null;
        if (weekDay > 0 && classSeq > 0
                && weekDay <= mClasses.length
                && classSeq <= mClasses[0].length) {
            return mClasses[weekDay - 1][classSeq - 1];
        }
        return null;
    }

    BorrowInfo[] getBorrowInfos() {
        return mBorrows;
    }

    String getUserInfoJson() {
        return Constants.getGson().toJson(this);
    }

}
