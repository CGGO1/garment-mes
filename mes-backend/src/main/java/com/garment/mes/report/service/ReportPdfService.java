package com.garment.mes.report.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.garment.mes.report.dto.ReportResult;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报表 PDF 导出（HTML → PDF）
 */
@Service
public class ReportPdfService {

    public byte[] export(ReportResult report) {
        String html = buildHtml(report);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 导出失败: " + e.getMessage(), e);
        }
    }

    private String buildHtml(ReportResult report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        sb.append("<style>");
        sb.append("body{font-family:'Microsoft YaHei','SimSun',sans-serif;color:#333;margin:32px;}");
        sb.append("h1{font-size:22px;color:#2f7de0;margin-bottom:4px;}");
        sb.append(".meta{color:#999;font-size:12px;margin-bottom:16px;}");
        sb.append(".summary{background:#f4f6fa;padding:10px 14px;border-radius:6px;margin-bottom:16px;}");
        sb.append(".analysis{white-space:pre-wrap;line-height:1.7;margin-bottom:16px;}");
        sb.append("h2{font-size:16px;color:#2f7de0;margin:20px 0 8px;}");
        sb.append("table{width:100%;border-collapse:collapse;font-size:12px;}");
        sb.append("th,td{border:1px solid #ddd;padding:6px 8px;text-align:left;}");
        sb.append("th{background:#f4f6fa;}");
        sb.append("</style></head><body>");

        sb.append("<h1>").append(escape(report.getTitle())).append("</h1>");
        sb.append("<div class=\"meta\">生成时间：").append(report.getGeneratedAt()).append("</div>");
        if (report.getSummary() != null) {
            sb.append("<div class=\"summary\">").append(escape(report.getSummary())).append("</div>");
        }
        if (report.getAnalysis() != null) {
            sb.append("<div class=\"analysis\">").append(escape(report.getAnalysis())).append("</div>");
        }
        if (report.getTables() != null) {
            for (ReportResult.TableData table : report.getTables()) {
                sb.append("<h2>").append(escape(table.getTitle())).append("</h2>");
                sb.append("<table>");
                if (table.getHeaders() != null) {
                    sb.append("<tr>");
                    for (String h : table.getHeaders()) {
                        sb.append("<th>").append(escape(h)).append("</th>");
                    }
                    sb.append("</tr>");
                }
                if (table.getRows() != null) {
                    for (List<Object> row : table.getRows()) {
                        sb.append("<tr>");
                        for (Object cell : row) {
                            sb.append("<td>").append(escape(cell == null ? "" : String.valueOf(cell))).append("</td>");
                        }
                        sb.append("</tr>");
                    }
                }
                sb.append("</table>");
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
