package cc.metapro.openct.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static OkHttpClient client;
    private static Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public static Response curl(@NonNull String url,
                                @Nullable Map<String, String> header,
                                @Nullable String contentType, @Nullable String body,
                                @Nullable String filePath,
                                @Nullable Callback callback) throws IOException {
        if (client == null) {
            synchronized (OkCurl.class) {
                if (client == null) {
                    client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url.host(), cookies);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = cookieStore.get(url.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    }).build();
                }
            }
        }
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

    public static Response curlAsynGET(@NonNull String url,
                                       @Nullable Map<String, String> header,
                                       @Nullable String filePath, @NonNull Callback callback) throws IOException {
        return curl(url, header, null, null, filePath, callback);
    }

    public static Response curlSynPOST(@NonNull String url,
                                       @Nullable Map<String, String> header,
                                       @NonNull String contentType,
                                       @NonNull String body) throws IOException {
        return curl(url, header, contentType, body, null, null);
    }

    public static Response curlAsynPOST(@NonNull String url,
                                        @Nullable Map<String, String> header,
                                        @NonNull String contentType, @NonNull String body,
                                        @Nullable String filePath,
                                        @NonNull Callback callback) throws IOException {
        return curl(url, header, contentType, body, filePath, callback);
    }

    public static List<Cookie> getCookieOf(String url) {
        if (url == null || "".equals(url)) return null;
        HttpUrl httpUrl = HttpUrl.parse(url);
        return cookieStore.get(httpUrl.host());
    }

    public static void addCookieOf(String url, String cookie) {
        if (Strings.isNullOrEmpty(cookie)) return;
        HttpUrl httpUrl = HttpUrl.parse(url);
        Cookie cookie1 = Cookie.parse(httpUrl, cookie);
        List<Cookie> cookieList = getCookieOf(url);
        if (cookieList == null) {
            cookieList = new ArrayList<>(1);
        }
        cookieList.add(cookie1);
        cookieStore.put(httpUrl.host(), cookieList);
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

    @NonNull
    public static String getString(InputStream stream, String charset) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, charset));
        String tmp = br.readLine();
        StringBuilder sb = new StringBuilder();
        while (tmp != null) {
            sb.append(tmp);
            tmp = br.readLine();
        }
        return sb.toString();
    }
}