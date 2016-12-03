package cc.metapro.openct.university.CMS.CMSInterface;

import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Created by jeffrey on 11/20/16.
 */

public interface CMSPageParser {

    /**
     * all needed page parser
     * using jsoup or any html parser
     */

    // get viewstate from login page
    String getVIEWSTATE(String html);

    // get table address from user main_activity page
    String getTableAddr(String html);

    String getGradeAddr(String html);

    // get table from table page
    String parseTable(String tablePage);

    String parseGrade(String gradePage);

    // get strings from selected classTable
    List<String> classTableToList(String classTable);

    List<Element> gradeTableToList(String gradeTable);

}
