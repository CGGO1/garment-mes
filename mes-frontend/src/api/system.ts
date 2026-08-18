import { get, post, put, del } from '@/utils/request'

// ===== 用户 =====
export const getUserPage = (params: any) => get('/system/user/page', params)
export const createUser = (data: any) => post('/system/user', data)
export const updateUser = (data: any) => put('/system/user', data)
export const deleteUser = (id: string) => del(`/system/user/${id}`)

// ===== 角色 =====
export const getRolePage = (params: any) => get('/system/role/page', params)
export const getRoleList = () => get('/system/role/list')
export const createRole = (data: any) => post('/system/role', data)
export const updateRole = (data: any) => put('/system/role', data)
export const deleteRole = (id: string) => del(`/system/role/${id}`)
export const getRoleMenus = (roleId: string) => get(`/system/role/${roleId}/menus`)
export const saveRoleMenus = (roleId: string, menuIds: string[]) => put(`/system/role/${roleId}/menus`, menuIds)

// ===== 菜单 =====
export const getMenuTree = () => get('/system/menu/tree')
export const getMenuList = () => get('/system/menu/list')
export const createMenu = (data: any) => post('/system/menu', data)
export const updateMenu = (data: any) => put('/system/menu', data)
export const deleteMenu = (id: string) => del(`/system/menu/${id}`)

// ===== 字典 =====
export const getDictTypePage = (params: any) => get('/system/dict/type/page', params)
export const createDictType = (data: any) => post('/system/dict/type', data)
export const updateDictType = (data: any) => put('/system/dict/type', data)
export const deleteDictType = (id: string) => del(`/system/dict/type/${id}`)
export const getDictData = (type: string) => get(`/system/dict/data/${type}`)
export const createDictData = (data: any) => post('/system/dict/data', data)
export const updateDictData = (data: any) => put('/system/dict/data', data)
export const deleteDictData = (id: string) => del(`/system/dict/data/${id}`)
