package cc.metapro.openct.university;

import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.BaseForm;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import cc.metapro.openct.utils.OkCurl;


/**
 * Created by jeffrey on 16/12/17.
 */

public class UniversityHelper {

    public final static String METHOD = "method", ACTION = "action", CONTENT = "content";

    public static Map<String, String> formSearchGetContent(Map<String, String> searchMap, String baseURL) throws IOException {
        String loginPageHtml = OkCurl.curlSynGET(baseURL, null, null).body().string();

        FormHandler handler = new FormHandler(loginPageHtml, baseURL);
        BaseForm form = handler.getForm(0);

        if (form == null) return null;

        Map<String, String> res = new HashMap<>();
        res.put(CONTENT, FormUtils.genLibSearchRequestContent(form, searchMap, "utf-8"));
        res.put(METHOD, form.getMethod());
        res.put(ACTION, form.getAction());
        return res;
    }

    public static Map<String, String> formLoginPostContent(Map<String, String> loginMap, String baseURL, @Nullable String radioOption) throws IOException {
        String loginPageHtml = OkCurl.curlSynGET(baseURL, null, null).body().string();

        FormHandler handler = new FormHandler(loginPageHtml, baseURL);
        BaseForm form = handler.getForm(0);

        if (form == null) return null;

        if (!Strings.isNullOrEmpty(radioOption)) {
            loginMap.put(Constants.HTML_FORM_RADIO_OPTION, radioOption);
        }
        Map<String, String> res = new HashMap<>();
        res.put(CONTENT, FormUtils.genCMSLoginRequestContent(form, loginMap, "utf-8"));
        res.put(METHOD, form.getMethod());
        res.put(ACTION, form.getAction());
        return res;
    }

}
