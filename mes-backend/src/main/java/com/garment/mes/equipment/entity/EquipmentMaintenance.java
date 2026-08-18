package com.garment.mes.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备维护工单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stp_equipment_maintenance")
public class EquipmentMaintenance extends BaseEntity {
    @TableId(value = "maintenance_id", type = IdType.ASSIGN_ID)
    private String maintenanceId;
    private String equipmentId;
    private String maintenanceNo;
    /** DAILY/PERIODIC/REPAIR/UPGRADE */
    private String maintenanceType;
    private String title;
    private String content;
    private LocalDate planDate;
    private LocalDate doneDate;
    /** PLANNED/DOING/DONE/CANCELLED */
    private String status;
    private String ownerId;
    private BigDecimal costAmount;
    private String remark;
}
