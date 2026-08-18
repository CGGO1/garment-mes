package com.garment.mes.seed;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import com.garment.mes.production.entity.Cutting;
import com.garment.mes.production.entity.ProductionReport;
import com.garment.mes.production.entity.ScanRecord;
import com.garment.mes.production.entity.WorkOrder;
import com.garment.mes.production.entity.WorkOrderItem;
import com.garment.mes.production.mapper.CuttingMapper;
import com.garment.mes.production.mapper.ProductionReportMapper;
import com.garment.mes.production.mapper.ScanRecordMapper;
import com.garment.mes.production.mapper.WorkOrderItemMapper;
import com.garment.mes.production.mapper.WorkOrderMapper;
import com.garment.mes.quality.entity.QcDefect;
import com.garment.mes.quality.entity.QcInspection;
import com.garment.mes.quality.entity.QcInspectionItem;
import com.garment.mes.quality.entity.QcStandard;
import com.garment.mes.quality.mapper.QcDefectMapper;
import com.garment.mes.quality.mapper.QcInspectionItemMapper;
import com.garment.mes.quality.mapper.QcInspectionMapper;
import com.garment.mes.quality.mapper.QcStandardMapper;
import com.garment.mes.system.entity.SysDictData;
import com.garment.mes.system.entity.SysDictType;
import com.garment.mes.system.entity.SysRole;
import com.garment.mes.system.entity.SysRoleMenu;
import com.garment.mes.system.entity.SysUser;
import com.garment.mes.system.mapper.SysDictDataMapper;
import com.garment.mes.system.mapper.SysDictTypeMapper;
import com.garment.mes.system.mapper.SysMenuMapper;
import com.garment.mes.system.mapper.SysRoleMapper;
import com.garment.mes.system.mapper.SysRoleMenuMapper;
import com.garment.mes.system.mapper.SysUserMapper;
import com.garment.mes.equipment.entity.Equipment;
import com.garment.mes.equipment.entity.EquipmentMaintenance;
import com.garment.mes.equipment.entity.EquipmentScan;
import com.garment.mes.equipment.mapper.EquipmentMaintenanceMapper;
import com.garment.mes.equipment.mapper.EquipmentMapper;
import com.garment.mes.equipment.mapper.EquipmentScanMapper;
import com.garment.mes.trade.entity.Shipment;
import com.garment.mes.trade.entity.TradeDocument;
import com.garment.mes.trade.entity.TradeOrder;
import com.garment.mes.trade.entity.TradeOrderItem;
import com.garment.mes.trade.mapper.ShipmentMapper;
import com.garment.mes.trade.mapper.TradeDocumentMapper;
import com.garment.mes.trade.mapper.TradeOrderItemMapper;
import com.garment.mes.trade.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 演示数据种子：按页面维度为每个核心业务表生成 50 条真实感数据。
 * 仅在表为空时执行，可重复启动不重复插入。
 */
@Slf4j
@Component
public class DataSeed implements CommandLineRunner {

    private final Random rnd = new Random(20260817);

    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;
    private final MaterialMapper materialMapper;
    private final ProcessMapper processMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderItemMapper workOrderItemMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockMapper stockMapper;
    private final InboundMapper inboundMapper;
    private final InboundItemMapper inboundItemMapper;
    private final OutboundMapper outboundMapper;
    private final OutboundItemMapper outboundItemMapper;
    private final StockLogMapper stockLogMapper;
    private final QcStandardMapper qcStandardMapper;
    private final QcInspectionMapper qcInspectionMapper;
    private final QcInspectionItemMapper qcInspectionItemMapper;
    private final QcDefectMapper qcDefectMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final PasswordEncoder passwordEncoder;
    // 7 张 P0 业务表（M3 时未造）
    private final SupplierMapper supplierMapper;
    private final BomMapper bomMapper;
    private final BomItemMapper bomItemMapper;
    private final TradeDocumentMapper documentMapper;
    private final ShipmentMapper shipmentMapper;
    private final CuttingMapper cuttingMapper;
    private final ProductionReportMapper reportMapper;
    // 扫码 + 设备（M4 扩展）
    private final ScanRecordMapper scanRecordMapper;
    private final EquipmentMapper equipmentMapper;
    private final EquipmentMaintenanceMapper equipmentMaintenanceMapper;
    private final EquipmentScanMapper equipmentScanMapper;

    public DataSeed(CustomerMapper customerMapper, ProductMapper productMapper, MaterialMapper materialMapper,
                    ProcessMapper processMapper, TradeOrderMapper tradeOrderMapper,
                    TradeOrderItemMapper tradeOrderItemMapper, WorkOrderMapper workOrderMapper,
                    WorkOrderItemMapper workOrderItemMapper, WarehouseMapper warehouseMapper, StockMapper stockMapper,
                    InboundMapper inboundMapper, InboundItemMapper inboundItemMapper, OutboundMapper outboundMapper,
                    OutboundItemMapper outboundItemMapper, StockLogMapper stockLogMapper,
                    QcStandardMapper qcStandardMapper, QcInspectionMapper qcInspectionMapper,
                    QcInspectionItemMapper qcInspectionItemMapper, QcDefectMapper qcDefectMapper,
                    SysUserMapper userMapper, SysRoleMapper roleMapper, SysDictTypeMapper dictTypeMapper,
                    SysDictDataMapper dictDataMapper, PasswordEncoder passwordEncoder,
                    SupplierMapper supplierMapper, BomMapper bomMapper, BomItemMapper bomItemMapper,
                    TradeDocumentMapper documentMapper, ShipmentMapper shipmentMapper,
                    CuttingMapper cuttingMapper, ProductionReportMapper reportMapper,
                    SysRoleMenuMapper roleMenuMapper, SysMenuMapper menuMapper,
                    ScanRecordMapper scanRecordMapper,
                    EquipmentMapper equipmentMapper, EquipmentMaintenanceMapper equipmentMaintenanceMapper,
                    EquipmentScanMapper equipmentScanMapper) {
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
        this.materialMapper = materialMapper;
        this.processMapper = processMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeOrderItemMapper = tradeOrderItemMapper;
        this.workOrderMapper = workOrderMapper;
        this.workOrderItemMapper = workOrderItemMapper;
        this.warehouseMapper = warehouseMapper;
        this.stockMapper = stockMapper;
        this.inboundMapper = inboundMapper;
        this.inboundItemMapper = inboundItemMapper;
        this.outboundMapper = outboundMapper;
        this.outboundItemMapper = outboundItemMapper;
        this.stockLogMapper = stockLogMapper;
        this.qcStandardMapper = qcStandardMapper;
        this.qcInspectionMapper = qcInspectionMapper;
        this.qcInspectionItemMapper = qcInspectionItemMapper;
        this.qcDefectMapper = qcDefectMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
        this.passwordEncoder = passwordEncoder;
        this.supplierMapper = supplierMapper;
        this.bomMapper = bomMapper;
        this.bomItemMapper = bomItemMapper;
        this.documentMapper = documentMapper;
        this.shipmentMapper = shipmentMapper;
        this.cuttingMapper = cuttingMapper;
        this.reportMapper = reportMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.scanRecordMapper = scanRecordMapper;
        this.equipmentMapper = equipmentMapper;
        this.equipmentMaintenanceMapper = equipmentMaintenanceMapper;
        this.equipmentScanMapper = equipmentScanMapper;
    }

