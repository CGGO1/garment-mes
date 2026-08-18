package com.garment.mes.quality.entity;

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
@TableName("qc_inspection")
public class QcInspection extends BaseEntity {
    @TableId(value = "inspection_id", type = IdType.ASSIGN_ID)
    private String inspectionId;
    private String inspectionNo;
    private String orderId;
    private String workOrderId;
    /** IQC / IPQC / FQC / OQC / QA */
    private String inspectionType;
    private String standardId;
    private BigDecimal sampleQty;
    private BigDecimal passQty;
    private BigDecimal failQty;
    /** PENDING / PASS / FAIL */
    private String result;
    private LocalDateTime inspectDate;
    private String inspector;
    private String remark;

    @TableField(exist = false)
    private List<QcInspectionItem> items;

    /** 疵点记录（非表字段） */
    @TableField(exist = false)
    private List<QcDefect> defects;
}
