package cc.metapro.openct.university.CMS.ConcreteCMS;

import android.support.annotation.NonNull;
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
import cc.metapro.openct.university.CMS.LoginUtil;
import cc.metapro.openct.university.University.CMSInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/5.
 */

public class ZFsoft extends AbstractCMS {

    public ZFsoft(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;

        if (!mCMSInfo.mCmsurl.endsWith("/")) mCMSInfo.mCmsurl += "/";

        mLoginReferer = mCMSInfo.mCmsurl;
        mCaptchaURL = mCMSInfo.mCmsurl + "CheckCode.aspx";
        mLoginURL = mCMSInfo.mCmsurl + "default2.aspx";
    }

    @Override
    public void getCAPTCHA(String path) throws IOException {
        OkCurl.curlSynGET(mCaptchaURL, null, path);
    }

    @Override
    protected String login(Map<String, String> loginMap) {
        String userCenter = null;
        int i = 0;
        for (; i < 10; i++) {
            // try 10 times to login, for exception of time out
            try {
                loginMap.put(Constants.VIEWSTATE_KEY, getCmsViewstate());
                String content = getPostContent(loginMap);

                Map<String, String> headers = new HashMap<>(1);
                headers.put("Referer", mLoginReferer);
                userCenter = OkCurl.curlSynPOST(mLoginURL, headers, "application/x-www-form-urlencoded", content).body().string();
                // login successful
                if (userCenter.contains("为保障您的个人信息的安全")) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (i >= 10) {
            return null;
        }
        return userCenter;
    }

    @NonNull
    private String getPostContent(Map<String, String> loginMap) {
        String charset = mCMSInfo.mCharset;
        String sb = LoginUtil.appendParams(
                "__VIEWSTATE", getViewstate(loginMap), true, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mUsernameBoxName, getUsername(loginMap), false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mPasswordBoxName, getPassword(loginMap), false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mCaptchaBoxName, getCaptcha(loginMap), false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mRadioButtonName, mCMSInfo.mRadioOptionText, false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mOtherBoxNameAndValues);
        return sb;
    }

    @Nullable
    @Override
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) {
        try {
            String userCenter = login(loginMap);
            // login fail, no more actions
            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success
            String tableURL = null;
            Document doc = Jsoup.parse(userCenter, mCMSInfo.mCmsurl);
            Elements addresses = doc.select("a");
            for (Element e : addresses) {
                if (e.hasAttr("onclick") && e.attr("onclick").equals("GetMc('学生个人课表');")) {
                    tableURL = mCMSInfo.mCmsurl + e.attr("href");
                    break;
                }
            }
            // fetch table url fail, no more actions
            if (Strings.isNullOrEmpty(tableURL)) return null;

            // fetched table url, get table page
            Map<String, String> header = new HashMap<>(1);
            header.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableURL, header, null).body().string();

            // fetch table page fail, no more actions
            if (Strings.isNullOrEmpty(tablePage)) return null;

            // fetched table page, target to table with class
            tablePage = tablePage.replaceAll("(<br.*?/?>)|(\\(调.*?\\))", "&");
            // double or more continuous & means a class info sep in one td
            doc = Jsoup.parse(tablePage, mCMSInfo.mCmsurl);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (table.attr("id").equals(mCMSInfo.mClassTableInfo.mClassTableID)) {
                    targetTable = table;
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
            // login fail, no more actions
            if (Strings.isNullOrEmpty(userCenter)) return null;

            // login success
            String tableURL = null;
            Document doc = Jsoup.parse(userCenter, mCMSInfo.mCmsurl);
            Elements ele = doc.select("a");
            for (Element e : ele) {
                if (e.hasAttr("onclick") && e.attr("onclick").equals("GetMc('平时成绩查询');")) {
                    tableURL = mCMSInfo.mCmsurl + e.attr("href");
                    break;
                }
            }
            // fetch table url fail, no more actions
            if (Strings.isNullOrEmpty(tableURL)) return null;

            // fetched table url, get table page
            Map<String, String> header = new HashMap<>(1);
            header.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableURL, header, null).body().string();

            // fetch table page fail, no more actions
            if (Strings.isNullOrEmpty(tablePage)) return null;

            tablePage = tablePage.replaceAll("(<br.*?/?>)", "&");
            doc = Jsoup.parse(tablePage, mCMSInfo.mCmsurl);
            ele = doc.select("table");
            Element targetTable = null;
            for (Element e : ele) {
                if (e.attr("id").equals(mCMSInfo.mGradeTableInfo.mGradeTableID)) {
                    targetTable = e;
                }
            }

            return targetTable == null ? null : generateGradeInfos(targetTable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
