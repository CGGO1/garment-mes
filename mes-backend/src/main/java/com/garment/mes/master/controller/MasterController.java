package com.garment.mes.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.mes.common.R;
import com.garment.mes.master.entity.Bom;
import com.garment.mes.master.entity.BomItem;
import com.garment.mes.master.entity.Customer;
import com.garment.mes.master.entity.Material;
import com.garment.mes.master.entity.Process;
import com.garment.mes.master.entity.Product;
import com.garment.mes.master.entity.Supplier;
import com.garment.mes.master.mapper.BomItemMapper;
import com.garment.mes.master.mapper.BomMapper;
import com.garment.mes.master.mapper.CustomerMapper;
import com.garment.mes.master.mapper.MaterialMapper;
import com.garment.mes.master.mapper.ProcessMapper;
import com.garment.mes.master.mapper.ProductMapper;
import com.garment.mes.master.mapper.SupplierMapper;
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
 * 主数据管理：客户/供应商/产品/物料/工序/BOM
 */
@RestController
@RequestMapping("/api/master")
public class MasterController {

    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final MaterialMapper materialMapper;
    private final ProcessMapper processMapper;
    private final BomMapper bomMapper;
    private final BomItemMapper bomItemMapper;

    public MasterController(CustomerMapper customerMapper, SupplierMapper supplierMapper,
                            ProductMapper productMapper, MaterialMapper materialMapper,
                            ProcessMapper processMapper, BomMapper bomMapper, BomItemMapper bomItemMapper) {
        this.customerMapper = customerMapper;
        this.supplierMapper = supplierMapper;
        this.productMapper = productMapper;
        this.materialMapper = materialMapper;
        this.processMapper = processMapper;
        this.bomMapper = bomMapper;
        this.bomItemMapper = bomItemMapper;
    }

