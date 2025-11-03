package org.openwes.mock.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorkStationStatusEnum implements IEnum {

    ONLINE("ONLINE", "在线"),

    PAUSED("PAUSED", "暂停"),

    OFFLINE("OFFLINE", "离线");

    private final String value;
    private final String label;
}
