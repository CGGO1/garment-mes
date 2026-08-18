package com.garment.mes.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_warehouse")
public class Warehouse extends BaseEntity {
    @TableId(value = "warehouse_id", type = IdType.ASSIGN_ID)
    private String warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String location;
    private String remark;
}
