package com.garment.mes.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备扫码登记：与 prd_scan_record（生产扫码）区分。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stp_equipment_scan")
public class EquipmentScan extends BaseEntity {
    @TableId(value = "scan_id", type = IdType.ASSIGN_ID)
    private String scanId;
    private String equipmentId;
    private String operatorId;
    /** CHECK_IN/CHECK_OUT/MAINTAIN/INSPECT/SCRAP */
    private String scanType;
    private LocalDateTime scanTime;
    private BigDecimal qty;
    private String remark;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String equipmentCode;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String equipmentName;
}
