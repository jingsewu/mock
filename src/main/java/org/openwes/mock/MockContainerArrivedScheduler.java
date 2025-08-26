package org.openwes.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class MockContainerArrivedScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpUtils httpUtils;

    @Value("${api.call.host}")
    private String host;

    @Scheduled(cron = "0/3 * * * * *")
    public void schedule() throws JsonProcessingException {

        String sql = "select container_code,container_face,destinations,task_code,container_task_type from e_container_task " +
                "where task_status = 'NEW' ";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
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
                sendArrived(containerCode, containerFace, destinations, taskCode);
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
        RequestBody body = RequestBody.create(JsonUtils.obj2String(requestBody), MediaType.get("application/json"));

        httpUtils.call("http://" + host + ":9010/api/execute?apiType=CONTAINER_TASK_STATUS_REPORT", requestBody);

    }

    private void sendArrived(String containerCode, String containerFace, Object destinations, String taskCode) throws JsonProcessingException {

        List<Long> stationIds = JsonUtils.string2List(JsonUtils.obj2String(destinations), Long.class);

        Long stationId = stationIds.get(0);

        String sql = "select work_locations from w_work_station " +
                "where id = ? ";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, stationId);

        if (result.isEmpty()) {
            return;
        }

        Object workLocations = result.get(0).get("work_locations");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(workLocations.toString());
        String locationCode = "";
        String workLocationCode = "";
        locationCode = getLocationCode(rootNode);
        workLocationCode = getWorkLocationCode(rootNode);

        Map<String, Object> containerDetails = Map.of("containerCode", containerCode,
                "face", containerFace,
                "robotCode", "robot_1",
                "locationCode", locationCode
        );

        ArrayList<Object> objects = new ArrayList<>();
        objects.add(containerDetails);

        Map<String, Object> requestBody = Map.of("workLocationCode", workLocationCode,
                "workStationId", stationIds.get(0),
                "containerDetails", objects);

        httpUtils.call("http://" + host + ":9010/api/execute?apiType=CONTAINER_ARRIVE", requestBody);
    }

    private static String getLocationCode(JsonNode rootNode) {
        return rootNode.get(0)
                .get("workLocationSlots")
                .get(0)
                .get("slotCode")
                .asText();
    }

    private static String getWorkLocationCode(JsonNode rootNode) {
        return rootNode.get(0)
                .get("workLocationCode")
                .asText();
    }

}
