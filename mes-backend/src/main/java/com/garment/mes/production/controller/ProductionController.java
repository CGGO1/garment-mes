package com.garment.mes.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.master.entity.Product;
import com.garment.mes.master.mapper.ProductMapper;
import com.garment.mes.production.entity.Cutting;
import com.garment.mes.production.entity.ProductionReport;
import com.garment.mes.production.entity.WorkOrder;
import com.garment.mes.production.entity.WorkOrderItem;
import com.garment.mes.production.mapper.CuttingMapper;
import com.garment.mes.production.mapper.ProductionReportMapper;
import com.garment.mes.production.mapper.WorkOrderItemMapper;
import com.garment.mes.production.mapper.WorkOrderMapper;
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
 * 生产执行管理：工单 / 裁床 / 报工
 */
@RestController
@RequestMapping("/api/production")
public class ProductionController {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderItemMapper workOrderItemMapper;
    private final CuttingMapper cuttingMapper;
    private final ProductionReportMapper reportMapper;
    private final ProductMapper productMapper;

    public ProductionController(WorkOrderMapper workOrderMapper, WorkOrderItemMapper workOrderItemMapper,
                                CuttingMapper cuttingMapper, ProductionReportMapper reportMapper,
                                ProductMapper productMapper) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderItemMapper = workOrderItemMapper;
        this.cuttingMapper = cuttingMapper;
        this.reportMapper = reportMapper;
        this.productMapper = productMapper;
    }

    @GetMapping("/work-order/page")
    public R<Page<WorkOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                   @RequestParam(defaultValue = "10") long pageSize,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(WorkOrder::getWorkOrderNo, keyword);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(WorkOrder::getStatus, status);
        }
        qw.orderByDesc(WorkOrder::getCreateTime);
        Page<WorkOrder> page = workOrderMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillProductName(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/work-order/{id}")
    public R<WorkOrder> get(@PathVariable String id) {
        WorkOrder wo = workOrderMapper.selectById(id);
        if (wo != null) {
            fillProductName(List.of(wo));
            wo.setItems(workOrderItemMapper.selectList(
                    new LambdaQueryWrapper<WorkOrderItem>()
                            .eq(WorkOrderItem::getWorkOrderId, id).orderByAsc(WorkOrderItem::getSeq)));
            wo.setCuttings(cuttingMapper.selectList(
                    new LambdaQueryWrapper<Cutting>()
                            .eq(Cutting::getWorkOrderId, id).orderByDesc(Cutting::getCreateTime)));
            wo.setReports(reportMapper.selectList(
                    new LambdaQueryWrapper<ProductionReport>()
                            .eq(ProductionReport::getWorkOrderId, id)
                            .orderByDesc(ProductionReport::getReportTime)));
        }
        return R.ok(wo);
    }

    @PostMapping("/work-order")
    @Transactional
    public R<Void> create(@RequestBody WorkOrder wo) {
        if (!StringUtils.hasText(wo.getWorkOrderNo())) {
            wo.setWorkOrderNo("WO" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(wo.getStatus())) {
            wo.setStatus("PLANNED");
        }
        if (wo.getFinishQty() == null) {
            wo.setFinishQty(BigDecimal.ZERO);
        }
        workOrderMapper.insert(wo);
        saveItems(wo);
        return R.ok();
    }

    @PutMapping("/work-order")
    @Transactional
    public R<Void> update(@RequestBody WorkOrder wo) {
        workOrderMapper.updateById(wo);
        if (wo.getItems() != null) {
            workOrderItemMapper.delete(new LambdaQueryWrapper<WorkOrderItem>()
                    .eq(WorkOrderItem::getWorkOrderId, wo.getWorkOrderId()));
            saveItems(wo);
        }
        return R.ok();
    }

    @DeleteMapping("/work-order/{id}")
    public R<Void> delete(@PathVariable String id) {
        workOrderMapper.deleteById(id);
        return R.ok();
    }

    @PutMapping("/work-order/{id}/status")
    public R<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderId(id);
        wo.setStatus(body.get("status"));
        workOrderMapper.updateById(wo);
        return R.ok();
    }

    // ===== 裁床 =====
    @GetMapping("/cutting/{workOrderId}")
    public R<List<Cutting>> cuttings(@PathVariable String workOrderId) {
        return R.ok(cuttingMapper.selectList(
                new LambdaQueryWrapper<Cutting>().eq(Cutting::getWorkOrderId, workOrderId)));
    }

    @PostMapping("/cutting")
    public R<Void> createCutting(@RequestBody Cutting cutting) {
        cuttingMapper.insert(cutting);
        return R.ok();
    }

    @DeleteMapping("/cutting/{id}")
    public R<Void> deleteCutting(@PathVariable String id) {
        cuttingMapper.deleteById(id);
        return R.ok();
    }

    // ===== 报工 =====
    @GetMapping("/report/{workOrderId}")
    public R<List<ProductionReport>> reports(@PathVariable String workOrderId) {
        return R.ok(reportMapper.selectList(
                new LambdaQueryWrapper<ProductionReport>()
                        .eq(ProductionReport::getWorkOrderId, workOrderId)
                        .orderByDesc(ProductionReport::getReportTime)));
    }

    @PostMapping("/report")
    @Transactional
    public R<Void> createReport(@RequestBody ProductionReport report) {
        if (report.getReportTime() == null) {
            report.setReportTime(LocalDateTime.now());
        }
        reportMapper.insert(report);
        // 更新工序完成数
        if (StringUtils.hasText(report.getProcessId())) {
            WorkOrderItem item = workOrderItemMapper.selectById(report.getProcessId());
            if (item != null) {
                BigDecimal finish = (item.getFinishQty() == null ? BigDecimal.ZERO : item.getFinishQty())
                        .add(report.getReportQty() == null ? BigDecimal.ZERO : report.getReportQty());
                item.setFinishQty(finish);
                item.setStatus("DOING");
                workOrderItemMapper.updateById(item);
            }
        }
        // 更新工单完成数
        WorkOrder wo = workOrderMapper.selectById(report.getWorkOrderId());
        if (wo != null) {
            BigDecimal finish = (wo.getFinishQty() == null ? BigDecimal.ZERO : wo.getFinishQty())
                    .add(report.getReportQty() == null ? BigDecimal.ZERO : report.getReportQty());
            wo.setFinishQty(finish);
            if ("IN_PRODUCTION".equals(wo.getStatus())) {
                // 保持
            }
            workOrderMapper.updateById(wo);
        }
        return R.ok();
    }

    private void saveItems(WorkOrder wo) {
        if (wo.getItems() == null) {
            return;
        }
        for (WorkOrderItem item : wo.getItems()) {
            item.setWorkOrderId(wo.getWorkOrderId());
            workOrderItemMapper.insert(item);
        }
    }

    private void fillProductName(List<WorkOrder> list) {
        List<String> ids = list.stream().map(WorkOrder::getProductId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Product> map = productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        list.forEach(wo -> {
            Product p = map.get(wo.getProductId());
            if (p != null) {
                wo.setProductName(p.getProductCode() + " " + p.getProductName());
            }
        });
    }
}
