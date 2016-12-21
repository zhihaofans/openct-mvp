package cc.metapro.openct.utils;

import android.graphics.Color;

/**
 * Created by jeffrey on 11/30/16.
 */

public final class Constants {

    // encryption seed
    public final static String seed = "MIIEpAIBAAKCAQEAxYCLrrLLFEWZ+lL08B+a2YREEroqyfksL6RkyWeWodSaiUa6OH7d2NBc6gvz6zykca9GrHjpQk+puiu81naUO7H4HckEiSoOSLWM2FtyXJvfa0NqjwjiRYaNOycnAna8Rb6ZsEKMNAz3fJZ3tZkPic+XbUMeiKuUuwjkOd7VIBVBW1I1316DygBIl472TobCiZu+cXrbb5GWdRMCHGqS02zIzc2XRVnPcmX1rOWiDtWeXmaZsDnmLR2Zc00o6mh/s83Fji08trNSofP1Aq6+vDsW6maF98k4iC2//opdAmueDF5t0gmb5wxoqdX5z6Q8xax58DfmvnRETCGDDpLcrwIDAQABAoIBAQDBgxrZuBtcqHmfGKsfn4fkukDMrJYCQV826vdJk3K2643jFWaetkd+CWIQfFepVEi3jwpLMUkzjMR7QiGLbLH+73hxDqWgE5HdUe1HjAo4jQ1SlsKQlR7HCnfUzp7dPiNonsENP5cJ5O6UpwVx7B+aYsk26D3BoYRf4e1kel4OSKw6lr611GXAjqiX+59qTZQKb5xTiai6jGiE8BLoIoRvtIh+8rv8Rg0jQkxbmwKUHxGPm/wB3OVoIEzXUjFB2J03NwTEeh4aQfDr983ro5FSVRZ646dkZzOFQZSIvqJAAg46SO6H7D4g9u40fkwXU5/XZzle6dRGOjnfM9s2OP/BAoGBAOoplhu3LHyrCnz5zGJRRx7QxYoFLxIJnU/C/LX+YJrqHoa8buNvv0Cijcf8o3magjtVCdMG6h6A/Y3krWrFZPePwOUD+4g7xUdlNBe0srVGGWUnR4aeybzhs7pSScOMx/j7PkdteMTIk8tfA6fM/iHbvBlF8iUSWWZ87EnDH5ihAoGBANfrulW5dqk7zGukHz13K7yE+Yg7RGwScInPly1RqlktwgxD1kjl9AO1qGk19say/6GBhjk+oIi829D8TovyjuhehtjlMRe8gecwjBPIE4PVpu6EHkRKO5VSsfDeaaioi2qeu9EOs1E+ZpDZFglhroQ3Gxu41tZBbAgrs391/ONPAoGAWW2ac2lIZzBXaBVqlh3eYIlw99409NmRJ7YbF1JYCLHjCKgMXXX+/6tVJIx6zmVQ2WIHx307inzO2RL9m+pZPia7j3su4/+Xv0WKWIddPNfRuQ0ARwx7lVRJdJ4ap8ErWg5x4YuQdO75atEVr08du0aVFr3c1YWlePu6rg0EEyECgYAPsT8imc8I76KktBHdKrsQAW4NO5l2bUSmj2LCVWwW/R9cOtXpCGVam6o4s0ZTHJE9kKdLo8SRC4DCSIQA4ckHFE+ilc2ilv2t6rZTfbgFXdK7BPkaJ3b7HD54bgGp004GmrC1uebIYcucfVp/pgzD6SlcM2vRuIF8eoiaG94cOwKBgQC6rS+E8UrvvZeCTV9HkhJ3wioq1K3qxZeBZFhXOFrJ09iJUCrVDE/IZE7T1kn45uEIqu/gs2Fxqiid8ieIy06Z2u542nmKy3mt4cGMhgTabK0aiySD2aDbPBqWicZ/VSDXeJ9a5RwtSzj0ZnmlEa7oeUKV+7nQzA/t3ZkpwjyGNg==";

    public final static String POST_CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public final static String BR_REPLACER = "&";

    public final static String FORM_ITEMS_RE = "(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)";

    //
    public final static String TITLE = "title";
    public final static String URL = "url";

    // login map keys
    public final static String USERNAME_KEY = "username";
    public final static String PASSWORD_KEY = "password";
    public final static String CAPTCHA_KEY = "captcha";

    // cet map keys
    public final static String CET_NUM_KEY = "cet_num";
    public final static String CET_NAME_KEY = "cet_name";
    public final static String CET_TYPE_KEY = "cet_type";
    public final static String CET_SCHOOL_KEY = "cet_school";
    public final static String CET_TIME_KEY = "cet_key";
    public final static String CET_GRADE_KEY = "cet_grade";

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
    public final static String NJHUIWEN = "njhuiwen";

    public final static String SEARCH_TYPE = "type";
    public final static String SEARCH_CONTENT = "content";

    // loader results
    public final static int CAPTCHA_IMG_OK = 1, CAPTCHA_IMG_FAIL = -1;

    // results for classtable
    public final static int GET_CLASS_OK = 2, GET_CLASS_FAIL = -2;

    // results for gradeList
    public final static int GET_GRADE_OK = 3, GET_GRADE_FAIL = -3;
    public final static int GET_CET_GRADE_OK = 4, GET_CET_GRADE_FAIL = -4;

    // results for libBorrow
    public final static int LIB_BORROW_OK = 5, LIB_BORROW_FAIL = -5;

    // results for libSearch
    public final static int LIB_SEARCH_OK = 6, LIB_SEARCH_FAIL = -6;
    public final static int NEXT_PAGE_OK = 7, NEXT_PAGE_FAIL = -7;

    // results for login fail
    public final static int LOGIN_FAIL = -8;

    public final static int NETWORK_ERROR = -9;
    public final static int NETWORK_TIMEOUT = -10;

    public final static int EMPTY = -11;

    public final static int FILE_FETCH_ERROR = -12;

    public final static int FATAL_UNIVERSITY_NULL = -13;

    public final static int UNKNOWN_ERROR = Integer.MIN_VALUE;

    // filename
    public final static String STU_CLASS_INFOS_FILE = "cms_class.json";
    public final static String STU_GRADE_INFOS_FILE = "cms_grade.json";
    public final static String LIB_BORROW_INFOS_FILE = "lib_borrow.json";
    public final static String CAPTCHA_FILENAME = "captcha";
    // class info background colors
    public final static String[] colorString = {
            "#8BC34A", "#03A9F4", "#FF9800", "#C5CAE9", "#FFCDD2", "#009688", "#536DFE"
    };
    public static String CAPTCHA_FILE;

    public static int getColor(int seq) {
        return Color.parseColor(colorString[seq]);
    }
}