    // ====================== 数据池 ======================
    private static final String[] CITIES = {"嘉兴", "杭州", "上海", "苏州", "宁波", "广州", "深圳", "青岛", "南通", "无锡", "绍兴", "温州", "东莞", "泉州"};
    private static final String[] TRADE_WORDS = {"纺织", "服饰", "服装", "针织", "进出口", "贸易", "工贸", "实业", "时装", "羽绒"};
    private static final String[] SUFFIX = {"有限公司", "股份有限公司"};
    private static final String[] COUNTRIES = {"美国", "德国", "日本", "英国", "法国", "澳大利亚", "加拿大", "西班牙", "意大利", "韩国", "荷兰", "墨西哥", "中国"};
    private static final String[] PRODUCT_NAMES = {"男士衬衫", "女士连衣裙", "儿童T恤", "羽绒服", "连帽卫衣", "西服套装", "牛仔裤", "针织开衫", "风衣", "Polo衫", "睡衣套装", "保暖内衣", "工装裤", "夹克外套", "羊毛毛衣", "背带裤", "半身裙", "运动套装", "沙滩短裤", "校服制服"};
    private static final String[] PRODUCT_CATS = {"上装", "下装", "外套", "内衣", "童装", "制服", "针织", "羽绒"};
    private static final String[] MATERIAL_NAMES = {"纯棉平纹布", "涤棉斜纹布", "弹力府绸", "雪纺纱", "羊毛呢", "牛仔布", "双面针织布", "牛津纺", "摇粒绒", "天丝棉", "人棉印花布", "麂皮绒", "灯芯绒", "网眼布", "复合面料"};
    private static final String[] COLORS = {"本白", "藏青", "经典黑", "浅灰", "卡其", "酒红", "墨绿", "宝蓝", "米色", "驼色"};
    private static final String[] MATERIAL_TYPES = {"面料", "辅料", "里料", "填充物", "印花料"};
    private static final String[] PROCESS_NAMES = {"裁剪", "车缝", "锁眼钉扣", "整烫", "质检", "包装", "绣花", "印花", "水洗", "检验", "绗缝", "压胶"};
    private static final String[] QC_STD_NAMES = {"AQL1.0通用成衣检验", "AQL1.5针织服装", "AQL2.5外套类", "AQL4.0辅料检验", "羽绒服充绒检验", "牛仔水洗检验", "童装安全检验", "内衣缝制检验", "西装外观检验", "针织衫外观检验"};
    private static final String[] INSPECTORS = {"张伟", "李娜", "王芳", "刘洋", "陈静", "杨磊", "赵敏", "周强", "孙莉", "吴昊"};
    private static final String[] DEFECT_TYPES = {"破洞", "污渍", "针距不均", "跳线", "色差", "尺寸偏差", "纽扣脱落", "起球", "抽丝", "烫黄", "绱领不正", "里料外露"};
    private static final String[] ORDER_STATUSES = {"DRAFT", "PENDING", "IN_PRODUCTION", "QC", "SHIPPED", "COMPLETED"};
    private static final String[] WO_STATUSES = {"PLANNED", "IN_PRODUCTION", "COMPLETED"};
    private static final String[] INCOTERMS = {"FOB", "CIF", "CFR", "EXW", "FCA", "DAP", "DDP"};
    private static final String[] CURRENCIES = {"USD", "EUR", "CNY", "GBP"};
    private static final String[] QC_TYPES = {"IQC", "IPQC", "FQC", "OQC", "QA"};
    private static final String[] QC_RESULTS = {"PENDING", "PASS", "FAIL"};
    private static final String[] CHECK_ITEMS = {"外观", "尺寸", "缝制", "辅料", "包装", "水洗标", "色牢度", "钮扣"};
    private static final String[] SEVERITIES = {"MINOR", "MAJOR", "CRITICAL"};
    private static final String[] SIZES = {"S", "M", "L", "XL", "XXL"};
    private static final String[] INBOUND_TYPES = {"采购入库", "生产入库", "退料入库", "调拨入库", "退货入库"};
    private static final String[] OUTBOUND_TYPES = {"生产领料", "销售出库", "调拨出库", "报废出库", "样品出库"};
    private static final String[] USER_NAMES = {
            "王志强", "李秀英", "张建国", "刘桂兰", "陈晓东", "杨丽华", "赵磊", "黄敏", "周文斌", "吴佳",
            "徐宏伟", "孙倩", "马俊杰", "朱琳", "胡兵", "郭婷", "林峰", "何雅静", "高翔", "罗雪",
            "郑凯", "梁静", "宋涛", "唐燕", "韩雪松", "冯洁", "董磊", "程菲", "曹宇", "邓丽",
            "蒋勤", "沈浩", "彭勇", "潘虹", "袁媛", "蔡明", "余波", "杜娟", "叶枫", "钟丽",
            "田刚", "范晓", "方婷", "石磊", "姚远", "谭静", "廖辉", "邹颖", "熊伟", "陆敏"
    };

    private static final int N = 50;

