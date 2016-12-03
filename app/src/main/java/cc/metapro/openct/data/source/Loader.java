package cc.metapro.openct.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.classtable.ClassPresenter;
import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.gradelist.GradePresenter;
import cc.metapro.openct.libborrowinfo.LibBorrowPresenter;
import cc.metapro.openct.libsearch.LibSearchPresnter;
import cc.metapro.openct.university.CMS.ConcreteCMS.NJsuwen;
import cc.metapro.openct.university.CMS.ConcreteCMS.ZFsoft;
import cc.metapro.openct.university.CMS.UniversityCMS;
import cc.metapro.openct.university.Library.ConcreteLibrary.OPAC;
import cc.metapro.openct.university.Library.UniversityLibrary;
import cc.metapro.openct.university.University;
import cc.metapro.openct.utils.Constants;

import static cc.metapro.openct.utils.Constants.PASSWORD;
import static cc.metapro.openct.utils.Constants.USERNAME;

/**
 * Created by jeffrey on 11/30/16.
 */

public class Loader {

    private static UniversityLibrary mLibrary;
    private static UniversityCMS mCMS;
    private static University university;
    private CallBack mCallBack;
    private RequestType mRequestType;

    public Loader(RequestType type, @NonNull CallBack callBack) {
        mRequestType = type;
        mCallBack = callBack;
    }

    public static Map<String, String> getLibStuInfo(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> map = new HashMap<>(2);
        map.put(USERNAME, preferences.getString("pref_lib_user_name", ""));
        map.put(PASSWORD, preferences.getString("pref_lib_password", ""));
        return map;
    }

    public static Map<String, String> getCmsStuInfo(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> map = new HashMap<>(2);
        map.put(USERNAME, preferences.getString("pref_cms_user_name", ""));
        map.put(PASSWORD, preferences.getString("pref_cms_password", ""));
        return map;
    }

    public static int getDailyClasses() {
        return university.cmsInfo.classTableInfo.dailyClasses;
    }

    public static int getClassLength() {
        return university.cmsInfo.classTableInfo.classLength;
    }

    public static int getCurrentWeek(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int i = Integer.parseInt(preferences.getString("current_week_seq", "1"));
        return i;
    }

    public static boolean cmsNeedCAPTCHA() {
        return university.cmsInfo.needCAPTCHA;
    }

    public static boolean libNeedCAPTCHA() {
        return university.libraryInfo.needCAPTCHA;
    }

