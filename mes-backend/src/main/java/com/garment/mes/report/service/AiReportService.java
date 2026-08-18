package com.garment.mes.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garment.mes.ai.AiClient;
import com.garment.mes.inventory.entity.Stock;
import com.garment.mes.inventory.mapper.StockMapper;
import com.garment.mes.production.entity.WorkOrder;
import com.garment.mes.production.mapper.WorkOrderMapper;
import com.garment.mes.quality.entity.QcInspection;
import com.garment.mes.quality.mapper.QcInspectionMapper;
import com.garment.mes.report.dto.ReportResult;
import com.garment.mes.trade.entity.TradeOrder;
import com.garment.mes.trade.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 即席报表服务：按模块查询数据 → 生成报表（优先 LLM，失败降级为本地统计）
 */
@Slf4j
@Service
public class AiReportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TradeOrderMapper orderMapper;
    private final WorkOrderMapper workOrderMapper;
    private final StockMapper stockMapper;
    private final QcInspectionMapper inspectionMapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public AiReportService(TradeOrderMapper orderMapper, WorkOrderMapper workOrderMapper,
                           StockMapper stockMapper, QcInspectionMapper inspectionMapper,
                           AiClient aiClient, ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.workOrderMapper = workOrderMapper;
        this.stockMapper = stockMapper;
        this.inspectionMapper = inspectionMapper;
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成报表
     * @param moduleType 模块：order / production / inventory / quality
     * @param prompt 用户需求（自然语言）
     */
    public ReportResult generate(String moduleType, String prompt) {
        List<Map<String, Object>> rows = queryData(moduleType);
        // 尝试 LLM 生成
        ReportResult llm = tryLlm(moduleType, rows, prompt);
        if (llm != null) {
            return llm;
        }
        // 降级：本地生成
        return buildFallback(moduleType, rows, prompt);
    }

    private List<Map<String, Object>> queryData(String moduleType) {
        return switch (moduleType == null ? "" : moduleType) {
            case "order" -> orderRows();
            case "production" -> workOrderRows();
            case "inventory" -> stockRows();
            case "quality" -> inspectionRows();
            default -> List.of();
        };
    }

    private List<Map<String, Object>> orderRows() {
        List<TradeOrder> list = orderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>().orderByDesc(TradeOrder::getCreateTime).last("limit 200"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TradeOrder o : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("orderNo", o.getOrderNo());
            m.put("orderType", o.getOrderType());
            m.put("incoterm", o.getIncoterm());
            m.put("status", o.getStatus());
            m.put("totalAmount", o.getTotalAmount());
            m.put("deliveryDate", o.getDeliveryDate());
            rows.add(m);
        }
        return rows;
    }

    private List<Map<String, Object>> workOrderRows() {
        List<WorkOrder> list = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().orderByDesc(WorkOrder::getCreateTime).last("limit 200"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (WorkOrder wo : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("workOrderNo", wo.getWorkOrderNo());
            m.put("planQty", wo.getPlanQty());
            m.put("finishQty", wo.getFinishQty());
            m.put("status", wo.getStatus());
            m.put("startDate", wo.getStartDate());
            rows.add(m);
        }
        return rows;
    }

    private List<Map<String, Object>> stockRows() {
        List<Stock> list = stockMapper.selectList(new LambdaQueryWrapper<Stock>().last("limit 200"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Stock s : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("warehouseId", s.getWarehouseId());
            m.put("materialId", s.getMaterialId());
            m.put("qty", s.getQty());
            m.put("updateTime", s.getUpdateTime());
            rows.add(m);
        }
        return rows;
    }

    private List<Map<String, Object>> inspectionRows() {
        List<QcInspection> list = inspectionMapper.selectList(
                new LambdaQueryWrapper<QcInspection>().orderByDesc(QcInspection::getCreateTime).last("limit 200"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QcInspection q : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("inspectionNo", q.getInspectionNo());
            m.put("inspectionType", q.getInspectionType());
            m.put("sampleQty", q.getSampleQty());
            m.put("passQty", q.getPassQty());
            m.put("failQty", q.getFailQty());
            m.put("result", q.getResult());
            m.put("inspector", q.getInspector());
            rows.add(m);
        }
        return rows;
    }

    private ReportResult tryLlm(String moduleType, List<Map<String, Object>> rows, String prompt) {
        if (!aiClient.enabled() || rows.isEmpty()) {
            return null;
        }
        try {
            String dataJson = objectMapper.writeValueAsString(rows);
            String llmPrompt = """
                    你是服装制造 MES 系统的数据分析助手。请基于以下数据生成一份报表，严格输出 JSON（不要输出其他文字）：
                    {"title":"报表标题","summary":"一句话概述","analysis":"分析文本(可多行)","charts":[{"type":"bar|line|pie","title":"图表标题","labels":["a","b"],"series":[{"name":"系列","data":[1,2]}]}],"tables":[{"title":"表格标题","headers":["列1","列2"],"rows":[["值1","值2"]]}]}
                    用户需求：%s
                    模块：%s
                    数据：%s
                    """.formatted(prompt == null ? "生成汇总报表" : prompt, moduleType, dataJson);
            String resp = aiClient.chat(llmPrompt);
            if (resp == null) {
                return null;
            }
            // 提取 JSON
            String json = extractJson(resp);
            if (json == null) {
                return null;
            }
            ReportResult result = objectMapper.readValue(json, ReportResult.class);
            if (result.getTitle() == null) {
                return null;
            }
            result.setModuleType(moduleType);
            result.setGeneratedAt(LocalDateTime.now().format(FMT));
            return result;
        } catch (Exception e) {
            log.warn("LLM 报表生成失败，降级为本地生成: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private ReportResult buildFallback(String moduleType, List<Map<String, Object>> rows, String prompt) {
        String moduleName = switch (moduleType == null ? "" : moduleType) {
            case "order" -> "进出口订单";
            case "production" -> "生产工单";
            case "inventory" -> "库存";
            case "quality" -> "质量检验";
            default -> "业务数据";
        };
        ReportResult result = new ReportResult();
        result.setTitle(moduleName + "报表");
        result.setSummary("共 " + rows.size() + " 条记录（系统自动生成）");
        result.setAnalysis(prompt != null && !prompt.isBlank()
                ? "报表需求：\n" + prompt + "\n\n（AI 服务未连接或返回异常，以下为系统基于数据的自动统计。）"
                : "（AI 服务未连接或返回异常，以下为系统基于数据的自动统计。）");
        result.setModuleType(moduleType);
        result.setGeneratedAt(LocalDateTime.now().format(FMT));

        // 表格：直接把数据行转成表格
        if (!rows.isEmpty()) {
            List<String> headers = new ArrayList<>(rows.get(0).keySet());
            ReportResult.TableData table = new ReportResult.TableData();
            table.setTitle(moduleName + "明细");
            table.setHeaders(headers);
            List<List<Object>> tableRows = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                List<Object> r = new ArrayList<>();
                for (String h : headers) {
                    r.add(row.get(h));
                }
                tableRows.add(r);
            }
            table.setRows(tableRows);
            result.setTables(List.of(table));
        }
        // 简单统计图表
        ReportResult.ChartConfig chart = buildStatusChart(moduleType, rows);
        if (chart != null) {
            result.setCharts(List.of(chart));
        }
        return result;
    }

    private ReportResult.ChartConfig buildStatusChart(String moduleType, List<Map<String, Object>> rows) {
        String statusKey = switch (moduleType == null ? "" : moduleType) {
            case "order", "production" -> "status";
            case "quality" -> "result";
            default -> null;
        };
        if (statusKey == null) {
            return null;
        }
        Map<String, Long> counter = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object v = row.get(statusKey);
            String key = v == null ? "未知" : String.valueOf(v);
            counter.merge(key, 1L, Long::sum);
        }
        ReportResult.ChartConfig chart = new ReportResult.ChartConfig();
        chart.setType("pie");
        chart.setTitle("状态分布");
        chart.setLabels(new ArrayList<>(counter.keySet()));
        ReportResult.ChartConfig.Series series = new ReportResult.ChartConfig.Series();
        series.setName("数量");
        List<Double> data = new ArrayList<>();
        for (Long v : counter.values()) {
            data.add(v.doubleValue());
        }
        series.setData(data);
        chart.setSeries(List.of(series));
        return chart;
    }
}
