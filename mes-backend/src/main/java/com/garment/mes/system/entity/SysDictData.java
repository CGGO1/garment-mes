package com.garment.mes.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.garment.mes.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    @TableId(value = "dict_code", type = IdType.ASSIGN_ID)
    private String dictCode;

    private Integer dictSort;

    private String dictLabel;

    private String dictValue;

    private String dictType;

    private String status;
}
