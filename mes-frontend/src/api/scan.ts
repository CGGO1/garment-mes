import { get, post, del } from '@/utils/request'

export const receiveScan = (data: any) => post('/scan/receive', data)
export const getScanList = (params: any) => get('/scan/list', params)
export const getScanRecent = () => get('/scan/recent')
export const getScanByBarcode = (barcode: string) => get(`/scan/by-barcode/${barcode}`)
export const deleteScan = (id: string) => del(`/scan/${id}`)
