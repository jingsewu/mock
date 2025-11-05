package org.openwes.mock.controller.parameter;

import lombok.Data;

@Data
public class ContainerInitRequestDTO {
    private String warehouseCode = "test";
    private String containerSpecCode = "xx";
    private String containerCodePrefix = "AAA";
    private int startIndex = 1;
    private int indexNumber = 8;
    private int createNumber = 500000;
}
