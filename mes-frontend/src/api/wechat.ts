import { get } from '@/utils/request'

export interface WechatJsSdkConfig {
  /** 是否已配置微信 JSSDK（公众号 appId/secret） */
  enabled: boolean
  appId?: string
  timestamp?: number
  nonceStr?: string
  signature?: string
}

/** 获取微信 JSSDK 签名配置（用于 wx.config） */
export const getWechatJsSdk = (url: string) =>
  get<WechatJsSdkConfig>('/wechat/jssdk', { url })
