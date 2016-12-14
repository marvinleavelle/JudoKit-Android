package com.judopay.api;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.judopay.DeviceDna;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

class DeviceDnaInterceptor implements Interceptor {

    private final DeviceDna deviceDna;
    private final String deviceId;

    DeviceDnaInterceptor(Context context, String deviceId) {
        this.deviceDna = new DeviceDna(context);
        this.deviceId = deviceId;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Request request = chain.request();

        if (isPost(request)) {
            JsonElement body = getJsonRequestBody(request.body());

            if (body.isJsonObject()) {
                JsonObject json = body.getAsJsonObject();

                addClientDetails(json);
                RequestBody requestBody = getJsonRequestBody(json);

                return chain.proceed(request.newBuilder()
                        .post(requestBody)
                        .build());
            } else {
                return chain.proceed(request);
            }
        }
        return chain.proceed(request);
    }

    private void addClientDetails(JsonObject json) {
        Map<String, String> signals = deviceDna.deviceSignals();
        JsonObject clientDetailsJson = new JsonObject();

        for (Map.Entry<String, String> entry : signals.entrySet()) {
            clientDetailsJson.addProperty(entry.getKey(), entry.getValue());
        }

        clientDetailsJson.addProperty("deviceIdentifier", deviceId);
        json.add("clientDetails", clientDetailsJson);
    }

    private boolean isPost(okhttp3.Request request) {
        return request.body() != null && "POST".equals(request.method());
    }

    private RequestBody getJsonRequestBody(JsonObject json) {
        MediaType mediaType = MediaType.parse("application/json");

        return RequestBody.create(mediaType, json.toString());
    }

    private JsonElement getJsonRequestBody(final okhttp3.RequestBody request) throws IOException {
        final Buffer buffer = new Buffer();

        request.writeTo(buffer);
        String body = buffer.readUtf8();

        JsonParser parser = new JsonParser();
        return parser.parse(body);
    }
}