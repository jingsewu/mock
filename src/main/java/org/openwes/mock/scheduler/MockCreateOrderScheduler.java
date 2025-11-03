package org.openwes.mock.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class MockCreateOrderScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpUtils httpUtils;

    @Value("${api.call.host}")
    private String host;

    @Scheduled(cron = "0 0/1 * * * *")
    public void schedule() {

        try {
            createInboundPlanOrder();
        } catch (Exception e) {
            log.error("create inbound plan order error", e);
        }

        createOutboundPlanOrder();
    }

    private void createInboundPlanOrder() {
        String sql = "select warehouse_code as warehouseCode,owner_code as ownerCode,sku_code as skuCode from m_sku_main_data limit 10";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        if (result.isEmpty()) {
            return;
        }

        for (Map<String, Object> map : result) {
            map.put("qtyRestocked", 1000);
        }

        String warehouseCode = result.get(0).get("warehouseCode").toString();

        Map<String, Object> requestBody = Map.of("customerOrderNo", UUID.randomUUID().toString(),
                "lpnCode", UUID.randomUUID().toString(),
                "warehouseCode", warehouseCode,
                "storageType", "STORAGE",
                "details", result);

        httpUtils.call("http://" + host + ":9010/api/execute?apiType=ORDER_INBOUND_CREATE", requestBody);

    }

    private void createOutboundPlanOrder() {
        String sql = "select min(t2.sku_code) as skuCode,min(t2.owner_code) as ownerCode,min(t2.warehouse_code) as warehouseCode," +
                "sum(t1.available_qty) from w_container_stock t1 inner join m_sku_main_data t2 on t1.sku_id = t2.id" +
                " where t1.available_qty>0  GROUP BY t2.id limit 10;";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        if (result.isEmpty()) {
            return;
        }

        for (Map<String, Object> map : result) {
            map.put("qtyRequired", 5);
        }

        String warehouseCode = result.get(0).get("warehouseCode").toString();

        Map<String, Object> requestBody = Map.of("customerOrderNo", UUID.randomUUID().toString(),
                "warehouseCode", warehouseCode,
                "details", result);

        httpUtils.call("http://" + host + ":9010/api/execute?apiType=ORDER_OUTBOUND_CREATE", requestBody);
    }


}
