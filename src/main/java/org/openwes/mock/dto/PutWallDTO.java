package org.openwes.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.mock.constants.PutWallDisplayOrderEnum;
import org.openwes.mock.constants.PutWallStatusEnum;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PutWallDTO implements Serializable {

    private Long id;

    private String warehouseCode;

    private Long workStationId;
    private String putWallCode;
    private String putWallName;
    private String containerSpecCode;
    private List<PutWallSlotDTO> putWallSlots;

    private String location;

    private boolean enable;

    private PutWallDisplayOrderEnum displayOrder;

    private Long version;

    private PutWallStatusEnum putWallStatus;

    private boolean active;

}
