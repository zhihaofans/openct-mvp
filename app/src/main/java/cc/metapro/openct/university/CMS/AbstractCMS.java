package cc.metapro.openct.university.CMS;

import com.google.common.base.Strings;

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
import cc.metapro.openct.university.University.CMSInfo;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.OkCurl;

/**
 * Created by jeffrey on 16/12/5.
 */

public abstract class AbstractCMS {

    protected String mLoginURL, mCaptchaURL, mUserHomeURL, mLoginReferer, mDynPart;

    protected final static String METHOD = "method", ACTION = "action", CONTENT = "content";

    protected CMSInfo mCMSInfo;

    protected abstract String login(Map<String, String> loginMap);

    public abstract void getCAPTCHA(String path) throws IOException;

    public abstract List<ClassInfo> getClassInfos(Map<String, String> loginMap);

    public abstract List<GradeInfo> getGradeInfos(Map<String, String> loginMap);

    protected Map<String, String> formLoginPostContent(Map<String, String> loginMap) throws IOException {
        String loginPageHtml = OkCurl.curlSynGET(mCMSInfo.mCmsURL, null, null).body().string();

        FormHandler handler = new FormHandler(loginPageHtml, mLoginURL);
        Form form = handler.getForm(0);

        if (form == null) return null;

        loginMap.put(Form.RADIOOPTION, mCMSInfo.mRadioOptionText);
        Map<String, String> res = new HashMap<>();
        res.put(CONTENT, form.formLoginContent(loginMap,"utf-8"));
        res.put(METHOD, form.getMethod());
        res.put(ACTION, form.getAction());
        return res;
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

    protected String getDynPart() {
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
