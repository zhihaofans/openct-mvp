package cc.metapro.openct.university.CMS.ConcreteCMS;

import android.util.SparseArray;

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
import cc.metapro.openct.university.CMSInfo;
import cc.metapro.openct.utils.OkCurl;
import cc.metapro.openct.utils.RE;
import okhttp3.Response;

import static cc.metapro.openct.utils.Constants.PASSWD_INDEX;
import static cc.metapro.openct.utils.Constants.USER_INDEX;
import static cc.metapro.openct.utils.Constants.VIEWSTATE_INDEX;


/**
 * Created by jeffrey on 16/10/10.
 */
public class NJsuwen extends ZFsoft {
    private String dynPart;
    private String classTableAddr, gradeTableAddr;

    public NJsuwen(CMSInfo cmsInfo) {
        this.cmsInfo = cmsInfo;
    }

    @Override
    public String loginPost(String content) throws IOException {
        Map<String, String> map = new HashMap<>(1);
        map.put("Referer", postRefererURL);
        Response s = OkCurl.curlSynPOST(loginURL, map, "application/x-www-form-urlencoded", content);
        return s.body().string();
    }

    @Override
    public String getLoginPage() throws IOException {
        return OkCurl.curlSynGET(loginURL, null, null).body().string();
    }

    @Override
    public String formPostContent(SparseArray<String> values) {
        return appendParams("__VIEWSTATE", values.get(VIEWSTATE_INDEX), true) +
                appendParams(cmsInfo.usernameBoxName, values.get(USER_INDEX)) +
                appendParams(cmsInfo.passwordBoxName, values.get(PASSWD_INDEX)) +
                appendParams(cmsInfo.radioButtonName, cmsInfo.radioOptionText) +
                appendParams(cmsInfo.otherBoxNameAndValues);
    }

    @Override
    public void setUserHomeURL(String username) {
    }

    @Override
    public String getTableAddr(String html) {
        return classTableAddr;
    }

    @Override
    public String getGradeAddr(String html) {
        return gradeTableAddr;
    }

    @Override
    public List<ClassInfo> generateClasses(List<String> classes) {
        List<ClassInfo> classInfos = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            String s = classes.get(i);
            String[] classStrings = s.split(cmsInfo.classTableInfo.classStringSep);
            int[] class_start_end = {(i / 7) * 2 + 1, (i / 7) * 2 + 2};
            int[] week_start_end_1;
            // standerd class info, whose length equals json configuration
            if (classStrings.length == cmsInfo.classTableInfo.classStringCount) {
                String time = classStrings[cmsInfo.classTableInfo.classTimeIndex];
                week_start_end_1 = RE.getStartEnd(time);

                ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);
                c.setTime(class_start_end[0], class_start_end[1]);
                c.setDuring(week_start_end_1[0], week_start_end_1[1], -1, -1);
                c.setRawInfo(s);
                classInfos.add(c);
            } else if (classStrings.length == 2 * cmsInfo.classTableInfo.classStringCount) {
                // two class info in one line and have same classname
                if (classStrings[cmsInfo.classTableInfo.classNameIndex]
                        .equals(classStrings[cmsInfo.classTableInfo.classStringCount + cmsInfo.classTableInfo.classNameIndex])) {

                    String time = classStrings[cmsInfo.classTableInfo.classTimeIndex];
                    week_start_end_1 = RE.getStartEnd(time);
                    String time1 = classStrings[cmsInfo.classTableInfo.classTimeIndex + cmsInfo.classTableInfo.classStringCount];
                    int[] week_start_end_2 = RE.getStartEnd(time1);

                    ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);
                    c.setTime(class_start_end[0], class_start_end[1]);
                    c.setDuring(week_start_end_1[0], week_start_end_1[1], week_start_end_2[0], week_start_end_2[1]);
                    c.setRawInfo(s);
                    classInfos.add(c);
                }
                // two class info in one line and have different classname
                else {
                    // class 1
                    String time = classStrings[cmsInfo.classTableInfo.classTimeIndex];
                    week_start_end_1 = RE.getStartEnd(time);
                    ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);
                    c.setTime(class_start_end[0], class_start_end[1]);
                    c.setDuring(week_start_end_1[0], week_start_end_1[1], -1, -1);
                    c.setRawInfo(s);

                    // class 2 (class 1's subclass)
                    String time1 = classStrings[cmsInfo.classTableInfo.classTimeIndex + cmsInfo.classTableInfo.classStringCount];
                    int[] week_start_end_2 = RE.getStartEnd(time1);
                    ClassInfo c_sub = getClassInfo(cmsInfo.classTableInfo, classStrings);
                    c_sub.setTime(class_start_end[0], class_start_end[1]);
                    c_sub.setDuring(week_start_end_2[0], week_start_end_2[1], -1, -1);
                    c_sub.setRawInfo(s);
                    c.setSubClassInfo(c_sub);
                    classInfos.add(c);
                }
            }
            // other class info
            else {
                if (s.length() > 10) {
                    classInfos.add(new ClassInfo().setRawInfo(s));
                } else {
                    classInfos.add(new ClassInfo());
                }
            }
        }
        return classInfos;
    }

    @Override
    public void formURLs() {
        userHomeURL = cmsInfo.cmsURL + dynPart + "/public/newslist.aspx";
        classTableAddr = cmsInfo.cmsURL + dynPart + "/public/kebiaoall.aspx";
        gradeTableAddr = cmsInfo.cmsURL + dynPart + "/student/chengji.aspx";
    }

    public void prepareLoginURL() {
        try {
            URL url = new URL(cmsInfo.cmsURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            if (conn.getResponseCode() == 302) {
                String dynURL = conn.getHeaderField("Location");
                if (dynURL != null) {
                    String s = cmsInfo.cmsURL;
                    if (s.endsWith("/")) {
                        loginURL = s.substring(0, s.length() - 1) + dynURL;
                    } else {
                        loginURL = cmsInfo.cmsURL + dynURL;
                    }
                    postRefererURL = loginURL;
                    Pattern pattern = Pattern.compile(cmsInfo.dynLoginURLRegualrExp);
                    Matcher m = pattern.matcher(dynURL);
                    if (m.find()) {
                        dynPart = m.group();
                    }
                }
            }
            conn.disconnect();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


}
