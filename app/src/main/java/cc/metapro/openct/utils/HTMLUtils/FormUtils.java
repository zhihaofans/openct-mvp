package cc.metapro.openct.utils.HTMLUtils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.utils.Constants;

/**
 * Created by jeffrey on 16/12/16.
 */

public class FormUtils {

    private final static Pattern INVISIBLE_FORM_ITEM = Pattern.compile("(DISPLAY: none)|(hidden)");

    private final static Pattern typeKey = Pattern.compile("(strSearchType)");

    private final static Pattern searchKey = Pattern.compile("(strText)");

    @NonNull
    public static String genLibSearchRequestContent(
            @NonNull Form form, @NonNull Map<String, String> kvs, @NonNull String charset) {
        StringBuilder sb = new StringBuilder();

        String searchType = kvs.get(Constants.SEARCH_TYPE);
        String searchContent = kvs.get(Constants.SEARCH_CONTENT);

        boolean clicked = false;
        for (Elements elements : form.mMap.values()) {
            Element element = classify(elements, null);

            if (element == null) continue;

            String type = element.attr("type");
            String key = element.attr("name");
            String value = element.attr("value");

            if (type.equalsIgnoreCase("image")) {
                type = "submit";
                key = "x=0&y";
                value = "0";
            }

            String onclick = element.attr("onclick");

            try {
                if (typeKey.matcher(key).find()) {
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(searchType, charset)).append("&");
                } else if ("radio".equalsIgnoreCase(type)) {
                    // radio options
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(value, charset)).append("&");
                } else if ("submit".equalsIgnoreCase(type)) {
                    // submit buttons
                    if (Strings.isNullOrEmpty(onclick) && !clicked) {
                        if (!Strings.isNullOrEmpty(key)) {
                            sb.append(key).append("=")
                                    .append(value).append("&");
                        }
                        clicked = true;
                    }
                } else if ("text".equalsIgnoreCase(type)) {
                    if (searchKey.matcher(key).find()) {
                        sb.append(URLEncoder.encode(key, charset)).append("=")
                                .append(URLEncoder.encode(searchContent, charset)).append("&");
                    }
                } else {
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(value, charset)).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @NonNull
    public static String genCMSLoginRequestContent(
            @NonNull Form form, @NonNull Map<String, String> kvs, @NonNull String charset) {

        StringBuilder sb = new StringBuilder();
        Elements prev = null;

        boolean clicked = false;
        for (Elements elements : form.mMap.values()) {
            Element element = classify(elements, null);

            if (element == null) continue;

            String type = element.attr("type");
            String key = element.attr("name");
            String value = element.attr("value");
            String onclick = element.attr("onclick");

            try {
                if ("radio".equalsIgnoreCase(type)) {
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(value, charset)).append("&");
                } else if ("submit".equalsIgnoreCase(type)) {
                    // submit buttons
                    if (Strings.isNullOrEmpty(onclick) && !clicked) {
                        sb.append(URLEncoder.encode(key, charset)).append("=")
                                .append(URLEncoder.encode(value, charset)).append("&");
                        clicked = true;
                    }
                } else if ("password".equalsIgnoreCase(type) && prev != null) {
                    // password text
                    String username = kvs.get(Constants.USERNAME_KEY);
                    String password = kvs.get(Constants.PASSWORD_KEY);
                    sb.append(prev.attr("name")).append("=").append(username).append("&");
                    sb.append(key).append("=").append(password).append("&");
                } else if ("text".equalsIgnoreCase(type)) {
                    // common text

                    // secret code text (after password)
                    if (prev != null && "password".equalsIgnoreCase(prev.attr("type"))) {
                        String code = kvs.get(Constants.CAPTCHA_KEY);
                        sb.append(URLEncoder.encode(key, charset)).append("=").append(URLEncoder.encode(code, charset)).append("&");
                    } else {
                        Matcher matcher = INVISIBLE_FORM_ITEM.matcher(elements.toString());
                        if (matcher.find()) {
                            sb.append(URLEncoder.encode(key, charset)).append("=")
                                    .append(URLEncoder.encode(value, charset)).append("&");
                        }
                    }
                } else {
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(value, charset)).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            prev = elements;
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static Element classify(@NonNull Elements elements, @Nullable String preferedValue) {
        if (elements.size() == 0) return null;
        String tagName = elements.get(0).tagName();
        if ("input".equalsIgnoreCase(tagName)) {
            String type = elements.attr("type");
            switch (type) {
                case "radio":
                    return radio(elements, preferedValue);
            }
        }
        return elements.get(0);
    }

    private static Element radio(@NonNull Elements radios, @Nullable String preferedValue) {
        for (Element r : radios) {
            if (r.hasAttr("checked")) {
                if (!Strings.isNullOrEmpty(preferedValue)) {
                    r = r.attr("value", preferedValue);
                }
                return r;
            }
        }
        return radios.get(0);
    }
}