    public void loadUniversity(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String school = preferences.getString("pref_school_name", "njit") + ".json";
                    String s = StoreHelper.getAssetTextFile(context, school);
                    Gson gson = new Gson();
                    university = gson.fromJson(s, University.class);
                    if (university != null) {
                        mCallBack.onResultOk(null);
                    } else {
                        mCallBack.onResultFail();
                    }
                } catch (IOException e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void prepareCms() {
        String s;
        try {
            s = university.cmsInfo.cmsSys.toLowerCase();
        } catch (Exception e) {
            s = "zfsoft";
        }
        switch (s) {
            case "njsuwen":
                mCMS = new NJsuwen(university.cmsInfo);
                break;
            case "zfsoft":
                mCMS = new ZFsoft(university.cmsInfo);
                break;
        }
    }

    private void prepareLibrary() {
        String s;
        try {
            s = university.libraryInfo.libSys.toLowerCase();
        } catch (Exception e) {
            s = "opac";
        }
        switch (s) {
            case "opac":
                mLibrary = new OPAC(university.libraryInfo);
                break;
            default:
                mLibrary = new OPAC(university.libraryInfo);
        }
    }

    /**
     * load from web page
     */
    public void loadFromRemote(Map<String, String> requestMap) {
        if (university == null) {
            mCallBack.onResultFail();
            return;
        }
        prepareCms();
        prepareLibrary();
        try {
            switch (mRequestType) {

                // cms related
                case LOAD_CLASS_TABLE:
                    getCalssInfo(requestMap);
                    break;
                case LOAD_GRADE_TABLE:
                    getGradeInfo(requestMap);
                    break;
                case LOAD_CMS_CAPTCHA:
                    getCmsCAPTCHA();
                    break;

                // library related
                case LOAD_BORROW_INFO:
                    getBorrowInfo(requestMap);
                    break;
                case LOAD_LIB_CAPTCHA:
                    getLibCAPTCHA();
                    break;
                case SEARCH_LIB:
                    searchLib(requestMap);
                    break;
                case GET_LIB_RESULT_PAGE:
                    getPageAt(requestMap);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCmsCAPTCHA() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCMS.getVCodePic(GradePresenter.CAPTCHA_FILE_FULL_URI);
                    mCallBack.onResultOk(null);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    @Nullable
    private String loginToCms(final Map<String, String> kvs) throws IOException {
        mCMS.prepareLoginURL();
        mCMS.formURLs();
        String loginPage = mCMS.getLoginPage();
        if (Strings.isNullOrEmpty(loginPage)) {
            return null;
        }
        String viewState = mCMS.getVIEWSTATE(loginPage);
        SparseArray<String> ssa = new SparseArray<>(4);
        ssa.put(Constants.VIEWSTATE_INDEX, viewState);
        ssa.put(Constants.USER_INDEX, kvs.get(Constants.USERNAME));
        ssa.put(Constants.PASSWD_INDEX, kvs.get(Constants.PASSWORD));
        ssa.put(Constants.VCODE_INDEX, kvs.get(Constants.CAPTCHA));
        mCMS.setUserHomeURL(kvs.get(Constants.USERNAME));
        String postBody = mCMS.formPostContent(ssa);
        return mCMS.loginPost(postBody);
    }

    private void getCalssInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userCenter = loginToCms(kvs);
                    String classTableAddr = mCMS.getTableAddr(userCenter);
                    String classPage = mCMS.getWholeTablePage(classTableAddr);
                    String classTable = mCMS.parseTable(classPage);
                    List<String> classes = mCMS.classTableToList(classTable);
                    List<ClassInfo> classInfos = mCMS.generateClasses(classes);
                    mCallBack.onResultOk(classInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void getGradeInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userCenter = loginToCms(kvs);
                    String gradeTableAddr = mCMS.getGradeAddr(userCenter);
                    String gradePage = mCMS.getWholeGradePage(gradeTableAddr);
                    String gradeTable = mCMS.parseGrade(gradePage);
                    List<Element> grades = mCMS.gradeTableToList(gradeTable);
                    List<GradeInfo> gradeInfos = mCMS.generatrGrades(grades);
                    mCallBack.onResultOk(gradeInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void searchLib(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String queryContent = mLibrary.getQuery(kvs);
                    String resultPage = mLibrary.search(queryContent);
                    List<BookInfo> infos = mLibrary.parseBook(resultPage);
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail();
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void getPageAt(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = Integer.parseInt(kvs.get(LibSearchPresnter.PAGE_INDEX));
                    String resultPage = mLibrary.getPageAt(kvs, index);
                    List<BookInfo> infos = mLibrary.parseBook(resultPage);
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail();
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void getLibCAPTCHA() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mLibrary.getVCODE(LibBorrowPresenter.CAPTCHA_FILE_FULL_URI);
                    mCallBack.onResultOk(null);
                } catch (IOException e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void getBorrowInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userCenterPage = mLibrary.login(kvs);
                    String borrowPage = mLibrary.getBorrowPage(userCenterPage);
                    List<BorrowInfo> infos = mLibrary.parseBorrow(borrowPage);
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail();
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    /**
     * load form loacl file
     */

    public void loadFromLocal(Context context) {
        if (university == null) {
            mCallBack.onResultFail();
            return;
        }
        switch (mRequestType) {
            case LOAD_CLASS_TABLE:
                loadLocalClassInfo(context);
                break;
            case LOAD_BORROW_INFO:
                loadLocalBorrowInfo(context);
                break;
            case LOAD_GRADE_TABLE:
                loadLocalGradeInfo(context);
                break;
        }
    }

    private void loadLocalClassInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = StoreHelper.getTextFile(context, ClassPresenter.CLASS_INFO_FILENAME);
                    JsonParser parser = new JsonParser();
                    JsonArray  jsonArray = parser.parse(s).getAsJsonArray();
                    List<ClassInfo> infos = new ArrayList<ClassInfo>(jsonArray.size());
                    Gson gson = new Gson();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        infos.add(gson.fromJson(jsonArray.get(i), ClassInfo.class));
                    }
                    mCallBack.onResultOk(infos);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void loadLocalBorrowInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BorrowInfo> mBorrowInfos;
                    Gson gson = new Gson();
                    String s = StoreHelper.getTextFile(context, LibBorrowPresenter.BORROW_INFO_FILENAME);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
                    mBorrowInfos = new ArrayList<>(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        mBorrowInfos.add(gson.fromJson(jsonArray.get(i), BorrowInfo.class));
                    }
                    mCallBack.onResultOk(mBorrowInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void loadLocalGradeInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Gson gson = new Gson();
                    String s = StoreHelper.getTextFile(context, GradePresenter.GRADE_INFO_FILENAME);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
                    List<GradeInfo> gradeInfos = new ArrayList<GradeInfo>(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        gradeInfos.add(gson.fromJson(jsonArray.get(i), GradeInfo.class));
                    }
                    mCallBack.onResultOk(gradeInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    public interface CallBack {

        void onResultOk(Object results);

        void onResultFail();
    }
}
