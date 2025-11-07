package org.openwes.mock.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class MockConfig {

    private boolean openMockContainerArrived = false;
    private boolean openMockCreateInboundPlanOrder = false;
    private boolean openMockCreateOutboundPlanOrder = false;

    private boolean openMockInboundOrderAcceptance = false;
    private boolean openMockCompleteAcceptOrder = false;

    private boolean openMockPicking = false;

    public void setAllTrue() {
        this.openMockContainerArrived = true;
        this.openMockCreateInboundPlanOrder = true;
        this.openMockCreateOutboundPlanOrder = true;
        this.openMockInboundOrderAcceptance = true;
        this.openMockCompleteAcceptOrder = true;
        this.openMockPicking = true;
        log.info("All mock open");
    }
}
