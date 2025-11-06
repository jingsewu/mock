package org.openwes.mock.init.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BaseDataGenerator {

    private final Random random = new Random();

    public List<BaseDataInserter.WarehouseData> generateMockWarehouses(int count) {
        List<BaseDataInserter.WarehouseData> warehouses = new ArrayList<>();

        String[] businessTypes = {"TOB", "TOC", "RETURN", "CROSS_BORDER", "CONSUMABLES"};
        String[] structureTypes = {"STEEL", "FLOOR", "FLAT", "OUTSIDE"};
        String[] attrTypes = {"NORMAL", "COLD_CHAIN", "DANGEROUS", "BONDED"};
        String[] levels = {"A", "B", "C", "D", "E", "F"};
        String[] cities = {"Shanghai", "Beijing", "Shenzhen", "Guangzhou", "Hangzhou"};
        String[] provinces = {"Shanghai", "Beijing", "Guangdong", "Zhejiang", "Jiangsu"};

        for (int i = 0; i < count; i++) {
            BaseDataInserter.WarehouseData warehouse = new BaseDataInserter.WarehouseData();
            warehouse.setId(generateId());
            warehouse.setWarehouseCode("WH" + String.format("%03d", i + 1));
            warehouse.setWarehouseName("Warehouse " + (i + 1));
            warehouse.setWarehouseType("CENTER_WAREHOUSE");
            warehouse.setBusinessType(businessTypes[random.nextInt(businessTypes.length)]);
            warehouse.setStructureType(structureTypes[random.nextInt(structureTypes.length)]);
            warehouse.setWarehouseAttrType(attrTypes[random.nextInt(attrTypes.length)]);
            warehouse.setWarehouseLevel(levels[random.nextInt(levels.length)]);
            warehouse.setWarehouseLabel("LABEL-" + (i + 1));
            warehouse.setVirtualWarehouse(random.nextDouble() < 0.2); // 20% virtual
            warehouse.setArea(5000 + random.nextInt(20000)); // 5000-25000 m²
            warehouse.setHeight(8 + random.nextInt(12)); // 8-20m height
            warehouse.setCountry("China");
            warehouse.setProvince(provinces[i % provinces.length]);
            warehouse.setCity(cities[i % cities.length]);
            warehouse.setDistrict("District " + ((i % 5) + 1));
            warehouse.setAddress(warehouse.getDistrict() + ", " + warehouse.getCity() + ", " + warehouse.getProvince());
            warehouse.setName("Manager " + (i + 1));
            warehouse.setTel("+86-21-" + String.format("%08d", random.nextInt(100000000)));
            warehouse.setFax("+86-21-" + String.format("%08d", random.nextInt(100000000)));
            warehouse.setMail("warehouse" + (i + 1) + "@company.com");

            warehouses.add(warehouse);
        }

        return warehouses;
    }

    public List<BaseDataInserter.OwnerData> generateMockOwners(int count,
                                                               List<BaseDataInserter.WarehouseData> warehouses) {
        List<BaseDataInserter.OwnerData> owners = new ArrayList<>();

        String[] ownerTypes = {"SELF", "THIRD_PARTY"};
        String[] companySuffixes = {"Co., Ltd.", "Group", "International", "Trading", "Logistics"};

        for (int i = 0; i < count; i++) {
            BaseDataInserter.OwnerData owner = new BaseDataInserter.OwnerData();
            owner.setId(generateId());
            owner.setOwnerCode("OWNER" + String.format("%03d", i + 1));
            owner.setOwnerName("Company " + (i + 1) + " " + companySuffixes[random.nextInt(companySuffixes.length)]);
            owner.setOwnerType(ownerTypes[random.nextInt(ownerTypes.length)]);

            // Distribute owners across warehouses
            BaseDataInserter.WarehouseData warehouse = warehouses.get(i % warehouses.size());
            owner.setWarehouseCode(warehouse.getWarehouseCode());

            owner.setCountry("China");
            owner.setProvince(warehouse.getProvince());
            owner.setCity(warehouse.getCity());
            owner.setDistrict(warehouse.getDistrict());
            owner.setAddress("Address for " + owner.getOwnerName());
            owner.setName("Contact " + (i + 1));
            owner.setTel("+86-" + String.format("%011d", random.nextLong(100000000000L)));
            owner.setFax("+86-" + String.format("%011d", random.nextLong(100000000000L)));
            owner.setMail("contact" + (i + 1) + "@" + owner.getOwnerCode().toLowerCase() + ".com");

            owners.add(owner);
        }

        return owners;
    }

    public List<BaseDataInserter.WarehouseAreaData> generateMockWarehouseAreas(
            BaseDataInserter.WarehouseData warehouse, int areasCount) {

        List<BaseDataInserter.WarehouseAreaData> areas = new ArrayList<>();

        String[] areaTypes = {"STORAGE_AREA"};
        String[] areaUses = {"PICK"};
        String[] workTypes = {"ROBOT"};
        String[] groups = {"G1", "G2", "G3", "G4", "G5"};

        for (int i = 0; i < areasCount; i++) {
            BaseDataInserter.WarehouseAreaData area = new BaseDataInserter.WarehouseAreaData();
            area.setId(generateId());
            area.setWarehouseAreaCode(warehouse.getWarehouseCode() + "-AREA" + String.format("%02d", i + 1));
            area.setWarehouseAreaName(areaTypes[i % areaTypes.length] + " Area " + (i + 1));
            area.setWarehouseAreaType(areaTypes[i % areaTypes.length]);
            area.setWarehouseAreaUse(areaUses[random.nextInt(areaUses.length)]);
            area.setWarehouseAreaWorkType(workTypes[random.nextInt(workTypes.length)]);
            area.setWarehouseCode(warehouse.getWarehouseCode());
            area.setWarehouseGroupCode(groups[i % groups.length]);
            area.setLevel(1 + random.nextInt(3)); // 1-3 levels
            area.setTemperatureLimit(warehouse.getWarehouseAttrType().equals("COLD_CHAIN") ?
                    -20 + random.nextInt(25) : 15 + random.nextInt(20));
            area.setWetLimit(30 + random.nextInt(50)); // 30-80%
            area.setEnable(random.nextDouble() > 0.05); // 95% enabled
            area.setRemark("This is a " + area.getWarehouseAreaType() + " area for " + area.getWarehouseAreaUse() + " storage");

            areas.add(area);
        }

        return areas;
    }

    public Long generateId() {
        return System.currentTimeMillis() + random.nextInt(10000);
    }


    public List<BaseDataInserter.ContainerSpecData> generateMockContainerSpecs(
            BaseDataInserter.WarehouseData warehouse, int specsCount) {

        List<BaseDataInserter.ContainerSpecData> containerSpecs = new ArrayList<>();

        String[] containerTypes = {"CONTAINER", "PUT_WALL"};
        String[] locations = {"LEFT", "MIDDLE", "RIGHT"};
        String[] sizeTypes = {"SMALL", "MEDIUM", "LARGE", "EXTRA_LARGE"};

        // Standard dimensions for different container types
        Map<String, long[]> typeDimensions = new HashMap<>();
        typeDimensions.put("CONTAINER", new long[]{400, 300, 200}); // LxWxH in mm
        typeDimensions.put("PUT_WALL", new long[]{1200, 800, 150});

        for (int i = 0; i < specsCount; i++) {
            BaseDataInserter.ContainerSpecData spec = new BaseDataInserter.ContainerSpecData();
            spec.setId(generateId());

            String containerType = containerTypes[i % containerTypes.length];
            String sizeType = sizeTypes[random.nextInt(sizeTypes.length)];

            spec.setContainerSpecCode(warehouse.getWarehouseCode() + "-" + containerType + "-" + sizeType + "-" + (i + 1));
            spec.setContainerSpecName(containerType + " Container " + sizeType + " - " + (i + 1));
            spec.setContainerType(containerType);
            spec.setWarehouseCode(warehouse.getWarehouseCode());
            spec.setLocation(locations[random.nextInt(locations.length)]);

            // Set dimensions based on container type and size
            long[] baseDims = typeDimensions.get(containerType);
            double sizeMultiplier = getSizeMultiplier(sizeType);

            spec.setLength((long) (baseDims[0] * sizeMultiplier));
            spec.setWidth((long) (baseDims[1] * sizeMultiplier));
            spec.setHeight((long) (baseDims[2] * sizeMultiplier));
            spec.setVolume(spec.getLength() * spec.getWidth() * spec.getHeight());

            spec.setDescription(containerType + " container for " + sizeType.toLowerCase() + " items storage");

            // Generate container slot specifications
            List<BaseDataInserter.ContainerSpecData.ContainerSlotSpec> slotSpecs = generateContainerSlotSpecs(spec, containerType);
            spec.setContainerSlotSpecs(slotSpecs);
            spec.setContainerSlotNum(slotSpecs.size());

            containerSpecs.add(spec);
        }

        return containerSpecs;
    }

    private double getSizeMultiplier(String sizeType) {
        switch (sizeType) {
            case "SMALL":
                return 0.7;
            case "MEDIUM":
                return 1.0;
            case "LARGE":
                return 1.3;
            case "EXTRA_LARGE":
                return 1.6;
            default:
                return 1.0;
        }
    }

    private List<BaseDataInserter.ContainerSpecData.ContainerSlotSpec> generateContainerSlotSpecs(BaseDataInserter.ContainerSpecData spec,
                                                                                                  String containerType) {
        List<BaseDataInserter.ContainerSpecData.ContainerSlotSpec> slotSpecs = new ArrayList<>();

        int slotsPerLevel = 0;
        int levels = 0;

        // Define slot configuration based on container type
        switch (containerType) {
            case "CONTAINER":
                slotsPerLevel = 4;
                levels = 3;
                break;
            default:
                slotsPerLevel = 4;
                levels = 2;
        }

        // Calculate slot dimensions
        long slotWidth = spec.getWidth() / (slotsPerLevel / 2); // Assume 2 bays
        long slotLength = spec.getLength() / 2; // Assume 2 columns
        long slotHeight = spec.getHeight() / levels;
        long slotVolume = slotLength * slotWidth * slotHeight;

        String[] faces = {"FRONT", "BACK"};

        int slotIndex = 1;
        for (int level = 1; level <= levels; level++) {
            for (int bay = 1; bay <= (slotsPerLevel / 2); bay++) {
                for (String face : faces) {
                    if (slotIndex <= slotsPerLevel * levels) {
                        BaseDataInserter.ContainerSpecData.ContainerSlotSpec slotSpec = new BaseDataInserter.ContainerSpecData.ContainerSlotSpec();
                        slotSpec.setContainerSlotSpecCode(spec.getContainerSpecCode() + "-SLOT" + String.format("%02d", slotIndex));
                        slotSpec.setFace(face);
                        slotSpec.setLength(Math.toIntExact(slotLength));
                        slotSpec.setWidth(Math.toIntExact(slotWidth));
                        slotSpec.setHeight(Math.toIntExact(slotHeight));
                        slotSpec.setVolume(slotVolume);
                        slotSpec.setLevel(level);
                        slotSpec.setBay(bay);
                        slotSpec.setLocLevel(level);
                        slotSpec.setLocBay(bay);

                        slotSpecs.add(slotSpec);
                        slotIndex++;
                    }
                }
            }
        }

        return slotSpecs;
    }
}
