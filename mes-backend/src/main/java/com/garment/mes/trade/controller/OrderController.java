package com.garment.mes.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.master.entity.Customer;
import com.garment.mes.master.entity.Product;
import com.garment.mes.master.mapper.CustomerMapper;
import com.garment.mes.master.mapper.ProductMapper;
import com.garment.mes.trade.entity.Shipment;
import com.garment.mes.trade.entity.TradeDocument;
import com.garment.mes.trade.entity.TradeOrder;
import com.garment.mes.trade.entity.TradeOrderItem;
import com.garment.mes.trade.mapper.ShipmentMapper;
import com.garment.mes.trade.mapper.TradeDocumentMapper;
import com.garment.mes.trade.mapper.TradeOrderItemMapper;
import com.garment.mes.trade.mapper.TradeOrderMapper;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 进出口订单管理
 */
@RestController
@RequestMapping("/api/trade")
public class OrderController {

    private final TradeOrderMapper orderMapper;
    private final TradeOrderItemMapper orderItemMapper;
    private final TradeDocumentMapper documentMapper;
    private final ShipmentMapper shipmentMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;

    public OrderController(TradeOrderMapper orderMapper, TradeOrderItemMapper orderItemMapper,
                           TradeDocumentMapper documentMapper, ShipmentMapper shipmentMapper,
                           CustomerMapper customerMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.documentMapper = documentMapper;
        this.shipmentMapper = shipmentMapper;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
    }

    @GetMapping("/order/page")
    public R<Page<TradeOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                    @RequestParam(defaultValue = "10") long pageSize,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String orderType) {
        LambdaQueryWrapper<TradeOrder> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(TradeOrder::getOrderNo, keyword);
        }
        if (StringUtils.hasText(status)) {
            qw.eq(TradeOrder::getStatus, status);
        }
        if (StringUtils.hasText(orderType)) {
            qw.eq(TradeOrder::getOrderType, orderType);
        }
        qw.orderByDesc(TradeOrder::getCreateTime);
        Page<TradeOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillCustomerName(page.getRecords());
        fillCounts(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/order/{id}")
    public R<TradeOrder> get(@PathVariable String id) {
        TradeOrder order = orderMapper.selectById(id);
        if (order != null) {
            fillCustomerName(List.of(order));
            List<TradeOrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, id));
            fillProductName(items);
            order.setItems(items);
        }
        return R.ok(order);
    }

    @PostMapping("/order")
    @Transactional
    public R<TradeOrder> create(@RequestBody TradeOrder order) {
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(order.getStatus())) {
            order.setStatus("PENDING");
        }
        orderMapper.insert(order);
        saveItems(order);
        return R.ok(order);
    }

    @PutMapping("/order")
    @Transactional
    public R<Void> update(@RequestBody TradeOrder order) {
        orderMapper.updateById(order);
        if (order.getItems() != null) {
            orderItemMapper.delete(new LambdaQueryWrapper<TradeOrderItem>()
                    .eq(TradeOrderItem::getOrderId, order.getOrderId()));
            saveItems(order);
        }
        return R.ok();
    }

    @DeleteMapping("/order/{id}")
    @Transactional
    public R<Void> delete(@PathVariable String id) {
        orderMapper.deleteById(id);
        orderItemMapper.delete(new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, id));
        return R.ok();
    }

    /** 订单状态流转 */
    @PutMapping("/order/{id}/status")
    public R<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        TradeOrder order = new TradeOrder();
        order.setOrderId(id);
        order.setStatus(body.get("status"));
        orderMapper.updateById(order);
        return R.ok();
    }

    // ===== 单证 =====
    @GetMapping("/document/{orderId}")
    public R<List<TradeDocument>> documents(@PathVariable String orderId) {
        return R.ok(documentMapper.selectList(
                new LambdaQueryWrapper<TradeDocument>().eq(TradeDocument::getOrderId, orderId)));
    }

    @PostMapping("/document")
    public R<Void> createDocument(@RequestBody TradeDocument doc) {
        documentMapper.insert(doc);
        return R.ok();
    }

    @DeleteMapping("/document/{id}")
    public R<Void> deleteDocument(@PathVariable String id) {
        documentMapper.deleteById(id);
        return R.ok();
    }

    // ===== 出货 =====
    @GetMapping("/shipment/{orderId}")
    public R<List<Shipment>> shipments(@PathVariable String orderId) {
        return R.ok(shipmentMapper.selectList(
                new LambdaQueryWrapper<Shipment>().eq(Shipment::getOrderId, orderId)));
    }

    @PostMapping("/shipment")
    public R<Void> createShipment(@RequestBody Shipment shipment) {
        shipmentMapper.insert(shipment);
        return R.ok();
    }

    @DeleteMapping("/shipment/{id}")
    public R<Void> deleteShipment(@PathVariable String id) {
        shipmentMapper.deleteById(id);
        return R.ok();
    }

    private void saveItems(TradeOrder order) {
        if (order.getItems() == null) {
            return;
        }
        for (TradeOrderItem item : order.getItems()) {
            item.setOrderId(order.getOrderId());
            orderItemMapper.insert(item);
        }
    }

    private void fillCustomerName(List<TradeOrder> orders) {
        List<String> ids = orders.stream().map(TradeOrder::getCustomerId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Customer> map = customerMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Customer::getCustomerId, Function.identity()));
        orders.forEach(o -> {
            Customer c = map.get(o.getCustomerId());
            if (c != null) {
                o.setCustomerName(c.getCustomerName());
            }
        });
    }

    private void fillProductName(List<TradeOrderItem> items) {
        List<String> ids = items.stream().map(TradeOrderItem::getProductId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Product> map = productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        items.forEach(i -> {
            Product p = map.get(i.getProductId());
            if (p != null) {
                i.setProductName(p.getProductCode() + " " + p.getProductName());
            }
        });
    }

    private void fillCounts(List<TradeOrder> orders) {
        List<String> ids = orders.stream().map(TradeOrder::getOrderId).distinct().toList();
        if (ids.isEmpty()) return;
        Map<String, Long> itemCounts = orderItemMapper.selectList(
                        new LambdaQueryWrapper<TradeOrderItem>().in(TradeOrderItem::getOrderId, ids))
                .stream().collect(Collectors.groupingBy(TradeOrderItem::getOrderId, Collectors.counting()));
        Map<String, Long> docCounts = documentMapper.selectList(
                        new LambdaQueryWrapper<TradeDocument>().in(TradeDocument::getOrderId, ids))
                .stream().collect(Collectors.groupingBy(TradeDocument::getOrderId, Collectors.counting()));
        Map<String, Long> shipCounts = shipmentMapper.selectList(
                        new LambdaQueryWrapper<Shipment>().in(Shipment::getOrderId, ids))
                .stream().collect(Collectors.groupingBy(Shipment::getOrderId, Collectors.counting()));
        orders.forEach(o -> {
            o.setItemsCount(itemCounts.getOrDefault(o.getOrderId(), 0L).intValue());
            o.setDocCount(docCounts.getOrDefault(o.getOrderId(), 0L).intValue());
            o.setShipCount(shipCounts.getOrDefault(o.getOrderId(), 0L).intValue());
        });
    }
}
