package cc.metapro.openct.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cc.metapro.openct.classtable.ClassPresenter;
import cc.metapro.openct.data.BookInfo;
import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.gradelist.GradePresenter;
import cc.metapro.openct.libborrowinfo.LibBorrowPresenter;
import cc.metapro.openct.university.UniversityInfo;
import cc.metapro.openct.university.cms.AbstractCMS;
import cc.metapro.openct.university.cms.concretecms.NJsuwen;
import cc.metapro.openct.university.cms.concretecms.ZFsoft;
import cc.metapro.openct.university.library.AbstractLibrary;
import cc.metapro.openct.university.library.concretelibrary.OPAC;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.EncryptionUtils;

import static cc.metapro.openct.utils.Constants.PASSWORD_KEY;
import static cc.metapro.openct.utils.Constants.USERNAME_KEY;

/**
 * Created by jeffrey on 11/30/16.
 */

public class Loader {

    private static AbstractLibrary mLibrary;
    private static AbstractCMS mCMS;
    private static UniversityInfo university;
    private CallBack mCallBack;
    private RequestType mRequestType;

    public Loader(RequestType type, @NonNull CallBack callBack) {
        mRequestType = type;
        mCallBack = callBack;
    }

    public static Map<String, String> getLibStuInfo(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_LIB_PASSWORD_KEY, "");
            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            Map<String, String> map = new HashMap<>(2);
            map.put(USERNAME_KEY, preferences.getString(Constants.PREF_LIB_USERNAME_KEY, ""));
            map.put(PASSWORD_KEY, decryptedCode);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> getCmsStuInfo(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_CMS_PASSWORD_KEY, "");
            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            Map<String, String> map = new HashMap<>(2);
            map.put(USERNAME_KEY, preferences.getString(Constants.PREF_CMS_USERNAME_KEY, ""));
            map.put(PASSWORD_KEY, decryptedCode);
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
                        mCallBack.onResultFail();
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
                    mCallBack.onResultFail();
                }
            }
        }).start();
    }

    private void prepareCms() {
        String s;
        try {
            s = university.mCMSInfo.mCmsSys.toLowerCase();
        } catch (Exception e) {
            s = Constants.ZFSOFT;
        }

        switch (s) {
            case Constants.NJSUWEN:
                mCMS = new NJsuwen(university.mCMSInfo);
                break;
            case Constants.ZFSOFT:
                mCMS = new ZFsoft(university.mCMSInfo);
                break;
            default:
                mCMS = new ZFsoft(university.mCMSInfo);
                break;
        }
    }

    private void prepareLibrary() {
        String s;
        try {
            s = university.mLibraryInfo.mLibSys.toLowerCase();
        } catch (Exception e) {
            s = Constants.OPAC;
        }
        switch (s) {
            case Constants.OPAC:
                mLibrary = new OPAC(university.mLibraryInfo);
                break;
            default:
                mLibrary = new OPAC(university.mLibraryInfo);
                break;
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
        try {
            switch (mRequestType) {

                // cms related
                case LOAD_CLASS_TABLE:
                    prepareCms();
                    getCalssInfo(requestMap);
                    break;
                case LOAD_GRADE_TABLE:
                    prepareCms();
                    getGradeInfo(requestMap);
                    break;
                case LOAD_CMS_CAPTCHA:
                    prepareCms();
                    getCmsCAPTCHA();
                    break;

                // library related
                case LOAD_BORROW_INFO:
                    prepareLibrary();
                    getBorrowInfo(requestMap);
                    break;
                case LOAD_LIB_CAPTCHA:
                    prepareLibrary();
                    getLibCAPTCHA();
                    break;
                case SEARCH_LIB:
                    prepareLibrary();
                    searchLib(requestMap);
                    break;
                case GET_LIB_RESULT_PAGE:
                    prepareLibrary();
                    getNextPage();
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
                    mCMS.getCAPTCHA(GradePresenter.CAPTCHA_FILE_FULL_URI);
                    mCallBack.onResultOk(null);
                } catch (Exception e) {
                    mCallBack.onResultFail();
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
                        mCallBack.onResultFail();
                    } else {
                        mCallBack.onResultOk(classes);
                    }
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
                    List<GradeInfo> gradeInfos = mCMS.getGradeInfos(kvs);
                    if (gradeInfos == null || gradeInfos.size() == 0) {
                        mCallBack.onResultFail();
                    } else {
                        mCallBack.onResultOk(gradeInfos);
                    }
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
                    List<BookInfo> infos = mLibrary.search(kvs);
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

    private void getNextPage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BookInfo> infos = mLibrary.getNextPage();
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
                    mLibrary.getCODE(LibBorrowPresenter.CAPTCHA_FILE_FULL_URI);
                    mCallBack.onResultOk(null);
                } catch (Exception e) {
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
                    List<BorrowInfo> infos = mLibrary.genBorrowInfo(kvs);
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
                    JsonArray jsonArray = parser.parse(s).getAsJsonArray();
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
