package cc.metapro.openct.university;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import cc.metapro.openct.utils.HTMLUtils.PageStringUtils;
import cc.metapro.openct.utils.OkCurl;
import retrofit2.Call;

/**
 * Created by jeffrey on 16/12/5.
 */

public class CmsFactory {

    private final static Pattern successPattern = Pattern.compile("(个人信息)");

    protected CMSInfo mCMSInfo;

    private String pageResult;

    public CmsFactory(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;
        if (!mCMSInfo.mCmsURL.endsWith("/"))
            mCMSInfo.mCmsURL += "/";
    }

    private UniversityService login(Map<String, String> loginMap)
            throws IOException, LoginException
    {
        if (mCMSInfo.mDynLoginURL) {
            String dynPart = getDynPart();
            if (!Strings.isNullOrEmpty(dynPart)) {
                mCMSInfo.mCmsURL += dynPart + "/";
            }
        }
        UniversityService service = ServiceGenerator
                .createService(UniversityService.class, ServiceGenerator.HTML_CONVERTER);

        String loginPageHtml = service.getPage(mCMSInfo.mCmsURL, null).execute().body();

        FormHandler handler = new FormHandler(loginPageHtml, mCMSInfo.mCmsURL);
        Form form = handler.getForm(0);

        if (form == null) return null;

        Map<String, String> res = FormUtils.getLoginFiledMap(form, loginMap);
        String action = res.get(Constants.ACTION);
        res.remove(Constants.ACTION);

        Call<String> call = service.login(action, action, res);
        String userCenter = call.execute().body();

        if (successPattern.matcher(userCenter).find()) {
            pageResult = userCenter;
            return service;
        } else {
            throw new LoginException("login fail");
        }
    }

    public void getCAPTCHA(String path) throws IOException {
        if (mCMSInfo.mDynLoginURL) {
            String dynPart = getDynPart();
            if (!Strings.isNullOrEmpty(dynPart)) {
                mCMSInfo.mCmsURL += dynPart + "/";
            }
        }
        String captchaURL = mCMSInfo.mCmsURL + "CheckCode.aspx";
        OkCurl.curlSynGET(captchaURL, null, path);
    }

