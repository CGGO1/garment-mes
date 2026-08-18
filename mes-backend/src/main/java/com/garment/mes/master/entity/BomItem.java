package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mst_bom_item")
public class BomItem {
    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    private String itemId;
    private String bomId;
    private String materialId;
    private BigDecimal qty;
    private String unit;
    private String remark;

    /** 物料名称（非表字段） */
    @TableField(exist = false)
    private String materialName;
}
