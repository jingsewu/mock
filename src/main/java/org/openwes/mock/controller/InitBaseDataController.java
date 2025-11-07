package org.openwes.mock.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.controller.parameter.ContainerInitRequestDTO;
import org.openwes.mock.controller.parameter.MockBaseDataRequest;
import org.openwes.mock.controller.parameter.MockPutWallRequest;
import org.openwes.mock.init.data.*;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InitBaseDataController {

    private final SkuBatchInserter skuBatchInserter;
    private final WorkStationInserter workStationInserter;
    private final PutWallBatchInserter putWallBatchInserter;
    private final BaseDataInserter baseDataInserter;
    private final ApiKeyInserter apiKeyInserter;
    private final ApiService apiService;
    private final DatabaseQueryService databaseQueryService;
    private final MockConfig mockConfig;

    @PostMapping("baseData")
    @Operation(summary = "01.初始化基础数据")
    @Order(1)
    public void initBaseData(@RequestBody @Valid MockBaseDataRequest request) {
        baseDataInserter.initMockBaseData(request);
    }

    @PostMapping("sku")
    @Operation(summary = "02.初始化SKU")
    @Order(2)
    public void initSku(int number) {
        if (databaseQueryService.queryFirstOwner().isEmpty()) {
            throw new RuntimeException("No available owner found");
        }
        CompletableFuture.runAsync(() -> {
            skuBatchInserter.insert100MRecords(number);
        }).exceptionally(throwable -> {
            log.error("Error occurred while inserting skus", throwable);
            return null;
        });
    }

    @PostMapping("container")
    @Operation(summary = "03.初始化容器")
    @Order(3)
    public void initContainer(@RequestBody @Valid ContainerInitRequestDTO request) {

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

        request.setWarehouseCode(warehouseCode);
        request.setContainerSpecCode(containerSpecCode);

        CompletableFuture.runAsync(() -> {
            apiService.call("basic/container/create", request);
        }).exceptionally(throwable -> {
            log.error("Error occurred while inserting containers", throwable);
            return null;
        });
    }

    @PostMapping("workStation")
    @Operation(summary = "04.初始化工作站")
    @Order(4)
    public void initWorkStation() {

        String warehouseCode = databaseQueryService.queryFirstWarehouseCode();
        if (StringUtils.isEmpty(warehouseCode)) {
            throw new RuntimeException("No available warehouse found");
        }
        Long warehouseAreaId = databaseQueryService.queryFirstWarehouseAreaId();
        if (warehouseAreaId == null) {
            throw new RuntimeException("No available warehouse area found");
        }
        workStationInserter.batchInsertMockWorkStations(100, "test", warehouseCode, warehouseAreaId);
    }

    @PostMapping("putWall")
    @Operation(summary = "05.初始化PutWall")
    @Order(5)
    public void initPutWall(@RequestBody @Valid MockPutWallRequest request) {
        putWallBatchInserter.initMockPutWalls(request);
    }

    @PostMapping("apiKey")
    @Operation(summary = "06.初始化ApiKey")
    @Order(5)
    public void initApiKey(@RequestParam(required = false) String apiKey) {
        apiKeyInserter.insert(apiKey);
    }

    @PostMapping("openMock")
    @Operation(summary = "07.开启模拟")
    @Order(6)
    public void openMock() {
        mockConfig.setAllTrue();
    }
}
