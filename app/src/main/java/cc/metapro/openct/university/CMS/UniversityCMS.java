package cc.metapro.openct.university.CMS;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.CMS.CMSInterface.CMSAssembler;
import cc.metapro.openct.university.CMS.CMSInterface.CMSLogin;
import cc.metapro.openct.university.CMS.CMSInterface.CMSPageGetter;
import cc.metapro.openct.university.CMS.CMSInterface.CMSPageParser;
import cc.metapro.openct.university.URLGenerator;

/**
 * Created by jeffrey on 11/15/16.
 */

public abstract class UniversityCMS implements
        CMSAssembler, CMSPageGetter,
        CMSPageParser, CMSLogin,
        URLGenerator {

    protected ClassInfo getClassInfo(ClassTableInfo classTableInfo, String[] strings) {
        Pattern teacherPattern;
        String teacher = strings[classTableInfo.classTeacherIndex];
        if (classTableInfo.teacherRegularExp != null && !"".equals(classTableInfo.teacherRegularExp)) {
            teacherPattern = Pattern.compile(classTableInfo.teacherRegularExp);
            Matcher m = teacherPattern.matcher(teacher);
            if (m.find()) {
                teacher = m.group();
            } else {
                teacher = "";
            }
        }
        return new ClassInfo(
                strings[classTableInfo.classNameIndex],
                strings[classTableInfo.classTypeIndex],
                teacher,
                strings[classTableInfo.classPlaceIndex]);
    }

    protected GradeInfo getGradeInfo(GradeTableInfo gradeTableInfo, Element gradeTr) {
        Elements ele = gradeTr.select("td");
        return new GradeInfo(
                ele.get(gradeTableInfo.classCodeIndex).text(),
                ele.get(gradeTableInfo.classNameIndex).text(),
                ele.get(gradeTableInfo.classTypeIndex).text(),
                ele.get(gradeTableInfo.pointsIndex).text(),
                ele.get(gradeTableInfo.gradeSummaryIndex).text(),
                ele.get(gradeTableInfo.gradePracticeIndex).text(),
                ele.get(gradeTableInfo.gradeCommonIndex).text(),
                ele.get(gradeTableInfo.gradeMidExamIndex).text(),
                ele.get(gradeTableInfo.gradeFinalExamIndex).text(),
                ele.get(gradeTableInfo.gradeMakeupIndex).text());
    }
}