    @Override
    public void run(String... args) {
        ensureAdminRole();
        List<String> warehouseIds = seedWarehouse();
        List<String> customerIds = seedCustomer();
        List<String> supplierIds = seedSupplier();
        List<String> productIds = seedProduct();
        List<String> materialIds = seedMaterial();
        List<String> processIds = seedProcess();
        List<String> standardIds = seedQcStandard();
        List<TradeOrder> orders = seedTradeOrder(customerIds, productIds);
        List<String> bomIds = seedBom(productIds, materialIds);
        seedTradeDocument(orders);
        seedShipment(orders);
        List<WorkOrder> workOrders = seedWorkOrder(orders, productIds, processIds);
        seedCutting(workOrders);
        seedReport(workOrders, processIds);
        seedStock(warehouseIds, materialIds);
        seedInbound(warehouseIds, materialIds);
        seedOutbound(warehouseIds, materialIds);
        seedQcInspection(orders, workOrders, standardIds);
        seedUser();
        seedRole();
        seedScanRecord(workOrders);
        List<String> equipmentIds = seedEquipment();
        seedEquipmentMaintenance(equipmentIds);
        seedEquipmentScan(equipmentIds);
        seedDict();
        seedRoleMenu();
        log.info("MES 演示数据初始化完成（全部业务页面 ≥50 条演示数据，含明细）");
    }

    // ====================== 工具 ======================
    private String id() { return IdWorker.getIdStr(); }

    private String pick(String[] arr) { return arr[rnd.nextInt(arr.length)]; }

    private <T> T pick(List<T> list) { return list.get(rnd.nextInt(list.size())); }

    private int rand(int min, int max) { return min + rnd.nextInt(max - min + 1); }

