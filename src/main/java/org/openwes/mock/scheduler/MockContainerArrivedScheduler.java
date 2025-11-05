package org.openwes.mock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.config.MockConfig;
import org.openwes.mock.constants.WorkStationStatusEnum;
import org.openwes.mock.dto.WorkLocationExtend;
import org.openwes.mock.dto.WorkStationDTO;
import org.openwes.mock.dto.WorkStationVO;
import org.openwes.mock.service.ApiService;
import org.openwes.mock.service.DatabaseQueryService;
import org.openwes.mock.service.StationService;
import org.openwes.mock.utils.JsonUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockContainerArrivedScheduler {

    private final DatabaseQueryService databaseQueryService;
    private final StationService stationService;
    private final ApiService apiService;
    private final MockConfig mockConfig;

    @Scheduled(cron = "0/3 * * * * *")
    public void schedule() {

        if (!mockConfig.isOpenMockContainerArrived()) {
            return;
        }

        List<Map<String, Object>> result = databaseQueryService.queryContainerTasks();
        if (result.isEmpty()) {
            return;
        }

        for (Map<String, Object> map : result) {
            String containerCode = (String) map.get("container_code");
            String containerFace = (String) map.get("container_face");
            Object destinations = Optional.ofNullable(map.get("destinations")).orElse("");
            String taskCode = (String) map.get("task_code");
            String containerTaskType = (String) map.get("container_task_type");

            if ("OUTBOUND".equals(containerTaskType)) {
                boolean sentResult = sendArrived(containerCode, containerFace, destinations, taskCode);
                if (!sentResult) {
                    log.info("send arrived failed");
                    return;
                }
            }

            sendTaskStatusUpdate(taskCode, containerCode);
        }

    }

    private void sendTaskStatusUpdate(String taskCode, String containerCode) {
        Map<String, Object> requestBody = Map.of("taskCode", taskCode,
                "containerCode", containerCode,
                "taskStatus", "WCS_SUCCEEDED",
                "robotCode", "robot_1",
                "locationCode", "locationCode_1");

        apiService.call("api/execute?apiType=CONTAINER_TASK_STATUS_REPORT", requestBody);
    }

    private boolean sendArrived(String containerCode, String containerFace, Object destinations, String taskCode) {

        List<Long> stationIds = JsonUtils.string2List(JsonUtils.obj2String(destinations), Long.class);

        Long stationId = stationIds.getFirst();

        WorkStationVO workStationVO = stationService.getWorkStationVO(stationId);
        if (workStationVO == null || workStationVO.getWorkStationStatus() == WorkStationStatusEnum.OFFLINE) {
            return false;
        }

        List<WorkLocationExtend> workLocationViews = workStationVO.getWorkLocationArea().getWorkLocationViews();
        if (CollectionUtils.isEmpty(workLocationViews)) {
            return false;
        }

        boolean hasContainer = isHasContainer(workLocationViews);
        if (hasContainer) {
            log.info("there are still exist container on the work station location. do not push");
            return false;
        }

        List<WorkLocationExtend> workLocations = workStationVO.getWorkLocationArea().getWorkLocationViews();
        String locationCode = "";
        String workLocationCode = "";
        locationCode = getLocationCode(workLocations);
        workLocationCode = getWorkLocationCode(workLocations);

        Map<String, Object> containerDetails = Map.of("containerCode", containerCode,
                "face", containerFace,
                "robotCode", "robot_1",
                "locationCode", locationCode
        );

        ArrayList<Object> objects = new ArrayList<>();
        objects.add(containerDetails);

        Map<String, Object> requestBody = Map.of("workLocationCode", workLocationCode,
                "workStationId", stationIds.getFirst(),
                "containerDetails", objects);

        apiService.call("api/execute?apiType=CONTAINER_ARRIVE", requestBody);
        return true;
    }

    private static boolean isHasContainer(List<WorkLocationExtend> workLocationViews) {
        boolean hasContainer = false;
        for (WorkLocationExtend workLocation : workLocationViews) {
            if (workLocation.getWorkLocationSlots() != null && !CollectionUtils.isEmpty(workLocation.getWorkLocationSlots())) {
                for (WorkLocationExtend.WorkLocationSlotExtend workLocationSlot : workLocation.getWorkLocationSlots()) {
                    if (workLocationSlot.getArrivedContainer() != null) {
                        hasContainer = true;
                        break;
                    }
                }
            }
        }
        return hasContainer;
    }

    private static String getLocationCode(List<WorkLocationExtend> workLocations) {

        if (workLocations.isEmpty()) {
            return "";
        }

        for (WorkLocationExtend workLocation : workLocations) {
            if (workLocation != null && workLocation.isEnable()
                    && workLocation.getWorkLocationSlots() != null && !CollectionUtils.isEmpty(workLocation.getWorkLocationSlots())) {
                WorkStationDTO.WorkLocationSlot workLocationSlot = workLocation.getWorkLocationSlots().getFirst();
                if (!StringUtils.isEmpty(workLocationSlot.getSlotCode())) {
                    return workLocationSlot.getSlotCode();
                }
            }
        }

        return "";
    }

    private static String getWorkLocationCode(List<WorkLocationExtend> workLocations) {
        if (workLocations.isEmpty()) {
            return "";
        }

        return workLocations.stream().filter(workLocation -> workLocation != null && workLocation.isEnable())
                .map(WorkStationDTO.WorkLocation::getWorkLocationCode)
                .filter(Objects::nonNull)
                .findFirst().orElse("");
    }

}
