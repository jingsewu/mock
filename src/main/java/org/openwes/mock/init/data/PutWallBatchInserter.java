package org.openwes.mock.init.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openwes.mock.controller.parameter.MockPutWallRequest;
import org.openwes.mock.service.DatabaseQueryService;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PutWallBatchInserter {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();
    private final PutWallGenerator putWallGenerator;
    private final DatabaseQueryService databaseQueryService;

    @Transactional
    public void initMockPutWalls(MockPutWallRequest request) {
        String warehouseCode = databaseQueryService.queryFirstWarehouseCode();
        List<Long> stationIds = databaseQueryService.queryAllWorkStationIds();
        if (stationIds.isEmpty()) {
            throw new RuntimeException("No work station found");
        }

        List<PutWallData> putWalls = putWallGenerator.generateMockPutWalls(request.getPutWallCount(), warehouseCode, stationIds);

        batchInsertPutWalls(putWalls, "test");

        for (PutWallData putWall : putWalls) {
            int slotsCount = request.getMinSlotsPerPutWall() +
                    random.nextInt(request.getMaxSlotsPerPutWall() - request.getMinSlotsPerPutWall() + 1);

            List<PutWallSlotData> slots = putWallGenerator.generateMockPutWallSlots(putWall, slotsCount);
            batchInsertPutWallSlots(slots, "test");
        }
    }

    public int[] batchInsertPutWalls(List<PutWallData> putWalls, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO w_put_wall (" +
                "id, create_time, create_user, update_time, update_user, " +
                "container_spec_code, delete_time, deleted, enable, " +
                "location, put_wall_code, put_wall_name, put_wall_status, " +
                "version, work_station_id, warehouse_code" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PutWallData putWall = putWalls.get(i);

                ps.setLong(1, putWall.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setString(6, putWall.getContainerSpecCode());
                ps.setLong(7, 0L); // delete_time
                ps.setBoolean(8, false); // deleted
                ps.setBoolean(9, putWall.isEnable());
                ps.setString(10, putWall.getLocation());
                ps.setString(11, putWall.getPutWallCode());
                ps.setString(12, putWall.getPutWallName());
                ps.setString(13, putWall.getPutWallStatus());
                ps.setLong(14, 1L); // version
                ps.setLong(15, putWall.getWorkStationId());
                ps.setString(16, putWall.getWarehouseCode());
            }

            @Override
            public int getBatchSize() {
                return putWalls.size();
            }
        });
    }

    public int[] batchInsertPutWallSlots(List<PutWallSlotData> slots, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO w_put_wall_slot (" +
                "id, create_time, create_user, update_time, update_user, " +
                "bay, enable, face, level, loc_bay, loc_level, " +
                "picking_order_id, ptl_tag, put_wall_code, put_wall_id, " +
                "put_wall_slot_code, put_wall_slot_status, transfer_container_code, " +
                "transfer_container_record_id, version, work_station_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PutWallSlotData slot = slots.get(i);

                ps.setLong(1, slot.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setString(6, slot.getBay());
                ps.setBoolean(7, slot.isEnable());
                ps.setString(8, slot.getFace());
                ps.setString(9, slot.getLevel());
                ps.setInt(10, slot.getLocBay());
                ps.setInt(11, slot.getLocLevel());
                ps.setLong(12, 0L); // picking_order_id - default 0
                ps.setString(13, slot.getPtlTag());
                ps.setString(14, slot.getPutWallCode());
                ps.setLong(15, slot.getPutWallId());
                ps.setString(16, slot.getPutWallSlotCode());
                ps.setString(17, slot.getPutWallSlotStatus());
                ps.setString(18, null); // transfer_container_code - default null
                ps.setLong(19, 0L); // transfer_container_record_id - default 0
                ps.setLong(20, 1L); // version
                ps.setLong(21, slot.getWorkStationId());
            }

            @Override
            public int getBatchSize() {
                return slots.size();
            }
        });
    }

    @Data
    public static class PutWallSlotData {
        private Long id;
        private String putWallSlotCode;
        private String putWallSlotStatus;
        private String bay;
        private String level;
        private String face;
        private Integer locBay;
        private Integer locLevel;
        private String ptlTag;
        private String putWallCode;
        private Long putWallId;
        private Long workStationId;
        private Long taskDispatchRuleId;
        private boolean enable;
    }


    @Data
    public static class PutWallData {
        private Long id;
        private String putWallCode;
        private String putWallName;
        private String location;
        private String putWallStatus;
        private String containerSpecCode;
        private String warehouseCode;
        private Long workStationId;
        private boolean enable;

        // Constructors, Getters and Setters
        public PutWallData() {
        }
    }

}
