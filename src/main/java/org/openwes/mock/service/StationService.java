package org.openwes.mock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.mock.constants.ApiCodeEnum;
import org.openwes.mock.constants.WorkStationModeEnum;
import org.openwes.mock.constants.WorkStationStatusEnum;
import org.openwes.mock.dto.PositionDTO;
import org.openwes.mock.dto.WorkStationDTO;
import org.openwes.mock.dto.WorkStationVO;
import org.openwes.mock.utils.HttpUtils;
import org.openwes.mock.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final JdbcTemplate jdbcTemplate;
    private final HttpUtils httpUtils;

    @Value("${api.call.host}")
    private String host;

    public WorkStationVO getWorkStationVO(Long workStationId) {
        String response = httpUtils.get("http://" + host + ":9040/api?stationCode=" + workStationId);

        if (response == null) {
            return null;
        }

        WorkStationVO workStationVO = JsonUtils.string2Object(response, WorkStationVO.class);
        if (workStationVO == null || workStationVO.getWorkStationStatus() == WorkStationStatusEnum.OFFLINE) {
            return null;
        }

        return workStationVO;
    }


    public List<WorkStationDTO> getAllWorkStation() {
        String sql = "select * from w_work_station";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        return result.stream().map(row -> {
            WorkStationDTO dto = WorkStationDTO.builder()
                    .id((Long) row.get("id"))
                    .stationCode((String) row.get("station_code"))
                    .stationName((String) row.get("station_name"))
                    .warehouseCode((String) row.get("warehouse_code"))
                    .warehouseAreaId((Long) row.get("warehouse_area_id"))
                    .enable((Boolean) row.get("enable"))
                    .version((Long) row.get("version"))
                    .build();

            // 处理枚举类型
            String statusStr = (String) row.get("work_station_status");
            if (statusStr != null) {
                dto.setWorkStationStatus(WorkStationStatusEnum.valueOf(statusStr));
            }

            String modeStr = (String) row.get("work_station_mode");
            if (modeStr != null) {
                dto.setWorkStationMode(WorkStationModeEnum.valueOf(modeStr));
            }

            // 处理JSON字段 - allow_work_station_modes
            String allowModesJson = (String) row.get("allow_work_station_modes");
            if (allowModesJson != null && !allowModesJson.isEmpty()) {
                try {
                    List<String> modeStrList = JsonUtils.string2Object(allowModesJson, List.class);
                    if (modeStrList != null) {
                        List<WorkStationModeEnum> modes = modeStrList.stream()
                                .map(WorkStationModeEnum::valueOf)
                                .collect(Collectors.toList());
                        dto.setAllowWorkStationModes(modes);
                    }
                } catch (Exception e) {
                    // 处理JSON解析异常
                    log.warn("Failed to parse allow_work_station_modes for station: {}",
                            dto.getStationCode(), e);
                }
            }

            // 处理JSON字段 - position
            String positionJson = (String) row.get("position");
            if (positionJson != null && !positionJson.isEmpty()) {
                try {
                    PositionDTO position = JsonUtils.string2Object(positionJson, PositionDTO.class);
                    dto.setPosition(position);
                } catch (Exception e) {
                    log.warn("Failed to parse position for station: {}", dto.getStationCode(), e);
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public void execute(Long workStationId, ApiCodeEnum apiCodeEnum, Object putWallSlotCode) {
        httpUtils.put("http://" + host + ":9040/api?stationCode=" + workStationId + "&apiCode=" + apiCodeEnum.name(), putWallSlotCode);
    }
}
