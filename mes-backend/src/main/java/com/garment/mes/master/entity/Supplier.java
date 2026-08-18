package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mst_supplier")
public class Supplier extends BaseEntity {
    @TableId(value = "supplier_id", type = IdType.ASSIGN_ID)
    private String supplierId;
    private String supplierCode;
    private String supplierName;
    private String supplierType;
    private String contactPerson;
    private String phone;
    private String email;
    private String country;
    private String address;
    private String remark;
}
