import { get, post, put, del } from '@/utils/request'

export const getStandardList = () => get('/quality/standard/list')
export const createStandard = (data: any) => post('/quality/standard', data)
export const updateStandard = (data: any) => put('/quality/standard', data)
export const deleteStandard = (id: string) => del(`/quality/standard/${id}`)

export const getInspectionPage = (params: any) => get('/quality/inspection/page', params)
export const getInspectionDetail = (id: string) => get(`/quality/inspection/${id}`)
export const createInspection = (data: any) => post('/quality/inspection', data)
export const updateInspection = (data: any) => put('/quality/inspection', data)
export const deleteInspection = (id: string) => del(`/quality/inspection/${id}`)
export const updateInspectionResult = (id: string, result: string) => put(`/quality/inspection/${id}/result`, { result })

export const getDefects = (inspectionId: string) => get(`/quality/defect/${inspectionId}`)
export const createDefect = (data: any) => post('/quality/defect', data)
export const deleteDefect = (id: string) => del(`/quality/defect/${id}`)
