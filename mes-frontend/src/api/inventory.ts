import { get, post, put, del } from '@/utils/request'

// 仓库
export const getWarehouseList = () => get('/inventory/warehouse/list')
export const createWarehouse = (data: any) => post('/inventory/warehouse', data)
export const updateWarehouse = (data: any) => put('/inventory/warehouse', data)
export const deleteWarehouse = (id: string) => del(`/inventory/warehouse/${id}`)

// 入库
export const getInboundPage = (params: any) => get('/inventory/inbound/page', params)
export const createInbound = (data: any) => post('/inventory/inbound', data)
export const getInboundDetail = (id: string) => get(`/inventory/inbound/${id}`)
export const updateInbound = (data: any) => put('/inventory/inbound', data)
export const deleteInbound = (id: string) => del(`/inventory/inbound/${id}`)

// 出库
export const getOutboundPage = (params: any) => get('/inventory/outbound/page', params)
export const createOutbound = (data: any) => post('/inventory/outbound', data)
export const getOutboundDetail = (id: string) => get(`/inventory/outbound/${id}`)
export const updateOutbound = (data: any) => put('/inventory/outbound', data)
export const deleteOutbound = (id: string) => del(`/inventory/outbound/${id}`)

// 库存
export const getStockPage = (params: any) => get('/inventory/stock/page', params)
export const getStockLog = (params: any) => get('/inventory/stock/log', params)
