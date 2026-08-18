package com.garment.mes.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inv_stock_log")
public class StockLog {
    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    private String logId;
    private String warehouseId;
    private String materialId;
    /** IN / OUT */
    private String changeType;
    private BigDecimal changeQty;
    private BigDecimal balanceQty;
    private String bizNo;
    private LocalDateTime logTime;

    /** 物料名称（非表字段） */
    @TableField(exist = false)
    private String materialName;

    /** 仓库名称（非表字段） */
    @TableField(exist = false)
    private String warehouseName;
}
