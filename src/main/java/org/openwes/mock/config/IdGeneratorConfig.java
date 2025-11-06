package org.openwes.mock.config;

import org.openwes.mock.utils.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    @Value("${data.center.id:2}")
    private long dataCenterId;

    @Value("${worker.id:3}")
    private long workerId;

    @Bean
    public Snowflake snowflake() {
        return new Snowflake(dataCenterId, workerId);
    }
}
