package com.garment.mes.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_inbound")
public class Inbound extends BaseEntity {
    @TableId(value = "inbound_id", type = IdType.ASSIGN_ID)
    private String inboundId;
    private String inboundNo;
    private String warehouseId;
    private String inboundType;
    private String sourceNo;
    private LocalDateTime inboundDate;
    private String remark;

    @TableField(exist = false)
    private List<InboundItem> items;

    /** 仓库名称（非表字段） */
    @TableField(exist = false)
    private String warehouseName;

    /** 明细合计数量（非表字段） */
    @TableField(exist = false)
    private java.math.BigDecimal totalQty;
}
