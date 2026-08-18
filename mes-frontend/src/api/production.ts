import { get, post, put, del } from '@/utils/request'

export const getWorkOrderPage = (params: any) => get('/production/work-order/page', params)
export const getWorkOrderDetail = (id: string) => get(`/production/work-order/${id}`)
export const createWorkOrder = (data: any) => post('/production/work-order', data)
export const updateWorkOrder = (data: any) => put('/production/work-order', data)
export const deleteWorkOrder = (id: string) => del(`/production/work-order/${id}`)
export const updateWorkOrderStatus = (id: string, status: string) => put(`/production/work-order/${id}/status`, { status })

export const getCuttings = (workOrderId: string) => get(`/production/cutting/${workOrderId}`)
export const createCutting = (data: any) => post('/production/cutting', data)
export const deleteCutting = (id: string) => del(`/production/cutting/${id}`)

export const getReports = (workOrderId: string) => get(`/production/report/${workOrderId}`)
export const createReport = (data: any) => post('/production/report', data)
