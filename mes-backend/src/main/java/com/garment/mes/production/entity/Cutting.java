package com.garment.mes.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prd_cutting")
public class Cutting extends BaseEntity {
    @TableId(value = "cutting_id", type = IdType.ASSIGN_ID)
    private String cuttingId;
    private String workOrderId;
    private String batchNo;
    private String bundleNo;
    private BigDecimal fabricUsed;
    private BigDecimal cutQty;
    private String barcode;
    private String remark;
}
