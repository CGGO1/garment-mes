import { post } from '@/utils/request'

// AI 对话（后端代理到 Lili 知识库）
export const aiChat = (message: string) => post('/ai/chat', { message })
