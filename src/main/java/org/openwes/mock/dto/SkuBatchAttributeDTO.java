package org.openwes.mock.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SkuBatchAttributeDTO implements Serializable {

    private Long id;

    private Long skuId;

    private Map<String, Object> skuAttributes;

    private String batchNo;

    /**
     * for task query to mapping
     */
    private List<Long> skuBatchStockIds;

    public static final String INBOUND_DATE = "INBOUND_DATE";
    public static final String EXPIRED_DATE = "EXPIRED_DATE";
    public static final String PRODUCT_DATE = "PRODUCT_DATE";

    public static final String BATCH_ATTRIBUTE_PREFIX = "BATCH_ATTRIBUTE_";

}
