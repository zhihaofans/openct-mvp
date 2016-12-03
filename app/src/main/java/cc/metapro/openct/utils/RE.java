package cc.metapro.openct.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeffrey on 11/30/16.
 */

public class RE {

    private static Pattern time_during_pattern = Pattern.compile("[0-9]{1,2}");
    private static Pattern character = Pattern.compile("\\b[a-zA-Z]+\\b");
    private static Pattern doubleWidthChar = Pattern.compile("[^\\x00-\\xff]+");
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

    public static List<Integer> getInt(String s) {
        List<Integer> list = new ArrayList<>();
        Matcher m = time_during_pattern.matcher(s);
        while (m.find()) {
            list.add(Integer.parseInt(m.group()));
        }
        return list;
    }

    public static String getEnString(String s) {
        StringBuilder sb = new StringBuilder();
        Matcher m = character.matcher(s);
        while (m.find()) {
            sb.append(m.group());
        }
        return sb.toString();
    }

    public static String getCnString(String s) {
        StringBuilder sb = new StringBuilder();
        Matcher m = doubleWidthChar.matcher(s);
        while (m.find()) {
            sb.append(sb);
        }
        return sb.toString();
    }

    public static boolean isEmpty(String s) {
        if (s == null) return true;
        Matcher m = empty.matcher(s);
        return m.find();
    }

}
