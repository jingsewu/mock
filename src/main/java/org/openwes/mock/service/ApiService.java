package org.openwes.mock.service;

import lombok.RequiredArgsConstructor;
import org.openwes.mock.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final HttpUtils httpUtils;

    @Value("${api.call.host}")
    private String host;

    public boolean call(String api, Object requestBody) {
        return httpUtils.call("http://" + host + ":9010/" + api, requestBody);
    }
}
