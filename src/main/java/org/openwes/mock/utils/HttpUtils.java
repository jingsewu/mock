package org.openwes.mock.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpUtils {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    @Value("${api.call.key}")
    private String apiKey;

    public boolean call(String url, Object requestBody) {

        RequestBody body = RequestBody.create(JsonUtils.obj2String(requestBody), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("call url: {} response error, response code: {}", url, response.code());
                return false;
            }

            String responseBody = "";
            if (response.body() == null || (responseBody = response.body().string()).isEmpty()) {
                log.info("call url: {} response: {}", url, responseBody);
                return true;
            }

            log.info("call url: {} response: {}", url, responseBody);

            java.util.Map map = JsonUtils.string2Object(responseBody, java.util.Map.class);
            return "0".equals(map.get("code"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get(String url) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.error("get url: {} response error, response body is null： {}", url, request);
                return null;
            }

            String body = response.body().string();
            log.debug("get url: {} response: {}", url, body);

            return body;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String url, Object requestBody) {

        RequestBody body = RequestBody.create(JsonUtils.obj2String(requestBody), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.error("put url: {} response error, response body is null： {}", url, request);
            }
            log.info("put url: {} response: {}", url, response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
