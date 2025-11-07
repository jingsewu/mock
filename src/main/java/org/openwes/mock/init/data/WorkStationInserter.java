package org.openwes.mock.init.data;

import org.openwes.mock.utils.JsonUtils;
import org.openwes.mock.utils.SnowflakeUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Component
public class WorkStationInserter {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    public WorkStationInserter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsertMockWorkStations(int count, String createUser, String warehouseCode, Long warehouseAreaId) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO w_work_station (" +
                "id, create_time, create_user, update_time, update_user, " +
                "allow_work_station_modes, delete_time, deleted, enable, " +
                "position, station_code, station_name, version, " +
                "warehouse_area_id, warehouse_code, work_locations, " +
                "work_station_mode, work_station_status" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // Generate mock data
                String stationCode = "WS" + String.format("%04d", i + 1);
                String stationName = "Work Station " + (i + 1);

                ps.setLong(1, SnowflakeUtils.generateId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);

                // Mock allow_work_station_modes JSON
                List<String> modes = Arrays.asList("PICKING");
                Collections.shuffle(modes);
                List<String> selectedModes = modes.subList(0, random.nextInt(modes.size()) + 1);
                ps.setString(6, JsonUtils.obj2String(selectedModes));

                ps.setLong(7, 0L); // delete_time
                ps.setBoolean(8, false); // deleted
                ps.setBoolean(9, true); // enable

                // Mock position JSON
                Map<String, Object> position = new HashMap<>();
                position.put("x", random.nextInt(100));
                position.put("y", random.nextInt(100));
                position.put("z", 0);
                ps.setString(10, JsonUtils.obj2String(position));

                ps.setString(11, stationCode);
                ps.setString(12, stationName);
                ps.setLong(13, 1L); // version
                ps.setLong(14, warehouseAreaId); // warehouse_area_id
                ps.setString(15, warehouseCode); // warehouse_code

                // Mock work_locations JSON
                List<String> workLocations = new ArrayList<>();
                int locationCount = random.nextInt(3) + 1;
                for (int j = 0; j < locationCount; j++) {
                    workLocations.add("LOC" + (random.nextInt(20) + 1));
                }
                Map<String, Object> locationsMap = new HashMap<>();
                locationsMap.put("locations", workLocations);
                ps.setString(16, "[{\"enable\": true, \"stationCode\": \"string\", \"workLocationCode\": \"string\", \"workLocationType\": \"BUFFER_SHELVING\", \"workLocationSlots\": [{\"bay\": 0, \"level\": 0, \"enable\": true, \"slotCode\": \"string\", \"groupCode\": \"string\", \"workLocationCode\": \"string\"}]}]");

                // Mock work_station_mode
                ps.setString(17, "PICKING");

                // Mock work_station_status
                ps.setString(18, "ONLINE");

            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }

}
