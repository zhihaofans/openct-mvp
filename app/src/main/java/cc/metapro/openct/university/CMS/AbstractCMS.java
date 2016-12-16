package cc.metapro.openct.university.CMS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.University.CMSInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/5.
 */

public abstract class AbstractCMS {

    protected String mLoginURL, mCaptchaURL, mUserHomeURL, mLoginReferer;

    protected CMSInfo mCMSInfo;

    protected abstract String login(Map<String, String> loginMap);

    public abstract void getCAPTCHA(String path) throws IOException;

    public abstract List<ClassInfo> getClassInfos(Map<String, String> loginMap);

    public abstract List<GradeInfo> getGradeInfos(Map<String, String> loginMap);

    protected String getCmsViewstate() throws IOException {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Referer", mCMSInfo.mCmsurl);
        String loginPageHtml = OkCurl.curlSynGET(mLoginURL, headers, null).body().string();
        Document doc = Jsoup.parse(loginPageHtml, mCMSInfo.mCharset);
        Elements ele = doc.select("input");
        for (Element e : ele) {
            if (e.attr("type").equals("hidden")) {
                return e.attr("value");
            }
        }
        return null;
    }

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

    protected String getUsername(Map<String, String> loginMap) {
        return loginMap.get(Constants.USERNAME_KEY);
    }

    protected String getPassword(Map<String, String> loginMap) {
        return loginMap.get(Constants.PASSWORD_KEY);
    }

    protected String getCaptcha(Map<String, String> loginMap) {
        return loginMap.get(Constants.CAPTCHA_KEY);
    }

    protected String getViewstate(Map<String, String> loginMap) {
        return loginMap.get(Constants.VIEWSTATE_KEY);
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
