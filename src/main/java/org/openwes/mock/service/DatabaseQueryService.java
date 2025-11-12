package org.openwes.mock.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DatabaseQueryService {

    private final JdbcTemplate jdbcTemplate;
    private volatile Long countSku = 0L;

    private List<Map<String, Object>> containers = null;

    public List<Map<String, Object>> querySku() {
        Long count = countSku();
        int skuNumber = new Random().nextInt(10, 100);
        int randomOffset = new Random().nextInt(Math.max(1, (int) (count - skuNumber)));

        String sql = "SELECT warehouse_code as warehouseCode, owner_code as ownerCode, sku_code as skuCode " +
                "FROM m_sku_main_data LIMIT " + skuNumber + " OFFSET " + randomOffset;

        return jdbcTemplate.queryForList(sql);
    }

    private Long countSku() {

        if (countSku > 0) {
            return countSku;
        }

        String sql = "SELECT COUNT(id) FROM m_sku_main_data";
        countSku = jdbcTemplate.queryForObject(sql, Long.class);
        return countSku;
    }

    public List<Map<String, Object>> queryContainerTasks() {
        String sql = "select container_code,container_face,destinations,task_code,container_task_type from e_container_task " +
                "where task_status = 'NEW' limit 30";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> querySkuBatchStock() {

        Long totalCount = querySkuBatchStockCount();
        if (totalCount == null || totalCount == 0) {
            return Collections.emptyList();
        }

        // 计算随机数量和偏移量
        int skuNumber = new Random().nextInt(10, 1000);
        int randomOffset = new Random().nextInt(Math.max(1, totalCount.intValue() - skuNumber));

        String sql = "select t2.sku_code as skuCode,t2.owner_code as ownerCode,t2.warehouse_code as warehouseCode," +
                "t1.available_qty from w_sku_batch_stock t1 inner join m_sku_main_data t2 on t1.sku_id = t2.id" +
                " where t1.available_qty>0 LIMIT " + skuNumber + " OFFSET " + randomOffset + ";";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryInboundOrders() {
        String sql = "SELECT t1.id, warehouse_code, customer_order_no " +
                "FROM w_inbound_plan_order t1 " +
                "WHERE inbound_plan_order_status in ('NEW','ACCEPTING') " +
                "LIMIT 30";

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryInboundOrderDetails(Long orderId) {
        String detailSql = "SELECT id, sku_code, qty_restocked, qty_accepted,qty_abnormal " +
                "FROM w_inbound_plan_order_detail " +
                "WHERE inbound_plan_order_id = ? AND qty_accepted < qty_restocked-qty_abnormal limit 1";
        return jdbcTemplate.queryForList(detailSql, orderId);
    }

    public synchronized List<Map<String, Object>> queryContainers() {

        if (ObjectUtils.isNotEmpty(containers)) {
            return containers;
        }

        String containerSql = "SELECT id, container_code, container_spec_code, container_slots, " +
                "empty_slot_num, warehouse_area_id, warehouse_logic_id " +
                "FROM w_container " +
                "WHERE container_status = 'OUT_SIDE' AND empty_slot_num > 0 ";
        containers = jdbcTemplate.queryForList(containerSql);
        return containers;
    }

    public List<Map<String, Object>> queryAcceptOrders() {
        String sql = "SELECT id " +
                "FROM w_accept_order " +
                "WHERE  accept_order_status in ('NEW') " +
                "LIMIT 100";
        return jdbcTemplate.queryForList(sql);

    }

    public Long querySkuId(String skuCode, String warehouseCode) {
        String skuSql = "SELECT id FROM m_sku_main_data WHERE sku_code = ? AND warehouse_code = ?";
        return jdbcTemplate.queryForObject(skuSql, Long.class, skuCode, warehouseCode);
    }

    public List<Map<String, Object>> queryWarehouses() {
        String sql = "SELECT warehouse_code as warehouseCode FROM m_warehouse_main_data LIMIT 1";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryFirstOwner() {
        String sql = "SELECT * FROM m_owner_main_data LIMIT 1";
        return jdbcTemplate.queryForList(sql);
    }

    public String queryFirstWarehouseCode() {
        String sql = "SELECT warehouse_code as warehouseCode FROM m_warehouse_main_data LIMIT 1";
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    public Long queryFirstWarehouseAreaId() {
        String sql = "SELECT id as id FROM w_warehouse_area LIMIT 1";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }


    public List<Map<String, Object>> queryContainerSpecs() {
        String sql = "SELECT container_spec_code as containerSpecCode FROM w_container_spec WHERE container_type = 'CONTAINER' LIMIT 1";
        return jdbcTemplate.queryForList(sql);
    }

    public Long querySkuBatchStockCount() {
        String sql = "SELECT COUNT(id) FROM w_sku_batch_stock t1 WHERE t1.available_qty > 0 and t1.warehouse_area_id >1";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public List<Long> queryAllWorkStationIds() {
        String sql = "SELECT id FROM w_work_station";
        return jdbcTemplate.queryForList(sql).stream().map(item -> (long) item.get("id"))
                .toList();
    }
}
