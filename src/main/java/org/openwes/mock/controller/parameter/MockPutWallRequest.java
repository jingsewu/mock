package org.openwes.mock.controller.parameter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockPutWallRequest {
    @NotNull
    @Min(1)
    private int putWallCount;
    @NotNull
    @Min(1)
    private int minSlotsPerPutWall;
    @NotNull
    @Min(1)
    private int maxSlotsPerPutWall;
}
