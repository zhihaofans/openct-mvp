package cc.metapro.openct.university.CMS.CMSInterface;

import java.io.IOException;

/**
 * Created by jeffrey on 11/20/16.
 */

public interface CMSPageGetter {

    /**
     * get verify code pic and store it
     *
     * @param path is the place where to store verify code
     */

    void getVCodePic(String path) throws IOException;

    /**
     * get login page, it may have some info
     *
     * @return
     */
    String getLoginPage() throws IOException;

    /**
     * use tableURL to get table page
     *
     * @param tableURL
     * @return classTablePage
     */
    String getWholeTablePage(String tableURL) throws IOException;

    /**
     * use gradeURL to get grade page
     *
     * @param gradeURL
     * @return gradePage
     */
    String getWholeGradePage(String gradeURL) throws IOException;

}
