package com.garment.mes.master.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.mes.master.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
