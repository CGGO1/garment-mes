-- ============================================================
-- 服装制造业进出口贸易 MES 系统 — 数据库结构
-- 数据库：PostgreSQL 16
-- 命名约定：snake_case 表/列；主键 VARCHAR(32) 雪花ID；逻辑删除 is_delete
-- ============================================================

-- ============ 系统管理 ============
CREATE TABLE IF NOT EXISTS sys_user (
    user_id     VARCHAR(32) PRIMARY KEY,
    username    VARCHAR(64) NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    phone       VARCHAR(32),
    email       VARCHAR(128),
    role_id     VARCHAR(32),
    status      VARCHAR(16) DEFAULT 'NORMAL',
    create_by   VARCHAR(32),
    create_time TIMESTAMP,
    update_by   VARCHAR(32),
    update_time TIMESTAMP,
    is_delete   VARCHAR(1) DEFAULT 'N'
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user(username) WHERE is_delete = 'N';

CREATE TABLE IF NOT EXISTS sys_role (
    role_id     VARCHAR(32) PRIMARY KEY,
    role_name   VARCHAR(64) NOT NULL,
    role_code   VARCHAR(64) NOT NULL,
    remark      VARCHAR(255),
    create_by   VARCHAR(32),
    create_time TIMESTAMP,
    update_by   VARCHAR(32),
    update_time TIMESTAMP,
    is_delete   VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id     VARCHAR(32) PRIMARY KEY,
    parent_id   VARCHAR(32) DEFAULT '0',
    menu_name   VARCHAR(64) NOT NULL,
    menu_type   VARCHAR(16) NOT NULL,
    path        VARCHAR(128),
    component   VARCHAR(128),
    icon        VARCHAR(64),
    sort        INT DEFAULT 0,
    perms       VARCHAR(128),
    visible     VARCHAR(1) DEFAULT 'N',
    create_by   VARCHAR(32),
    create_time TIMESTAMP,
    update_by   VARCHAR(32),
    update_time TIMESTAMP,
    is_delete   VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id      VARCHAR(32) PRIMARY KEY,
    role_id VARCHAR(32),
    menu_id VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id     VARCHAR(32) PRIMARY KEY,
    dict_name   VARCHAR(64),
    dict_type   VARCHAR(64),
    status      VARCHAR(16) DEFAULT 'NORMAL',
    remark      VARCHAR(255),
    create_by   VARCHAR(32),
    create_time TIMESTAMP,
    update_by   VARCHAR(32),
    update_time TIMESTAMP,
    is_delete   VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code   VARCHAR(32) PRIMARY KEY,
    dict_sort   INT DEFAULT 0,
    dict_label  VARCHAR(64),
    dict_value  VARCHAR(64),
    dict_type   VARCHAR(64),
    status      VARCHAR(16) DEFAULT 'NORMAL',
    create_by   VARCHAR(32),
    create_time TIMESTAMP,
    update_by   VARCHAR(32),
    update_time TIMESTAMP,
    is_delete   VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS sys_login_log (
    id          VARCHAR(32) PRIMARY KEY,
    username    VARCHAR(64),
    ip          VARCHAR(64),
    status      VARCHAR(16),
    message     VARCHAR(255),
    login_time  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_oper_log (
    id          VARCHAR(32) PRIMARY KEY,
    username    VARCHAR(64),
    module      VARCHAR(64),
    operation   VARCHAR(128),
    method      VARCHAR(255),
    params      TEXT,
    status      VARCHAR(16),
    cost_ms     BIGINT,
    oper_time   TIMESTAMP
);

-- ============ 主数据 ============
CREATE TABLE IF NOT EXISTS mst_customer (
    customer_id     VARCHAR(32) PRIMARY KEY,
    customer_code   VARCHAR(64),
    customer_name   VARCHAR(128) NOT NULL,
    contact_person  VARCHAR(64),
    phone           VARCHAR(64),
    email           VARCHAR(128),
    country         VARCHAR(64),
    address         VARCHAR(255),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS mst_supplier (
    supplier_id     VARCHAR(32) PRIMARY KEY,
    supplier_code   VARCHAR(64),
    supplier_name   VARCHAR(128) NOT NULL,
    supplier_type   VARCHAR(32),
    contact_person  VARCHAR(64),
    phone           VARCHAR(64),
    email           VARCHAR(128),
    country         VARCHAR(64),
    address         VARCHAR(255),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS mst_product (
    product_id      VARCHAR(32) PRIMARY KEY,
    product_code    VARCHAR(64) NOT NULL,
    product_name    VARCHAR(128),
    category        VARCHAR(32),
    hs_code         VARCHAR(32),
    composition     VARCHAR(128),
    gsm             NUMERIC(10,2),
    width           VARCHAR(32),
    unit            VARCHAR(16),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS mst_material (
    material_id     VARCHAR(32) PRIMARY KEY,
    material_code   VARCHAR(64) NOT NULL,
    material_name   VARCHAR(128),
    material_type   VARCHAR(32),
    color           VARCHAR(64),
    spec            VARCHAR(128),
    unit            VARCHAR(16),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS mst_bom (
    bom_id      VARCHAR(32) PRIMARY KEY,
    product_id  VARCHAR(32),
    version     VARCHAR(32),
    remark      VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS mst_bom_item (
    item_id     VARCHAR(32) PRIMARY KEY,
    bom_id      VARCHAR(32),
    material_id VARCHAR(32),
    qty         NUMERIC(12,3),
    unit        VARCHAR(16),
    remark      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS mst_process (
    process_id      VARCHAR(32) PRIMARY KEY,
    process_code    VARCHAR(64),
    process_name    VARCHAR(128),
    seq             INT DEFAULT 0,
    price           NUMERIC(12,4),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- ============ 进出口贸易 ============
CREATE TABLE IF NOT EXISTS trd_order (
    order_id        VARCHAR(32) PRIMARY KEY,
    order_no        VARCHAR(64) NOT NULL,
    order_type      VARCHAR(16) DEFAULT 'EXPORT',
    customer_id     VARCHAR(32),
    incoterm        VARCHAR(16),
    currency        VARCHAR(16),
    order_date      TIMESTAMP,
    delivery_date   TIMESTAMP,
    status          VARCHAR(32) DEFAULT 'PENDING',
    total_amount    NUMERIC(18,2),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS trd_order_item (
    item_id     VARCHAR(32) PRIMARY KEY,
    order_id    VARCHAR(32),
    product_id  VARCHAR(32),
    qty         NUMERIC(12,2),
    price       NUMERIC(18,2),
    amount      NUMERIC(18,2),
    size        VARCHAR(32),
    remark      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS trd_document (
    doc_id      VARCHAR(32) PRIMARY KEY,
    order_id    VARCHAR(32),
    doc_type    VARCHAR(32),
    doc_no      VARCHAR(64),
    file_url    VARCHAR(255),
    remark      VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS trd_shipment (
    shipment_id VARCHAR(32) PRIMARY KEY,
    order_id    VARCHAR(32),
    port_from   VARCHAR(64),
    port_to     VARCHAR(64),
    etd         TIMESTAMP,
    eta         TIMESTAMP,
    container   VARCHAR(32),
    remark      VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- ============ 生产执行 ============
CREATE TABLE IF NOT EXISTS prd_work_order (
    work_order_id   VARCHAR(32) PRIMARY KEY,
    work_order_no   VARCHAR(64) NOT NULL,
    order_id        VARCHAR(32),
    product_id      VARCHAR(32),
    plan_qty        NUMERIC(12,2),
    finish_qty      NUMERIC(12,2) DEFAULT 0,
    status          VARCHAR(32) DEFAULT 'PLANNED',
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS prd_work_order_item (
    item_id         VARCHAR(32) PRIMARY KEY,
    work_order_id   VARCHAR(32),
    process_id      VARCHAR(32),
    process_name    VARCHAR(128),
    plan_qty        NUMERIC(12,2),
    finish_qty      NUMERIC(12,2) DEFAULT 0,
    status          VARCHAR(32) DEFAULT 'PENDING',
    seq             INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS prd_cutting (
    cutting_id      VARCHAR(32) PRIMARY KEY,
    work_order_id   VARCHAR(32),
    batch_no        VARCHAR(64),
    bundle_no       VARCHAR(64),
    fabric_used     NUMERIC(12,2),
    cut_qty         NUMERIC(12,2),
    barcode         VARCHAR(128),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS prd_report (
    report_id       VARCHAR(32) PRIMARY KEY,
    work_order_id   VARCHAR(32),
    process_id      VARCHAR(32),
    worker_name     VARCHAR(64),
    report_qty      NUMERIC(12,2),
    report_time     TIMESTAMP,
    barcode         VARCHAR(128),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- ============ 库存管理 ============
CREATE TABLE IF NOT EXISTS inv_warehouse (
    warehouse_id    VARCHAR(32) PRIMARY KEY,
    warehouse_code  VARCHAR(64),
    warehouse_name  VARCHAR(128),
    location        VARCHAR(128),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS inv_inbound (
    inbound_id      VARCHAR(32) PRIMARY KEY,
    inbound_no      VARCHAR(64) NOT NULL,
    warehouse_id    VARCHAR(32),
    inbound_type    VARCHAR(32),
    source_no       VARCHAR(64),
    inbound_date    TIMESTAMP,
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS inv_inbound_item (
    item_id     VARCHAR(32) PRIMARY KEY,
    inbound_id  VARCHAR(32),
    material_id VARCHAR(32),
    qty         NUMERIC(12,2),
    remark      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inv_outbound (
    outbound_id     VARCHAR(32) PRIMARY KEY,
    outbound_no     VARCHAR(64) NOT NULL,
    warehouse_id    VARCHAR(32),
    outbound_type   VARCHAR(32),
    source_no       VARCHAR(64),
    outbound_date   TIMESTAMP,
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS inv_outbound_item (
    item_id     VARCHAR(32) PRIMARY KEY,
    outbound_id VARCHAR(32),
    material_id VARCHAR(32),
    qty         NUMERIC(12,2),
    remark      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inv_stock (
    stock_id    VARCHAR(32) PRIMARY KEY,
    warehouse_id VARCHAR(32),
    material_id VARCHAR(32),
    qty         NUMERIC(12,2) DEFAULT 0,
    update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inv_stock_log (
    log_id      VARCHAR(32) PRIMARY KEY,
    warehouse_id VARCHAR(32),
    material_id VARCHAR(32),
    change_type VARCHAR(16),
    change_qty  NUMERIC(12,2),
    balance_qty NUMERIC(12,2),
    biz_no      VARCHAR(64),
    log_time    TIMESTAMP
);

-- ============ 质量管理 ============
CREATE TABLE IF NOT EXISTS qc_standard (
    standard_id     VARCHAR(32) PRIMARY KEY,
    standard_name   VARCHAR(128),
    aql_level       VARCHAR(16),
    description     VARCHAR(255),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS qc_inspection (
    inspection_id   VARCHAR(32) PRIMARY KEY,
    inspection_no   VARCHAR(64) NOT NULL,
    order_id        VARCHAR(32),
    work_order_id   VARCHAR(32),
    inspection_type VARCHAR(32),
    standard_id     VARCHAR(32),
    sample_qty      NUMERIC(12,2),
    pass_qty        NUMERIC(12,2),
    fail_qty        NUMERIC(12,2) DEFAULT 0,
    result          VARCHAR(32) DEFAULT 'PENDING',
    inspect_date    TIMESTAMP,
    inspector       VARCHAR(64),
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS qc_inspection_item (
    item_id         VARCHAR(32) PRIMARY KEY,
    inspection_id   VARCHAR(32),
    check_item      VARCHAR(128),
    check_result    VARCHAR(32),
    defect_qty      NUMERIC(12,2) DEFAULT 0,
    remark          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS qc_defect (
    defect_id       VARCHAR(32) PRIMARY KEY,
    inspection_id   VARCHAR(32),
    defect_type     VARCHAR(64),
    defect_desc     VARCHAR(255),
    defect_qty      NUMERIC(12,2),
    severity        VARCHAR(16),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- ===================== 扫码模块（M4 扩展） =====================
CREATE TABLE IF NOT EXISTS prd_scan_record (
    scan_id        VARCHAR(32) PRIMARY KEY,
    barcode        VARCHAR(64) NOT NULL,
    scan_type      VARCHAR(20) NOT NULL,           -- CUTTING/REPORT/RECEIVE/TRANSFER/OQC
    work_order_id  VARCHAR(32),
    process_id     VARCHAR(32),
    equipment_id   VARCHAR(32),
    operator_id    VARCHAR(32),
    scan_time      TIMESTAMP NOT NULL DEFAULT NOW(),
    scan_qty       NUMERIC(18,2),
    remark         VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);
CREATE INDEX IF NOT EXISTS idx_scan_barcode ON prd_scan_record(barcode);
CREATE INDEX IF NOT EXISTS idx_scan_time    ON prd_scan_record(scan_time);
CREATE INDEX IF NOT EXISTS idx_scan_wo      ON prd_scan_record(work_order_id);

-- ===================== 设备管理（M4 完整模块） =====================
-- 设备台账
CREATE TABLE IF NOT EXISTS stp_equipment (
    equipment_id    VARCHAR(32) PRIMARY KEY,
    equipment_code  VARCHAR(32) UNIQUE NOT NULL,
    equipment_name  VARCHAR(128) NOT NULL,
    equipment_type  VARCHAR(32),                    -- 字典：裁剪机/缝纫机/绣花机/包装线/整烫台/检验台
    model           VARCHAR(64),
    manufacturer    VARCHAR(64),
    purchase_date   DATE,
    purchase_amount NUMERIC(18,2),
    location        VARCHAR(128),
    status          VARCHAR(16) NOT NULL DEFAULT 'NORMAL',  -- NORMAL/REPAIR/SCRAP/RENT
    manager_id      VARCHAR(32),
    last_scan_time  TIMESTAMP,
    remark          VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- 库存状态（在库/已出库）：兼容老库的增量列
ALTER TABLE stp_equipment ADD COLUMN IF NOT EXISTS stock_status VARCHAR(16) DEFAULT 'IN_STOCK';

-- 设备维护工单
CREATE TABLE IF NOT EXISTS stp_equipment_maintenance (
    maintenance_id   VARCHAR(32) PRIMARY KEY,
    equipment_id     VARCHAR(32) NOT NULL,
    maintenance_no   VARCHAR(32) UNIQUE NOT NULL,
    maintenance_type VARCHAR(20),                    -- DAILY/PERIODIC/REPAIR/UPGRADE
    title            VARCHAR(128) NOT NULL,
    content          VARCHAR(500),
    plan_date        DATE,
    done_date        DATE,
    status           VARCHAR(16) DEFAULT 'PLANNED',  -- PLANNED/DOING/DONE/CANCELLED
    owner_id         VARCHAR(32),
    cost_amount      NUMERIC(18,2),
    remark           VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

-- 设备扫码登记（与 prd_scan_record 区分：这里是设备生命周期事件）
CREATE TABLE IF NOT EXISTS stp_equipment_scan (
    scan_id      VARCHAR(32) PRIMARY KEY,
    equipment_id VARCHAR(32) NOT NULL,
    operator_id  VARCHAR(32),
    scan_type    VARCHAR(20),                        -- CHECK_IN(入库)/CHECK_OUT(出库)/STOCKTAKE(盘点)/MAINTAIN/INSPECT/SCRAP
    scan_time    TIMESTAMP DEFAULT NOW(),
    qty          NUMERIC(18,2),
    remark       VARCHAR(255),
    create_by   VARCHAR(32), create_time TIMESTAMP, update_by VARCHAR(32), update_time TIMESTAMP, is_delete VARCHAR(1) DEFAULT 'N'
);

CREATE INDEX IF NOT EXISTS idx_eq_equipment_code   ON stp_equipment(equipment_code);
CREATE INDEX IF NOT EXISTS idx_eq_maintain_equip   ON stp_equipment_maintenance(equipment_id);
CREATE INDEX IF NOT EXISTS idx_eq_scan_equip       ON stp_equipment_scan(equipment_id);
