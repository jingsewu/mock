package org.openwes.mock.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class MockConfig {

    private boolean openMockContainerArrived = true;
    private boolean openMockCreateInboundPlanOrder = true;
    private boolean openMockCreateOutboundPlanOrder = true;

    private boolean openMockInboundOrderAcceptance = true;
    private boolean openMockCompleteAcceptOrder = true;

    private boolean openMockPicking = true;
}