    /**
     * tend to get class info page
     *
     * @param loginMap - cms user info and captcha code (if needed)
     * @return a list of class info
     * @throws IOException
     * @throws LoginException
     */
    @Nullable
    public List<ClassInfo> getClassInfos(Map<String, String> loginMap) throws IOException, LoginException {
        UniversityService service = login(loginMap);
        String tableURL = null;
        String tablePage = null;
        switch (mCMSInfo.mCmsSys) {
            case "njsuwen":
                String tableAddr = mCMSInfo.mCmsURL + "public/kebiaoall.aspx";
                tablePage = service
                        .getPage(tableAddr, mCMSInfo.mCmsURL)
                        .execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return null;
                return generateClassInfos(tablePage.replaceAll("◇", Constants.BR_REPLACER));

            case "zfsoft":
                Document doc = Jsoup.parse(pageResult, mCMSInfo.mCmsURL);
                Elements addresses = doc.select("a");
                for (Element e : addresses) {
                    if ("GetMc('学生个人课表');".equals(e.attr("onclick"))) {
                        tableURL = mCMSInfo.mCmsURL + e.attr("href");
                        break;
                    }
                }
                if (Strings.isNullOrEmpty(tableURL)) return null;
                tablePage = service.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return null;
                return generateClassInfos(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));

            default:return null;
        }
    }

    /**
     * tend to get grade info page
     *
     * @param loginMap - cms user info
     * @return a list of grade info
     * @throws IOException
     * @throws LoginException
     */
    @Nullable
    public List<GradeInfo> getGradeInfos(Map<String, String> loginMap) throws IOException, LoginException {
        UniversityService service = login(loginMap);
        String tableURL = null;
        String tablePage = null;
        switch (mCMSInfo.mCmsSys) {
            case "njsuwen":
                String tableAddr = mCMSInfo.mCmsURL + "student/chengji.aspx";
                tablePage = service
                        .getPage(tableAddr, mCMSInfo.mCmsURL)
                        .execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return null;
                return generateGradeInfos(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));

            case "zfsoft":
                Document doc = Jsoup.parse(pageResult, mCMSInfo.mCmsURL);
                Elements ele = doc.select("a");
                for (Element e : ele) {
                    if ("GetMc('平时成绩查询');".equals(e.attr("onclick"))) {
                        tableURL = mCMSInfo.mCmsURL + e.attr("href");
                        break;
                    }
                }
                if (Strings.isNullOrEmpty(tableURL)) return null;
                tablePage = service.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return null;
                return generateGradeInfos(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));

            default:return null;
        }
    }

    /**
     * use this to generate class info, don't handle it by yourself
     *
     * @param html of class page
     * @return a list of generated class info
     */
    @Nullable
    protected List<ClassInfo> generateClassInfos(String html) {
        Document doc = Jsoup.parse(html, mCMSInfo.mCmsURL);
        Elements tables = doc.select("table");
        Element targetTable = null;
        for (Element table : tables) {
            if (table.attr("id").equals(mCMSInfo.mClassTableInfo.mClassTableID)) {
                targetTable = table;
            }
        }

        if (targetTable == null) return null;

        Pattern pattern = Pattern.compile(mCMSInfo.mClassTableInfo.mClassInfoStart);
        List<ClassInfo> classInfos = new ArrayList<>(mCMSInfo.mClassTableInfo.mDailyClasses * 7);

        for (Element tr : targetTable.select("tr")) {
            Elements tds = tr.select("td");
            Element td = tds.first();

            boolean found = false;
            while (td != null) {
                Matcher matcher = pattern.matcher(td.text());
                if (matcher.find()) {
                    td = td.nextElementSibling();
                    found = true;
                    break;
                }
                td = td.nextElementSibling();
            }
            if (!found) continue;

            // add class infos
            int i = 0;
            while (td != null) {
                i++;
                classInfos.add(new ClassInfo(td.text(), mCMSInfo.mClassTableInfo));
                td = td.nextElementSibling();
            }

            // make up to 7 classes in one tr
            for (; i < 7; i++) {
                classInfos.add(new ClassInfo());
            }
        }
        return classInfos;
    }

    /**
     * use this to generate grade info, don't handle it by yourself
     *
     * @param html of grade page
     * @return a list of generated grade info
     */
    @Nullable
    protected List<GradeInfo> generateGradeInfos(String html) {
        Document doc = Jsoup.parse(html, mCMSInfo.mCmsURL);
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
    }

    private String getDynPart() {
        try {
            String dynURL;
            URL url = new URL(mCMSInfo.mCmsURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            if (conn.getResponseCode() == 302) {
                dynURL = conn.getHeaderField("Location");
                if (!Strings.isNullOrEmpty(dynURL)) {
                    Pattern pattern = Pattern.compile("\\(.*\\)+");
                    Matcher m = pattern.matcher(dynURL);
                    if (m.find()) {
                        return m.group();
                    }
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class GradeTableInfo {
        public int
                mClassCodeIndex,
                mClassNameIndex,
                mClassTypeIndex,
                mPointsIndex,
                mGradeSummaryIndex,
                mGradePracticeIndex,
                mGradeCommonIndex,
                mGradeMidExamIndex,
                mGradeFinalExamIndex,
                mGradeMakeupIndex;

        public String mGradeTableID;
    }

    public static class ClassTableInfo {

        public int
                mDailyClasses,
                mNameIndex,
                mTypeIndex,
                mDuringIndex,
                mPlaceIndex,
                mTimeIndex,
                mTeacherIndex,
                mClassStringCount,
                mClassLength;

        public String
                mClassTableID,
                mClassInfoStart;

        // Regular Expressions to parse class infos
        public String
                mNameRE, mTypeRE,
                mDuringRE, mTimeRE,
                mTeacherRE, mPlaceRE;
    }

}
