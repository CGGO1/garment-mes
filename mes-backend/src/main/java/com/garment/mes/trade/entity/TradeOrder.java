package com.garment.mes.trade.entity;

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
@TableName("trd_order")
public class TradeOrder extends BaseEntity {
    @TableId(value = "order_id", type = IdType.ASSIGN_ID)
    private String orderId;
    private String orderNo;
    /** EXPORT / IMPORT */
    private String orderType;
    private String customerId;
    private String incoterm;
    private String currency;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    /** DRAFT / PENDING / IN_PRODUCTION / QC / SHIPPED / COMPLETED */
    private String status;
    private BigDecimal totalAmount;
    private String remark;

    /** 客户名称（非表字段） */
    @TableField(exist = false)
    private String customerName;

    /** 明细（非表字段） */
    @TableField(exist = false)
    private List<TradeOrderItem> items;

    /** 明细行数（非表字段） */
    @TableField(exist = false)
    private Integer itemsCount;

    /** 单证数（非表字段） */
    @TableField(exist = false)
    private Integer docCount;

    /** 出货计划数（非表字段） */
    @TableField(exist = false)
    private Integer shipCount;
}
