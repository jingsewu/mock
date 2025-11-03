package org.openwes.mock.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutWallLocationEnum implements IEnum {

    LEFT("LEFT", "左"),
    RIGHT("RIGHT", "右");

    private final String value;
    private final String label;

    private final String name = "播种墙位置";
}
