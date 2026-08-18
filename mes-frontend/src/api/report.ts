import { post } from '@/utils/request'

// 生成 AI 报表
export const generateReport = (moduleType: string, prompt: string) =>
  post('/report/ai/generate', { moduleType, prompt })

// 导出 PDF
export const exportReportPdf = (report: any) =>
  post('/report/ai/export/pdf', report, { responseType: 'blob' })

// 导出 Excel
export const exportReportExcel = (report: any) =>
  post('/report/ai/export/excel', report, { responseType: 'blob' })

// 触发浏览器下载 blob
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
