package cc.metapro.openct.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.data.ServerService.ServiceGenerator;
import cc.metapro.openct.university.CmsFactory;
import cc.metapro.openct.university.LibraryFactory;
import cc.metapro.openct.university.UniversityInfo;
import cc.metapro.openct.university.UniversityService;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.EncryptionUtils;

public class Loader {

    public static LibraryFactory mLibrary;
    private static CmsFactory mCMS;

    private static UniversityInfo university;
    private static UniversityService service;

    private CallBack mCallBack;
    private RequestType mRequestType;

    public Loader(@Nullable RequestType type, @NonNull CallBack callBack) {
        mRequestType = type;
        mCallBack = callBack;
        if (service == null) {
            service = ServiceGenerator
                    .createService(UniversityService.class, ServiceGenerator.HTML);
        }
    }

    public static LibraryFactory getLibrary() {
        if (service == null) {
            service = ServiceGenerator
                    .createService(UniversityService.class, ServiceGenerator.HTML);
        }
        return new LibraryFactory(service, university.mLibraryInfo);
    }

    public static CmsFactory getCms() {
        if (service == null) {
            service = ServiceGenerator
                    .createService(UniversityService.class, ServiceGenerator.HTML);
        }
        return new CmsFactory(service, university.mCMSInfo);
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

    public static void loadUniversity(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        DBManger manger = DBManger.getInstance(context);
        String school = preferences.getString(Constants.PREF_SCHOOL_NAME_KEY, Constants.DEFAULT_SCHOOL_NAME);
        university = manger.getUniversity(school);

        assert university != null;

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
                    mCMS = new CmsFactory(service, university.mCMSInfo);
                    getCalssInfo(requestMap);
                    break;
                case LOAD_GRADE_TABLE:
                    mCMS = new CmsFactory(service, university.mCMSInfo);
                    getGradeInfo(requestMap);
                    break;
                case LOAD_CMS_CAPTCHA:
                    mCMS = new CmsFactory(service, university.mCMSInfo);
                    getCmsCAPTCHA();
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
                    if (classes.size() == 0) {
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
