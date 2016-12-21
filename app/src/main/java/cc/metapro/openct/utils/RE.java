package cc.metapro.openct.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 11/30/16.
 */

public class RE {

    private static Pattern time_during_pattern = Pattern.compile("[0-9]{1,2}");
    private static Pattern empty = Pattern.compile("^\\s+$");


    public static int[] getStartEnd(String s) {
        int[] res = {-1, -1};
        Matcher m = time_during_pattern.matcher(s);
        for (int i = 0; i < res.length; i++) {
            if (m.find()) {
                res[i] = Integer.parseInt(m.group());
            }
        }
        if (res[0] > res[1]) {
            return new int[]{-1, -1};
        }
        return res;
    }

    public static boolean isEmpty(String s) {
        if (s == null) return true;
        Matcher m = empty.matcher(s);
        return m.find();
    }

}
