package org.openwes.mock.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.openwes.mock.constants.OperationTaskStatusEnum;
import org.openwes.mock.constants.OperationTaskTypeEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * abstract of operation task contains all tasks. eg: inbound, outbound, relocation, etc.
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class OperationTaskDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String warehouseCode;

    private OperationTaskTypeEnum taskType;

    private Long workStationId;

    private Long skuId;

    private Integer priority;

    private Long skuBatchStockId;
    private Long skuBatchAttributeId;
    private Long containerStockId;

    private String sourceContainerCode;
    private String sourceContainerFace;
    private String sourceContainerSlot;

    private String boxNo;

    private Integer requiredQty;
    private Integer operatedQty;
    private Integer abnormalQty = 0;
    private Integer toBeOperatedQty;

    private String targetLocationCode;
    private String targetContainerCode;
    private Long transferContainerRecordId;
    private String targetContainerSlot;

    private Long orderId;
    private String orderNo;
    private Long detailId;

    private org.openwes.mock.constants.OperationTaskStatusEnum taskStatus;

    private Map<Long, String> assignedStationSlot;

    private boolean abnormal;

    private boolean shortComplete;

    private String updateUser;

    public OperationTaskDTO() {
        this.taskStatus = OperationTaskStatusEnum.NEW;
    }


    public Integer getToBeOperatedQty() {
        return this.requiredQty - this.operatedQty - this.abnormalQty;
    }


}
