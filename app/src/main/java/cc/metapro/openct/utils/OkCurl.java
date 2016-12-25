package cc.metapro.openct.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jeffrey on 11/30/16.
 */

public class OkCurl {

    private static OkHttpClient client =
            new OkHttpClient.Builder().cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            }).connectTimeout(20, TimeUnit.SECONDS).build();

    private static Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public static Response curl(@NonNull String url,
                                @Nullable Map<String, String> header,
                                @Nullable String contentType, @Nullable String body,
                                @Nullable String filePath,
                                @Nullable Callback callback) throws IOException {
        Request request = formRequest(url, header, contentType, body);
        Call call = client.newCall(request);
        if (callback != null) {
            call.enqueue(callback);
        } else {
            Response response = call.execute();
            if (response.isSuccessful()) {
                // save file according to filePath
                if (filePath != null && !"".equals(filePath)) {
                    DataInputStream ins = new DataInputStream(response.body().byteStream());
                    DataOutputStream out = new DataOutputStream(new FileOutputStream(filePath));
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = ins.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                    }
                    out.close();
                    ins.close();
                    return null;
                } else {
                    return response;
                }
            }
        }
        return null;
    }

    public static Response curlSynGET(@NonNull String url,
                                      @Nullable Map<String, String> header,
                                      @Nullable String filePath) throws IOException {
        return curl(url, header, null, null, filePath, null);
    }

    private static Request formRequest(@NonNull String url,
                                       @Nullable Map<String, String> header,
                                       @Nullable String contentType,
                                       @Nullable String body) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                if (key != null && val != null) {
                    builder.addHeader(key, val);
                }
            }
        }
        if (contentType != null && body != null) {
            builder.post(RequestBody.create(MediaType.parse(contentType), body));
        }
        return builder.build();
    }

    public static OkHttpClient getClient() {
        return client;
    }
}