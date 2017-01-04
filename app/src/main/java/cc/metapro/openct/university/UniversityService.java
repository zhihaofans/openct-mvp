package cc.metapro.openct.university;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface UniversityService {

    @POST
    @FormUrlEncoded
    Call<String> login(
            @Url String url,
            @Header("Referer") String referer,
            @FieldMap Map<String, String> loginMap
    );

    @Streaming
    @GET
    Call<ResponseBody> getCAPTCHA(
            @Url String url
    );

    @GET
    Call<String> searchLibrary(
            @Url String url,
            @Header("Referer") String referer,
            @QueryMap Map<String, String> searchMap
    );

    @GET
    Call<String> getPage(
            @Url String url,
            @Header("Referer") String referer
    );

    @GET("http://www.chsi.com.cn/cet/query")
    Call<String> queryCET(
            @Header("Referer") String referer,
            @Query("zkzh") String num,
            @Query("xm") String name,
            @Query("_t") String t
    );
}
