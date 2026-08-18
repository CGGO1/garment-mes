package com.garment.mes.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("trd_order_item")
public class TradeOrderItem {
    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    private String itemId;
    private String orderId;
    private String productId;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal amount;
    private String size;
    private String remark;

    /** 产品名称/款号（非表字段） */
    @TableField(exist = false)
    private String productName;
}
