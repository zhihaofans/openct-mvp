package cc.metapro.openct.utils;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by jeffrey on 11/30/16.
 */

public final class Constants {

    // encryption seed
    public final static String seed = "openctPassWDSeed";

    // login map keys
    public final static String USERNAME_KEY = "username";
    public final static String PASSWORD_KEY = "password";
    public final static String CAPTCHA_KEY = "captcha";

    // preference related
    public final static String PREF_INITED = "pref_inited";

    public final static String PREF_SCHOOL_NAME_KEY = "pref_school_name";

    public final static String PREF_CMS_USERNAME_KEY = "pref_cms_username";
    public final static String PREF_CMS_PASSWORD_KEY = "pref_cms_password";

    public final static String PREF_LIB_USERNAME_KEY = "pref_lib_username";
    public final static String PREF_LIB_PASSWORD_KEY = "pref_lib_password";

    public final static String PREF_WEEK_SET_KEY = "pref_tmp_week_set";
    public final static String PREF_CURRENT_WEEK_KEY = "current_week_seq";

    public final static String PREF_CMS_PASSWORD_ENCRYPTED = "cms_encrypted";
    public final static String PREF_LIB_PASSWORD_ENCRYPTED = "lib_encrypted";

    // default school name
    public final static String DEFAULT_SCHOOL_NAME = "njit";

    // school cms
    public final static String ZFSOFT = "zfsoft";
    public final static String NJSUWEN = "njsuwen";

    // library system
    public final static String OPAC = "opac";

    // loader results
    public final static int LOGIN_SUCCESS = 1, LOGIN_FAIL = -1;
    public final static int RESULT_OK = 2, RESULT_FAIL = -2;
    public final static int CAPTCHA_IMG_OK = 4, CAPTCHA_IMG_FAIL = -4;
    public final static int LOAD_MORE_OK = 5, LOAD_MORE_FAIL = -5;

    // class info background colors
    public final static String[] colorString = {
            "#8BC34A", "#03A9F4", "#FF9800", "#C5CAE9", "#FFCDD2", "#009688", "#536DFE"
    };

    public static int getColor(int seq) {
        return Color.parseColor(colorString[seq]);
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
