import request from './request'

// ==================== 资产分类管理 ====================
export const getAssetCategoryLevel1Page = (page, size, status, categoryName) => {
  const params = { page, size }
  if (status !== null && status !== undefined) {
    params.status = status
  }
  if (categoryName !== null && categoryName !== undefined && categoryName !== '') {
    params.categoryName = categoryName
  }
  return request.get('/asset/category/level1/page', { params })
}

export const getAssetCategoryLevel2Page = (page, size, parentId, status, categoryName) => {
  const params = { page, size }
  if (parentId !== null && parentId !== undefined) {
    params.parentId = parentId
  }
  if (status !== null && status !== undefined) {
    params.status = status
  }
  if (categoryName !== null && categoryName !== undefined && categoryName !== '') {
    params.categoryName = categoryName
  }
  return request.get('/asset/category/level2/page', { params })
}

export const getAssetCategoryLevel3Page = (page, size, parentId, level1Id, status, categoryName) => {
  const params = { page, size }
  if (parentId !== null && parentId !== undefined) {
    params.parentId = parentId
  }
  if (level1Id !== null && level1Id !== undefined) {
    params.level1Id = level1Id
  }
  if (status !== null && status !== undefined) {
    params.status = status
  }
  if (categoryName !== null && categoryName !== undefined && categoryName !== '') {
    params.categoryName = categoryName
  }
  return request.get('/asset/category/level3/page', { params })
}

export const getAssetCategoryLevel1List = (status) => {
  const params = {}
  if (status !== null && status !== undefined) {
    params.status = status
  }
  return request.get('/asset/category/level1/list', { params })
}

export const getAssetCategoryLevel2List = (parentId, status) => {
  const params = {}
  if (parentId !== null && parentId !== undefined) {
    params.parentId = parentId
  }
  if (status !== null && status !== undefined) {
    params.status = status
  }
  return request.get('/asset/category/level2/list', { params })
}

export const getAssetCategoryLevel3List = (parentId, status) => {
  const params = {}
  if (parentId !== null && parentId !== undefined) {
    params.parentId = parentId
  }
  if (status !== null && status !== undefined) {
    params.status = status
  }
  return request.get('/asset/category/level3/list', { params })
}

export const getAssetCategoryById = (id) => request.get(`/asset/category/${id}`)

export const saveAssetCategoryLevel1 = (data) => request.post('/asset/category/level1', data)
export const saveAssetCategoryLevel2 = (data) => request.post('/asset/category/level2', data)
export const saveAssetCategoryLevel3 = (data) => request.post('/asset/category/level3', data)

export const updateAssetCategory = (data) => request.put('/asset/category', data)
export const deleteAssetCategory = (id) => request.delete(`/asset/category/${id}`)
export const stopAssetCategory = (id) => request.post(`/asset/category/${id}/stop`)
export const startAssetCategory = (id) => request.post(`/asset/category/${id}/start`)

// ==================== 资产信息维护 ====================
export const getAssetItemPage = (params) => request.get('/asset/item/page', { params })
export const getAssetItemById = (id) => request.get(`/asset/item/${id}`)
export const getAssetItemList = (params) => request.get('/asset/item/list', { params })
export const saveAssetItem = (data) => request.post('/asset/item', data)
export const updateAssetItem = (data) => request.put('/asset/item', data)
export const deleteAssetItem = (id) => request.delete(`/asset/item/${id}`)
export const stopAssetItem = (id) => request.post(`/asset/item/${id}/stop`)
export const startAssetItem = (id) => request.post(`/asset/item/${id}/start`)

// ==================== 采购申请 ====================
export const getAssetPurchaseApplyPage = (params) => request.get('/asset/purchase/apply/page', { params })
export const getAssetPurchaseApplyById = (id) => request.get(`/asset/purchase/apply/${id}`)
export const getAssetPurchaseApplyByNo = (applyNo) => request.get(`/asset/purchase/apply/no/${applyNo}`)
export const saveAssetPurchaseApply = (data) => request.post('/asset/purchase/apply', data)
export const updateAssetPurchaseApply = (data) => request.put('/asset/purchase/apply', data)
export const deleteAssetPurchaseApply = (id) => request.delete(`/asset/purchase/apply/${id}`)
export const submitAssetPurchaseApply = (id) => request.post(`/asset/purchase/apply/${id}/submit`)
export const withdrawAssetPurchaseApply = (id) => request.post(`/asset/purchase/apply/${id}/withdraw`)

