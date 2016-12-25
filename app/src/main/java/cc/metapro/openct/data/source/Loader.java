package cc.metapro.openct.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.university.UniversityInfo;
import cc.metapro.openct.university.CmsFactory;
import cc.metapro.openct.university.LibraryFactory;
import cc.metapro.openct.university.UniversityService;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.EncryptionUtils;
import cc.metapro.openct.utils.OkCurl;

public class Loader {

    private static LibraryFactory mLibrary;
    private static CmsFactory mCMS;
    private static UniversityInfo university;
    private CallBack mCallBack;
    private RequestType mRequestType;

    public Loader(@Nullable RequestType type, @NonNull CallBack callBack) {
        mRequestType = type;
        mCallBack = callBack;
    }

    @Nullable
    public static Map<String, String> getLibStuInfo(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_LIB_PASSWORD_KEY, "");

            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            Map<String, String> map = new HashMap<>(2);
            map.put(Constants.USERNAME_KEY, preferences.getString(Constants.PREF_LIB_USERNAME_KEY, ""));
            map.put(Constants.PASSWORD_KEY, decryptedCode);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Map<String, String> getCmsStuInfo(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_CMS_PASSWORD_KEY, "");
            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            Map<String, String> map = new HashMap<>(2);
            map.put(Constants.USERNAME_KEY, preferences.getString(Constants.PREF_CMS_USERNAME_KEY, ""));
            map.put(Constants.PASSWORD_KEY, decryptedCode);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getDailyClasses() {
        return university.mCMSInfo.mClassTableInfo.mDailyClasses;
    }

    public static int getClassLength() {
        return university.mCMSInfo.mClassTableInfo.mClassLength;
    }

    public static int getCurrentWeek(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(preferences.getString(Constants.PREF_CURRENT_WEEK_KEY, "1"));
    }

    public static boolean cmsNeedCAPTCHA() {
        return university.mCMSInfo.mNeedCAPTCHA;
    }

    public static boolean libNeedCAPTCHA() {
        return university.mLibraryInfo.mNeedCAPTCHA;
    }

