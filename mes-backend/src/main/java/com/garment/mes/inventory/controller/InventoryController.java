package com.garment.mes.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.BusinessException;
import com.garment.mes.common.R;
import com.garment.mes.inventory.entity.Inbound;
import com.garment.mes.inventory.entity.InboundItem;
import com.garment.mes.inventory.entity.Outbound;
import com.garment.mes.inventory.entity.OutboundItem;
import com.garment.mes.inventory.entity.Stock;
import com.garment.mes.inventory.entity.StockLog;
import com.garment.mes.inventory.entity.Warehouse;
import com.garment.mes.inventory.mapper.InboundItemMapper;
import com.garment.mes.inventory.mapper.InboundMapper;
import com.garment.mes.inventory.mapper.OutboundItemMapper;
import com.garment.mes.inventory.mapper.OutboundMapper;
import com.garment.mes.inventory.mapper.StockLogMapper;
import com.garment.mes.inventory.mapper.StockMapper;
import com.garment.mes.inventory.mapper.WarehouseMapper;
import com.garment.mes.master.entity.Material;
import com.garment.mes.master.mapper.MaterialMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存管理：仓库 / 入库 / 出库 / 现存量 / 流水
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final WarehouseMapper warehouseMapper;
    private final InboundMapper inboundMapper;
    private final InboundItemMapper inboundItemMapper;
    private final OutboundMapper outboundMapper;
    private final OutboundItemMapper outboundItemMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final MaterialMapper materialMapper;

    public InventoryController(WarehouseMapper warehouseMapper, InboundMapper inboundMapper,
                               InboundItemMapper inboundItemMapper, OutboundMapper outboundMapper,
                               OutboundItemMapper outboundItemMapper, StockMapper stockMapper,
                               StockLogMapper stockLogMapper, MaterialMapper materialMapper) {
        this.warehouseMapper = warehouseMapper;
        this.inboundMapper = inboundMapper;
        this.inboundItemMapper = inboundItemMapper;
        this.outboundMapper = outboundMapper;
        this.outboundItemMapper = outboundItemMapper;
        this.stockMapper = stockMapper;
        this.stockLogMapper = stockLogMapper;
        this.materialMapper = materialMapper;
    }

    // ===== 仓库 =====
    @GetMapping("/warehouse/list")
    public R<List<Warehouse>> warehouseList() {
        return R.ok(warehouseMapper.selectList(null));
    }

    @PostMapping("/warehouse")
    public R<Void> createWarehouse(@RequestBody Warehouse w) {
        warehouseMapper.insert(w);
        return R.ok();
    }

    @PutMapping("/warehouse")
    public R<Void> updateWarehouse(@RequestBody Warehouse w) {
        warehouseMapper.updateById(w);
        return R.ok();
    }

    @DeleteMapping("/warehouse/{id}")
    public R<Void> deleteWarehouse(@PathVariable String id) {
        warehouseMapper.deleteById(id);
        return R.ok();
    }

    // ===== 入库 =====
    @GetMapping("/inbound/page")
    public R<Page<Inbound>> inboundPage(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize) {
        Page<Inbound> page = inboundMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Inbound>().orderByDesc(Inbound::getCreateTime));
        fillInboundExtra(page.getRecords());
        return R.ok(page);
    }

    @PostMapping("/inbound")
    @Transactional
    public R<Void> createInbound(@RequestBody Inbound inbound) {
        if (!StringUtils.hasText(inbound.getInboundNo())) {
            inbound.setInboundNo("IN" + System.currentTimeMillis());
        }
        if (inbound.getInboundDate() == null) {
            inbound.setInboundDate(LocalDateTime.now());
        }
        inboundMapper.insert(inbound);
        if (inbound.getItems() != null) {
            for (InboundItem item : inbound.getItems()) {
                item.setInboundId(inbound.getInboundId());
                inboundItemMapper.insert(item);
                updateStock(inbound.getWarehouseId(), item.getMaterialId(), item.getQty(),
                        "IN", inbound.getInboundNo());
            }
        }
        return R.ok();
    }

    @GetMapping("/inbound/{id}")
    public R<Inbound> inboundGet(@PathVariable String id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound != null) {
            List<InboundItem> items = inboundItemMapper.selectList(
                    new LambdaQueryWrapper<InboundItem>().eq(InboundItem::getInboundId, id));
            fillMaterialName(items.stream().map(i -> (Object) i).toList());
            inbound.setItems(items);
            fillInboundExtra(List.of(inbound));
        }
        return R.ok(inbound);
    }

    @PutMapping("/inbound")
    public R<Void> updateInbound(@RequestBody Inbound inbound) {
        inboundMapper.updateById(inbound);
        return R.ok();
    }

    @DeleteMapping("/inbound/{id}")
    public R<Void> deleteInbound(@PathVariable String id) {
        inboundMapper.deleteById(id);
        return R.ok();
    }

    // ===== 出库 =====
    @GetMapping("/outbound/page")
    public R<Page<Outbound>> outboundPage(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize) {
        Page<Outbound> page = outboundMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Outbound>().orderByDesc(Outbound::getCreateTime));
        fillOutboundExtra(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/outbound/{id}")
    public R<Outbound> outboundGet(@PathVariable String id) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound != null) {
            List<OutboundItem> items = outboundItemMapper.selectList(
                    new LambdaQueryWrapper<OutboundItem>().eq(OutboundItem::getOutboundId, id));
            fillMaterialName(items.stream().map(i -> (Object) i).toList());
            outbound.setItems(items);
            fillOutboundExtra(List.of(outbound));
        }
        return R.ok(outbound);
    }

    @PutMapping("/outbound")
    public R<Void> updateOutbound(@RequestBody Outbound outbound) {
        outboundMapper.updateById(outbound);
        return R.ok();
    }

    @DeleteMapping("/outbound/{id}")
    public R<Void> deleteOutbound(@PathVariable String id) {
        outboundMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping("/outbound")
    @Transactional
    public R<Void> createOutbound(@RequestBody Outbound outbound) {
        if (!StringUtils.hasText(outbound.getOutboundNo())) {
            outbound.setOutboundNo("OUT" + System.currentTimeMillis());
        }
        if (outbound.getOutboundDate() == null) {
            outbound.setOutboundDate(LocalDateTime.now());
        }
        outboundMapper.insert(outbound);
        if (outbound.getItems() != null) {
            for (OutboundItem item : outbound.getItems()) {
                item.setOutboundId(outbound.getOutboundId());
                outboundItemMapper.insert(item);
                updateStock(outbound.getWarehouseId(), item.getMaterialId(),
                        item.getQty().negate(), "OUT", outbound.getOutboundNo());
            }
        }
        return R.ok();
    }

    // ===== 现存量 =====
    @GetMapping("/stock/page")
    public R<Page<Stock>> stockPage(@RequestParam(defaultValue = "1") long pageNum,
                                    @RequestParam(defaultValue = "10") long pageSize,
                                    @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Stock> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            List<String> materialIds = materialMapper.selectList(
                    new LambdaQueryWrapper<Material>().like(Material::getMaterialName, keyword))
                    .stream().map(Material::getMaterialId).toList();
            if (!materialIds.isEmpty()) {
                qw.in(Stock::getMaterialId, materialIds);
            } else {
                qw.eq(Stock::getMaterialId, "-1");
            }
        }
        Page<Stock> page = stockMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillStockName(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/stock/log")
    public R<Page<StockLog>> stockLog(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize) {
        Page<StockLog> page = stockLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StockLog>().orderByDesc(StockLog::getLogTime));
        fillLogName(page.getRecords());
        return R.ok(page);
    }

    // ===== 私有 =====
    private void fillInboundExtra(List<Inbound> list) {
        List<String> ids = list.stream().map(Inbound::getWarehouseId)
                .filter(StringUtils::hasText).distinct().toList();
        Map<String, Warehouse> map = ids.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Function.identity()));
        list.forEach(ib -> {
            Warehouse w = map.get(ib.getWarehouseId());
            if (w != null) ib.setWarehouseName(w.getWarehouseName());
            if (ib.getItems() != null && !ib.getItems().isEmpty()) {
                BigDecimal total = ib.getItems().stream()
                        .map(InboundItem::getQty)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                ib.setTotalQty(total);
            }
        });
    }

    private void fillOutboundExtra(List<Outbound> list) {
        List<String> ids = list.stream().map(Outbound::getWarehouseId)
                .filter(StringUtils::hasText).distinct().toList();
        Map<String, Warehouse> map = ids.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Function.identity()));
        list.forEach(ob -> {
            Warehouse w = map.get(ob.getWarehouseId());
            if (w != null) ob.setWarehouseName(w.getWarehouseName());
            if (ob.getItems() != null && !ob.getItems().isEmpty()) {
                BigDecimal total = ob.getItems().stream()
                        .map(OutboundItem::getQty)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                ob.setTotalQty(total);
            }
        });
    }

    private void fillLogName(List<StockLog> logs) {
        List<String> materialIds = logs.stream().map(StockLog::getMaterialId)
                .filter(StringUtils::hasText).distinct().toList();
        List<String> warehouseIds = logs.stream().map(StockLog::getWarehouseId)
                .filter(StringUtils::hasText).distinct().toList();
        Map<String, Material> mMap = materialIds.isEmpty() ? Map.of()
                : materialMapper.selectBatchIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getMaterialId, Function.identity()));
        Map<String, Warehouse> wMap = warehouseIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Function.identity()));
        logs.forEach(l -> {
            Material m = mMap.get(l.getMaterialId());
            Warehouse w = wMap.get(l.getWarehouseId());
            if (m != null) l.setMaterialName(m.getMaterialName());
            if (w != null) l.setWarehouseName(w.getWarehouseName());
        });
    }

    private void updateStock(String warehouseId, String materialId, BigDecimal changeQty,
                             String changeType, String bizNo) {
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getWarehouseId, warehouseId)
                .eq(Stock::getMaterialId, materialId));
        BigDecimal balance;
        if (stock == null) {
            stock = new Stock();
            stock.setWarehouseId(warehouseId);
            stock.setMaterialId(materialId);
            stock.setQty(changeQty);
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.insert(stock);
            balance = changeQty;
        } else {
            BigDecimal newQty = stock.getQty().add(changeQty);
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("库存不足，当前库存 " + stock.getQty());
            }
            stock.setQty(newQty);
            stock.setUpdateTime(LocalDateTime.now());
            stockMapper.updateById(stock);
            balance = newQty;
        }
        StockLog log = new StockLog();
        log.setWarehouseId(warehouseId);
        log.setMaterialId(materialId);
        log.setChangeType(changeType);
        log.setChangeQty(changeQty);
        log.setBalanceQty(balance);
        log.setBizNo(bizNo);
        log.setLogTime(LocalDateTime.now());
        stockLogMapper.insert(log);
    }

    private void fillMaterialName(List<Object> items) {
        // 简化：逐项填充（InboundItem / OutboundItem 都有 materialId + materialName）
        List<String> ids = items.stream().map(obj -> {
            if (obj instanceof InboundItem i) return i.getMaterialId();
            if (obj instanceof OutboundItem o) return o.getMaterialId();
            return null;
        }).filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Material> map = materialMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Material::getMaterialId, Function.identity()));
        items.forEach(obj -> {
            String name = null;
            if (obj instanceof InboundItem i) {
                Material m = map.get(i.getMaterialId());
                name = m != null ? m.getMaterialName() : null;
                i.setMaterialName(name);
            } else if (obj instanceof OutboundItem o) {
                Material m = map.get(o.getMaterialId());
                name = m != null ? m.getMaterialName() : null;
                o.setMaterialName(name);
            }
        });
    }

    private void fillStockName(List<Stock> list) {
        List<String> materialIds = list.stream().map(Stock::getMaterialId).distinct().toList();
        List<String> warehouseIds = list.stream().map(Stock::getWarehouseId).distinct().toList();
        Map<String, Material> materialMap = materialIds.isEmpty() ? Map.of()
                : materialMapper.selectBatchIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getMaterialId, Function.identity()));
        Map<String, Warehouse> warehouseMap = warehouseIds.isEmpty() ? Map.of()
                : warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Function.identity()));
        list.forEach(s -> {
            Material m = materialMap.get(s.getMaterialId());
            Warehouse w = warehouseMap.get(s.getWarehouseId());
            if (m != null) s.setMaterialName(m.getMaterialName());
            if (w != null) s.setWarehouseName(w.getWarehouseName());
        });
    }
}
