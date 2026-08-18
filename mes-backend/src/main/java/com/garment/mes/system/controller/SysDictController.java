package com.garment.mes.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.system.entity.SysDictData;
import com.garment.mes.system.entity.SysDictType;
import com.garment.mes.system.mapper.SysDictDataMapper;
import com.garment.mes.system.mapper.SysDictTypeMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典管理
 */
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public SysDictController(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    // ===== 字典类型 =====
    @GetMapping("/type/page")
    public R<Page<SysDictType>> typePage(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(dictTypeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysDictType>().orderByDesc(SysDictType::getCreateTime)));
    }

    @PostMapping("/type")
    public R<Void> createType(@RequestBody SysDictType type) {
        dictTypeMapper.insert(type);
        return R.ok();
    }

    @PutMapping("/type")
    public R<Void> updateType(@RequestBody SysDictType type) {
        dictTypeMapper.updateById(type);
        return R.ok();
    }

    @DeleteMapping("/type/{id}")
    public R<Void> deleteType(@PathVariable String id) {
        dictTypeMapper.deleteById(id);
        return R.ok();
    }

    // ===== 字典数据 =====
    @GetMapping("/data/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.ok(dictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .orderByAsc(SysDictData::getDictSort)));
    }

    @PostMapping("/data")
    public R<Void> createData(@RequestBody SysDictData data) {
        dictDataMapper.insert(data);
        return R.ok();
    }

    @PutMapping("/data")
    public R<Void> updateData(@RequestBody SysDictData data) {
        dictDataMapper.updateById(data);
        return R.ok();
    }

    @DeleteMapping("/data/{id}")
    public R<Void> deleteData(@PathVariable String id) {
        dictDataMapper.deleteById(id);
        return R.ok();
    }
}
