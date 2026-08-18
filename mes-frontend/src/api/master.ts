import { get, post, put, del } from '@/utils/request'

// 客户
export const getCustomerPage = (params: any) => get('/master/customer/page', params)
export const getCustomerList = () => get('/master/customer/list')
export const createCustomer = (data: any) => post('/master/customer', data)
export const updateCustomer = (data: any) => put('/master/customer', data)
export const deleteCustomer = (id: string) => del(`/master/customer/${id}`)

// 供应商
export const getSupplierPage = (params: any) => get('/master/supplier/page', params)
export const getSupplierList = () => get('/master/supplier/list')
export const createSupplier = (data: any) => post('/master/supplier', data)
export const updateSupplier = (data: any) => put('/master/supplier', data)
export const deleteSupplier = (id: string) => del(`/master/supplier/${id}`)

// 产品
export const getProductPage = (params: any) => get('/master/product/page', params)
export const getProductList = () => get('/master/product/list')
export const createProduct = (data: any) => post('/master/product', data)
export const updateProduct = (data: any) => put('/master/product', data)
export const deleteProduct = (id: string) => del(`/master/product/${id}`)

// 物料
export const getMaterialPage = (params: any) => get('/master/material/page', params)
export const getMaterialList = () => get('/master/material/list')
export const createMaterial = (data: any) => post('/master/material', data)
export const updateMaterial = (data: any) => put('/master/material', data)
export const deleteMaterial = (id: string) => del(`/master/material/${id}`)

// 工序
export const getProcessPage = (params: any) => get('/master/process/page', params)
export const getProcessList = () => get('/master/process/list')
export const createProcess = (data: any) => post('/master/process', data)
export const updateProcess = (data: any) => put('/master/process', data)
export const deleteProcess = (id: string) => del(`/master/process/${id}`)

// BOM
export const getBomPage = (params: any) => get('/master/bom/page', params)
export const getBomDetail = (id: string) => get(`/master/bom/${id}`)
export const createBom = (data: any) => post('/master/bom', data)
export const updateBom = (data: any) => put('/master/bom', data)
export const deleteBom = (id: string) => del(`/master/bom/${id}`)
export const createBomItem = (data: any) => post('/master/bom/item', data)
export const updateBomItem = (data: any) => put('/master/bom/item', data)
export const deleteBomItem = (id: string) => del(`/master/bom/item/${id}`)
