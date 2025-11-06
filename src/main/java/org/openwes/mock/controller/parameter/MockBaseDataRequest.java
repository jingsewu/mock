package org.openwes.mock.controller.parameter;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

@Data
public class MockBaseDataRequest {
    private int warehouseCount;
    private int ownerCount;
    private int areasPerWarehouse;

    @Hidden
    private String createUser = "init";
}
