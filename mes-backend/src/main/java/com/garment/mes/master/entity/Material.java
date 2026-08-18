package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mst_material")
public class Material extends BaseEntity {
    @TableId(value = "material_id", type = IdType.ASSIGN_ID)
    private String materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private String color;
    private String spec;
    private String unit;
    private String remark;
}
