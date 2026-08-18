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
@TableName("mst_process")
public class Process extends BaseEntity {
    @TableId(value = "process_id", type = IdType.ASSIGN_ID)
    private String processId;
    private String processCode;
    private String processName;
    private Integer seq;
    private BigDecimal price;
    private String remark;
}
