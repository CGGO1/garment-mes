package com.garment.mes.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prd_report")
public class ProductionReport extends BaseEntity {
    @TableId(value = "report_id", type = IdType.ASSIGN_ID)
    private String reportId;
    private String workOrderId;
    private String processId;
    private String workerName;
    private BigDecimal reportQty;
    private LocalDateTime reportTime;
    private String barcode;
    private String remark;
}
