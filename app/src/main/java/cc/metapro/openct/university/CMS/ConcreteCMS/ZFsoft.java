package cc.metapro.openct.university.cms.concretecms;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.university.cms.AbstractCMS;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.PageStringUtils;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/5.
 */

public class ZFsoft extends AbstractCMS {

    public ZFsoft(CMSInfo cmsInfo) {
        super(cmsInfo);
    }

    @Nullable
    @Override
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) throws IOException, LoginException {
        String userCenter = login(loginMap);

        if (Strings.isNullOrEmpty(userCenter)) return null;

        String tableURL = null;
        Document doc = Jsoup.parse(userCenter, mCMSInfo.mCmsURL);
        Elements addresses = doc.select("a");
        for (Element e : addresses) {
            if ("GetMc('学生个人课表');".equals(e.attr("onclick"))) {
                tableURL = mCMSInfo.mCmsURL + e.attr("href");
                break;
            }
        }

        if (Strings.isNullOrEmpty(tableURL)) return null;

        Map<String, String> header = new HashMap<>(1);
        header.put("Referer", mCMSInfo.mCmsURL);
        String tablePage = OkCurl.curlSynGET(tableURL, header, null).body().string();

        if (Strings.isNullOrEmpty(tablePage)) return null;

        return generateClassInfos(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));
    }

    @Nullable
    @Override
    public List<GradeInfo> getGradeInfos(Map<String, String> loginMap) throws IOException, LoginException {
        String userCenter = login(loginMap);
        // login fail, no more actions
        if (Strings.isNullOrEmpty(userCenter)) return null;

        String tableURL = null;
        Document doc = Jsoup.parse(userCenter, mCMSInfo.mCmsURL);
        Elements ele = doc.select("a");
        for (Element e : ele) {
            if ("GetMc('平时成绩查询');".equals(e.attr("onclick"))) {
                tableURL = mCMSInfo.mCmsURL + e.attr("href");
                break;
            }
        }

        if (Strings.isNullOrEmpty(tableURL)) return null;

        Map<String, String> header = new HashMap<>(1);
        header.put("Referer", mCMSInfo.mCmsURL);
        String tablePage = OkCurl.curlSynGET(tableURL, header, null).body().string();

        if (Strings.isNullOrEmpty(tablePage)) return null;

        return generateGradeInfos(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));
    }

}
