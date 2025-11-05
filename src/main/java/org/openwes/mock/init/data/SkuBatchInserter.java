package org.openwes.mock.init.data;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SkuBatchInserter {

    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 5000; // Increased batch size for JdbcTemplate
    private static final int THREAD_POOL_SIZE = 8; // Adjust based on your system

    @Autowired
    public SkuBatchInserter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        configureJdbcTemplate();
    }

    private void configureJdbcTemplate() {
        // Optimize JdbcTemplate for bulk operations
        jdbcTemplate.setFetchSize(1000);
        jdbcTemplate.setMaxRows(0); // No limit
        jdbcTemplate.setQueryTimeout(0); // No timeout for long-running operations
    }

    /**
     * Main method to insert 100M records using parallel processing
     */
    @Transactional
    public void insert100MRecords() {
        long totalRecords = 100_000_000L;
        long recordsPerThread = totalRecords / THREAD_POOL_SIZE;

        System.out.println("Starting insertion of " + totalRecords + " records...");
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            long startId = i * recordsPerThread + 1;
            long endId = (i == THREAD_POOL_SIZE - 1) ? totalRecords : (i + 1) * recordsPerThread;

            CompletableFuture<Void> future = insertRecordsInRangeAsync(startId, endId, i);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        System.out.println("Insertion completed in " + duration + " seconds");
    }

    /**
     * Async method for parallel processing
     */
    @Async
    public CompletableFuture<Void> insertRecordsInRangeAsync(long startId, long endId, int threadId) {
        return CompletableFuture.runAsync(() -> {
            try {
                insertRecordsInRange(startId, endId, threadId);
            } catch (Exception e) {
                System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Insert records in a specific range using batch operations
     */
    private void insertRecordsInRange(long startId, long endId, int threadId) {
        System.out.println("Thread " + threadId + " processing records " + startId + " to " + endId);

        long batchStart = startId;

        while (batchStart <= endId) {
            long batchEnd = Math.min(batchStart + BATCH_SIZE - 1, endId);
            int batchSize = (int) (batchEnd - batchStart + 1);

            List<SkuDataGenerator.SkuRecord> batchRecords = generateBatchRecords(batchStart, batchSize);
            insertBatch(batchRecords, threadId);

            batchStart = batchEnd + 1;

            // Progress reporting
            if (batchStart % (BATCH_SIZE * 100) == 0 || batchStart > endId) {
                System.out.println("Thread " + threadId + " progress: " +
                    ((batchStart - startId) * 100 / (endId - startId + 1)) + "%");
            }
        }

        System.out.println("Thread " + threadId + " completed");
    }

    /**
     * Generate a batch of records
     */
    private List<SkuDataGenerator.SkuRecord> generateBatchRecords(long startId, int batchSize) {
        List<SkuDataGenerator.SkuRecord> records = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            SkuDataGenerator.SkuRecord record = SkuDataGenerator.generateSkuRecord();
            record.setId(startId + i); // Ensure unique IDs
            records.add(record);
        }

        return records;
    }

    /**
     * Insert batch using JdbcTemplate batchUpdate
     */
    private void insertBatch(List<SkuDataGenerator.SkuRecord> records, int threadId) {
        String sql = "INSERT INTO m_sku_main_data (" +
            "id, create_time, create_user, update_time, update_user, " +
            "barcode_rule_code, brand, calculate_heat, color, effective_days, " +
            "enable_effective, enable_sn, gross_weight, heat, height, " +
            "image_url, length, max_stock, min_stock, net_weight, " +
            "no_barcode, owner_code, shelf_life, size, sku_attribute_category, " +
            "sku_attribute_sub_category, sku_code, sku_first_category, sku_name, " +
            "sku_second_category, sku_third_category, style, unit, version, " +
            "volume, warehouse_code, width) VALUES (" +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?)";

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SkuDataGenerator.SkuRecord record = records.get(i);
                setPreparedStatementParameters(ps, record);
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });

        // Verify all records were inserted
        int totalInserted = Arrays.stream(results).sum();
        if (totalInserted != records.size()) {
            System.err.println("Thread " + threadId + ": Batch insertion mismatch. Expected: " +
                records.size() + ", Actual: " + totalInserted);
        }
    }

    /**
     * Set parameters for PreparedStatement
     */
    private void setPreparedStatementParameters(PreparedStatement ps, SkuDataGenerator.SkuRecord record)
            throws SQLException {
        int paramIndex = 1;
        ps.setLong(paramIndex++, record.getId());
        ps.setLong(paramIndex++, record.getCreateTime());
        ps.setString(paramIndex++, record.getCreateUser());
        ps.setLong(paramIndex++, record.getUpdateTime());
        ps.setString(paramIndex++, record.getUpdateUser());
        ps.setString(paramIndex++, record.getBarcodeRuleCode());
        ps.setString(paramIndex++, record.getBrand());
        ps.setBoolean(paramIndex++, record.isCalculateHeat());
        ps.setString(paramIndex++, record.getColor());
        ps.setInt(paramIndex++, record.getEffectiveDays());
        ps.setBoolean(paramIndex++, record.isEnableEffective());
        ps.setBoolean(paramIndex++, record.isEnableSn());
        ps.setLong(paramIndex++, record.getGrossWeight());
        ps.setString(paramIndex++, record.getHeat());
        ps.setLong(paramIndex++, record.getHeight());
        ps.setString(paramIndex++, record.getImageUrl());
        ps.setLong(paramIndex++, record.getLength());
        ps.setObject(paramIndex++, record.getMaxStock());
        ps.setObject(paramIndex++, record.getMinStock());
        ps.setLong(paramIndex++, record.getNetWeight());
        ps.setBoolean(paramIndex++, record.isNoBarcode());
        ps.setString(paramIndex++, record.getOwnerCode());
        ps.setInt(paramIndex++, record.getShelfLife());
        ps.setString(paramIndex++, record.getSize());
        ps.setString(paramIndex++, record.getSkuAttributeCategory());
        ps.setString(paramIndex++, record.getSkuAttributeSubCategory());
        ps.setString(paramIndex++, record.getSkuCode());
        ps.setString(paramIndex++, record.getSkuFirstCategory());
        ps.setString(paramIndex++, record.getSkuName());
        ps.setString(paramIndex++, record.getSkuSecondCategory());
        ps.setString(paramIndex++, record.getSkuThirdCategory());
        ps.setString(paramIndex++, record.getStyle());
        ps.setString(paramIndex++, record.getUnit());
        ps.setObject(paramIndex++, record.getVersion());
        ps.setLong(paramIndex++, record.getVolume());
        ps.setString(paramIndex++, record.getWarehouseCode());
        ps.setLong(paramIndex++, record.getWidth());
    }

    /**
     * Utility method to check insertion progress
     */
    public long getInsertedRecordCount() {
        String sql = "SELECT COUNT(*) FROM m_sku_main_data";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    /**
     * Clean up table (for testing)
     */
    @Transactional
    public void cleanupTable() {
        String sql = "TRUNCATE TABLE m_sku_main_data";
        jdbcTemplate.execute(sql);
        System.out.println("Table truncated");
    }
}
