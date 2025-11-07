package org.openwes.mock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.constants.ApiCodeEnum;
import org.openwes.mock.constants.PutWallSlotStatusEnum;
import org.openwes.mock.constants.WorkStationStatusEnum;
import org.openwes.mock.dto.WorkStationDTO;
import org.openwes.mock.dto.WorkStationVO;
import org.openwes.mock.service.StationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockOrderPickingScheduler {

    private final StationService stationService;
    private final MockConfig mockConfig;

    @Scheduled(cron = "0/1 * * * * *")
    public void schedulePicking() {

        if (!mockConfig.isOpenMockPicking()) {
            return;
        }

        List<WorkStationDTO> workStations = stationService.getAllWorkStation();

        if (ObjectUtils.isEmpty(workStations) || workStations.stream().noneMatch(v -> v.getWorkStationStatus() != WorkStationStatusEnum.OFFLINE)) {
            return;
        }

        workStations.forEach(workStationDTO -> {
            WorkStationVO workStation = stationService.getWorkStationVO(workStationDTO.getId());
            execute(workStation);
        });

    }

    private void execute(WorkStationVO workStation) {
        workStation.getPutWallArea().getPutWallViews()
                .forEach(putWallView -> {
                    putWallView.getPutWallSlots().forEach(putWallSlotDTO -> {

                        if (putWallSlotDTO.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_BINDING) {

                            //bind container
                            stationService.execute(workStation.getWorkStationId(), ApiCodeEnum.INPUT, putWallSlotDTO.getPutWallSlotCode());
                            stationService.execute(workStation.getWorkStationId(), ApiCodeEnum.INPUT, UUID.randomUUID());

                            sleep(200);
                        } else if (putWallSlotDTO.getPutWallSlotStatus() == PutWallSlotStatusEnum.DISPATCH) {
                            stationService.execute(workStation.getWorkStationId(), ApiCodeEnum.TAP_PUT_WALL_SLOT,
                                    Map.of("putWallSlotCode", putWallSlotDTO.getPutWallSlotCode()));
                            sleep(200);
                        } else if (putWallSlotDTO.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_SEAL) {
                            stationService.execute(workStation.getWorkStationId(), ApiCodeEnum.TAP_PUT_WALL_SLOT,
                                    Map.of("putWallSlotCode", putWallSlotDTO.getPutWallSlotCode()));
                            sleep(200);
                        } else if (putWallSlotDTO.getPutWallSlotStatus() == PutWallSlotStatusEnum.BOUND) {

                            String skuCode = getPickingSkuCode(workStation);
                            if (ObjectUtils.isEmpty(skuCode)) {
                                return;
                            }
                            stationService.execute(workStation.getWorkStationId(), ApiCodeEnum.SCAN_BARCODE, skuCode);
                            sleep(200);
                        }
                    });
                });
    }

    private void sleep(int i) {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            log.error("sleep interrupt", e);
            Thread.currentThread().interrupt();
        }
    }

    private String getPickingSkuCode(WorkStationVO workStation) {
        WorkStationVO.SkuArea skuArea = workStation.getSkuArea();
        if (skuArea == null) {
            return "";
        }

        List<WorkStationVO.SkuArea.SkuTaskInfo> pickingViews = skuArea.getPickingViews();
        if (ObjectUtils.isEmpty(pickingViews)) {
            return "";
        }

        return pickingViews.getFirst().getSkuMainDataDTO().getSkuCode();
    }

}
