package cc.metapro.openct.data.university;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.annotation.NonNull;

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.PageStringUtils;

public class CmsFactory extends UniversityFactory {

    private CmsURLFactory mURLFactory;

    public CmsFactory(UniversityService service, CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;
        mService = service;
        mClassTableInfo = cmsInfo.mClassTableInfo;
        mGradeTableInfo = cmsInfo.mGradeTableInfo;
        mURLFactory = new CmsURLFactory(cmsInfo.mCmsSys, cmsInfo.mCmsURL);
    }

    /**
     * tend to get class info page
     *
     * @param loginMap - cms user info and captcha code (if needed)
     * @return a list of class info
     * @throws IOException
     * @throws LoginException
     */
    @NonNull
    public List<ClassInfo>
    getClasses(Map<String, String> loginMap) throws Exception {

        String page = login(loginMap);
        String tableURL = null;
        String tablePage;
        if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.NJSUWEN)) {
            tablePage = mService.getPage(mURLFactory.CLASS_URL, mCMSInfo.mCmsURL)
                    .execute().body();
            if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
            return generateClasses(tablePage.replaceAll("◇", Constants.BR_REPLACER));
        } else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.ZFSOFT)) {
            Document doc = Jsoup.parse(page, mCMSInfo.mCmsURL);
            Elements addresses = doc.select("a");
            for (Element e : addresses) {
                if ("GetMc('学生个人课表');".equals(e.attr("onclick"))) {
                    tableURL = mCMSInfo.mCmsURL + e.attr("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
            tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
            if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
            return generateClasses(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));
        } else {
            return new ArrayList<>(0);
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
    @NonNull
    public List<GradeInfo>
    getGrades(Map<String, String> loginMap) throws Exception {
        String page = login(loginMap);
        String tableURL = null;
        String tablePage;
        switch (mCMSInfo.mCmsSys) {
            case Constants.NJSUWEN:
                tablePage = mService
                        .getPage(mURLFactory.GRADE_URL, mCMSInfo.mCmsURL)
                        .execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
                return generateGrades(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));

            case Constants.ZFSOFT:
                Document doc = Jsoup.parse(page, mCMSInfo.mCmsURL);
                Elements ele = doc.select("a");
                for (Element e : ele) {
                    if ("GetMc('平时成绩查询');".equals(e.attr("onclick"))) {
                        tableURL = mCMSInfo.mCmsURL + e.attr("href");
                        break;
                    }
                }
                if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
                tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
                if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
                return generateGrades(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER));

            default:
                return new ArrayList<>(0);
        }
    }

    /**
     * use this to generate class info, don't handle it by yourself
     *
     * @param html of class page
     * @return a list of generated class info
     */
    @NonNull
    private List<ClassInfo>
    generateClasses(String html) {
        Document doc = Jsoup.parse(html, mCMSInfo.mCmsURL);
        Elements tables = doc.select("table");
        Element targetTable = null;
        for (Element table : tables) {
            if (table.attr("id").equals(mClassTableInfo.mClassTableID)) {
                targetTable = table;
            }
        }

        if (targetTable == null) return new ArrayList<>(0);

        Pattern pattern = Pattern.compile(mClassTableInfo.mClassInfoStart);
        List<ClassInfo> classes = new ArrayList<>(mClassTableInfo.mDailyClasses * 7);

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

            // add class
            int i = 0;
            while (td != null) {
                i++;
                classes.add(new ClassInfo(td.text(), mClassTableInfo));
                td = td.nextElementSibling();
            }

            // make up to 7 classes in one tr
            for (; i < 7; i++) {
                classes.add(new ClassInfo());
            }
        }
        return classes;
    }

    /**
     * use this to generate grade info, don't handle it by yourself
     *
     * @param html of grade page
     * @return a list of generated grade info
     */
    @NonNull
    private List<GradeInfo>
    generateGrades(String html) {
        Document doc = Jsoup.parse(html, mCMSInfo.mCmsURL);
        Elements tables = doc.select("table");
        Element targetTable = null;
        for (Element table : tables) {
            if (mGradeTableInfo.mGradeTableID.equals(table.attr("id"))) {
                targetTable = table;
                break;
            }
        }

        if (targetTable == null) return new ArrayList<>(0);

        List<GradeInfo> grades = new ArrayList<>();

        Elements trs = targetTable.select("tr");
        trs.remove(0);
        for (Element tr : trs) {
            Elements tds = tr.select("td");
            grades.add(new GradeInfo(tds, mGradeTableInfo));
        }
        return grades;
    }

    @Override
    protected String
    getCaptchaURL() {
        return mURLFactory.CAPTCHA_URL;
    }

    @Override
    protected String
    getLoginURL() {
        return mURLFactory.LOGIN_URL;
    }

    @Override
    protected String
    getLoginRefer() {
        return mURLFactory.LOGIN_REF;
    }

    @Override
    protected void resetURLFactory() {
        mURLFactory = new CmsURLFactory(mCMSInfo.mCmsSys, mCMSInfo.mCmsURL + "/" + dynPart + "/");
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

        // Regular Expressions to parse class
        public String
                mNameRE, mTypeRE,
                mDuringRE, mTimeRE,
                mTeacherRE, mPlaceRE;
    }

    private class CmsURLFactory {
        String
                LOGIN_URL, LOGIN_REF,
                CLASS_URL, GRADE_URL,
                CAPTCHA_URL;

        CmsURLFactory(@NonNull String cmsSys, @NonNull String cmsBaseURL) {
            if (!cmsBaseURL.endsWith("/")) {
                cmsBaseURL += "/";
            }

            // NJSuWen
            if (Constants.NJSUWEN.equalsIgnoreCase(cmsSys)) {
                LOGIN_URL = cmsBaseURL;
                LOGIN_REF = cmsBaseURL;
                CLASS_URL = cmsBaseURL + "public/kebiaoall.aspx";
                GRADE_URL = cmsBaseURL + "student/chengji.aspx";
            }
            // ZFSoft
            else if (Constants.ZFSOFT.equalsIgnoreCase(cmsSys)) {
                LOGIN_URL = cmsBaseURL;
                LOGIN_REF = cmsBaseURL;
                CAPTCHA_URL = cmsBaseURL + "CheckCode.aspx";
            }
        }
    }

}
