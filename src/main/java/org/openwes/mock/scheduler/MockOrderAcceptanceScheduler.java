package org.openwes.mock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.openwes.mock.utils.JsonUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockOrderAcceptanceScheduler {

    private final MockConfig mockConfig;
    private final DatabaseQueryService databaseQueryService;
    private final ApiService apiService;

    @Scheduled(cron = "0/1 * * * * *")
    public void scheduleInboundOrderAcceptance() {

        if (!mockConfig.isOpenMockInboundOrderAcceptance()) {
            return;
        }

        try {
            processInboundOrderAcceptance();
        } catch (Exception e) {
            log.error("Error processing inbound order acceptance", e);
        }
    }

    @Scheduled(cron = "0/3 * * * * *")
    public void scheduleCompleteAcceptOrder() {

        if (!mockConfig.isOpenMockCompleteAcceptOrder()) {
            return;
        }

        try {
            completeAcceptOrder();
        } catch (Exception e) {
            log.error("Error processing inbound order acceptance", e);
        }
    }

    private void processInboundOrderAcceptance() {

        List<Map<String, Object>> inboundOrders = databaseQueryService.queryInboundOrders();

        if (inboundOrders.isEmpty()) {
            log.debug("No pending inbound orders found for acceptance");
            return;
        }

        for (Map<String, Object> order : inboundOrders) {
            Long orderId = (Long) order.get("id");
            String warehouseCode = (String) order.get("warehouse_code");

            List<Map<String, Object>> orderDetails = databaseQueryService.queryInboundOrderDetails(orderId);

            if (orderDetails.isEmpty()) {
                log.info("No pending details found for order {}", orderId);
                continue;
            }

            Map<String, Object> detail = orderDetails.getFirst();

            Long detailId = (Long) detail.get("id");
            String skuCode = (String) detail.get("sku_code");
            Integer qtyPlanned = (Integer) detail.get("qty_restocked");
            Integer qtyAccepted = detail.get("qty_accepted") != null ? (Integer) detail.get("qty_accepted") : 0;
            Integer qtyAbnormal = detail.get("qty_abnormal") != null ? (Integer) detail.get("qty_abnormal") : 0;

            Integer remainingDetailQty = qtyPlanned - qtyAccepted - qtyAbnormal;
            if (remainingDetailQty <= 0) {
                continue;
            }

            // Query SKU information to get skuId
            Long skuId;
            try {
                skuId = databaseQueryService.querySkuId(skuCode, warehouseCode);
            } catch (Exception e) {
                log.warn("SKU not found for code: {}, warehouse: {}", skuCode, warehouseCode);
                continue;
            }

            Map<String, Object> containerInfo = null;
            List<Map<String, Object>> containers = databaseQueryService.queryOutsideRandomContainer();
            if (!containers.isEmpty()) {
                containerInfo = containers.getFirst();
            }

            if (containerInfo == null) {
                log.warn("No outside containers available for warehouse: {}, skipping detail {}", warehouseCode, detailId);
                continue;
            }

            Long targetContainerId = (Long) containerInfo.get("id");
            String targetContainerCode = (String) containerInfo.get("container_code");
            String targetContainerSpecCode = (String) containerInfo.get("container_spec_code");

            // Extract slot information from JSON container_slots
            String targetContainerSlotCode = null;
            Object containerSlotsObj = containerInfo.get("container_slots");
            // Parse JSON array and find first available slot
            String containerSlotsJson = JsonUtils.obj2String(containerSlotsObj);
            List<Map> slots = JsonUtils.string2List(containerSlotsJson, Map.class);
            targetContainerSlotCode = (String) slots.getFirst().get("containerSlotCode");

            // Use warehouse area and logic IDs as workstation fallback
            Long workStationId = (Long) containerInfo.get("warehouse_area_id");
            if (workStationId == null || workStationId == 0L) {
                workStationId = (Long) containerInfo.get("warehouse_logic_id");
                if (workStationId == null || workStationId == 0L) {
                    workStationId = 1L; // Default fallback
                }
            }

            String targetContainerFace = "FRONT"; // Default face

            Map<String, Object> acceptanceDetail = new HashMap<>();
            acceptanceDetail.put("inboundPlanOrderId", orderId);
            acceptanceDetail.put("inboundPlanOrderDetailId", detailId);
            acceptanceDetail.put("warehouseCode", warehouseCode);
            acceptanceDetail.put("qtyAccepted", remainingDetailQty);
            acceptanceDetail.put("skuId", skuId);
            acceptanceDetail.put("targetContainerId", targetContainerId);
            acceptanceDetail.put("targetContainerCode", targetContainerCode);
            acceptanceDetail.put("targetContainerSpecCode", targetContainerSpecCode);
            acceptanceDetail.put("targetContainerSlotCode", targetContainerSlotCode);
            acceptanceDetail.put("targetContainerFace", targetContainerFace);
            acceptanceDetail.put("workStationId", workStationId);

            Map<String, Object> batchAttributes = new HashMap<>();
            batchAttributes.put("batchNo", "BATCH_" + System.currentTimeMillis());
            batchAttributes.put("operator", "SYSTEM_SCHEDULER");
            acceptanceDetail.put("batchAttributes", batchAttributes);

            apiService.call("inbound/plan/accept", acceptanceDetail);
        }
    }

    private void completeAcceptOrder() {
        List<Map<String, Object>> acceptOrders = databaseQueryService.queryAcceptOrders();
        if (acceptOrders.isEmpty()) {
            log.debug("No accept orders found");
            return;
        }

        String acceptOrderId = acceptOrders.getFirst().get("id").toString();
        apiService.call("inbound/accept/completeById?acceptOrderId=" + acceptOrderId, "");
    }

}
