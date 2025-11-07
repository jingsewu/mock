package org.openwes.mock.controller.parameter;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

@Data
public class MockBaseDataRequest {
    private int warehouseCount = 1;
    private int ownerCount = 1;
    private int areasPerWarehouse = 1;

    @Hidden
    private String createUser = "init";
}
