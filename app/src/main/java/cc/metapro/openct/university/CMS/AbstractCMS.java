package cc.metapro.openct.university.cms;

import com.google.common.base.Strings;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.UniversityHelper;
import cc.metapro.openct.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/5.
 */

public abstract class AbstractCMS {

    private final static Pattern successPattern = Pattern.compile("(个人信息)|(为保障您的个人信息的安全)");

    protected String mCaptchaURL;

    protected CMSInfo mCMSInfo;

    protected AbstractCMS(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;

        if (!mCMSInfo.mCmsURL.endsWith("/"))
            mCMSInfo.mCmsURL += "/";
    }

    protected String login(Map<String, String> loginMap) throws IOException, LoginException {
        if (mCMSInfo.mDynLoginURL) {
            String dynPart = getDynPart();
            if (!Strings.isNullOrEmpty(dynPart)) {
                mCMSInfo.mCmsURL += dynPart + "/";
            }
        }

        // generate request body according to form
        Map<String, String> res = UniversityHelper.
                formLoginPostContent(loginMap, mCMSInfo.mCmsURL);
        if (res == null) return null;

        String content = res.get(UniversityHelper.CONTENT);
        String action = res.get(UniversityHelper.ACTION);

        // post to login
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Referer", action);
        String userCenter = OkCurl.curlSynPOST(action, headers, Constants.POST_CONTENT_TYPE_FORM_URLENCODED, content).body().string();

        if (successPattern.matcher(userCenter).find()) {
            return userCenter;
        } else {
            throw new LoginException("login fail");
        }
    }


    public void getCAPTCHA(String path) throws IOException {
        OkCurl.curlSynGET(mCaptchaURL, null, path);
    }

    public abstract List<ClassInfo> getClassInfos(Map<String, String> loginMap) throws IOException, LoginException;

    public abstract List<GradeInfo> getGradeInfos(Map<String, String> loginMap) throws IOException, LoginException;

    protected List<ClassInfo> generateClassInfos(Element targetTable) {
        Pattern pattern = Pattern.compile(mCMSInfo.mClassTableInfo.mClassInfoStart);
        List<ClassInfo> classInfos = new ArrayList<>
                (mCMSInfo.mClassTableInfo.mDailyClasses * 7);
        Elements trs = targetTable.select("tr");
        for (Element tr : trs) {
            Elements tds = tr.select("td");
            Element tdTmp = tds.first();

            boolean found = false;
            while (tdTmp != null) {
                Matcher matcher = pattern.matcher(tdTmp.text());
                if (matcher.find()) {
                    tdTmp = tdTmp.nextElementSibling();
                    found = true;
                    break;
                }
                tdTmp = tdTmp.nextElementSibling();
            }
            if (!found) continue;

            // add class infos
            int i = 0;
            while (tdTmp != null) {
                i++;
                classInfos.add(new ClassInfo(tdTmp.text(), mCMSInfo.mClassTableInfo));
                tdTmp = tdTmp.nextElementSibling();
            }

            // make up to 7 classes in one tr
            for (; i < 7; i++) {
                classInfos.add(new ClassInfo());
            }
        }
        return classInfos;
    }

    protected List<GradeInfo> generateGradeInfos(Element targetTable) {
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
