package cc.metapro.openct.university.CMS.ConcreteCMS;

import android.util.SparseArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.CMS.UniversityCMS;
import cc.metapro.openct.university.CMSInfo;
import cc.metapro.openct.utils.OkCurl;
import cc.metapro.openct.utils.RE;
import okhttp3.Cookie;

import static cc.metapro.openct.utils.Constants.PASSWD_INDEX;
import static cc.metapro.openct.utils.Constants.USER_INDEX;
import static cc.metapro.openct.utils.Constants.VCODE_INDEX;
import static cc.metapro.openct.utils.Constants.VIEWSTATE_INDEX;

/**
 * Created by jeffrey on 16/10/9.
 */
public class ZFsoft extends UniversityCMS {

    protected String codeURL, userHomeURL, loginURL, postRefererURL;

    protected CMSInfo cmsInfo;

    public ZFsoft() {

    }

    public ZFsoft(CMSInfo cmsInfo) {
        this.cmsInfo = cmsInfo;
        codeURL = cmsInfo.cmsURL + "/CheckCode.aspx";
    }

    @Override
    public void formURLs() {
        postRefererURL = cmsInfo.cmsURL;
        loginURL = cmsInfo.cmsURL + "default2.aspx";
        userHomeURL = cmsInfo.cmsURL + "xs_main.aspx?xh=";
    }

    public String getLoginPage() throws IOException {
        return OkCurl.curlSynGET(loginURL, null, null).body().string();
    }

    public String getVIEWSTATE(String loginPageHtml) {
        Document doc = Jsoup.parse(loginPageHtml, cmsInfo.charset);
        Elements ele = doc.select("input");
        for (Element e : ele) {
            if (e.attr("type").equals("hidden")) {
                return e.attr("value");
            }
        }
        return null;
    }

    public void getVCodePic(String path) throws IOException {
        OkCurl.curlSynGET(codeURL, null, path);
    }

    @Override
    public void prepareLoginURL() {

    }

    public String formPostContent(SparseArray<String> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(appendParams("__VIEWSTATE", values.get(VIEWSTATE_INDEX), true));
        sb.append(appendParams(cmsInfo.usernameBoxName, values.get(USER_INDEX)));
        sb.append(appendParams(cmsInfo.passwordBoxName, values.get(PASSWD_INDEX)));
        sb.append(appendParams(cmsInfo.captchaBoxName, values.get(VCODE_INDEX)));
        sb.append(appendParams(cmsInfo.radioButtonName, cmsInfo.radioOptionText));
        sb.append(appendParams(cmsInfo.otherBoxNameAndValues));
        return sb.toString();
    }

