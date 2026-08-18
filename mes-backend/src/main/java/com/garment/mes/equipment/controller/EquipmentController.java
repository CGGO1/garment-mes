package com.garment.mes.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.equipment.entity.Equipment;
import com.garment.mes.equipment.entity.EquipmentMaintenance;
import com.garment.mes.equipment.entity.EquipmentScan;
import com.garment.mes.equipment.mapper.EquipmentMaintenanceMapper;
import com.garment.mes.equipment.mapper.EquipmentMapper;
import com.garment.mes.equipment.mapper.EquipmentScanMapper;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.mapper.SysUserMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备管理（M4 完整模块）：台账 + 维护工单 + 设备扫码。
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentMapper equipmentMapper;
    private final EquipmentMaintenanceMapper maintenanceMapper;
    private final EquipmentScanMapper scanMapper;
    private final SysUserMapper userMapper;

    public EquipmentController(EquipmentMapper equipmentMapper,
                               EquipmentMaintenanceMapper maintenanceMapper,
                               EquipmentScanMapper scanMapper,
                               SysUserMapper userMapper) {
        this.equipmentMapper = equipmentMapper;
        this.maintenanceMapper = maintenanceMapper;
        this.scanMapper = scanMapper;
        this.userMapper = userMapper;
    }

    // ==================== 设备台账 ====================

    @GetMapping("/page")
    public R<Page<Equipment>> page(@RequestParam(defaultValue = "1") long pageNum,
                                   @RequestParam(defaultValue = "10") long pageSize,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String equipmentType,
                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Equipment> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(Equipment::getEquipmentCode, keyword)
                    .or().like(Equipment::getEquipmentName, keyword)
                    .or().like(Equipment::getModel, keyword));
        }
        if (StringUtils.hasText(equipmentType)) qw.eq(Equipment::getEquipmentType, equipmentType);
        if (StringUtils.hasText(status))         qw.eq(Equipment::getStatus, status);
        qw.orderByAsc(Equipment::getEquipmentCode);
        Page<Equipment> page = equipmentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillManagerName(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<Equipment> get(@PathVariable String id) {
        Equipment eq = equipmentMapper.selectById(id);
        if (eq != null) {
            fillManagerName(List.of(eq));
            eq.setMaintenances(maintenanceMapper.selectList(
                    new LambdaQueryWrapper<EquipmentMaintenance>()
                            .eq(EquipmentMaintenance::getEquipmentId, id)
                            .orderByDesc(EquipmentMaintenance::getPlanDate)));
            eq.setRecentScans(scanMapper.selectList(
                    new LambdaQueryWrapper<EquipmentScan>()
                            .eq(EquipmentScan::getEquipmentId, id)
                            .orderByDesc(EquipmentScan::getScanTime)
                            .last("LIMIT 20")));
        }
        return R.ok(eq);
    }

    /** 按编码查询（扫码识别设备身份用） */
    @GetMapping("/by-code/{code}")
    public R<Equipment> byCode(@PathVariable String code) {
        Equipment eq = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>().eq(Equipment::getEquipmentCode, code));
        if (eq != null) fillManagerName(List.of(eq));
        return R.ok(eq);
    }

    @PostMapping
    public R<Equipment> create(@RequestBody Equipment equipment) {
        if (!StringUtils.hasText(equipment.getEquipmentCode())) {
            equipment.setEquipmentCode("EQ" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(equipment.getStatus())) equipment.setStatus("NORMAL");
        if (!StringUtils.hasText(equipment.getStockStatus())) equipment.setStockStatus("IN_STOCK");
        equipmentMapper.insert(equipment);
        return R.ok(equipment);
    }

    @PutMapping
    public R<Void> update(@RequestBody Equipment equipment) {
        equipmentMapper.updateById(equipment);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        equipmentMapper.deleteById(id);
        return R.ok();
    }

    /** 状态流转：NORMAL→REPAIR→NORMAL / NORMAL→SCRAP */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Equipment eq = new Equipment();
        eq.setEquipmentId(id);
        eq.setStatus(body.get("status"));
        equipmentMapper.updateById(eq);
        return R.ok();
    }

    // ==================== 维护工单 ====================

    @GetMapping("/{id}/maintenance")
    public R<List<EquipmentMaintenance>> listMaintenance(@PathVariable String id) {
        return R.ok(maintenanceMapper.selectList(
                new LambdaQueryWrapper<EquipmentMaintenance>()
                        .eq(EquipmentMaintenance::getEquipmentId, id)
                        .orderByDesc(EquipmentMaintenance::getPlanDate)));
    }

    @PostMapping("/maintenance")
    public R<EquipmentMaintenance> createMaintenance(@RequestBody EquipmentMaintenance m) {
        if (!StringUtils.hasText(m.getMaintenanceNo())) {
            m.setMaintenanceNo("MT" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(m.getStatus())) m.setStatus("PLANNED");
        maintenanceMapper.insert(m);
        return R.ok(m);
    }

    @PutMapping("/maintenance")
    public R<Void> updateMaintenance(@RequestBody EquipmentMaintenance m) {
        maintenanceMapper.updateById(m);
        return R.ok();
    }

    @DeleteMapping("/maintenance/{id}")
    public R<Void> deleteMaintenance(@PathVariable String id) {
        maintenanceMapper.deleteById(id);
        return R.ok();
    }

    /** 流转：PLANNED→DOING→DONE / PLANNED→CANCELLED */
    @PutMapping("/maintenance/{id}/status")
    public R<Void> updateMaintenanceStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        EquipmentMaintenance m = new EquipmentMaintenance();
        m.setMaintenanceId(id);
        m.setStatus(body.get("status"));
        if ("DONE".equals(body.get("status"))) {
            m.setDoneDate(java.time.LocalDate.now());
        }
        maintenanceMapper.updateById(m);
        return R.ok();
    }

    // ==================== 设备扫码登记 ====================

    /** 扫码入库/出库/盘点等：写入扫码记录并联动设备库存状态 */
    @PostMapping("/scan")
    @Transactional
    public R<EquipmentScan> scan(@RequestBody EquipmentScan scan) {
        if (scan.getScanTime() == null) scan.setScanTime(LocalDateTime.now());
        scanMapper.insert(scan);
        // 更新设备最后扫码时间
        Equipment eq = new Equipment();
        eq.setEquipmentId(scan.getEquipmentId());
        eq.setLastScanTime(scan.getScanTime());
        // 入库 → 在库；出库 → 已出库；盘点等其他事件不改库存状态
        if ("CHECK_IN".equals(scan.getScanType())) {
            eq.setStockStatus("IN_STOCK");
        } else if ("CHECK_OUT".equals(scan.getScanType())) {
            eq.setStockStatus("OUT_STOCK");
        }
        equipmentMapper.updateById(eq);
        return R.ok(scan);
    }

    @GetMapping("/scan/{id}")
    public R<List<EquipmentScan>> listScan(@PathVariable String id) {
        return R.ok(scanMapper.selectList(
                new LambdaQueryWrapper<EquipmentScan>()
                        .eq(EquipmentScan::getEquipmentId, id)
                        .orderByDesc(EquipmentScan::getScanTime)));
    }

    /** 最近设备扫码记录（含设备编码/名称，供扫码登记页展示） */
    @GetMapping("/scan/recent")
    public R<List<EquipmentScan>> recentScan(@RequestParam(defaultValue = "20") int limit) {
        List<EquipmentScan> list = scanMapper.selectList(
                new LambdaQueryWrapper<EquipmentScan>()
                        .orderByDesc(EquipmentScan::getScanTime)
                        .last("LIMIT " + Math.min(Math.max(limit, 1), 100)));
        if (!list.isEmpty()) {
            List<String> ids = list.stream().map(EquipmentScan::getEquipmentId).distinct().toList();
            Map<String, Equipment> eqMap = equipmentMapper.selectBatchIds(ids).stream()
                    .collect(Collectors.toMap(Equipment::getEquipmentId, Function.identity()));
            list.forEach(s -> {
                Equipment e = eqMap.get(s.getEquipmentId());
                if (e != null) {
                    s.setEquipmentCode(e.getEquipmentCode());
                    s.setEquipmentName(e.getEquipmentName());
                }
            });
        }
        return R.ok(list);
    }

    // ==================== 仪表盘/工作台统计 ====================

    @GetMapping("/stats/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> res = new HashMap<>();
        long total = equipmentMapper.selectCount(null);
        long normal = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "NORMAL"));
        long repair = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "REPAIR"));
        long scrap  = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "SCRAP"));
        long inStock  = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStockStatus, "IN_STOCK"));
        long outStock = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStockStatus, "OUT_STOCK"));
        long pendingMt = maintenanceMapper.selectCount(new LambdaQueryWrapper<EquipmentMaintenance>().eq(EquipmentMaintenance::getStatus, "PLANNED"));
        res.put("total", total);
        res.put("normal", normal);
        res.put("repair", repair);
        res.put("scrap", scrap);
        res.put("inStock", inStock);
        res.put("outStock", outStock);
        res.put("pendingMaintenance", pendingMt);
        return R.ok(res);
    }

    // ==================== 内部工具 ====================

    private void fillManagerName(List<Equipment> list) {
        List<String> ids = list.stream().map(Equipment::getManagerId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) return;
        Map<String, SysUser> map = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        list.forEach(e -> {
            SysUser u = map.get(e.getManagerId());
            if (u != null) e.setManagerName(u.getNickname());
        });
    }
}
