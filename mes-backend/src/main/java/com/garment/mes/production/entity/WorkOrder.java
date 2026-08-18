package com.garment.mes.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prd_work_order")
public class WorkOrder extends BaseEntity {
    @TableId(value = "work_order_id", type = IdType.ASSIGN_ID)
    private String workOrderId;
    private String workOrderNo;
    private String orderId;
    private String productId;
    private BigDecimal planQty;
    private BigDecimal finishQty;
    /** PLANNED / IN_PRODUCTION / COMPLETED */
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String remark;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private List<WorkOrderItem> items;

    /** 裁床记录（非表字段） */
    @TableField(exist = false)
    private List<Cutting> cuttings;

    /** 报工记录（非表字段） */
    @TableField(exist = false)
    private List<ProductionReport> reports;
}