    protected String appendParams(String key, String value, boolean isFirst) {
        try {
            String a = "";
            if (!isFirst)
                a += "&";
            a += URLEncoder.encode(key, cmsInfo.charset) + "=" + URLEncoder.encode(value, cmsInfo.charset);
            return a;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    protected String appendParams(String key, String value) {
        return appendParams(key, value, false);
    }

    protected String appendParams(String str) {
        return "&" + str;
    }

    public String loginPost(String content) throws IOException {
        StringBuilder sb = new StringBuilder();
        URL url = new URL(loginURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Referer", postRefererURL);
        List<Cookie> cookies = OkCurl.getCookieOf(cmsInfo.cmsURL);
        if (cookies != null) {
            Cookie cookie1 = cookies.get(cookies.size() - 1);
            String cookie = cookie1.toString();
            conn.setRequestProperty("Cookie", cookie);
        }
        conn.setDoOutput(true);
        if (content != null) {
            conn.getOutputStream().write(content.getBytes());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), cmsInfo.charset));
        String tmp = br.readLine();
        while (tmp != null) {
            sb.append(tmp);
            tmp = br.readLine();
        }
        String c = conn.getHeaderField("Set-Cookie");
        if (c != null && !"".equals(c)) {
            OkCurl.setCookieOf(cmsInfo.cmsURL, c);
        }
        conn.disconnect();
        return sb.toString();
    }

    public void setUserHomeURL(String username) {
        if (!userHomeURL.endsWith(username)) {
            this.userHomeURL += username;
        }
    }

    public String getTableAddr(String html) {
        Document doc = Jsoup.parse(html, cmsInfo.charset);
        Elements ele = doc.select("a");
        for (Element e : ele) {
            if (e.hasAttr("onclick") && e.attr("onclick").equals("GetMc('学生个人课表');")) {
                return cmsInfo.cmsURL + e.attr("href");
            }
        }
        return null;
    }

    @Override
    public String getGradeAddr(String html) {
        Document doc = Jsoup.parse(html, cmsInfo.charset);
        Elements ele = doc.select("a");
        for (Element e : ele) {
            if (e.hasAttr("onclick") && e.attr("onclick").equals("GetMc('平时成绩查询');")) {
                return cmsInfo.cmsURL + e.attr("href");
            }
        }
        return null;
    }

    public String getWholeTablePage(String tableURL) throws IOException {
        Map<String, String> header = new HashMap<>(1);
        header.put("Referer", userHomeURL);
        return OkCurl.curlSynGET(tableURL, header, null).body().string();
    }

    @Override
    public String getWholeGradePage(String gradeURL) throws IOException {
        Map<String, String> header = new HashMap<>(1);
        header.put("Referer", userHomeURL);
        return OkCurl.curlSynGET(gradeURL, header, null).body().string();
    }

    public String parseTable(String tablePage) {
        Document doc = Jsoup.parse(tablePage, cmsInfo.charset);
        Elements elements = doc.select("table");
        for (Element e : elements) {
            if (e.attr("id").equals(cmsInfo.classTableInfo.classTableID)) {
                return e.toString();
            }
        }
        return null;
    }

    @Override
    public String parseGrade(String gradePage) {
        Document doc = Jsoup.parse(gradePage, "gb2312");
        Elements elements = doc.select("table");
        for (Element e : elements) {
            if (e.attr("id").equals("GridView1")) {
                return e.toString();
            }
        }
        return null;
    }

    @Override
    public List<String> classTableToList(String classTable) {
        Pattern p = Pattern.compile(cmsInfo.classTableInfo.classInfoStart);
        List<String> result = new ArrayList<>();
        Document doc = Jsoup.parse(classTable, cmsInfo.charset);
        Elements ele = doc.select("tr");
        for (Element tr : ele) {
            for (Element td : tr.children()) {
                Matcher m = p.matcher(td.text());
                if (m.find()) {
                    td = td.nextElementSibling();
                    int n = 0;
                    while (td != null && !p.matcher(td.text()).find()) {
                        result.add(td.text());
                        n++;
                        td = td.nextElementSibling();
                    }
                    for (int i = n; i < cmsInfo.classTableInfo.weekdays; i++) {
                        result.add(" ");
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<Element> gradeTableToList(String gradeTable) {
        List<Element> results = new ArrayList<>();
        Document doc = Jsoup.parse(gradeTable, cmsInfo.charset);
        Elements ele = doc.select("tr");
        ele.remove(0);
        for (Element tr : ele) {
            results.add(tr);
        }
        return results;
    }

    public List<ClassInfo> generateClasses(List<String> classes) {
        List<ClassInfo> classInfos = new ArrayList<>();
        int tindex = 0, dindex = 1;
        if (cmsInfo.classTableInfo.duringFront) {
            dindex = 0;
            tindex = 1;
        }
        for (String s : classes) {
            String[] classStrings = s.split(cmsInfo.classTableInfo.classStringSep);
            int[] class_start_end;
            int[] week_start_end_1;
            if (classStrings.length == cmsInfo.classTableInfo.classStringCount) {
                String tmp = classStrings[cmsInfo.classTableInfo.classTimeIndex];
                String[] timeANDduring = tmp.split(cmsInfo.classTableInfo.timeAndDuringSep);
                class_start_end = RE.getStartEnd(timeANDduring[tindex]);
                week_start_end_1 = RE.getStartEnd(timeANDduring[dindex]);

                ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);

                c.setTime(class_start_end[0], class_start_end[1]);
                c.setDuring(week_start_end_1[0], week_start_end_1[1], -1, -1);
                c.setRawInfo(s);
                classInfos.add(c);
            } else if (classStrings.length == 2 * cmsInfo.classTableInfo.classStringCount) {
                // two class info in one line and have same classname
                if (classStrings[cmsInfo.classTableInfo.classNameIndex]
                        .equals(classStrings[cmsInfo.classTableInfo.classStringCount
                                + cmsInfo.classTableInfo.classNameIndex])) {
                    String[] timeANDduring = classStrings[cmsInfo.classTableInfo.classTimeIndex].split(cmsInfo.classTableInfo.timeAndDuringSep);
                    class_start_end = RE.getStartEnd(timeANDduring[tindex]);
                    week_start_end_1 = RE.getStartEnd(timeANDduring[dindex]);
                    String[] timeANDduring_1 = classStrings[cmsInfo.classTableInfo.classTimeIndex].split(cmsInfo.classTableInfo.timeAndDuringSep);
                    int[] week_start_end_2 = RE.getStartEnd(timeANDduring_1[dindex]);

                    ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);
                    c.setTime(class_start_end[0], class_start_end[1]);
                    c.setDuring(week_start_end_1[0], week_start_end_1[1], week_start_end_2[0], week_start_end_2[1]);
                    c.setRawInfo(s);
                    classInfos.add(c);
                }
                // two class info in one line and have different classname
                else {
                    // class 1
                    String[] timeANDduring = classStrings[cmsInfo.classTableInfo.classTimeIndex].split(cmsInfo.classTableInfo.timeAndDuringSep);
                    class_start_end = RE.getStartEnd(timeANDduring[tindex]);
                    week_start_end_1 = RE.getStartEnd(timeANDduring[dindex]);
                    ClassInfo c = getClassInfo(cmsInfo.classTableInfo, classStrings);

                    c.setTime(class_start_end[0], class_start_end[1]);
                    c.setDuring(week_start_end_1[0], week_start_end_1[1], -1, -1);
                    c.setRawInfo(s);
                    // class 2 (class 1's subclass)
                    String[] timeANDduring_1 = classStrings[cmsInfo.classTableInfo.classStringCount + cmsInfo.classTableInfo.classTimeIndex].split(cmsInfo.classTableInfo.timeAndDuringSep);
                    int[] week_start_end_2 = RE.getStartEnd(timeANDduring_1[dindex]);

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
    public List<GradeInfo> generatrGrades(List<Element> grades) {
        List<GradeInfo> list = new ArrayList<>(grades.size());
        for (int i = 0; i < grades.size(); i++) {
            Element s = grades.get(i);
            list.add(getGradeInfo(cmsInfo.gradeTableInfo, s));
        }
        return list;
    }

    @Override
    public String toString() {
        return cmsInfo.cmsSys + "    " + cmsInfo.cmsURL;
    }

}
