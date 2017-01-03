package cc.metapro.openct.data.source;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by jeffrey on 12/1/16.
 */

public final class StoreHelper {

    private final static Gson gson = new Gson();

    public static void saveTextFile(Context context, String fileName, String content) throws IOException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
        br.write(content);
        br.flush();
        br.close();
        fos.close();
    }

    @NonNull
    static String getAssetText(Context context, String filename) throws IOException {
        InputStream fis = context.getAssets().open(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String tmp = br.readLine();
        while (tmp != null) {
            sb.append(tmp);
            tmp = br.readLine();
        }
        br.close();
        fis.close();
        return sb.toString();
    }

    public static String getJsonText(Object infos) {
        return gson.toJson(infos);
    }

    public static void storeBytes(String path, InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
        byte[] buffer = new byte[2048];
        int count;
        while ((count = din.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.close();
        din.close();
    }

}
