package org.openwes.mock.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("ThreadUtils.sleep()", e);
            Thread.currentThread().interrupt();
        }
    }
}
