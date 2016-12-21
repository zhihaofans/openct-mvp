package cc.metapro.openct.utils.HTMLUtils;

import android.support.annotation.NonNull;

import com.google.common.base.Strings;

/**
 * Created by jeffrey on 16/12/21.
 */

public class Utils {

    public final static String BR_RE = "<\\s*?br\\s*?/?>";

    public static String replaceAllBrWith(String html, @NonNull String preferedString) {
        return Strings.isNullOrEmpty(html) ? html : html.replaceAll(BR_RE, preferedString);
    }
}
