import { get, post, put, del } from '@/utils/request'

export const getOrderPage = (params: any) => get('/trade/order/page', params)
export const getOrderDetail = (id: string) => get(`/trade/order/${id}`)
export const createOrder = (data: any) => post('/trade/order', data)
export const updateOrder = (data: any) => put('/trade/order', data)
export const deleteOrder = (id: string) => del(`/trade/order/${id}`)
export const updateOrderStatus = (id: string, status: string) => put(`/trade/order/${id}/status`, { status })

export const getDocuments = (orderId: string) => get(`/trade/document/${orderId}`)
export const createDocument = (data: any) => post('/trade/document', data)
export const deleteDocument = (id: string) => del(`/trade/document/${id}`)

export const getShipments = (orderId: string) => get(`/trade/shipment/${orderId}`)
export const createShipment = (data: any) => post('/trade/shipment', data)
export const deleteShipment = (id: string) => del(`/trade/shipment/${id}`)
