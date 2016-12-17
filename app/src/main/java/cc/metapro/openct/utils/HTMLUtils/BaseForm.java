package cc.metapro.openct.utils.HTMLUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 16/12/16.
 */

public class BaseForm {

    private final static Pattern formItemPattern =
            Pattern.compile("(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)");
    protected LinkedHashMap<String, Elements> mMap;
    private String mName;
    private String mId;
    private String mMethod;
    private String mAction;

    public BaseForm(Element form) {
        mName = form.attr("name");
        mId = form.attr("id");
        mMethod = form.attr("method");
        mAction = form.absUrl("action");

        mMap = new LinkedHashMap<>();

        for (Element e : form.getAllElements()) {
            if (formItemPattern.matcher(e.tagName()).find()) {
                if ("select".equalsIgnoreCase(e.tagName())) {
                    Elements options = e.select("option");
                    if (options != null) {
                        Element defaultOption = options.get(0);
                        e = e.attr("value", defaultOption.attr("value"));
                    }
                }
                addFormItem(e);
            }
        }
    }

    private void addFormItem(Element item) {
        String key = item.attr("name");
        Elements stored = mMap.get(key);
        if (stored == null) {
            mMap.put(key, new Elements(item));
        } else {
            stored.add(item);
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

}
