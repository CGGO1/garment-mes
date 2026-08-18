package com.garment.mes.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inv_stock")
public class Stock {
    @TableId(value = "stock_id", type = IdType.ASSIGN_ID)
    private String stockId;
    private String warehouseId;
    private String materialId;
    private BigDecimal qty;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String materialName;
    @TableField(exist = false)
    private String warehouseName;
}