    public void loadUniversity(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String school = preferences.getString(Constants.PREF_SCHOOL_NAME_KEY, Constants.DEFAULT_SCHOOL_NAME) + ".json";
                    String s = StoreHelper.getAssetText(context, school);
                    Gson gson = new Gson();
                    university = gson.fromJson(s, UniversityInfo.class);

                    if (university != null) {
                        mCallBack.onResultOk(null);
                    } else {
                        mCallBack.onResultFail(Constants.FATAL_UNIVERSITY_NULL);
                    }

                    // update current week
                    int lastSetWeek = Integer.parseInt(preferences.getString(Constants.PREF_WEEK_SET_KEY, "1"));
                    Calendar cal = Calendar.getInstance(Locale.CHINA);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    int weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
                    int currentWeek = Integer.parseInt(preferences.getString(Constants.PREF_CURRENT_WEEK_KEY, "1"));
                    if (weekOfYearWhenSetCurrentWeek < lastSetWeek && lastSetWeek <= 53) {
                        if (lastSetWeek == 53) {
                            currentWeek += weekOfYearWhenSetCurrentWeek;
                        } else {
                            currentWeek += (52 - lastSetWeek) + weekOfYearWhenSetCurrentWeek;
                        }
                    } else {
                        currentWeek += (weekOfYearWhenSetCurrentWeek - lastSetWeek);
                    }
                    if (currentWeek >= 30) {
                        currentWeek = 1;
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constants.PREF_CURRENT_WEEK_KEY, currentWeek + "");
                    editor.putString(Constants.PREF_WEEK_SET_KEY, weekOfYearWhenSetCurrentWeek + "");
                    editor.apply();

                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    /**
     * load from web page
     */

    public void loadFromRemote(@Nullable Map<String, String> requestMap) {
        if (university == null) {
            mCallBack.onResultFail(Constants.FATAL_UNIVERSITY_NULL);
            return;
        }
        try {
            switch (mRequestType) {

                // cms related
                case LOAD_CLASS_TABLE:
                    mCMS = new CmsFactory(university.mCMSInfo);
                    getCalssInfo(requestMap);
                    break;
                case LOAD_GRADE_TABLE:
                    mCMS = new CmsFactory(university.mCMSInfo);
                    getGradeInfo(requestMap);
                    break;
                case LOAD_CMS_CAPTCHA:
                    mCMS = new CmsFactory(university.mCMSInfo);
                    getCmsCAPTCHA();
                    break;

                // library related
                case LOAD_BORROW_INFO:
                    mLibrary = new LibraryFactory(university.mLibraryInfo);
                    getBorrowInfo(requestMap);
                    break;
                case LOAD_LIB_CAPTCHA:
                    mLibrary = new LibraryFactory(university.mLibraryInfo);
                    getLibCAPTCHA();
                    break;
                case SEARCH_LIB:
                    mLibrary = new LibraryFactory(university.mLibraryInfo);
                    searchLib(requestMap);
                    break;
                case GET_LIB_NEXT_PAGE:
                    mLibrary = new LibraryFactory(university.mLibraryInfo);
                    getNextPage();
                    break;

                // query cet grades
                case QUERY_CET_GRADE:
                    queryCETGrade(requestMap);
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
                    mCMS.getCAPTCHA(Constants.CAPTCHA_FILE);
                    mCallBack.onResultOk(null);
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void getCalssInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ClassInfo> classes = mCMS.getClassInfos(kvs);
                    if (classes == null || classes.size() == 0) {
                        mCallBack.onResultFail(Constants.GET_CLASS_FAIL);
                    } else {
                        mCallBack.onResultOk(classes);
                    }
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (LoginException e) {
                    mCallBack.onResultFail(Constants.LOGIN_FAIL);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void getGradeInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<GradeInfo> gradeInfos = mCMS.getGradeInfos(kvs);
                    if (gradeInfos == null || gradeInfos.size() == 0) {
                        mCallBack.onResultFail(Constants.GET_GRADE_FAIL);
                    } else {
                        mCallBack.onResultOk(gradeInfos);
                    }
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (LoginException e) {
                    mCallBack.onResultFail(Constants.LOGIN_FAIL);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void searchLib(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BookInfo> infos = mLibrary.search(kvs);
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail(Constants.LIB_SEARCH_FAIL);
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void getNextPage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BookInfo> infos = mLibrary.getNextPage();
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail(Constants.NEXT_PAGE_FAIL);
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void getLibCAPTCHA() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mLibrary.getCAPTCHA(Constants.CAPTCHA_FILE);
                    mCallBack.onResultOk(null);
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void getBorrowInfo(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BorrowInfo> infos = mLibrary.getBorrowInfo(kvs);
                    if (infos == null || infos.size() == 0) {
                        mCallBack.onResultFail(Constants.EMPTY);
                    } else {
                        mCallBack.onResultOk(infos);
                    }
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (LoginException e) {
                    mCallBack.onResultFail(Constants.LOGIN_FAIL);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    private void queryCETGrade(final Map<String, String> kvs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UniversityService service = ServiceGenerator
                            .createService(UniversityService.class, ServiceGenerator.HTML_CONVERTER);
                    String res = service.queryCet("http://www.chsi.com.cn/cet/",
                            kvs.get(Constants.CET_NUM_KEY),
                            kvs.get(Constants.CET_NAME_KEY), "t")
                            .execute().body();

                    Document document = Jsoup.parse(res);
                    Elements elements = document.select("table[class=cetTable]");
                    Element targetTable = elements.first();
                    Elements tds = targetTable.getElementsByTag("td");
                    String name = tds.get(0).text();
                    String school = tds.get(1).text();
                    String type = tds.get(2).text();
                    String num = tds.get(3).text();
                    String time = tds.get(4).text();
                    String grade = tds.get(5).text();

                    Map<String, String> results = new HashMap<>(6);
                    results.put(Constants.CET_NAME_KEY, name);
                    results.put(Constants.CET_SCHOOL_KEY, school);
                    results.put(Constants.CET_TYPE_KEY, type);
                    results.put(Constants.CET_NUM_KEY, num);
                    results.put(Constants.CET_TIME_KEY, time);
                    results.put(Constants.CET_GRADE_KEY, grade);

                    mCallBack.onResultOk(results);
                } catch (SocketTimeoutException e) {
                    mCallBack.onResultFail(Constants.NETWORK_TIMEOUT);
                } catch (IOException e) {
                    mCallBack.onResultFail(Constants.NETWORK_ERROR);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.UNKNOWN_ERROR);
                }
            }
        }).start();
    }

    /**
     * load form loacl file
     */

    public void loadFromLocal(Context context) {
        if (university == null) {
            mCallBack.onResultFail(Constants.FATAL_UNIVERSITY_NULL);
            return;
        }
        switch (mRequestType) {
            case LOAD_CLASS_TABLE:
                getLocalClassInfo(context);
                break;
            case LOAD_BORROW_INFO:
                getLocalBorrowInfo(context);
                break;
            case LOAD_GRADE_TABLE:
                getLocalGradeInfo(context);
                break;
        }
    }

    private void getLocalClassInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = StoreHelper.getTextFile(context, Constants.STU_CLASS_INFOS_FILE);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
                    List<ClassInfo> infos = new ArrayList<>(jsonArray.size());
                    Gson gson = new Gson();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        infos.add(gson.fromJson(jsonArray.get(i), ClassInfo.class));
                    }
                    mCallBack.onResultOk(infos);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.FILE_FETCH_ERROR);
                }
            }
        }).start();
    }

    private void getLocalBorrowInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BorrowInfo> mBorrowInfos;
                    Gson gson = new Gson();
                    String s = StoreHelper.getTextFile(context, Constants.LIB_BORROW_INFOS_FILE);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
                    mBorrowInfos = new ArrayList<>(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        mBorrowInfos.add(gson.fromJson(jsonArray.get(i), BorrowInfo.class));
                    }
                    mCallBack.onResultOk(mBorrowInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.FILE_FETCH_ERROR);
                }
            }
        }).start();
    }

    private void getLocalGradeInfo(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Gson gson = new Gson();
                    String s = StoreHelper.getTextFile(context, Constants.STU_GRADE_INFOS_FILE);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
                    List<GradeInfo> gradeInfos = new ArrayList<>(jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        gradeInfos.add(gson.fromJson(jsonArray.get(i), GradeInfo.class));
                    }
                    mCallBack.onResultOk(gradeInfos);
                } catch (Exception e) {
                    mCallBack.onResultFail(Constants.FILE_FETCH_ERROR);
                }
            }
        }).start();
    }

    public interface CallBack {

        void onResultOk(@Nullable Object results);

        void onResultFail(int failType);
    }
}
