package org.openwes.mock.controller.parameter;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ContainerInitRequestDTO {
    @Hidden
    private String warehouseCode;
    @Hidden
    private String containerSpecCode;
    
    @NotNull
    private String containerType;

    @NotEmpty
    private String containerCodePrefix = "AAA";

    private int startIndex = 1;
    private int indexNumber = 8;
    private int createNumber = 500000;
}
