package cc.metapro.openct.university.CMS.ConcreteCMS;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.CMS.Cms;
import cc.metapro.openct.university.CMS.LoginUtil;
import cc.metapro.openct.university.CMSInfo;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/6.
 */

public class NJsuwen extends Cms {

    public String mDynPart;

    public NJsuwen(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;

        if (mCMSInfo.mCmsurl.endsWith("/"))
            mCMSInfo.mCmsurl = mCMSInfo.mCmsurl.substring(0, mCMSInfo.mCmsurl.length() - 1);

    }

    @Override
    protected String login(Map<String, String> loginMap) {
        String userCenter = null;
        try {
            int i = 0;
            for (; i < 10; i++) {
                // Prepare login url and get dyn part
                String dynURL;
                URL url = new URL(mCMSInfo.mCmsurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                if (conn.getResponseCode() == 302) {
                    dynURL = conn.getHeaderField("Location");
                    if (!Strings.isNullOrEmpty(dynURL)) {
                        Pattern pattern = Pattern.compile("\\(.*\\)+");
                        Matcher m = pattern.matcher(dynURL);
                        if (m.find()) {
                            mDynPart = m.group();
                        }
                    }
                }
                conn.disconnect();

                mLoginURL = mCMSInfo.mCmsurl + "/" + mDynPart + "/default.aspx";
                mLoginReferer = mLoginURL;
                mUserHomeURL = mCMSInfo.mCmsurl + "/" + mDynPart + "/public/newslist.aspx";

                // form content from kvs
                loginMap.put(VIEWSTATE, getCmsViewstate());
                String content = getPostContent(loginMap);

                // post login
                Map<String, String> headers = new HashMap<>(1);
                headers.put("Referer", mLoginReferer);
                userCenter = OkCurl.curlSynPOST(mLoginURL, headers, "application/x-www-form-urlencoded", content).body().string();

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

    private String getPostContent(Map<String, String> loginMap) {
        String charset = mCMSInfo.mCharset;
        return LoginUtil.appendParams(
                "__VIEWSTATE", getViewstate(loginMap), true, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mUsernameBoxName, getUsername(loginMap), false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mPasswordBoxName, getPassword(loginMap), false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mRadioButtonName, mCMSInfo.mRadioOptionText, false, charset) +
                LoginUtil.appendParams(
                        mCMSInfo.mOtherBoxNameAndValues);
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
            String tableAddr = mCMSInfo.mCmsurl + "/" + mDynPart + "/public/kebiaoall.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            tablePage = tablePage.replaceAll("◇", "&");
            Document doc = Jsoup.parse(tablePage, mCMSInfo.mCmsurl);
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
            String tableAddr = mCMSInfo.mCmsurl + "/" + mDynPart + "/student/chengji.aspx";
            Map<String, String> headers = new HashMap<>(1);
            headers.put("Referer", mLoginReferer);
            String tablePage = OkCurl.curlSynGET(tableAddr, headers, null).body().string();

            // fetch class table page fail, no more action
            if (Strings.isNullOrEmpty(tablePage)) return null;

            Document doc = Jsoup.parse(tablePage, mCMSInfo.mCmsurl);
            Elements tables = doc.select("table");
            Element targetTable = null;
            for (Element table : tables) {
                if (mCMSInfo.mGradeTableInfo.mGradeTableID.equals(table.attr("id"))) {
                    targetTable = table;
                    break;
                }
            }

            if (targetTable == null) return null;

            List<GradeInfo> gradeInfos = new ArrayList<>();
            Elements trs = targetTable.select("tr");
            trs.remove(0);
            for (Element tr : trs) {
                Elements tds = tr.select("td");
                gradeInfos.add(new GradeInfo(tds, mCMSInfo.mGradeTableInfo));
            }
            return gradeInfos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