    private BigDecimal bd(double v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP); }

    private LocalDateTime daysAgo(int max) { return LocalDateTime.now().minusDays(rnd.nextInt(Math.max(1, max))); }

    private boolean isEmpty(com.baomidou.mybatisplus.core.mapper.BaseMapper<?> mapper) {
        return mapper.selectCount(null) == 0;
    }

    /** 已有编码集合（用于幂等补数，避免重复编码） */
    private Set<String> codeSet(List<String> codes) {
        return new HashSet<>(codes);
    }

    private void ensureAdminRole() {
        if (roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "admin")) == 0) {
            SysRole role = new SysRole();
            role.setRoleId("role-admin");
            role.setRoleName("超级管理员");
            role.setRoleCode("admin");
            role.setRemark("系统内置超级管理员");
            roleMapper.insert(role);
        }
    }

    // ====================== 仓库 ======================
    private List<String> seedWarehouse() {
        List<String> ids = new ArrayList<>();
        if (!isEmpty(warehouseMapper)) {
            return warehouseMapper.selectList(null).stream().map(Warehouse::getWarehouseId).toList();
        }
        String[] wh = {"主仓库", "面辅料仓", "成品仓", "外发仓", "次品仓"};
        String[] loc = {"A区-01", "B区-02", "C区-03", "D区-04", "E区-05"};
        for (int i = 0; i < wh.length; i++) {
            Warehouse w = new Warehouse();
            w.setWarehouseId(id());
            w.setWarehouseCode("WH" + String.format("%02d", i + 1));
            w.setWarehouseName(wh[i]);
            w.setLocation(loc[i]);
            w.setRemark("演示仓库");
            warehouseMapper.insert(w);
            ids.add(w.getWarehouseId());
        }
        return ids;
    }

    // ====================== 客户 ======================
    private List<String> seedCustomer() {
        List<Customer> existing = customerMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Customer::getCustomerCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "C" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            Customer c = new Customer();
            c.setCustomerId(id());
            c.setCustomerCode(code);
            String city = CITIES[rnd.nextInt(CITIES.length)];
            String trade = TRADE_WORDS[rnd.nextInt(TRADE_WORDS.length)];
            c.setCustomerName(city + trade + SUFFIX[rnd.nextInt(SUFFIX.length)]);
            c.setContactPerson(pick(USER_NAMES));
            c.setPhone("1" + (rnd.nextInt(900000000) + 300000000));
            c.setEmail("cust" + i + "@example.com");
            c.setCountry(pick(COUNTRIES));
            c.setAddress(c.getCountry() + " " + city + " 工业区" + rand(1, 99) + "号");
            c.setRemark("演示客户");
            customerMapper.insert(c);
        }
        return customerMapper.selectList(null).stream().map(Customer::getCustomerId).toList();
    }

    // ====================== 产品 ======================
    private List<String> seedProduct() {
        List<Product> existing = productMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Product::getProductCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "P" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            Product p = new Product();
            p.setProductId(id());
            p.setProductCode(code);
            p.setProductName(PRODUCT_NAMES[i % PRODUCT_NAMES.length] + " " + pick(COLORS));
            p.setCategory(PRODUCT_CATS[rnd.nextInt(PRODUCT_CATS.length)]);
            p.setHsCode("61" + rand(10, 99) + "." + rand(10, 99));
            p.setComposition(pick(new String[]{"100%棉", "65%涤35%棉", "95%棉5%氨纶", "100%涤纶", "70%羊毛30%涤"}));
            p.setGsm(bd(rand(80, 320)));
            p.setWidth(rand(110, 180) + "cm");
            p.setUnit("件");
            p.setRemark("演示产品");
            productMapper.insert(p);
        }
        return productMapper.selectList(null).stream().map(Product::getProductId).toList();
    }

    // ====================== 物料 ======================
    private List<String> seedMaterial() {
        List<Material> existing = materialMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Material::getMaterialCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "M" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            Material m = new Material();
            m.setMaterialId(id());
            m.setMaterialCode(code);
            m.setMaterialName(MATERIAL_NAMES[i % MATERIAL_NAMES.length]);
            m.setMaterialType(pick(MATERIAL_TYPES));
            m.setColor(pick(COLORS));
            m.setSpec(rand(110, 180) + "cm " + rand(80, 320) + "g");
            m.setUnit(pick(new String[]{"米", "kg", "码", "卷"}));
            m.setRemark("演示物料");
            materialMapper.insert(m);
        }
        return materialMapper.selectList(null).stream().map(Material::getMaterialId).toList();
    }

    // ====================== 工序 ======================
    private List<String> seedProcess() {
        List<Process> existing = processMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Process::getProcessCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "PR" + String.format("%03d", i);
            if (codes.contains(code)) continue;
            Process p = new Process();
            p.setProcessId(id());
            p.setProcessCode(code);
            p.setProcessName(PROCESS_NAMES[i % PROCESS_NAMES.length]);
            p.setSeq(i);
            p.setPrice(bd(rand(150, 1500) / 100.0));
            p.setRemark("演示工序");
            processMapper.insert(p);
        }
        return processMapper.selectList(null).stream().map(Process::getProcessId).toList();
    }

    // ====================== 质检标准 ======================
    private List<String> seedQcStandard() {
        List<QcStandard> existing = qcStandardMapper.selectList(null);
        Set<String> names = codeSet(existing.stream().map(QcStandard::getStandardName)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String name = QC_STD_NAMES[i % QC_STD_NAMES.length] + " 第" + i + "版";
            if (names.contains(name)) continue;
            QcStandard s = new QcStandard();
            s.setStandardId(id());
            s.setStandardName(name);
            s.setAqlLevel(pick(new String[]{"1.0", "1.5", "2.5", "4.0"}));
            s.setDescription("依据 AQL 抽样标准对成衣进行" + pick(CHECK_ITEMS) + "检验");
            s.setRemark("演示标准");
            qcStandardMapper.insert(s);
        }
        return qcStandardMapper.selectList(null).stream().map(QcStandard::getStandardId).toList();
    }

    // ====================== 贸易订单 ======================
    private List<TradeOrder> seedTradeOrder(List<String> customerIds, List<String> productIds) {
        List<TradeOrder> orders = new ArrayList<>();
        if (!isEmpty(tradeOrderMapper)) {
            return tradeOrderMapper.selectList(null);
        }
        for (int i = 1; i <= N; i++) {
            TradeOrder o = new TradeOrder();
            o.setOrderId(id());
            String type = rnd.nextBoolean() ? "EXPORT" : "IMPORT";
            o.setOrderType(type);
            o.setOrderNo((type.equals("EXPORT") ? "SO" : "PO") + "2026" + String.format("%04d", i));
            o.setCustomerId(pick(customerIds));
            o.setIncoterm(pick(INCOTERMS));
            o.setCurrency(pick(CURRENCIES));
            LocalDateTime od = daysAgo(180);
            o.setOrderDate(od);
            o.setDeliveryDate(od.plusDays(rand(20, 60)));
            o.setStatus(pick(ORDER_STATUSES));
            tradeOrderMapper.insert(o);

            // 明细 3-5 条
            int itemCount = rand(3, 5);
            BigDecimal total = BigDecimal.ZERO;
            for (int j = 1; j <= itemCount; j++) {
                TradeOrderItem it = new TradeOrderItem();
                it.setItemId(id());
                it.setOrderId(o.getOrderId());
                it.setProductId(pick(productIds));
                BigDecimal qty = bd(rand(100, 2000));
                BigDecimal price = bd(rand(500, 20000) / 100.0);
                it.setQty(qty);
                it.setPrice(price);
                it.setAmount(qty.multiply(price).setScale(2, RoundingMode.HALF_UP));
                it.setSize(pick(SIZES));
                it.setRemark("演示明细");
                tradeOrderItemMapper.insert(it);
                total = total.add(it.getAmount());
            }
            o.setTotalAmount(total);
            tradeOrderMapper.updateById(o);
            orders.add(o);
        }
        return orders;
    }

    // ====================== 生产工单 ======================
    private List<WorkOrder> seedWorkOrder(List<TradeOrder> orders, List<String> productIds, List<String> processIds) {
        List<WorkOrder> workOrders = new ArrayList<>();
        if (!isEmpty(workOrderMapper)) {
            return workOrderMapper.selectList(null);
        }
        for (int i = 1; i <= N; i++) {
            WorkOrder wo = new WorkOrder();
            wo.setWorkOrderId(id());
            wo.setWorkOrderNo("WO2026" + String.format("%04d", i));
            TradeOrder order = pick(orders);
            wo.setOrderId(order.getOrderId());
            wo.setProductId(pick(productIds));
            BigDecimal planQty = bd(rand(200, 3000));
            wo.setPlanQty(planQty);
            String status = pick(WO_STATUSES);
            wo.setStatus(status);
            BigDecimal finishQty;
            if ("COMPLETED".equals(status)) finishQty = planQty;
            else if ("IN_PRODUCTION".equals(status)) finishQty = planQty.multiply(bd(rand(30, 90) / 100.0)).setScale(0, RoundingMode.HALF_UP);
            else finishQty = BigDecimal.ZERO;
            wo.setFinishQty(finishQty);
            LocalDateTime sd = daysAgo(120);
            wo.setStartDate(sd);
            wo.setEndDate(sd.plusDays(rand(10, 45)));
            wo.setRemark("演示工单");
            workOrderMapper.insert(wo);

            // 工序明细 2-4 条
            int pc = rand(2, 4);
            for (int j = 1; j <= pc; j++) {
                WorkOrderItem wi = new WorkOrderItem();
                wi.setItemId(id());
                wi.setWorkOrderId(wo.getWorkOrderId());
                String pid = pick(processIds);
                wi.setProcessId(pid);
                Process p = processMapper.selectById(pid);
                wi.setProcessName(p != null ? p.getProcessName() : "工序");
                wi.setPlanQty(planQty);
                wi.setSeq(j);
                String pstatus = "COMPLETED".equals(status) ? "DONE" : (rnd.nextBoolean() ? "DOING" : "PENDING");
                wi.setStatus(pstatus);
                workOrderItemMapper.insert(wi);
            }
            workOrders.add(wo);
        }
        return workOrders;
    }

    // ====================== 库存 ======================
    private void seedStock(List<String> warehouseIds, List<String> materialIds) {
        if (warehouseIds.isEmpty() || materialIds.isEmpty()) return;
        int existing = stockMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            Stock s = new Stock();
            s.setStockId(id());
            s.setWarehouseId(pick(warehouseIds));
            s.setMaterialId(pick(materialIds));
            s.setQty(bd(rand(50, 5000)));
            s.setUpdateTime(daysAgo(30));
            stockMapper.insert(s);
        }
    }

    // ====================== 入库单 + 明细 + 流水 ======================
    private void seedInbound(List<String> warehouseIds, List<String> materialIds) {
        if (!isEmpty(inboundMapper)) return;
        if (warehouseIds.isEmpty() || materialIds.isEmpty()) return;
        for (int i = 1; i <= N; i++) {
            Inbound ib = new Inbound();
            ib.setInboundId(id());
            ib.setInboundNo("IN2026" + String.format("%04d", i));
            ib.setWarehouseId(pick(warehouseIds));
            ib.setInboundType(pick(INBOUND_TYPES));
            ib.setSourceNo(pick(new String[]{"PO" + rand(1000, 9999), "WO" + rand(1000, 9999), "RET" + rand(100, 999)}));
            ib.setInboundDate(daysAgo(90));
            ib.setRemark("演示入库");
            inboundMapper.insert(ib);

            // 明细 2-5 条
            int ic = rand(2, 5);
            BigDecimal totalQty = BigDecimal.ZERO;
            for (int j = 1; j <= ic; j++) {
                InboundItem it = new InboundItem();
                it.setItemId(id());
                it.setInboundId(ib.getInboundId());
                String mid = pick(materialIds);
                it.setMaterialId(mid);
                BigDecimal qty = bd(rand(50, 3000));
                it.setQty(qty);
                it.setRemark("入库明细");
                inboundItemMapper.insert(it);
                totalQty = totalQty.add(qty);

                // 库存流水
                StockLog log = new StockLog();
                log.setLogId(id());
                log.setWarehouseId(ib.getWarehouseId());
                log.setMaterialId(mid);
                log.setChangeType("IN");
                log.setChangeQty(qty);
                log.setBalanceQty(qty); // 简化：不追踪累计余额
                log.setBizNo(ib.getInboundNo());
                log.setLogTime(ib.getInboundDate());
                stockLogMapper.insert(log);
            }
        }
    }

    // ====================== 出库单 + 明细 + 流水 ======================
    private void seedOutbound(List<String> warehouseIds, List<String> materialIds) {
        if (!isEmpty(outboundMapper)) return;
        if (warehouseIds.isEmpty() || materialIds.isEmpty()) return;
        for (int i = 1; i <= N; i++) {
            Outbound ob = new Outbound();
            ob.setOutboundId(id());
            ob.setOutboundNo("OUT2026" + String.format("%04d", i));
            ob.setWarehouseId(pick(warehouseIds));
            ob.setOutboundType(pick(OUTBOUND_TYPES));
            ob.setSourceNo(pick(new String[]{"SO" + rand(1000, 9999), "WO" + rand(1000, 9999), "SCRAP" + rand(100, 999)}));
            ob.setOutboundDate(daysAgo(60));
            ob.setRemark("演示出库");
            outboundMapper.insert(ob);

            // 明细 2-5 条
            int oc = rand(2, 5);
            for (int j = 1; j <= oc; j++) {
                OutboundItem ot = new OutboundItem();
                ot.setItemId(id());
                ot.setOutboundId(ob.getOutboundId());
                String mid = pick(materialIds);
                ot.setMaterialId(mid);
                BigDecimal qty = bd(rand(20, 1500));
                ot.setQty(qty);
                ot.setRemark("出库明细");
                outboundItemMapper.insert(ot);

                // 库存流水
                StockLog log = new StockLog();
                log.setLogId(id());
                log.setWarehouseId(ob.getWarehouseId());
                log.setMaterialId(mid);
                log.setChangeType("OUT");
                log.setChangeQty(qty.negate());
                log.setBalanceQty(BigDecimal.ZERO); // 简化
                log.setBizNo(ob.getOutboundNo());
                log.setLogTime(ob.getOutboundDate());
                stockLogMapper.insert(log);
            }
        }
    }

    // ====================== 质检 ======================
    private void seedQcInspection(List<TradeOrder> orders, List<WorkOrder> workOrders, List<String> standardIds) {
        if (!isEmpty(qcInspectionMapper)) return;
        if (orders.isEmpty()) orders = tradeOrderMapper.selectList(null);
        if (workOrders.isEmpty()) workOrders = workOrderMapper.selectList(null);
        if (standardIds.isEmpty()) {
            standardIds = qcStandardMapper.selectList(null).stream().map(QcStandard::getStandardId).toList();
        }
        if (orders.isEmpty() || workOrders.isEmpty() || standardIds.isEmpty()) return;
        for (int i = 1; i <= N; i++) {
            QcInspection q = new QcInspection();
            q.setInspectionId(id());
            q.setInspectionNo("QC2026" + String.format("%04d", i));
            q.setOrderId(pick(orders).getOrderId());
            q.setWorkOrderId(pick(workOrders).getWorkOrderId());
            q.setInspectionType(pick(QC_TYPES));
            q.setStandardId(pick(standardIds));
            BigDecimal sample = bd(rand(20, 200));
            q.setSampleQty(sample);
            String result = pick(QC_RESULTS);
            q.setResult(result);
            BigDecimal pass;
            if ("FAIL".equals(result)) {
                pass = sample.multiply(bd(rand(40, 85) / 100.0)).setScale(0, RoundingMode.HALF_UP);
            } else if ("PASS".equals(result)) {
                pass = sample.multiply(bd(rand(90, 100) / 100.0)).setScale(0, RoundingMode.HALF_UP);
            } else {
                pass = BigDecimal.ZERO;
            }
            q.setPassQty(pass);
            q.setFailQty(sample.subtract(pass).setScale(0, RoundingMode.HALF_UP));
            q.setInspectDate(daysAgo(150));
            q.setInspector(pick(INSPECTORS));
            q.setRemark("演示验货");
            qcInspectionMapper.insert(q);

            // 检验项 3-5 条
            int cnt = rand(3, 5);
            for (int j = 1; j <= cnt; j++) {
                QcInspectionItem it = new QcInspectionItem();
                it.setItemId(id());
                it.setInspectionId(q.getInspectionId());
                it.setCheckItem(pick(CHECK_ITEMS));
                boolean ok = rnd.nextDouble() < 0.8;
                it.setCheckResult(ok ? "PASS" : "FAIL");
                it.setDefectQty(ok ? BigDecimal.ZERO : bd(rand(1, 8)));
                it.setRemark("演示检验项");
                qcInspectionItemMapper.insert(it);
            }

            // 不合格则登记缺陷
            if (!"PASS".equals(result)) {
                int dc = rand(1, 3);
                for (int k = 1; k <= dc; k++) {
                    QcDefect d = new QcDefect();
                    d.setDefectId(id());
                    d.setInspectionId(q.getInspectionId());
                    d.setDefectType(pick(DEFECT_TYPES));
                    d.setDefectDesc(pick(DEFECT_TYPES) + "，位于" + pick(new String[]{"左袖", "前襟", "下摆", "领口", "口袋"}));
                    d.setDefectQty(bd(rand(1, 20)));
                    d.setSeverity(pick(SEVERITIES));
                    qcDefectMapper.insert(d);
                }
            }
        }
    }

    // ====================== 字典补充 ======================
    private void seedDict() {
        addDictTypeIfAbsent("产品类别", "product_category", PRODUCT_CATS);
        addDictTypeIfAbsent("面料类型", "fabric_type", MATERIAL_NAMES);
        addDictTypeIfAbsent("缺陷类型", "defect_type", DEFECT_TYPES);
        addDictTypeIfAbsent("订单状态", "order_status", new String[]{
                "DRAFT:草稿", "PENDING:待处理", "IN_PRODUCTION:生产中", "QC:质检中", "SHIPPED:已发货", "COMPLETED:已完成"});
        addDictTypeIfAbsent("工单状态", "wo_status", new String[]{
                "PLANNED:已排产", "IN_PRODUCTION:生产中", "COMPLETED:已完成"});
        addDictTypeIfAbsent("检验类型", "inspection_type", QC_TYPES);
        addDictTypeIfAbsent("检验结果", "qc_result", new String[]{
                "PENDING:待检", "PASS:合格", "FAIL:不合格"});
        addDictTypeIfAbsent("供应商类型", "supplier_type", new String[]{
                "面料", "辅料", "包装", "染料", "服务"});
        addDictTypeIfAbsent("设备类型", "equipment_type", EQ_TYPES);
        addDictTypeIfAbsent("设备状态", "equipment_status", new String[]{
                "NORMAL:正常", "REPAIR:维修中", "SCRAP:已报废", "RENT:租赁"});
        addDictTypeIfAbsent("维护类型", "maintenance_type", new String[]{
                "DAILY:日常点检", "PERIODIC:周期保养", "REPAIR:故障维修", "UPGRADE:升级改造"});
        addDictTypeIfAbsent("维护状态", "maintenance_status", new String[]{
                "PLANNED:待执行", "DOING:进行中", "DONE:已完成", "CANCELLED:已取消"});
        addDictTypeIfAbsent("币种", "currency", new String[]{
                "USD:美元", "EUR:欧元", "CNY:人民币", "GBP:英镑", "JPY:日元", "HKD:港币"});
        addDictTypeIfAbsent("单证类型", "doc_type", new String[]{
                "INVOICE:商业发票", "PACKING:装箱单", "BL:提单", "CO:原产地证",
                "LC:信用证", "PL:装箱明细", "CI:形式发票", "CC:保险单"});
        addDictTypeIfAbsent("出货方式", "container_type", new String[]{
                "FCL20:整柜20尺", "FCL40:整柜40尺", "LCL:拼箱", "AIR:空运", "EXPRESS:快递"});
        addDictTypeIfAbsent("入库类型", "inbound_type", INBOUND_TYPES);
        addDictTypeIfAbsent("出库类型", "outbound_type", OUTBOUND_TYPES);
        addDictTypeIfAbsent("物料单位", "material_unit", new String[]{
                "米", "kg", "码", "卷", "个", "打", "件", "套"});
        addDictTypeIfAbsent("缺陷等级", "severity", new String[]{
                "MINOR:次要", "MAJOR:主要", "CRITICAL:严重"});
        addDictTypeIfAbsent("检验项目", "check_item", CHECK_ITEMS);
        addDictTypeIfAbsent("尺码", "size", new String[]{
                "XS", "S", "M", "L", "XL", "XXL", "XXXL"});
        addDictTypeIfAbsent("订单来源", "order_source", new String[]{
                "展会", "老客户返单", "跨境电商", "业务开发", "代工贴牌", "内销订货"});
        addDictTypeIfAbsent("交期状态", "delivery_status", new String[]{
                "充足", "正常", "紧张", "逾期", "已交期"});
    }

    private void addDictTypeIfAbsent(String name, String type, String[] values) {
        if (dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, type)) > 0) return;
        SysDictType t = new SysDictType();
        t.setDictName(name);
        t.setDictType(type);
        t.setStatus("NORMAL");
        dictTypeMapper.insert(t);
        for (String v : values) {
            String[] kv = v.split(":");
            SysDictData d = new SysDictData();
            d.setDictType(type);
            d.setDictValue(kv[0]);
            d.setDictLabel(kv.length > 1 ? kv[1] : kv[0]);
            d.setStatus("NORMAL");
            dictDataMapper.insert(d);
        }
    }

    // ====================== 用户 ======================
    private void seedUser() {
        List<SysUser> existing = userMapper.selectList(null);
        Set<String> usernames = codeSet(existing.stream().map(SysUser::getUsername)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String username = "user" + String.format("%04d", i);
            if (usernames.contains(username)) continue;
            SysUser u = new SysUser();
            u.setUserId(id());
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("admin123"));
            u.setNickname(USER_NAMES[i - 1]);
            u.setPhone("1" + (rnd.nextInt(900000000) + 300000000));
            u.setEmail("user" + i + "@mes.com");
            u.setRoleId("role-admin");
            u.setStatus(rnd.nextDouble() < 0.9 ? "NORMAL" : "DISABLED");
            userMapper.insert(u);
        }
    }

    // ====================== 角色（50 条，页面演示用） ======================
    private static final String[] ROLE_NAMES = {
            "生产计划员", "车间主任", "裁剪主管", "车缝组长", "后整主管", "包装主管", "质检主管", "QC验货员",
            "面料采购", "辅料采购", "外发跟单", "贸易跟单", "单证员", "关务专员", "仓库主管", "仓管员",
            "设备管理员", "维修技师", "样品开发", "版房主管", "工艺工程师", "IE工程师", "生产统计", "成本会计",
            "应收会计", "应付会计", "总经理", "生产副总", "财务总监", "人事主管", "行政专员", "培训专员",
            "信息化管理员", "数据专员", "大客户经理", "渠道经理", "设计师", "设计助理", "陈列师", "运营专员",
            "门店店长", "导购员", "电商运营", "客服专员", "直播运营", "主播", "市场专员", "品牌经理",
            "合规专员", "法务顾问"
    };

    private void seedRole() {
        List<SysRole> existing = roleMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(SysRole::getRoleCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "role-" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            SysRole r = new SysRole();
            r.setRoleId(id());
            r.setRoleName(ROLE_NAMES[i - 1]);
            r.setRoleCode(code);
            r.setRemark("演示角色：" + ROLE_NAMES[i - 1] + "（可配置菜单权限）");
            roleMapper.insert(r);
        }
    }

    // ====================== P0 7 张表 ======================

    private List<String> seedSupplier() {
        String[] supplierWords = {"布业", "纺织", "辅料", "包装", "印染", "绣花", "线业", "纽扣", "化工", "塑业"};
        String[] supplierTypes = {"面料", "辅料", "包装", "染料", "服务"};
        List<Supplier> existing = supplierMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Supplier::getSupplierCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "S" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            Supplier s = new Supplier();
            s.setSupplierId(id());
            s.setSupplierCode(code);
            String city = pick(CITIES);
            s.setSupplierName(city + pick(supplierWords) + pick(SUFFIX));
            s.setSupplierType(pick(supplierTypes));
            s.setContactPerson(pick(USER_NAMES));
            s.setPhone("1" + (rnd.nextInt(900000000) + 300000000));
            s.setEmail("sup" + i + "@example.com");
            s.setCountry("中国");
            s.setAddress(city + " 工业园" + rand(1, 99) + "号");
            s.setRemark("演示供应商");
            supplierMapper.insert(s);
        }
        return supplierMapper.selectList(null).stream().map(Supplier::getSupplierId).toList();
    }

    private List<String> seedBom(List<String> productIds, List<String> materialIds) {
        // 依赖表已有数据时 seedProduct/seedMaterial 会返回空，兜底从 DB 重新加载
        if (productIds.isEmpty()) productIds = productMapper.selectList(null).stream().map(Product::getProductId).toList();
        if (materialIds.isEmpty()) materialIds = materialMapper.selectList(null).stream().map(Material::getMaterialId).toList();
        if (productIds.isEmpty() || materialIds.isEmpty()) return List.of();
        int existing = bomMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            Bom b = new Bom();
            b.setBomId(id());
            b.setProductId(pick(productIds));
            b.setVersion("V" + (1 + rnd.nextInt(3)) + ".0");
            b.setRemark("演示 BOM " + i);
            bomMapper.insert(b);

            int items = rand(3, 6);
            for (int j = 1; j <= items; j++) {
                BomItem bi = new BomItem();
                bi.setItemId(id());
                bi.setBomId(b.getBomId());
                bi.setMaterialId(pick(materialIds));
                bi.setQty(bd(rand(50, 5000) / 100.0));
                bi.setUnit(pick(new String[]{"米", "kg", "码", "卷"}));
                bi.setRemark("BOM 物料 " + j);
                bomItemMapper.insert(bi);
            }
        }
        return bomMapper.selectList(null).stream().map(Bom::getBomId).toList();
    }

    private void seedTradeDocument(List<TradeOrder> orders) {
        if (!isEmpty(documentMapper)) return;
        String[] docTypes = {"INVOICE", "PACKING", "BL", "CO"};
        for (TradeOrder order : orders) {
            int n = rand(1, 3);
            for (int j = 1; j <= n; j++) {
                TradeDocument d = new TradeDocument();
                d.setDocId(id());
                d.setOrderId(order.getOrderId());
                String type = pick(docTypes);
                d.setDocType(type);
                d.setDocNo(type + "-" + order.getOrderNo() + "-" + j);
                d.setFileUrl("/docs/" + d.getDocNo() + ".pdf");
                d.setRemark("演示单证");
                documentMapper.insert(d);
            }
        }
    }

    private void seedShipment(List<TradeOrder> orders) {
        if (!isEmpty(shipmentMapper)) return;
        for (TradeOrder order : orders) {
            // 仅出口订单、状态已到出货之后，才会有出货记录
            if (!"EXPORT".equals(order.getOrderType())) continue;
            if (rnd.nextDouble() > 0.5) continue;
            int n = rand(1, 2);
            for (int j = 1; j <= n; j++) {
                Shipment s = new Shipment();
                s.setShipmentId(id());
                s.setOrderId(order.getOrderId());
                s.setPortFrom("Shanghai");
                s.setPortTo(pick(COUNTRIES));
                s.setEtd(java.time.LocalDateTime.now().minusDays(rand(5, 60)));
                s.setEta(s.getEtd().plusDays(rand(15, 40)));
                s.setContainer(pick(new String[]{"FCL20", "FCL40", "LCL"}));
                s.setRemark("演示出货");
                shipmentMapper.insert(s);
            }
        }
    }

    private void seedCutting(List<WorkOrder> workOrders) {
        if (!isEmpty(cuttingMapper)) return;
        if (workOrders.isEmpty()) workOrders = workOrderMapper.selectList(null);
        if (workOrders.isEmpty()) return;
        for (int i = 1; i <= N; i++) {
            Cutting c = new Cutting();
            c.setCuttingId(id());
            WorkOrder wo = pick(workOrders);
            c.setWorkOrderId(wo.getWorkOrderId());
            c.setBatchNo("B" + String.format("%04d", i));
            c.setBundleNo("BUN" + String.format("%05d", i));
            c.setFabricUsed(bd(rand(500, 3000) / 10.0));
            c.setCutQty(bd(rand(50, 1500)));
            c.setBarcode("CT" + String.format("%010d", 2026000000L + i));
            c.setRemark("演示裁床");
            cuttingMapper.insert(c);
        }
    }

    private void seedReport(List<WorkOrder> workOrders, List<String> processIds) {
        if (workOrders.isEmpty()) workOrders = workOrderMapper.selectList(null);
        if (processIds.isEmpty()) processIds = processMapper.selectList(null).stream().map(Process::getProcessId).toList();
        if (workOrders.isEmpty() || processIds.isEmpty()) return;
        int existing = reportMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            ProductionReport r = new ProductionReport();
            r.setReportId(id());
            WorkOrder wo = pick(workOrders);
            r.setWorkOrderId(wo.getWorkOrderId());
            r.setProcessId(pick(processIds));
            r.setWorkerName(pick(USER_NAMES));
            r.setReportQty(bd(rand(10, 200)));
            r.setReportTime(wo.getStartDate() != null
                    ? wo.getStartDate().plusDays(rand(1, 20))
                    : daysAgo(30));
            r.setBarcode("RP" + String.format("%010d", 2026000000L + i));
            r.setRemark("演示报工");
            reportMapper.insert(r);
        }
    }

    private void seedRoleMenu() {
        if (!isEmpty(roleMenuMapper)) return;
        List<com.garment.mes.system.entity.SysMenu> menus = menuMapper.selectList(null);
        for (var m : menus) {
            if (!"MENU".equals(m.getMenuType()) && !"DIR".equals(m.getMenuType())) continue;
            SysRoleMenu rm = new SysRoleMenu();
            rm.setId(id());
            rm.setRoleId("role-admin");
            rm.setMenuId(m.getMenuId());
            roleMenuMapper.insert(rm);
        }
    }

    // ====================== M4 扫码 ======================

    private void seedScanRecord(List<WorkOrder> workOrders) {
        if (workOrders.isEmpty()) workOrders = workOrderMapper.selectList(null);
        if (workOrders.isEmpty()) return;
        String[] types = {"CUTTING", "REPORT", "RECEIVE", "TRANSFER", "OQC"};
        List<String> userIds = userMapper.selectList(null).stream().map(SysUser::getUserId).toList();
        int existing = scanRecordMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            ScanRecord s = new ScanRecord();
            s.setScanId(id());
            s.setBarcode("SC" + String.format("%010d", 2026000000L + i));
            s.setScanType(pick(types));
            WorkOrder wo = pick(workOrders);
            s.setWorkOrderId(wo.getWorkOrderId());
            List<com.garment.mes.production.entity.WorkOrderItem> items = workOrderItemMapper.selectList(
                    new LambdaQueryWrapper<WorkOrderItem>().eq(WorkOrderItem::getWorkOrderId, wo.getWorkOrderId()));
            s.setProcessId(items.isEmpty() ? null : pick(items).getProcessId());
            s.setOperatorId(userIds.isEmpty() ? null : pick(userIds));
            s.setScanTime(daysAgo(30));
            s.setScanQty(bd(rand(10, 200)));
            s.setRemark("演示扫码");
            scanRecordMapper.insert(s);
        }
    }

    // ====================== M4 设备管理 ======================

    private static final String[] EQ_TYPES = {"裁剪机", "缝纫机", "绣花机", "包装线", "整烫台", "检验台"};
    private static final String[] EQ_MODELS = {"DJ-9800", "JJ-8200", "XH-5600", "BZ-1500", "ZT-2200", "JY-800"};
    private static final String[] EQ_MANUFACTURERS = {"杰克股份", "上工申贝", "兄弟工业", "飞马工业", "兄弟牌", "瑞立集团"};
    private static final String[] EQ_LOCATIONS = {"A区-01", "B区-02", "C区-03", "D区-04", "E区-05", "F区-06"};
    private static final String[] MT_TYPES = {"DAILY", "PERIODIC", "REPAIR", "UPGRADE"};
    private static final String[] MT_TITLES = {"日常点检", "季度保养", "故障维修", "升级改造", "易损件更换", "润滑保养"};
    private static final String[] EQ_SCAN_TYPES = {"CHECK_IN", "CHECK_OUT", "MAINTAIN", "INSPECT", "SCRAP"};

    private List<String> seedEquipment() {
        List<String> userIds = userMapper.selectList(null).stream().map(SysUser::getUserId).toList();
        List<Equipment> existing = equipmentMapper.selectList(null);
        Set<String> codes = codeSet(existing.stream().map(Equipment::getEquipmentCode)
                .filter(java.util.Objects::nonNull).toList());
        for (int i = 1; i <= N; i++) {
            String code = "EQ" + String.format("%04d", i);
            if (codes.contains(code)) continue;
            Equipment e = new Equipment();
            e.setEquipmentId(id());
            e.setEquipmentCode(code);
            int tIdx = i % EQ_TYPES.length;
            e.setEquipmentName(EQ_TYPES[tIdx] + "-" + String.format("%02d", i));
            e.setEquipmentType(EQ_TYPES[tIdx]);
            e.setModel(pick(EQ_MODELS));
            e.setManufacturer(pick(EQ_MANUFACTURERS));
            e.setPurchaseDate(java.time.LocalDate.now().minusDays(rand(180, 1800)));
            e.setPurchaseAmount(bd(rand(8000, 200000)));
            e.setLocation(pick(EQ_LOCATIONS));
            e.setStatus(rnd.nextDouble() < 0.7 ? "NORMAL" : (rnd.nextDouble() < 0.7 ? "REPAIR" : "SCRAP"));
            e.setManagerId(userIds.isEmpty() ? null : pick(userIds));
            e.setLastScanTime(daysAgo(rnd.nextInt(30)));
            e.setRemark("演示设备");
            equipmentMapper.insert(e);
        }
        return equipmentMapper.selectList(null).stream().map(Equipment::getEquipmentId).toList();
    }

    private void seedEquipmentMaintenance(List<String> equipmentIds) {
        if (equipmentIds.isEmpty()) equipmentIds = equipmentMapper.selectList(null).stream().map(Equipment::getEquipmentId).toList();
        if (equipmentIds.isEmpty()) return;
        String[] mtStatuses = {"PLANNED", "DOING", "DONE", "CANCELLED"};
        List<String> userIds = userMapper.selectList(null).stream().map(SysUser::getUserId).toList();
        int existing = equipmentMaintenanceMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            EquipmentMaintenance m = new EquipmentMaintenance();
            m.setMaintenanceId(id());
            m.setEquipmentId(pick(equipmentIds));
            m.setMaintenanceNo("MT" + String.format("%08d", i));
            m.setMaintenanceType(pick(MT_TYPES));
            m.setTitle(pick(MT_TITLES));
            m.setContent("完成标准 SOP 第 " + rand(1, 50) + " 项；更换关键备件并验证负载。");
            m.setPlanDate(java.time.LocalDate.now().plusDays(rand(-30, 30)));
            m.setStatus(pick(mtStatuses));
            m.setOwnerId(userIds.isEmpty() ? null : pick(userIds));
            m.setCostAmount(bd(rand(200, 8000)));
            m.setRemark("演示维护工单");
            equipmentMaintenanceMapper.insert(m);
        }
    }

    private void seedEquipmentScan(List<String> equipmentIds) {
        if (equipmentIds.isEmpty()) equipmentIds = equipmentMapper.selectList(null).stream().map(Equipment::getEquipmentId).toList();
        if (equipmentIds.isEmpty()) return;
        List<String> userIds = userMapper.selectList(null).stream().map(SysUser::getUserId).toList();
        int existing = equipmentScanMapper.selectCount(null).intValue();
        for (int i = existing + 1; i <= N; i++) {
            EquipmentScan s = new EquipmentScan();
            s.setScanId(id());
            s.setEquipmentId(pick(equipmentIds));
            s.setOperatorId(userIds.isEmpty() ? null : pick(userIds));
            s.setScanType(pick(EQ_SCAN_TYPES));
            s.setScanTime(daysAgo(rnd.nextInt(15)));
            s.setQty(bd(rand(10, 200)));
            s.setRemark("演示设备扫码");
            equipmentScanMapper.insert(s);
        }
    }
}
