package com.garment.mes.quality.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("qc_inspection_item")
public class QcInspectionItem {
    @TableId(value = "item_id", type = IdType.ASSIGN_ID)
    private String itemId;
    private String inspectionId;
    private String checkItem;
    /** PASS / FAIL */
    private String checkResult;
    private BigDecimal defectQty;
    private String remark;
}
