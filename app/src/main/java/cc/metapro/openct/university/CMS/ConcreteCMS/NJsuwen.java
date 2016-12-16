package cc.metapro.openct.university.CMS.ConcreteCMS;

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

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.CMS.AbstractCMS;
import cc.metapro.openct.university.University.CMSInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/6.
 */

public class NJsuwen extends AbstractCMS {

    public NJsuwen(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;

        if (mCMSInfo.mCmsURL.endsWith("/"))
            mCMSInfo.mCmsURL = mCMSInfo.mCmsURL.substring(0, mCMSInfo.mCmsURL.length() - 1);

    }

    @Override
    protected String login(Map<String, String> loginMap) {
        String userCenter = null;
        try {
            int i = 0;
            for (; i < 10; i++) {
                // Prepare login url and get dyn part
                if (mCMSInfo.mDynLoginURL) {
                    mDynPart = getDynPart();

                    if (Strings.isNullOrEmpty(mDynPart)) continue;

                    mLoginURL = mCMSInfo.mCmsURL + "/" + mDynPart + "/default.aspx";
                    mLoginReferer = mLoginURL;
                    mUserHomeURL = mCMSInfo.mCmsURL + "/" + mDynPart + "/public/newslist.aspx";
                }

                // form content from kvs
                Map<String, String> res = formLoginPostContent(loginMap);
                String content = res.get(CONTENT);
                String action = res.get(ACTION);

                // post login
                Map<String, String> headers = new HashMap<>(1);
                headers.put("Referer", action);

                userCenter = OkCurl.curlSynPOST(action, headers, Constants.POST_CONTENT_TYPE_FORM_URLENCODED, content).body().string();

                // Login success
                if (userCenter.contains("个人信息")) break;
            }
            if (i == 10) {
                return null;
            }
            return userCenter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void getCAPTCHA(String path) throws IOException {
    }

    @Override
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) {
        try {
            String userCenter = login(loginMap);
            // login fail, no more actions
            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success, fetch class table
            String tableAddr = mCMSInfo.mCmsURL + "/" + mDynPart + "/public/kebiaoall.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            tablePage = tablePage.replaceAll("◇", "&");
            Document doc = Jsoup.parse(tablePage, mCMSInfo.mCmsURL);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (mCMSInfo.mClassTableInfo.mClassTableID.equals(table.attr("id"))) {
                    targetTable = table;
                    break;
                }
            }

            return targetTable == null ? null : generateClassInfos(targetTable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public List<GradeInfo> getGradeInfos(Map<String, String> loginMap) {
        try {
            String userCenter = login(loginMap);

            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success, fetch class table
            String tableAddr = mCMSInfo.mCmsURL + "/" + mDynPart + "/student/chengji.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            Document doc = Jsoup.parse(tablePage, mCMSInfo.mCmsURL);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (mCMSInfo.mGradeTableInfo.mGradeTableID.equals(table.attr("id"))) {
                    targetTable = table;
                    break;
                }
            }

            return targetTable == null ? null : generateGradeInfos(targetTable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