// ==================== 采购审批 ====================
export const getAssetPurchaseApprovalPage = (params) => request.get('/asset/purchase/apply/approval/page', { params })
export const approveAssetPurchaseApply = (id, userId, opinion, signature) => {
  const data = { userId, opinion }
  if (signature) {
    data.signature = signature
  }
  return request.post(`/asset/purchase/apply/${id}/approve`, data)
}
export const rejectAssetPurchaseApply = (id, userId, opinion) => 
  request.post(`/asset/purchase/apply/${id}/reject?userId=${userId}&opinion=${opinion || ''}`)
export const returnAssetPurchaseApply = (id, returnType, opinion) => {
  const data = { returnType: returnType || 'RETURN_TO_CURRENT', opinion: opinion || '' }
  return request.post(`/asset/purchase/apply/${id}/return`, data)
}

// ==================== 采购查询 ====================
export const getAssetPurchaseQueryPage = (params) => request.get('/asset/purchase/apply/query/page', { params })

// ==================== 采购入库 - 采购单 ====================
export const getAssetPurchasePage = (params) => request.get('/asset/purchase/page', { params })
export const getAssetPurchaseById = (id) => request.get(`/asset/purchase/${id}`)
export const getAssetPurchaseByOrderNo = (orderNo) => request.get(`/asset/purchase/order-no/${orderNo}`)
export const getAssetPurchaseByApplyNo = (applyNo) => request.get(`/asset/purchase/apply-no/${applyNo}`)
export const getAvailableApplies = () => request.get('/asset/purchase/available-applies')
export const getApplyByNo = (applyNo) => request.get(`/asset/purchase/apply-by-no/${applyNo}`)
export const saveAssetPurchase = (data) => request.post('/asset/purchase', data)
export const updateAssetPurchase = (data) => request.put('/asset/purchase', data)
export const deleteAssetPurchase = (id) => request.delete(`/asset/purchase/${id}`)
export const completeAssetPurchase = (id, contractNo) => request.post(`/asset/purchase/${id}/complete`, { contractNo })

// ==================== 采购入库 - 入库单 ====================
export const getAssetInStoragePage = (params) => request.get('/asset/storage/page', { params })
export const getAssetInStorageById = (id) => request.get(`/asset/storage/${id}`)
export const getAssetInStorageByStorageNo = (storageNo) => request.get(`/asset/storage/storage-no/${storageNo}`)
export const getAssetInStorageByOrderNo = (orderNo) => request.get(`/asset/storage/order-no/${orderNo}`)
export const getAssetInStorageByPurchaseId = (purchaseId) => request.get(`/asset/storage/purchase-id/${purchaseId}`)
export const saveAssetInStorage = (data) => request.post('/asset/storage', data)
export const updateAssetInStorage = (data) => request.put('/asset/storage', data)
export const deleteAssetInStorage = (id) => request.delete(`/asset/storage/${id}`)
export const completeAssetInStorage = (id) => request.post(`/asset/storage/${id}/complete`)

// ==================== 资产台账查询 ====================
export const getAssetAccountPage = (params) => request.get('/asset/account/page', { params })
export const getAssetAccountByAssetCode = (assetCode) => request.get(`/asset/account/code/${assetCode}`)
export const getAssetStorageInfo = (assetCode) => request.get(`/asset/account/storage-info/${assetCode}`)
export const getAssetReceiveInfo = (assetCode) => request.get(`/asset/account/receive-info/${assetCode}`)
export const updateAssetAccountStock = (assetCode, quantity) => request.put('/asset/account/stock', { params: { assetCode, quantity } })
export const saveAssetAccount = (data) => request.post('/asset/account', data)

// ==================== 资产领用 ====================
export const getAssetReceivePage = (params) => request.get('/asset/receive/page', { params })
export const getAssetReceiveById = (id) => request.get(`/asset/receive/${id}`)
export const getAssetReceiveByReceiveNo = (receiveNo) => request.get(`/asset/receive/receive-no/${receiveNo}`)
export const saveAssetReceive = (data) => request.post('/asset/receive', data)
export const generateReceiveNo = () => request.get('/asset/receive/generate-receive-no')