    // ===== 客户 =====
    @GetMapping("/customer/page")
    public R<Page<Customer>> customerPage(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Customer::getCustomerName, keyword).or().like(Customer::getCustomerCode, keyword);
        }
        return R.ok(customerMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/customer/list")
    public R<List<Customer>> customerList() {
        return R.ok(customerMapper.selectList(null));
    }

    @PostMapping("/customer")
    public R<Void> createCustomer(@RequestBody Customer c) {
        customerMapper.insert(c);
        return R.ok();
    }

    @PutMapping("/customer")
    public R<Void> updateCustomer(@RequestBody Customer c) {
        customerMapper.updateById(c);
        return R.ok();
    }

    @DeleteMapping("/customer/{id}")
    public R<Void> deleteCustomer(@PathVariable String id) {
        customerMapper.deleteById(id);
        return R.ok();
    }

    // ===== 供应商 =====
    @GetMapping("/supplier/page")
    public R<Page<Supplier>> supplierPage(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Supplier> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Supplier::getSupplierName, keyword).or().like(Supplier::getSupplierCode, keyword);
        }
        return R.ok(supplierMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/supplier/list")
    public R<List<Supplier>> supplierList() {
        return R.ok(supplierMapper.selectList(null));
    }

    @PostMapping("/supplier")
    public R<Void> createSupplier(@RequestBody Supplier s) {
        supplierMapper.insert(s);
        return R.ok();
    }

    @PutMapping("/supplier")
    public R<Void> updateSupplier(@RequestBody Supplier s) {
        supplierMapper.updateById(s);
        return R.ok();
    }

    @DeleteMapping("/supplier/{id}")
    public R<Void> deleteSupplier(@PathVariable String id) {
        supplierMapper.deleteById(id);
        return R.ok();
    }

    // ===== 产品 =====
    @GetMapping("/product/page")
    public R<Page<Product>> productPage(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize,
                                        @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Product::getProductName, keyword).or().like(Product::getProductCode, keyword);
        }
        return R.ok(productMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/product/list")
    public R<List<Product>> productList() {
        return R.ok(productMapper.selectList(null));
    }

    @PostMapping("/product")
    public R<Void> createProduct(@RequestBody Product p) {
        productMapper.insert(p);
        return R.ok();
    }

    @PutMapping("/product")
    public R<Void> updateProduct(@RequestBody Product p) {
        productMapper.updateById(p);
        return R.ok();
    }

    @DeleteMapping("/product/{id}")
    public R<Void> deleteProduct(@PathVariable String id) {
        productMapper.deleteById(id);
        return R.ok();
    }

    // ===== 物料 =====
    @GetMapping("/material/page")
    public R<Page<Material>> materialPage(@RequestParam(defaultValue = "1") long pageNum,
                                          @RequestParam(defaultValue = "10") long pageSize,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Material> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Material::getMaterialName, keyword).or().like(Material::getMaterialCode, keyword);
        }
        return R.ok(materialMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/material/list")
    public R<List<Material>> materialList() {
        return R.ok(materialMapper.selectList(null));
    }

    @PostMapping("/material")
    public R<Void> createMaterial(@RequestBody Material m) {
        materialMapper.insert(m);
        return R.ok();
    }

    @PutMapping("/material")
    public R<Void> updateMaterial(@RequestBody Material m) {
        materialMapper.updateById(m);
        return R.ok();
    }

    @DeleteMapping("/material/{id}")
    public R<Void> deleteMaterial(@PathVariable String id) {
        materialMapper.deleteById(id);
        return R.ok();
    }

    // ===== 工序 =====
    @GetMapping("/process/page")
    public R<Page<Process>> processPage(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize,
                                        @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Process> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(Process::getProcessName, keyword).or().like(Process::getProcessCode, keyword);
        }
        qw.orderByAsc(Process::getSeq);
        return R.ok(processMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/process/list")
    public R<List<Process>> processList() {
        return R.ok(processMapper.selectList(new LambdaQueryWrapper<Process>().orderByAsc(Process::getSeq)));
    }

    @PostMapping("/process")
    public R<Void> createProcess(@RequestBody Process p) {
        processMapper.insert(p);
        return R.ok();
    }

    @PutMapping("/process")
    public R<Void> updateProcess(@RequestBody Process p) {
        processMapper.updateById(p);
        return R.ok();
    }

    @DeleteMapping("/process/{id}")
    public R<Void> deleteProcess(@PathVariable String id) {
        processMapper.deleteById(id);
        return R.ok();
    }

    // ===== BOM =====
    @GetMapping("/bom/page")
    public R<Page<Bom>> bomPage(@RequestParam(defaultValue = "1") long pageNum,
                                @RequestParam(defaultValue = "10") long pageSize,
                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Bom> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            List<String> productIds = productMapper.selectList(
                            new LambdaQueryWrapper<Product>()
                                    .like(Product::getProductName, keyword)
                                    .or().like(Product::getProductCode, keyword))
                    .stream().map(Product::getProductId).toList();
            if (!productIds.isEmpty()) {
                qw.in(Bom::getProductId, productIds);
            } else {
                qw.eq(Bom::getProductId, "-1");
            }
        }
        qw.orderByDesc(Bom::getCreateTime);
        Page<Bom> page = bomMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        fillProductName(page.getRecords());
        return R.ok(page);
    }

    @GetMapping("/bom/{id}")
    public R<Bom> bomGet(@PathVariable String id) {
        Bom bom = bomMapper.selectById(id);
        if (bom != null) {
            fillProductName(List.of(bom));
            List<BomItem> items = bomItemMapper.selectList(
                    new LambdaQueryWrapper<BomItem>().eq(BomItem::getBomId, id));
            fillMaterialName(items);
            bom.setItems(items);
        }
        return R.ok(bom);
    }

    @PostMapping("/bom")
    public R<Bom> createBom(@RequestBody Bom bom) {
        bomMapper.insert(bom);
        return R.ok(bom);
    }

    @PutMapping("/bom")
    public R<Void> updateBom(@RequestBody Bom bom) {
        bomMapper.updateById(bom);
        return R.ok();
    }

    @DeleteMapping("/bom/{id}")
    public R<Void> deleteBom(@PathVariable String id) {
        bomMapper.deleteById(id);
        return R.ok();
    }

    @GetMapping("/bom/{bomId}/items")
    public R<List<BomItem>> bomItems(@PathVariable String bomId) {
        return R.ok(bomItemMapper.selectList(new LambdaQueryWrapper<BomItem>().eq(BomItem::getBomId, bomId)));
    }

    @PostMapping("/bom/item")
    public R<Void> createBomItem(@RequestBody BomItem item) {
        bomItemMapper.insert(item);
        return R.ok();
    }

    @PutMapping("/bom/item")
    public R<Void> updateBomItem(@RequestBody BomItem item) {
        bomItemMapper.updateById(item);
        return R.ok();
    }

    @DeleteMapping("/bom/item/{id}")
    public R<Void> deleteBomItem(@PathVariable String id) {
        bomItemMapper.deleteById(id);
        return R.ok();
    }

    // ===== 内部工具 =====
    private void fillProductName(List<Bom> list) {
        List<String> ids = list.stream().map(Bom::getProductId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) return;
        Map<String, Product> map = productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        list.forEach(b -> {
            Product p = map.get(b.getProductId());
            if (p != null) b.setProductName(p.getProductCode() + " " + p.getProductName());
        });
    }

    private void fillMaterialName(List<BomItem> items) {
        List<String> ids = items.stream().map(BomItem::getMaterialId)
                .filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) return;
        Map<String, Material> map = materialMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Material::getMaterialId, Function.identity()));
        items.forEach(i -> {
            Material m = map.get(i.getMaterialId());
            if (m != null) i.setMaterialName(m.getMaterialName());
        });
    }
}
