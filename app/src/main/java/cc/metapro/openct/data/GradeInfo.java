package cc.metapro.openct.data;

import cc.metapro.openct.utils.RE;

/**
 * Created by jeffrey on 11/24/16.
 */

public class GradeInfo {
    private String
            mClassCode, mClassName, mClassType, mPoints,
            mGradeSummary, mGradePractice, mGradeCommon,
            mGradeMidExam, mGradeFinalExam, mGradeMakeup;

    public GradeInfo(
            String classCode,
            String className,
            String classType,
            String points,
            String gradeSummary,
            String gradePractice,
            String gradeCommon,
            String gradeMidExam,
            String gradeFinalExam,
            String gradeMakeup) {
        mClassCode = classCode;
        mClassName = className;
        mClassType = classType;
        mPoints = points;
        mGradeSummary = gradeSummary;
        mGradePractice = gradePractice;
        mGradeCommon = gradeCommon;
        mGradeMidExam = gradeMidExam;
        mGradeFinalExam = gradeFinalExam;
        mGradeMakeup = gradeMakeup;
    }

    @Override
    public String toString() {
        return "课程名称: " + mClassName + "\n总评成绩: " + mGradeSummary;
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        if (!RE.isEmpty(mClassName)) sb.append("名称: ").append(mClassName).append("\n\n");
        if (!RE.isEmpty(mClassType)) sb.append("类型: ").append(mClassType).append("\n\n");
        if (!RE.isEmpty(mPoints)) sb.append("学分: ").append(mPoints).append("\n\n");
        if (!RE.isEmpty(mGradeSummary))
            sb.append("成绩: ").append(mGradeSummary).append("\n\n");
        if (!RE.isEmpty(mGradeCommon))
            sb.append("平时成绩: ").append(mGradeCommon).append("\n\n");
        if (!RE.isEmpty(mGradePractice))
            sb.append("实践成绩: ").append(mGradePractice).append("\n\n");
        if (!RE.isEmpty(mGradeMidExam))
            sb.append("期中成绩: ").append(mGradeMidExam).append("\n\n");
        if (!RE.isEmpty(mGradeFinalExam))
            sb.append("期末成绩: ").append(mGradeFinalExam).append("\n\n");
        if (!RE.isEmpty(mGradeMakeup))
            sb.append("补考成绩: ").append(mGradeMakeup).append("\n\n");
        return sb.toString();
    }

    public String getClassName() {
        return mClassName;
    }

    public String getGradeSummary() {
        return mGradeSummary;
    }
}
