package cc.metapro.openct.data.ServerService;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import cc.metapro.openct.utils.OkCurl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by jeffrey on 16/12/25.
 */

public class ServiceGenerator {

    public static final String GSON_CONVERTER = "gson", HTML_CONVERTER = "html";

    private static final String API_BASE_URL = "http://openct.metapro.cc/";

    public static <S> S createService(Class<S> serviceClass, String convertType) {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .client(OkCurl.getClient());

        Retrofit retrofit = null;

        if (GSON_CONVERTER.equals(convertType)) {
            retrofit = builder
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else if (HTML_CONVERTER.equals(convertType)) {
            retrofit = builder
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }

        return retrofit == null ? null : retrofit.create(serviceClass);
    }

    private static class ToStringConvertFactory extends Converter.Factory {

        private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(
                Type type,
                Annotation[] annotations,
                Retrofit retrofit
        ) {
            if (String.class.equals(type)) {
                return new Converter<ResponseBody, String>() {
                    @Override
                    public String convert(ResponseBody value) throws IOException {
                        return value.string();
                    }
                };
            }
            return null;
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations,
                                                              Annotation[] methodAnnotations,
                                                              Retrofit retrofit
        ) {
            if (String.class.equals(type)) {
                return new Converter<String, RequestBody>() {
                    @Override
                    public RequestBody convert(String value) throws IOException {
                        return RequestBody.create(MEDIA_TYPE, value);
                    }
                };
            }
            return null;
        }
    }
}
