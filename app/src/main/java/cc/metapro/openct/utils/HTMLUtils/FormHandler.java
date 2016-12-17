package cc.metapro.openct.utils.HTMLUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by jeffrey on 16/12/16.
 */
public class FormHandler {

    // use linkedHashMap to ensure form seq
    private LinkedHashMap<String, List<BaseForm>> mForms;

    public FormHandler(String html, String baseURL) {
        mForms = new LinkedHashMap<>();
        Document document = Jsoup.parse(html, baseURL);
        Elements elements = document.getElementsByTag("form");
        for (Element form : elements) {
            addForm(form);
        }
    }

    private void addForm(Element form) {
        String name = form.attr("name");
        List<BaseForm> stored = mForms.get(name);
        if (stored == null) {
            List<BaseForm> toAdd = new ArrayList<>();
            toAdd.add(new BaseForm(form));
            mForms.put(name, toAdd);
        } else {
            stored.add(new BaseForm(form));
        }
    }

    public List<BaseForm> getFormsByName(String name) {
        return mForms.get(name);
    }

    public BaseForm getForm(int i) {
        int count = -1;
        for (List<BaseForm> baseForms : mForms.values()) {
            for (BaseForm s : baseForms) {
                count++;
                if (count == i) {
                    return s;
                }
            }
        }
        return null;
    }
}
