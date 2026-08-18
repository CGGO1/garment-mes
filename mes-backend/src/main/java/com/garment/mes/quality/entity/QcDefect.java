package com.garment.mes.quality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_defect")
public class QcDefect extends BaseEntity {
    @TableId(value = "defect_id", type = IdType.ASSIGN_ID)
    private String defectId;
    private String inspectionId;
    private String defectType;
    private String defectDesc;
    private BigDecimal defectQty;
    /** MINOR / MAJOR / CRITICAL */
    private String severity;
}
