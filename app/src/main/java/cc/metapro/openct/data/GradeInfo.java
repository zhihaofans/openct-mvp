package cc.metapro.openct.data;

import cc.metapro.openct.utils.RE;

/**
 * Created by jeffrey on 11/24/16.
 */

public class GradeInfo {
    public String
            classCode, className, classType, points,
            gradeSummary, gradePractice, gradeCommon,
            gradeMidExam, gradeFinalExam, gradeMakeup;

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
        this.classCode = classCode;
        this.className = className;
        this.classType = classType;
        this.points = points;
        this.gradeSummary = gradeSummary;
        this.gradePractice = gradePractice;
        this.gradeCommon = gradeCommon;
        this.gradeMidExam = gradeMidExam;
        this.gradeFinalExam = gradeFinalExam;
        this.gradeMakeup = gradeMakeup;
    }

    @Override
    public String toString() {
        return "课程名称: " + className + "\n总评成绩: " + gradeSummary;
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        if (!RE.isEmpty(className)) sb.append("名称: ").append(className).append("\n\n");
        if (!RE.isEmpty(classType)) sb.append("类型: ").append(classType).append("\n\n");
        if (!RE.isEmpty(points)) sb.append("学分: ").append(points).append("\n\n");
        if (!RE.isEmpty(gradeSummary))
            sb.append("成绩: ").append(gradeSummary).append("\n\n");
        if (!RE.isEmpty(gradeCommon))
            sb.append("平时成绩: ").append(gradeCommon).append("\n\n");
        if (!RE.isEmpty(gradePractice))
            sb.append("实践成绩: ").append(gradePractice).append("\n\n");
        if (!RE.isEmpty(gradeMidExam))
            sb.append("期中成绩: ").append(gradeMidExam).append("\n\n");
        if (!RE.isEmpty(gradeFinalExam))
            sb.append("期末成绩: ").append(gradeFinalExam).append("\n\n");
        if (!RE.isEmpty(gradeMakeup))
            sb.append("补考成绩: ").append(gradeMakeup).append("\n\n");
        return sb.toString();
    }

    public String getClassName() {
        return className;
    }

    public String getGradeSummary() {
        return gradeSummary;
    }
}
