package com.garment.mes.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统菜单（目录/菜单/按钮）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(value = "menu_id", type = IdType.ASSIGN_ID)
    private String menuId;

    private String parentId;

    private String menuName;

    /** DIR / MENU / BUTTON */
    private String menuType;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    private String perms;

    /** N / Y */
    private String visible;

    /** 子菜单（非表字段，树形组装用） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
