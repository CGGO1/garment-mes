package com.garment.mes.report.dto;

import lombok.Data;

import java.util.List;

/**
 * 报表结果（AI 即席报表的统一结构）
 */
@Data
public class ReportResult {

    private String title;
    private String summary;
    /** 分析文本（Markdown） */
    private String analysis;
    /** 图表配置 */
    private List<ChartConfig> charts;
    /** 表格数据 */
    private List<TableData> tables;
    private String moduleType;
    private String generatedAt;

    @Data
    public static class ChartConfig {
        /** bar / line / pie */
        private String type;
        private String title;
        private List<String> labels;
        private List<Series> series;

        @Data
        public static class Series {
            private String name;
            private List<Double> data;
        }
    }

    @Data
    public static class TableData {
        private String title;
        private List<String> headers;
        private List<List<Object>> rows;
    }
}
