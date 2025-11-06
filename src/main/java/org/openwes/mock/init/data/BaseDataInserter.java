package org.openwes.mock.init.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openwes.mock.controller.parameter.MockBaseDataRequest;
import org.openwes.mock.utils.JsonUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BaseDataInserter {

    private final JdbcTemplate jdbcTemplate;
    private final BaseDataGenerator baseDataGenerator;

    @Transactional
    public void initMockBaseData(MockBaseDataRequest request) {

        List<WarehouseData> warehouses = baseDataGenerator.generateMockWarehouses(request.getWarehouseCount());
        batchInsertWarehouses(warehouses, request.getCreateUser());

        List<OwnerData> owners = baseDataGenerator.generateMockOwners(request.getOwnerCount(), warehouses);
        batchInsertOwners(owners, request.getCreateUser());

        for (WarehouseData warehouse : warehouses) {
            List<WarehouseAreaData> areas = baseDataGenerator.
                    generateMockWarehouseAreas(warehouse, request.getAreasPerWarehouse());
            batchInsertWarehouseAreas(areas, request.getCreateUser());

            List<ContainerSpecData> containerSpecData = baseDataGenerator.generateMockContainerSpecs(warehouse, 1);
            batchInsertContainerSpecs(containerSpecData, request.getCreateUser());
        }

    }

    public int[] batchInsertContainerSpecs(List<ContainerSpecData> containerSpecs, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO w_container_spec (" +
                "id, create_time, create_user, update_time, update_user, " +
                "container_slot_num, container_slot_specs, container_spec_code, " +
                "container_spec_name, container_type, description, height, length, " +
                "location, version, volume, warehouse_code, width" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ContainerSpecData spec = containerSpecs.get(i);

                ps.setLong(1, spec.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setInt(6, spec.getContainerSlotNum());

                // Handle JSON field
                if (spec.getContainerSlotSpecs() != null) {
                    ps.setString(7, JsonUtils.obj2String(spec.getContainerSlotSpecs()));
                } else {
                    ps.setNull(7, Types.VARCHAR);
                }

                ps.setString(8, spec.getContainerSpecCode());
                ps.setString(9, spec.getContainerSpecName());
                ps.setString(10, spec.getContainerType());
                ps.setString(11, spec.getDescription());
                ps.setLong(12, spec.getHeight());
                ps.setLong(13, spec.getLength());
                ps.setString(14, spec.getLocation());
                ps.setLong(15, 1L); // version
                ps.setLong(16, spec.getVolume());
                ps.setString(17, spec.getWarehouseCode());
                ps.setLong(18, spec.getWidth());

            }

            @Override
            public int getBatchSize() {
                return containerSpecs.size();
            }
        });
    }


    public int[] batchInsertWarehouses(List<WarehouseData> warehouses, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO m_warehouse_main_data (" +
                "id, create_time, create_user, update_time, update_user, " +
                "address, area, business_type, city, country, district, " +
                "fax, height, mail, name, province, structure_type, tel, " +
                "version, virtual_warehouse, warehouse_attr_type, warehouse_code, " +
                "warehouse_label, warehouse_level, warehouse_name, warehouse_type" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                WarehouseData warehouse = warehouses.get(i);

                ps.setLong(1, warehouse.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setString(6, warehouse.getAddress());
                ps.setInt(7, warehouse.getArea());
                ps.setString(8, warehouse.getBusinessType());
                ps.setString(9, warehouse.getCity());
                ps.setString(10, warehouse.getCountry());
                ps.setString(11, warehouse.getDistrict());
                ps.setString(12, warehouse.getFax());
                ps.setInt(13, warehouse.getHeight());
                ps.setString(14, warehouse.getMail());
                ps.setString(15, warehouse.getName());
                ps.setString(16, warehouse.getProvince());
                ps.setString(17, warehouse.getStructureType());
                ps.setString(18, warehouse.getTel());
                ps.setLong(19, 1L); // version
                ps.setBoolean(20, warehouse.isVirtualWarehouse());
                ps.setString(21, warehouse.getWarehouseAttrType());
                ps.setString(22, warehouse.getWarehouseCode());
                ps.setString(23, warehouse.getWarehouseLabel());
                ps.setString(24, warehouse.getWarehouseLevel());
                ps.setString(25, warehouse.getWarehouseName());
                ps.setString(26, warehouse.getWarehouseType());
            }

            @Override
            public int getBatchSize() {
                return warehouses.size();
            }
        });
    }

    @Data
    public static class WarehouseData {
        private Long id;
        private String warehouseCode;
        private String warehouseName;
        private String warehouseType;
        private String businessType;
        private String structureType;
        private String warehouseAttrType;
        private String warehouseLevel;
        private String warehouseLabel;
        private boolean virtualWarehouse;
        private int area;
        private int height;
        private String country;
        private String province;
        private String city;
        private String district;
        private String address;
        private String name;
        private String tel;
        private String fax;
        private String mail;
    }

    public int[] batchInsertOwners(List<OwnerData> owners, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO m_owner_main_data (" +
                "id, create_time, create_user, update_time, update_user, " +
                "address, city, country, district, fax, mail, name, " +
                "owner_code, owner_name, owner_type, province, tel, version, warehouse_code" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OwnerData owner = owners.get(i);

                ps.setLong(1, owner.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setString(6, owner.getAddress());
                ps.setString(7, owner.getCity());
                ps.setString(8, owner.getCountry());
                ps.setString(9, owner.getDistrict());
                ps.setString(10, owner.getFax());
                ps.setString(11, owner.getMail());
                ps.setString(12, owner.getName());
                ps.setString(13, owner.getOwnerCode());
                ps.setString(14, owner.getOwnerName());
                ps.setString(15, owner.getOwnerType());
                ps.setString(16, owner.getProvince());
                ps.setString(17, owner.getTel());
                ps.setLong(18, 1L); // version
                ps.setString(19, owner.getWarehouseCode());
            }

            @Override
            public int getBatchSize() {
                return owners.size();
            }
        });
    }

    @Data
    public static class OwnerData {
        private Long id;
        private String ownerCode;
        private String ownerName;
        private String ownerType;
        private String warehouseCode;
        private String country;
        private String province;
        private String city;
        private String district;
        private String address;
        private String name;
        private String tel;
        private String fax;
        private String mail;
    }

    public int[] batchInsertWarehouseAreas(List<WarehouseAreaData> areas, String createUser) {
        long currentTime = System.currentTimeMillis();

        String sql = "INSERT INTO w_warehouse_area (" +
                "id, create_time, create_user, update_time, update_user, " +
                "delete_time, deleted, enable, level, remark, temperature_limit, " +
                "version, warehouse_area_code, warehouse_area_name, warehouse_area_type, " +
                "warehouse_area_use, warehouse_area_work_type, warehouse_code, " +
                "warehouse_group_code, wet_limit" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                WarehouseAreaData area = areas.get(i);

                ps.setLong(1, area.getId());
                ps.setLong(2, currentTime);
                ps.setString(3, createUser);
                ps.setLong(4, currentTime);
                ps.setString(5, createUser);
                ps.setLong(6, 0L); // delete_time
                ps.setBoolean(7, false); // deleted
                ps.setBoolean(8, area.isEnable());
                ps.setInt(9, area.getLevel());
                ps.setString(10, area.getRemark());
                ps.setInt(11, area.getTemperatureLimit());
                ps.setLong(12, 1L); // version
                ps.setString(13, area.getWarehouseAreaCode());
                ps.setString(14, area.getWarehouseAreaName());
                ps.setString(15, area.getWarehouseAreaType());
                ps.setString(16, area.getWarehouseAreaUse());
                ps.setString(17, area.getWarehouseAreaWorkType());
                ps.setString(18, area.getWarehouseCode());
                ps.setString(19, area.getWarehouseGroupCode());
                ps.setInt(20, area.getWetLimit());
            }

            @Override
            public int getBatchSize() {
                return areas.size();
            }
        });
    }

    @Data
    public static class WarehouseAreaData {
        private Long id;
        private String warehouseAreaCode;
        private String warehouseAreaName;
        private String warehouseAreaType;
        private String warehouseAreaUse;
        private String warehouseAreaWorkType;
        private String warehouseCode;
        private String warehouseGroupCode;
        private int level;
        private int temperatureLimit;
        private int wetLimit;
        private boolean enable;
        private String remark;
    }


    @Data
    public static class ContainerSpecData {
        private Long id;
        private String containerSpecCode;
        private String containerSpecName;
        private String containerType;
        private String description;
        private Long length;
        private Long width;
        private Long height;
        private Long volume;
        private String location;
        private String warehouseCode;
        private int containerSlotNum;
        private List<ContainerSlotSpec> containerSlotSpecs;

        @Data
        public static class ContainerSlotSpec {
            private String containerSlotSpecCode;
            private String face;
            private Integer length;
            private Integer width;
            private Integer height;
            private Long volume;
            private Integer level;
            private Integer bay;
            private Integer locLevel;
            private Integer locBay;
        }

    }
}
