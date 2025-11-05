package org.openwes.mock.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.controller.parameter.ContainerInitRequestDTO;
import org.openwes.mock.init.data.SkuBatchInserter;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
@Slf4j
public class InitBaseDataController {

    private final SkuBatchInserter skuBatchInserter;
    private final ApiService apiService;
    private final DatabaseQueryService databaseQueryService;

    @PostMapping("sku")
    public void initSku() {
        if (skuBatchInserter.getInsertedRecordCount() >= 1000000) {
            return;
        }
        skuBatchInserter.insert100MRecords();
    }

    @PostMapping("container")
    public void initContainer() {

        Long containerCount = databaseQueryService.queryContainerCount();
        if (containerCount != null && containerCount >= 500000) {
            log.info("Container count already over 500000, skip container creation");
            return;
        }

        List<Map<String, Object>> warehouses = databaseQueryService.queryWarehouses();
        if (warehouses.isEmpty()) {
            throw new RuntimeException("No available warehouse found");
        }
        String warehouseCode = (String) warehouses.getFirst().get("warehouseCode");

        // 再查询容器规格信息
        List<Map<String, Object>> containerSpecs = databaseQueryService.queryContainerSpecs();
        if (containerSpecs.isEmpty()) {
            throw new RuntimeException("No available container spec found");
        }
        String containerSpecCode = (String) containerSpecs.getFirst().get("containerSpecCode");

        // 构造请求参数
        ContainerInitRequestDTO request = new ContainerInitRequestDTO();
        request.setWarehouseCode(warehouseCode);
        request.setContainerSpecCode(containerSpecCode);

        apiService.call("basic/container/create", request);
    }
}
