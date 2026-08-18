package com.garment.mes.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备台账
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stp_equipment")
public class Equipment extends BaseEntity {
    @TableId(value = "equipment_id", type = IdType.ASSIGN_ID)
    private String equipmentId;
    private String equipmentCode;
    private String equipmentName;
    /** 字典：裁剪机/缝纫机/绣花机/包装线/整烫台/检验台 */
    private String equipmentType;
    private String model;
    private String manufacturer;
    private LocalDate purchaseDate;
    private BigDecimal purchaseAmount;
    private String location;
    /** NORMAL/REPAIR/SCRAP/RENT */
    private String status;
    /** 库存状态：IN_STOCK 在库 / OUT_STOCK 已出库 */
    private String stockStatus;
    private String managerId;
    private LocalDateTime lastScanTime;
    private String remark;

    @TableField(exist = false)
    private String managerName;

    @TableField(exist = false)
    private String typeLabel;

    @TableField(exist = false)
    private List<EquipmentMaintenance> maintenances;

    @TableField(exist = false)
    private List<EquipmentScan> recentScans;
}
