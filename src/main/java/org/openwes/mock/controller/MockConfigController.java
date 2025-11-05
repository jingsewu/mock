package org.openwes.mock.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.controller.parameter.MockConfigDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
@Slf4j
public class MockConfigController {

    private final MockConfig mockConfig;

    @PutMapping("/config")
    public void updateMockConfig(@RequestBody MockConfigDTO mockConfigDTO) {
        BeanUtils.copyProperties(mockConfigDTO, mockConfig);

        log.info("Mock configuration updated: {}", mockConfig);
    }

    @GetMapping("/config")
    public MockConfigDTO getMockConfig() {
        MockConfigDTO dto = new MockConfigDTO();
        BeanUtils.copyProperties(mockConfig, dto);
        return dto;
    }
}
