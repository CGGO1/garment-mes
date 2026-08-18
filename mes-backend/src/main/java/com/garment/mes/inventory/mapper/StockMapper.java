package com.garment.mes.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.mes.inventory.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
