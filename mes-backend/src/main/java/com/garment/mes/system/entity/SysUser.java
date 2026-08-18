package com.garment.mes.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private String userId;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String roleId;

    /** NORMAL / DISABLED */
    private String status;
}
