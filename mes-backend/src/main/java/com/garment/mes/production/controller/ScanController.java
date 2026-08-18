package com.garment.mes.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.production.entity.ScanRecord;
import com.garment.mes.production.mapper.ScanRecordMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫码登记：通用接口，覆盖裁床/报工/收货/转移/OQC。
 */
@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanRecordMapper scanRecordMapper;

    public ScanController(ScanRecordMapper scanRecordMapper) {
        this.scanRecordMapper = scanRecordMapper;
    }

    /** 接收扫码上报（前端 ScanInput 组件统一入口） */
    @PostMapping("/receive")
    public R<ScanRecord> receive(@RequestBody ScanRecord record) {
        if (!StringUtils.hasText(record.getScanType())) {
            record.setScanType("RECEIVE");
        }
        if (record.getScanTime() == null) {
            record.setScanTime(LocalDateTime.now());
        }
        scanRecordMapper.insert(record);
        return R.ok(record);
    }

    /** 分页查询 */
    @GetMapping("/list")
    public R<Page<ScanRecord>> list(@RequestParam(defaultValue = "1") long pageNum,
                                    @RequestParam(defaultValue = "10") long pageSize,
                                    @RequestParam(required = false) String scanType,
                                    @RequestParam(required = false) String barcode,
                                    @RequestParam(required = false) String workOrderId) {
        LambdaQueryWrapper<ScanRecord> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(scanType))    qw.eq(ScanRecord::getScanType, scanType);
        if (StringUtils.hasText(barcode))     qw.like(ScanRecord::getBarcode, barcode);
        if (StringUtils.hasText(workOrderId)) qw.eq(ScanRecord::getWorkOrderId, workOrderId);
        qw.orderByDesc(ScanRecord::getScanTime);
        return R.ok(scanRecordMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 最近 10 条（工作台/仪表盘用） */
    @GetMapping("/recent")
    public R<List<ScanRecord>> recent() {
        return R.ok(scanRecordMapper.selectList(
                new LambdaQueryWrapper<ScanRecord>()
                        .orderByDesc(ScanRecord::getScanTime)
                        .last("LIMIT 10")));
    }

    /** 按条码查询 */
    @GetMapping("/by-barcode/{barcode}")
    public R<List<ScanRecord>> byBarcode(@PathVariable String barcode) {
        return R.ok(scanRecordMapper.selectList(
                new LambdaQueryWrapper<ScanRecord>()
                        .eq(ScanRecord::getBarcode, barcode)
                        .orderByDesc(ScanRecord::getScanTime)
                        .last("LIMIT 50")));
    }

    /** 删除（仅管理员） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        scanRecordMapper.deleteById(id);
        return R.ok();
    }
}
