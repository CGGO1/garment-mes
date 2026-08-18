package com.garment.mes.report.controller;

import com.garment.mes.common.R;
import com.garment.mes.report.dto.ReportResult;
import com.garment.mes.report.service.AiReportService;
import com.garment.mes.report.service.ReportExcelService;
import com.garment.mes.report.service.ReportPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 报表接口：AI 即席报表生成 + PDF/Excel 下载
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final AiReportService aiReportService;
    private final ReportPdfService reportPdfService;
    private final ReportExcelService reportExcelService;

    public ReportController(AiReportService aiReportService, ReportPdfService reportPdfService,
                            ReportExcelService reportExcelService) {
        this.aiReportService = aiReportService;
        this.reportPdfService = reportPdfService;
        this.reportExcelService = reportExcelService;
    }

    /**
     * 生成报表
     */
    @PostMapping("/ai/generate")
    public R<ReportResult> generate(@RequestBody Map<String, String> body) {
        String moduleType = body.get("moduleType");
        String prompt = body.get("prompt");
        return R.ok(aiReportService.generate(moduleType, prompt));
    }

    /**
     * 导出 PDF
     */
    @PostMapping("/ai/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody ReportResult report) {
        byte[] bytes = reportPdfService.export(report);
        return fileResponse(bytes, "application/pdf", safeName(report.getTitle()) + ".pdf");
    }

    /**
     * 导出 Excel
     */
    @PostMapping("/ai/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody ReportResult report) {
        byte[] bytes = reportExcelService.export(report);
        return fileResponse(bytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                safeName(report.getTitle()) + ".xlsx");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] bytes, String contentType, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "报表";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
