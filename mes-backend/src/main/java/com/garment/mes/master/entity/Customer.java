package com.garment.mes.master.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mst_customer")
public class Customer extends BaseEntity {
    @TableId(value = "customer_id", type = IdType.ASSIGN_ID)
    private String customerId;
    private String customerCode;
    private String customerName;
    private String contactPerson;
    private String phone;
    private String email;
    private String country;
    private String address;
    private String remark;
}
