package cc.metapro.openct.utils.HTMLUtils;
import android.support.annotation.NonNull;

import com.google.common.base.Strings;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.utils.Constants;

/**
 * Created by jeffrey on 16/12/16.
 */
public class Form {

    public final static String RADIOOPTION = "radio_option";

    private final static Pattern INVISIBLE = Pattern.compile("(DISPLAY: none)|(hidden)");

    private String mName;

    private String mId;

    private String mMethod;

    private String mAction;

    // ensure inputs sequence
    public LinkedHashMap<String, Elements> mMap;

    public Form(Element form) {
        mName = form.attr("name");
        mId = form.attr("id");
        mMethod = form.attr("method");
        mAction = form.absUrl("action");

        mMap = new LinkedHashMap<>();

        Elements inputs = form.select("input");
        for (Element inputItem : inputs) {
            addInputItem(inputItem);
        }
    }

    private void addInputItem(Element inputItem) {
        String key = inputItem.attr("name");
        Elements stored = mMap.get(key);
        if (stored == null) {
            mMap.put(key, new Elements(inputItem));
        } else {
            stored.add(inputItem);
        }
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getMethod() {
        return mMethod;
    }

    public String getAction() {
        return mAction;
    }

    public String formLoginContent(@NonNull Map<String, String> kvs, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        Elements prev = null;

        boolean clicked = false;
        for (Elements e : mMap.values()) {
            String key = e.attr("name");
            String value = e.attr("value");
            String type = e.attr("type");
            String onclick = e.attr("onclick");

            if ("radio".equalsIgnoreCase(type)) {
                // radio options
                String radioOption = kvs.get(RADIOOPTION);
                if (radioOption != null) {
                    sb.append(URLEncoder.encode(key, charset)).append("=")
                            .append(URLEncoder.encode(radioOption, charset)).append("&");
                }
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
                    Matcher matcher = INVISIBLE.matcher(e.toString());
                    if (matcher.find()) {
                        sb.append(URLEncoder.encode(key, charset)).append("=")
                                .append(URLEncoder.encode(value, charset)).append("&");
                    }
                }
            } else {
                sb.append(URLEncoder.encode(key, charset)).append("=")
                        .append(URLEncoder.encode(value, charset)).append("&");
            }
            prev = e;
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
