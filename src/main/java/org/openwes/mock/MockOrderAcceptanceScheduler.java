package org.openwes.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class MockOrderAcceptanceScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpUtils httpUtils;

    @Value("${api.call.host}")
    private String host;

    @Scheduled(cron = "0/10 * * * * *")
    public void scheduleInboundOrderAcceptance() {
        try {
            processInboundOrderAcceptance();
        } catch (Exception e) {
            log.error("Error processing inbound order acceptance", e);
        }
    }

    @Scheduled(cron = "0/10 * * * * *")
    public void scheduleCompleteAcceptOrder() {
        try {
            completeAcceptOrder();
        } catch (Exception e) {
            log.error("Error processing inbound order acceptance", e);
        }
    }

    private void processInboundOrderAcceptance() {
        log.info("Processing inbound order acceptance");

        // Query pending inbound plan orders that need acceptance
        String sql = "SELECT t1.id, warehouse_code, customer_order_no, total_qty, qty_accepted " +
                "FROM w_inbound_plan_order t1,w_inbound_plan_order_detail t2 " +
                "WHERE t1.id = t2.inbound_plan_order_id and inbound_plan_order_status in ('NEW','ACCEPTING') AND qty_accepted < qty_restocked-qty_abnormal " +
                "LIMIT 5";

        List<Map<String, Object>> inboundOrders = jdbcTemplate.queryForList(sql);

        if (inboundOrders.isEmpty()) {
            log.info("No pending inbound orders found for acceptance");
            return;
        }

        for (Map<String, Object> order : inboundOrders) {
            Long orderId = (Long) order.get("id");
            String warehouseCode = (String) order.get("warehouse_code");

            // Query order details
            String detailSql = "SELECT id, sku_code, qty_restocked, qty_accepted,qty_abnormal " +
                    "FROM w_inbound_plan_order_detail " +
                    "WHERE inbound_plan_order_id = ? AND qty_accepted < qty_restocked-qty_abnormal limit 1";

            List<Map<String, Object>> orderDetails = jdbcTemplate.queryForList(detailSql, orderId);

            if (orderDetails.isEmpty()) {
                log.info("No pending details found for order {}", orderId);
                continue;
            }

            Map<String, Object> detail = orderDetails.iterator().next();

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
            String skuSql = "SELECT id FROM m_sku_main_data WHERE sku_code = ? AND warehouse_code = ?";
            Long skuId = null;
            try {
                skuId = jdbcTemplate.queryForObject(skuSql, Long.class, skuCode, warehouseCode);
            } catch (Exception e) {
                log.warn("SKU not found for code: {}, warehouse: {}", skuCode, warehouseCode);
                continue;
            }

            // Query available outside containers from database
            String containerSql = "SELECT id, container_code, container_spec_code, container_slots, " +
                    "empty_slot_num, warehouse_area_id, warehouse_logic_id " +
                    "FROM w_container " +
                    "WHERE container_status = 'OUT_SIDE' AND empty_slot_num > 0 " +
                    "LIMIT 1";

            Map<String, Object> containerInfo = null;
            List<Map<String, Object>> containers = jdbcTemplate.queryForList(containerSql);
            if (!containers.isEmpty()) {
                containerInfo = containers.get(0);
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
            targetContainerSlotCode = (String) slots.get(0).get("containerSlotCode");

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

            try {
                httpUtils.call("http://" + host + ":9010/inbound/plan/accept", acceptanceDetail);
                log.info("Successfully called accept API for order {}", orderId);
            } catch (Exception e) {
                log.error("Failed to call accept API for order {}", orderId, e);
            }
        }
    }

    private void completeAcceptOrder() {
        log.info("Processing complete accept order");
        String sql = "SELECT id " +
                "FROM w_accept_order " +
                "WHERE  accept_order_status in ('NEW') " +
                "LIMIT 1";

        List<Map<String, Object>> acceptOrders = jdbcTemplate.queryForList(sql);

        if (acceptOrders.isEmpty()) {
            log.info("No accept orders found");
            return;
        }

        httpUtils.call("http://" + host + ":9010/inbound/accept/completeById?acceptOrderId=" + acceptOrders.iterator().next().get("id"),"");

    }

}
