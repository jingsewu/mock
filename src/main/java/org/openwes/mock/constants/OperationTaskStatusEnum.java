package org.openwes.mock.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
public enum OperationTaskStatusEnum implements IEnum {

    NEW("NEW", "新任务"),
    PROCESSING("PROCESSING", "处理中"),
    PROCESSED("PROCESSED", "已完成"),
    CANCELED("CANCELED", "取消"),
    ;

    private final String value;
    private final String label;

    private final String name = "操作任务状态";

}
