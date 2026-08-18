package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mst_product")
public class Product extends BaseEntity {
    @TableId(value = "product_id", type = IdType.ASSIGN_ID)
    private String productId;
    private String productCode;
    private String productName;
    private String category;
    private String hsCode;
    private String composition;
    private BigDecimal gsm;
    private String width;
    private String unit;
    private String remark;
}
