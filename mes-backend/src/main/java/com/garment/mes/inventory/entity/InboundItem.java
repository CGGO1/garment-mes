package com.garment.mes.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("inv_inbound_item")
public class InboundItem {
    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    private String itemId;
    private String inboundId;
    private String materialId;
    private BigDecimal qty;
    private String remark;

    @TableField(exist = false)
    private String materialName;
}
