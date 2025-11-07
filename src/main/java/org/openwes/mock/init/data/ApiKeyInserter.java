package org.openwes.mock.init.data;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiKeyInserter {

    private final JdbcTemplate jdbcTemplate;
    @Value("${api.call.key}")
    private String apiKey;

    public Long insert(String apiKey) {

        if (StringUtils.isEmpty(apiKey)) {
            apiKey = this.apiKey;
        }

        String sql = """
                INSERT INTO a_api_key 
                (id, api_key_name, api_key) 
                VALUES (?, ?, ?)
                """;

        // Generate ID - you might want to use a sequence or other ID generation strategy
        Long id = 1L;

        int affectedRows = jdbcTemplate.update(sql,
                id,
                "mock",
                apiKey
        );

        if (affectedRows == 0) {
            throw new RuntimeException("Failed to insert API key");
        }

        return id;
    }
}
