package com.garment.mes.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trd_shipment")
public class Shipment extends BaseEntity {
    @TableId(value = "shipment_id", type = IdType.ASSIGN_ID)
    private String shipmentId;
    private String orderId;
    private String portFrom;
    private String portTo;
    private LocalDateTime etd;
    private LocalDateTime eta;
    /** FCL / LCL */
    private String container;
    private String remark;
}
