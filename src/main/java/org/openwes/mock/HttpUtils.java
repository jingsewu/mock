package org.openwes.mock;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpUtils {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    @Value("${api.call.key}")
    private String apiKey;

    public void call(String url, Object requestBody) {

        RequestBody body = RequestBody.create(JsonUtils.obj2String(requestBody), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("X-API-KEY", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                log.error("call response error, response body is null： {}", request);
            }
            log.info("call response: {}", response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
