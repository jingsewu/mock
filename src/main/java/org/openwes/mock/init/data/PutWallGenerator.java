package org.openwes.mock.init.data;

import lombok.RequiredArgsConstructor;
import org.openwes.mock.utils.SnowflakeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PutWallGenerator {

    private final Random random = new Random();

    public List<PutWallBatchInserter.PutWallData> generateMockPutWalls(int count, String warehouseCode, List<Long> workStationIds) {
        List<PutWallBatchInserter.PutWallData> putWalls = new ArrayList<>();
        String[] containerSpecs = {"test"};
        String[] locations = {"A"};
        String[] statuses = {"IDLE"};

        for (int i = 0; i < count; i++) {
            PutWallBatchInserter.PutWallData putWall = new PutWallBatchInserter.PutWallData();
            putWall.setId(SnowflakeUtils.generateId());
            putWall.setPutWallCode("PW" + String.format("%04d", i + 1));
            putWall.setPutWallName("Put Wall " + (i + 1));
            putWall.setLocation(locations[i % locations.length]);
            putWall.setPutWallStatus(statuses[random.nextInt(statuses.length)]);
            putWall.setContainerSpecCode(containerSpecs[random.nextInt(containerSpecs.length)]);
            putWall.setWarehouseCode(warehouseCode);
            putWall.setWorkStationId(workStationIds.get(i));
            putWall.setEnable(true);

            putWalls.add(putWall);
        }

        return putWalls;
    }

    public List<PutWallBatchInserter.PutWallSlotData> generateMockPutWallSlots(
            PutWallBatchInserter.PutWallData putWall, int slotsCount) {

        List<PutWallBatchInserter.PutWallSlotData> slots = new ArrayList<>();
        String[] statuses = {"IDLE"};
        String[] faces = {"FRONT", "BACK"};

        // Determine grid layout (e.g., 4 bays x 6 levels = 24 slots)
        int bays = Math.min(6, slotsCount / 4 + 1);
        int levels = (int) Math.ceil((double) slotsCount / bays);

        int slotIndex = 0;
        for (int bay = 1; bay <= bays && slotIndex < slotsCount; bay++) {
            for (int level = 1; level <= levels && slotIndex < slotsCount; level++) {
                PutWallBatchInserter.PutWallSlotData slot = new PutWallBatchInserter.PutWallSlotData();
                slot.setId(SnowflakeUtils.generateId());
                slot.setPutWallSlotCode(putWall.getPutWallCode() + "-" + putWall.getLocation() + "-" + bay + "-" + "$" + "-" + level);
                slot.setPutWallSlotStatus(statuses[random.nextInt(statuses.length)]);
                slot.setBay("B" + bay);
                slot.setLevel("L" + level);
                slot.setFace(faces[random.nextInt(faces.length)]);
                slot.setLocBay(bay);
                slot.setLocLevel(level);
                slot.setPtlTag("PTL-" + putWall.getPutWallCode() + putWall.getLocation() + "-" + bay + "-" + level);
                slot.setPutWallCode(putWall.getPutWallCode());
                slot.setPutWallId(putWall.getId());
                slot.setWorkStationId(putWall.getWorkStationId());
                slot.setTaskDispatchRuleId(2000L + (slotIndex % 5)); // Mock task dispatch rule IDs
                slot.setEnable(random.nextDouble() > 0.05); // 95% enabled

                slots.add(slot);
                slotIndex++;
            }
        }

        return slots;
    }

    public Long generateId() {
        return System.currentTimeMillis() + random.nextInt(10000);
    }
}
