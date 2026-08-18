/**
 * 通用展示格式化工具
 * - 时间：后端统一返回 yyyy-MM-dd HH:mm:ss，这里兼容 ISO/纯日期/时间戳
 * - 金额：千分位 + 2 位小数
 * - 数字：千分位
 */

function toDate(v: any): Date | null {
  if (v === null || v === undefined || v === '') return null
  if (v instanceof Date) return v
  if (typeof v === 'number') return new Date(v)
  if (typeof v === 'string') {
    // ISO 格式（含 T）浏览器可解析；纯日期补时间
    const s = v.includes('T') ? v : v.length === 10 ? `${v}T00:00:00` : v.replace(' ', 'T')
    const d = new Date(s)
    return isNaN(d.getTime()) ? null : d
  }
  return null
}

function pad(n: number) {
  return String(n).padStart(2, '0')
}

/** yyyy-MM-dd HH:mm:ss；无效返回 — */
export function fmtDateTime(v: any): string {
  const d = toDate(v)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** yyyy-MM-dd */
export function fmtDate(v: any): string {
  const d = toDate(v)
  if (!d) return '—'
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** HH:mm:ss */
export function fmtTime(v: any): string {
  const d = toDate(v)
  if (!d) return '—'
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 相对时间：刚刚 / n分钟前 / n小时前 / n天前 / 日期 */
export function fmtRelative(v: any): string {
  const d = toDate(v)
  if (!d) return '—'
  const diff = Date.now() - d.getTime()
  const min = 60 * 1000
  const hour = 60 * min
  const day = 24 * hour
  if (diff < min) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / min)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 30 * day) return `${Math.floor(diff / day)} 天前`
  return fmtDate(d)
}

/** 金额：千分位 + 2 位小数 */
export function fmtMoney(v: any, currency = ''): string {
  const n = Number(v)
  if (isNaN(n)) return '—'
  const s = n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  return currency ? `${s} ${currency}` : s
}

/** 数字千分位 */
export function fmtNum(v: any, digits = 0): string {
  const n = Number(v)
  if (isNaN(n)) return '—'
  return n.toLocaleString('zh-CN', { minimumFractionDigits: digits, maximumFractionDigits: digits })
}

/** 百分比 */
export function fmtPercent(v: any, digits = 0): string {
  const n = Number(v)
  if (isNaN(n)) return '—'
  return `${n.toFixed(digits)}%`
}
