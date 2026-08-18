package com.garment.mes.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trd_document")
public class TradeDocument extends BaseEntity {
    @TableId(value = "doc_id", type = IdType.ASSIGN_ID)
    private String docId;
    private String orderId;
    /** INVOICE / PACKING / BL / CO */
    private String docType;
    private String docNo;
    private String fileUrl;
    private String remark;
}
