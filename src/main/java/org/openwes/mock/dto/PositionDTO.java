package org.openwes.mock.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PositionDTO implements Serializable {
    private Integer x;
    private Integer y;
    private Integer z;
}
