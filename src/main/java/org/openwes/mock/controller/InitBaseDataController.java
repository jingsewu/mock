package org.openwes.mock.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.mock.controller.parameter.ContainerInitRequestDTO;
import org.openwes.mock.controller.parameter.MockBaseDataRequest;
import org.openwes.mock.controller.parameter.MockPutWallRequest;
import org.openwes.mock.init.data.BaseDataInserter;
import org.openwes.mock.init.data.PutWallBatchInserter;
import org.openwes.mock.init.data.SkuBatchInserter;
import org.openwes.mock.init.data.WorkStationInserter;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    private final ApiService apiService;
    private final DatabaseQueryService databaseQueryService;

    @PostMapping("baseData")
    public void initBaseData(@RequestBody @Valid MockBaseDataRequest request) {
        baseDataInserter.initMockBaseData(request);
    }

    @PostMapping("sku")
    public void initSku(int number) {
        if (databaseQueryService.queryFirstOwner().isEmpty()) {
            throw new RuntimeException("No available owner found");
        }
        skuBatchInserter.insert100MRecords(number);
    }

    @PostMapping("container")
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

        apiService.call("basic/container/create", request);
    }

    @PostMapping("workStation")
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
    public void initPutWall(@RequestBody @Valid MockPutWallRequest request) {
        putWallBatchInserter.initMockPutWalls(request);
    }
}
