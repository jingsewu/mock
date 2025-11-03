package org.openwes.mock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.mock.constants.PutWallSlotStatusEnum;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PutWallSlotDTO implements Serializable {

    private Long id;
    private Long workStationId;
    private Long putWallId;
    private String putWallCode;
    private String putWallSlotCode;

    //unique
    private String ptlTag;

    // it's define the put wall LEFT or RIGHT or MIDDLE
    private String face;

    private String level;
    private String bay;

    private Integer locLevel;
    private Integer locBay;

    private boolean enable;

    private Long pickingOrderId;
    private PutWallSlotStatusEnum putWallSlotStatus;
    private String transferContainerCode;
    private Long transferContainerRecordId;
    private Long version;

    // used when put wall area render
    private List<WorkStationConfigDTO.PageFieldConfig> putWallSlotDesc;

    // dispatched quantity when push to web
    private Integer qtyDispatched;

}
