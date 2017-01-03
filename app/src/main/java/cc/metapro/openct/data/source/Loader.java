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

    private static UniversityInfo university;
    private static UniversityService service;

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

}
