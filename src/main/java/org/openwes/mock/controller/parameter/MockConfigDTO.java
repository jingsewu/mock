package org.openwes.mock.controller.parameter;

import lombok.Data;

@Data
public class MockConfigDTO {

    private boolean openMockContainerArrived = true;
    private boolean openMockCreateInboundPlanOrder = true;
    private boolean openMockCreateOutboundPlanOrder = true;

    private boolean openMockInboundOrderAcceptance = true;
    private boolean openMockCompleteAcceptOrder = true;

    private boolean openMockPicking = true;

}
