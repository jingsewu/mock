package org.openwes.mock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@CacheConfig(cacheNames = "containers")
@RequiredArgsConstructor
public class ContainerService {

    private final DatabaseQueryService databaseQueryService;

    @Cacheable(value = "outsideContainers", sync = true)
    public List<Map<String, Object>> getAllOutsideContainers() {
        return databaseQueryService.queryContainers();
    }

    @CacheEvict(value = {"outsideContainers", "randomContainer"}, allEntries = true)
    public void refreshCache() {
        // Cache will be refreshed on next access
    }
}
