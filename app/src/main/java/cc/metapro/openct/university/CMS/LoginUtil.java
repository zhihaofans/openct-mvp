package cc.metapro.openct.university.CMS;

import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by jeffrey on 16/12/5.
 */

public class LoginUtil {

    public static String appendParams(String key, String value, boolean isFirst, String charSet) {
        try {
            String a = "";
            if (!isFirst)
                a += "&";
            a += URLEncoder.encode(key, charSet) + "=" + URLEncoder.encode(value, charSet);
            return a;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String appendParams(String str) {
        if (!Strings.isNullOrEmpty(str)) return "&" + str;
        return "";
    }
}
