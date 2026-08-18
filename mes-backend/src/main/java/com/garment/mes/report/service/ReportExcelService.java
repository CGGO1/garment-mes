package com.garment.mes.report.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.garment.mes.report.dto.ReportResult;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表 Excel 导出（多 sheet）
 */
@Service
public class ReportExcelService {

    public byte[] export(ReportResult report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ExcelWriter writer = EasyExcel.write(out).build();
            int index = 0;
            for (ReportResult.TableData table : report.getTables()) {
                if (table.getHeaders() == null || table.getHeaders().isEmpty()) {
                    continue;
                }
                List<List<String>> head = new ArrayList<>();
                for (String h : table.getHeaders()) {
                    head.add(List.of(h));
                }
                List<List<Object>> rows = table.getRows() == null ? List.of() : table.getRows();
                String sheetName = safeSheetName(table.getTitle(), index);
                WriteSheet sheet = EasyExcel.writerSheet(index, sheetName).head(head).build();
                writer.write(rows, sheet);
                index++;
            }
            if (index == 0) {
                // 至少写一个 sheet
                WriteSheet sheet = EasyExcel.writerSheet(0, "报表").head(List.of(List.of("标题"), List.of("摘要"))).build();
                writer.write(List.of(List.of(report.getTitle()), List.of(report.getSummary())), sheet);
            }
            writer.finish();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel 导出失败: " + e.getMessage(), e);
        }
    }

    private String safeSheetName(String name, int index) {
        if (name == null || name.isBlank()) {
            return "Sheet" + (index + 1);
        }
        String cleaned = name.replaceAll("[\\\\/:*?\\[\\]]", "_");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }
}
