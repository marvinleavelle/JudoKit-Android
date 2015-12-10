package com.judopay.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.judopay.JudoApiService;
import com.judopay.JudoPay;
import com.judopay.model.ClientDetails;
import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Factory that provides the {@link JudoApiService} used for performing all HTTP requests to the
 * judoPay APIs. As creating the JudoApiService requires lots of setup, it is better to use a shared
 * instance than create a new instance per request, so this class ensures that only one instance is
 * used in the application.
 */
public class JudoApiServiceFactory {

    private static final String PARTNER_API_SANDBOX_HOST = "partnerapi.judopay-sandbox.com";
    private static final String PARTNER_API_LIVE_HOST = "partnerapi.judopay.com";

    private static final String CERTIFICATE_1 = "sha1/SSAG1hz7m8LI/eapL/SSpd5o564=";
    private static final String CERTIFICATE_2 = "sha1/o5OZxATDsgmwgcIfIWIneMJ0jkw=";

    private static Retrofit retrofit;

    /**
     * @param context the calling Context
     * @return the Retrofit API service implementation containing the methods used
     * for interacting with the judoPay REST API.
     */
    public static JudoApiService getInstance(Context context) {
        return createOrGetInstance(context).create(JudoApiService.class);
    }

    private static Retrofit createOrGetInstance(Context context) {
        if (retrofit == null) {
            retrofit = createRetrofit(context.getApplicationContext());
        }
        return retrofit;
    }

    private static Retrofit createRetrofit(Context context) {
        return new Retrofit.Builder()
                .addConverterFactory(getGsonConverterFactory(context))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(JudoPay.getApiEnvironmentHost())
                .client(getOkHttpClient())
                .build();
    }

    private static OkHttpClient getOkHttpClient() {
        OkHttpClient client = new OkHttpClient();

        setTimeouts(client);
        setSslSocketFactory(client);
        setSslPinning(client);
        setInterceptors(client);

        return client;
    }

    private static void setInterceptors(OkHttpClient client) {
        AuthorizationEncoder authorizationEncoder = new AuthorizationEncoder();
        ApiHeadersInterceptor interceptor = new ApiHeadersInterceptor(authorizationEncoder);

        List<Interceptor> interceptors = client.interceptors();
        interceptors.add(new DeDuplicationInterceptor());
        interceptors.add(interceptor);
    }

    private static GsonConverterFactory getGsonConverterFactory(Context context) {
        return GsonConverterFactory.create(getGson(context));
    }

    private static Gson getGson(Context context) {
        return getGsonBuilder()
                .registerTypeAdapter(ClientDetails.class, new ClientDetailsSerializer(context))
                .create();
    }

    static GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateJsonDeserializer())
                .registerTypeAdapter(BigDecimal.class, new FormattedBigDecimalDeserializer());
    }

    private static void setSslPinning(OkHttpClient client) {
        if (JudoPay.isSslPinningEnabled()) {
            client.setCertificatePinner(new CertificatePinner.Builder()
                    .add(PARTNER_API_SANDBOX_HOST, CERTIFICATE_1)
                    .add(PARTNER_API_SANDBOX_HOST, CERTIFICATE_2)
                    .add(PARTNER_API_LIVE_HOST, CERTIFICATE_1)
                    .add(PARTNER_API_LIVE_HOST, CERTIFICATE_2)
                    .build());
        }
    }

    private static void setSslSocketFactory(OkHttpClient client) {
        try {
            client.setSslSocketFactory(new TlsSslSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setTimeouts(OkHttpClient client) {
        client.setConnectTimeout(30, SECONDS);
        client.setReadTimeout(30, SECONDS);
        client.setWriteTimeout(30, SECONDS);
    }

}