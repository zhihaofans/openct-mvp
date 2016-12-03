package cc.metapro.openct.university.CMS.CMSInterface;

import org.jsoup.nodes.Element;

import java.util.List;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;

/**
 * Created by jeffrey on 11/20/16.
 */

public interface CMSAssembler {

    /**
     * parse ClassInfo from string list
     *
     * @param classes
     * @return ClassInfo list
     */

    List<ClassInfo> generateClasses(List<String> classes);

    List<GradeInfo> generatrGrades(List<Element> grades);
}
