package org.openwes.mock.dto;

import lombok.*;
import org.openwes.mock.constants.WorkStationModeEnum;
import org.openwes.mock.constants.WorkStationProcessingStatusEnum;
import org.openwes.mock.constants.WorkStationStatusEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkStationVO {

    private Long workStationId;
    private String warehouseCode;
    private String warehouseAreaId;
    private String stationCode;
    private String stationName;
    private WorkStationStatusEnum workStationStatus;
    private WorkStationModeEnum workStationMode;
    private ChooseAreaEnum chooseArea;
    private String scanCode;

    private List<String> callContainers;

    private List<Tip> tips;

    private WorkLocationArea workLocationArea;
    private SkuArea skuArea;
    private PutWallArea putWallArea;
    private Toolbar toolbar;
    private OrderArea operationOrderArea;

    private boolean hasOrder;

    private WorkStationProcessingStatusEnum stationProcessingStatus;


    @Getter
    @AllArgsConstructor
    public enum ChooseAreaEnum {
        SKU_AREA("skuArea"),
        CONTAINER_AREA("containerArea"),
        PUT_WALL_AREA("putWallArea"),
        SCAN_AREA("scanArea"), // 主要用于非缓存货架的空箱出库
        ORDER_AREA("orderArea"),
        TIPS("tips");
        private final String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tip {
        private TipTypeEnum tipType;
        private String type;
        private Object data;
        private Long duration;
        private String tipCode;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Tip tip = (Tip) o;
            return Objects.equals(tipType, tip.tipType) && Objects.equals(tipCode, tip.tipCode);
        }

        @Override
        public int hashCode() {
            return tipType.hashCode() + tipCode.hashCode();
        }

        @Getter
        public enum TipTypeEnum {
            // 空箱处理提示
            EMPTY_CONTAINER_HANDLE_TIP,
            // 选择拣选任务提示
            CHOOSE_PICKING_TASK_TIP,
            // 封箱提示
            SEAL_CONTAINER_TIP,
            // 异常登记
            REPORT_ABNORMAL_TIP,
            // 扫描错误提示
            SCAN_ERROR_REMIND_TIP,
            // 整箱出库不拣选提示
            FULL_CONTAINER_AUTO_OUTBOUND_TIP,
            // 语音提醒
            PICKING_VOICE_TIP,
            /*入站异常提示*/
            INBOUND_ABNORMAL_TIP,
            /*一码多品提示*/
            BARCODE_2_MANY_SKU_CODE_TIP,
            /*skuCode对应多条订单或者多个货主提示*/
            SKU_ORDERS_OR_OWNER_CODES_TIP,

        }

        @Getter
        @AllArgsConstructor
        public enum TipShowTypeEnum {
            TIP("tip", "提示框"),
            CONFIRM("confirm", "弹框"),
            VOICE("voice", "语音播报"),
            ;

            private final String value;
            private final String name;
        }

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Toolbar {
        private boolean enableReportAbnormal;
        private boolean enableSplitContainer;
        private boolean enableReleaseSlot;
    }

    @Data
    @NoArgsConstructor
    public static class WorkLocationArea {
        private List<WorkLocationExtend> workLocationViews;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuArea {
        private String pickType;
        private List<SkuTaskInfo> pickingViews;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SkuTaskInfo {
            private SkuMainDataDTO skuMainDataDTO;
            private SkuBatchAttributeDTO skuBatchAttributeDTO;
            private List<OperationTaskDTO> operationTaskDTOS;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PutWallArea {
        private String putWallDisplayStyle;
        private List<PutWallDTO> putWallViews;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrderArea {
        private StocktakeOrderVO currentStocktakeOrder;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class StocktakeOrderVO {
        //common

        //stocktake
        private String taskNo;
    }


}
