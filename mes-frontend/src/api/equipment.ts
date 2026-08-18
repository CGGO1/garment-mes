import { get, post, put, del } from '@/utils/request'

// 设备台账
export const getEquipmentPage = (params: any) => get('/equipment/page', params)
export const getEquipmentDetail = (id: string) => get(`/equipment/${id}`)
export const getEquipmentByCode = (code: string) => get(`/equipment/by-code/${code}`)
export const createEquipment = (data: any) => post('/equipment', data)
export const updateEquipment = (data: any) => put('/equipment', data)
export const deleteEquipment = (id: string) => del(`/equipment/${id}`)
export const updateEquipmentStatus = (id: string, status: string) => put(`/equipment/${id}/status`, { status })

// 维护工单
export const listMaintenance = (id: string) => get(`/equipment/${id}/maintenance`)
export const createMaintenance = (data: any) => post('/equipment/maintenance', data)
export const updateMaintenance = (data: any) => put('/equipment/maintenance', data)
export const deleteMaintenance = (id: string) => del(`/equipment/maintenance/${id}`)
export const updateMaintenanceStatus = (id: string, status: string) => put(`/equipment/maintenance/${id}/status`, { status })

// 设备扫码
export const scanEquipment = (data: any) => post('/equipment/scan', data)
export const listEquipmentScan = (id: string) => get(`/equipment/scan/${id}`)
export const getRecentEquipmentScan = (limit = 20) => get('/equipment/scan/recent', { limit })

// 统计
export const getEquipmentSummary = () => get('/equipment/stats/summary')
