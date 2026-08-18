package com.garment.mes.quality.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.quality.entity.QcDefect;
import com.garment.mes.quality.entity.QcInspection;
import com.garment.mes.quality.entity.QcInspectionItem;
import com.garment.mes.quality.entity.QcStandard;
import com.garment.mes.quality.mapper.QcDefectMapper;
import com.garment.mes.quality.mapper.QcInspectionItemMapper;
import com.garment.mes.quality.mapper.QcInspectionMapper;
import com.garment.mes.quality.mapper.QcStandardMapper;
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

/**
 * 质量管理：质检标准 / 验货单 / 疵点
 */
@RestController
@RequestMapping("/api/quality")
public class QualityController {

    private final QcStandardMapper standardMapper;
    private final QcInspectionMapper inspectionMapper;
    private final QcInspectionItemMapper inspectionItemMapper;
    private final QcDefectMapper defectMapper;

    public QualityController(QcStandardMapper standardMapper, QcInspectionMapper inspectionMapper,
                             QcInspectionItemMapper inspectionItemMapper, QcDefectMapper defectMapper) {
        this.standardMapper = standardMapper;
        this.inspectionMapper = inspectionMapper;
        this.inspectionItemMapper = inspectionItemMapper;
        this.defectMapper = defectMapper;
    }

    // ===== 质检标准 =====
    @GetMapping("/standard/list")
    public R<List<QcStandard>> standardList() {
        return R.ok(standardMapper.selectList(null));
    }

    @PostMapping("/standard")
    public R<Void> createStandard(@RequestBody QcStandard s) {
        standardMapper.insert(s);
        return R.ok();
    }

    @PutMapping("/standard")
    public R<Void> updateStandard(@RequestBody QcStandard s) {
        standardMapper.updateById(s);
        return R.ok();
    }

    @DeleteMapping("/standard/{id}")
    public R<Void> deleteStandard(@PathVariable String id) {
        standardMapper.deleteById(id);
        return R.ok();
    }

    // ===== 验货单 =====
    @GetMapping("/inspection/page")
    public R<Page<QcInspection>> page(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String inspectionType,
                                      @RequestParam(required = false) String result) {
        LambdaQueryWrapper<QcInspection> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(QcInspection::getInspectionNo, keyword);
        }
        if (StringUtils.hasText(inspectionType)) {
            qw.eq(QcInspection::getInspectionType, inspectionType);
        }
        if (StringUtils.hasText(result)) {
            qw.eq(QcInspection::getResult, result);
        }
        qw.orderByDesc(QcInspection::getCreateTime);
        return R.ok(inspectionMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/inspection/{id}")
    public R<QcInspection> get(@PathVariable String id) {
        QcInspection inspection = inspectionMapper.selectById(id);
        if (inspection != null) {
            inspection.setItems(inspectionItemMapper.selectList(
                    new LambdaQueryWrapper<QcInspectionItem>()
                            .eq(QcInspectionItem::getInspectionId, id)));
            inspection.setDefects(defectMapper.selectList(
                    new LambdaQueryWrapper<QcDefect>()
                            .eq(QcDefect::getInspectionId, id)));
        }
        return R.ok(inspection);
    }

    @PostMapping("/inspection")
    @Transactional
    public R<Void> create(@RequestBody QcInspection inspection) {
        if (!StringUtils.hasText(inspection.getInspectionNo())) {
            inspection.setInspectionNo("QC" + System.currentTimeMillis());
        }
        if (inspection.getInspectDate() == null) {
            inspection.setInspectDate(LocalDateTime.now());
        }
        if (inspection.getFailQty() == null) {
            inspection.setFailQty(BigDecimal.ZERO);
        }
        inspection.setResult(inspection.getFailQty().compareTo(BigDecimal.ZERO) > 0 ? "FAIL" : "PASS");
        inspectionMapper.insert(inspection);
        if (inspection.getItems() != null) {
            for (QcInspectionItem item : inspection.getItems()) {
                item.setInspectionId(inspection.getInspectionId());
                inspectionItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    @PutMapping("/inspection")
    @Transactional
    public R<Void> update(@RequestBody QcInspection inspection) {
        if (inspection.getFailQty() == null) {
            inspection.setFailQty(BigDecimal.ZERO);
        }
        inspection.setResult(inspection.getFailQty().compareTo(BigDecimal.ZERO) > 0 ? "FAIL" : "PASS");
        inspectionMapper.updateById(inspection);
        if (inspection.getItems() != null) {
            inspectionItemMapper.delete(new LambdaQueryWrapper<QcInspectionItem>()
                    .eq(QcInspectionItem::getInspectionId, inspection.getInspectionId()));
            for (QcInspectionItem item : inspection.getItems()) {
                item.setInspectionId(inspection.getInspectionId());
                inspectionItemMapper.insert(item);
            }
        }
        return R.ok();
    }

    @DeleteMapping("/inspection/{id}")
    public R<Void> delete(@PathVariable String id) {
        inspectionMapper.deleteById(id);
        return R.ok();
    }

    @PutMapping("/inspection/{id}/result")
    public R<Void> updateResult(@PathVariable String id, @RequestBody Map<String, String> body) {
        QcInspection inspection = new QcInspection();
        inspection.setInspectionId(id);
        inspection.setResult(body.get("result"));
        inspectionMapper.updateById(inspection);
        return R.ok();
    }

    // ===== 疵点 =====
    @GetMapping("/defect/{inspectionId}")
    public R<List<QcDefect>> defects(@PathVariable String inspectionId) {
        return R.ok(defectMapper.selectList(
                new LambdaQueryWrapper<QcDefect>().eq(QcDefect::getInspectionId, inspectionId)));
    }

    @PostMapping("/defect")
    public R<Void> createDefect(@RequestBody QcDefect defect) {
        defectMapper.insert(defect);
        return R.ok();
    }

    @DeleteMapping("/defect/{id}")
    public R<Void> deleteDefect(@PathVariable String id) {
        defectMapper.deleteById(id);
        return R.ok();
    }
}
