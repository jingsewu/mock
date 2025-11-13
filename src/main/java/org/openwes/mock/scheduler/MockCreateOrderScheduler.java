package org.openwes.mock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockCreateOrderScheduler {

    private final DatabaseQueryService databaseQueryService;
    private final ApiService apiService;
    private final MockConfig mockConfig;

    @Scheduled(cron = "0/10 * * * * *")
    public void scheduleCreateInboundPlanOrder() {

        if (!mockConfig.isOpenMockCreateInboundPlanOrder()) {
            return;
        }

        try {
            createInboundPlanOrder();
        } catch (Exception e) {
            log.error("create inbound plan order error", e);
        }
    }

    @Scheduled(cron = "0/5 * * * * *")
    public void scheduleCreateOutboundPlanOrder() {
        if (!mockConfig.isOpenMockCreateOutboundPlanOrder()) {
            return;
        }

        createOutboundPlanOrder();
    }

    private void createInboundPlanOrder() {
        List<Map<String, Object>> result = databaseQueryService.querySku();
        if (result.isEmpty()) {
            return;
        }

        for (Map<String, Object> map : result) {
            map.put("qtyRestocked", new Random().nextInt(1, 1000));
        }

        String warehouseCode = result.getFirst().get("warehouseCode").toString();

        Map<String, Object> requestBody = Map.of("customerOrderNo", UUID.randomUUID().toString(),
                "lpnCode", UUID.randomUUID().toString(),
                "warehouseCode", warehouseCode,
                "storageType", "STORAGE",
                "details", result);

        apiService.call("api/execute?apiType=ORDER_INBOUND_CREATE", requestBody);

    }

    private void createOutboundPlanOrder() {
        List<Map<String, Object>> result = databaseQueryService.querySkuBatchStock();
        if (result.isEmpty()) {
            return;
        }

        for (Map<String, Object> map : result) {
            int availableQty = (int) map.get("available_qty");
            map.put("qtyRequired", availableQty == 1 ? 1 : new Random().nextInt(1, 100));
        }

        String warehouseCode = result.getFirst().get("warehouseCode").toString();

        Map<String, Object> requestBody = Map.of("customerOrderNo", UUID.randomUUID().toString(),
                "warehouseCode", warehouseCode,
                "shortOutbound", true,
                "details", result);

        apiService.call("api/execute?apiType=ORDER_OUTBOUND_CREATE", requestBody);
    }

}
