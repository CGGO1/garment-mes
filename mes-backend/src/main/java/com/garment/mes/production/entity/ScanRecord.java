package com.garment.mes.production.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扫码记录：裁床/报工/收货/转移/OQC 等生产事件。
 * 与 stp_equipment_scan（设备生命周期事件）区分。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prd_scan_record")
public class ScanRecord extends BaseEntity {
    @TableId(value = "scan_id", type = IdType.ASSIGN_ID)
    private String scanId;
    private String barcode;
    /** CUTTING/REPORT/RECEIVE/TRANSFER/OQC */
    private String scanType;
    private String workOrderId;
    private String processId;
    private String equipmentId;
    private String operatorId;
    private LocalDateTime scanTime;
    private BigDecimal scanQty;
    private String remark;
}
