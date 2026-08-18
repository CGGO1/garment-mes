package com.garment.mes.quality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qc_standard")
public class QcStandard extends BaseEntity {
    @TableId(value = "standard_id", type = IdType.ASSIGN_ID)
    private String standardId;
    private String standardName;
    private String aqlLevel;
    private String description;
    private String remark;
}
