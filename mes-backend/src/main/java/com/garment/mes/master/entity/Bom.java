package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mst_bom")
public class Bom extends BaseEntity {
    @TableId(value = "bom_id", type = IdType.ASSIGN_ID)
    private String bomId;
    private String productId;
    private String version;
    private String remark;

    /** 产品名称（非表字段） */
    @TableField(exist = false)
    private String productName;

    /** BOM 物料明细（非表字段） */
    @TableField(exist = false)
    private List<BomItem> items;
}
