package com.garment.mes.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("prd_work_order_item")
public class WorkOrderItem {
    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    private String itemId;
    private String workOrderId;
    private String processId;
    private String processName;
    private BigDecimal planQty;
    private BigDecimal finishQty;
    /** PENDING / DOING / DONE */
    private String status;
    private Integer seq;
}
