package cc.metapro.openct.data;

import org.jsoup.select.Elements;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.university.CmsFactory;
import cc.metapro.openct.utils.RE;

public class GradeInfo {
    private String
            mClassCode, mClassName, mClassType, mPoints,
            mGradeSummary, mGradePractice, mGradeCommon,
            mGradeMidExam, mGradeFinalExam, mGradeMakeup;

    public GradeInfo(Elements tds, CmsFactory.GradeTableInfo gradeInfo) {
        try {
            mClassCode = tds.get(gradeInfo.mClassCodeIndex).text();
            mClassName = tds.get(gradeInfo.mClassNameIndex).text();
            mClassType = tds.get(gradeInfo.mClassTypeIndex).text();
            mPoints = tds.get(gradeInfo.mPointsIndex).text();
            mGradeSummary = tds.get(gradeInfo.mGradeSummaryIndex).text();
            mGradePractice = tds.get(gradeInfo.mGradePracticeIndex).text();
            mGradeCommon = tds.get(gradeInfo.mGradeCommonIndex).text();
            mGradeMidExam = tds.get(gradeInfo.mGradeMidExamIndex).text();
            mGradeFinalExam = tds.get(gradeInfo.mGradeFinalExamIndex).text();
            mGradeMakeup = tds.get(gradeInfo.mGradeMakeupIndex).text();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
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

        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        return sb.toString();
    }

    public String getClassName() {
        return mClassName;
    }

    public String getGradeSummary() {
        return mGradeSummary;
    }
}
