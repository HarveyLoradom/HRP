import request from './request'

// 预算主体
export function getBudgetSubjects(isStop = null) {
  const params = isStop !== null && isStop !== undefined ? { isStop } : {}
  return request({
    url: '/budg/subject/list',
    method: 'get',
    params
  })
}

export function getBudgetSubjectTree() {
  return request({
    url: '/budg/subject/tree',
    method: 'get'
  })
}

export function saveBudgetSubject(data) {
  return request({
    url: '/budg/subject',
    method: 'post',
    data
  })
}

export function updateBudgetSubject(data) {
  return request({
    url: '/budg/subject',
    method: 'put',
    data
  })
}

export function deleteBudgetSubject(id) {
  return request({
    url: `/budg/subject/${id}`,
    method: 'delete'
  })
}

export function stopBudgetSubject(id) {
  return request({
    url: `/budg/subject/stop/${id}`,
    method: 'post'
  })
}

export function startBudgetSubject(id) {
  return request({
    url: `/budg/subject/start/${id}`,
    method: 'post'
  })
}

export function getBudgetSubjectRelatedDepts(subjectId) {
  return request({
    url: `/budg/subject/related-depts/${subjectId}`,
    method: 'get'
  })
}

// 预算项目
export function getBudgetItems() {
  return request({
    url: '/budg/item/list',
    method: 'get'
  })
}

export function getBudgetItemsByCategory(categoryId) {
  return request({
    url: `/budg/item/category/${categoryId}`,
    method: 'get'
  })
}

export function saveBudgetItem(data) {
  return request({
    url: '/budg/item',
    method: 'post',
    data
  })
}

export function updateBudgetItem(data) {
  return request({
    url: '/budg/item',
    method: 'put',
    data
  })
}

export function deleteBudgetItem(id) {
  return request({
    url: `/budg/item/${id}`,
    method: 'delete'
  })
}

// 分页查询项目预算
export function getBudgetItemsPage(page, size, params) {
  const requestParams = { page, size, ...params }
  return request({
    url: '/budg/item/page',
    method: 'get',
    params: requestParams
  })
}

// 停用项目预算
export function stopBudgetItem(id) {
  return request({
    url: `/budg/item/stop/${id}`,
    method: 'post'
  })
}

// 启用项目预算
export function startBudgetItem(id) {
  return request({
    url: `/budg/item/start/${id}`,
    method: 'post'
  })
}

// 分配主体
export function assignBudgetItemSubjects(itemId, subjectIds) {
  return request({
    url: `/budg/item/${itemId}/assign`,
    method: 'post',
    data: subjectIds
  })
}

// 获取项目分配的主体列表
export function getBudgetItemSubjects(itemId) {
  return request({
    url: `/budg/item/${itemId}/subjects`,
    method: 'get'
  })
}

// 获取主体分配的项目列表
export function getBudgetItemsBySubject(subjectId) {
  return request({
    url: `/budg/item/subject/${subjectId}/items`,
    method: 'get'
  })
}

// 预算
export function getBudgets() {
  return request({
    url: '/budg/budget/list',
    method: 'get'
  })
}

export function getBudgetsByYear(year) {
  return request({
    url: `/budg/budget/year/${year}`,
    method: 'get'
  })
}

export function getBudgetsBySubject(subjectId) {
  return request({
    url: `/budg/budget/subject/${subjectId}`,
    method: 'get'
  })
}

export function getBudgetsByItem(itemId) {
  return request({
    url: `/budg/budget/item/${itemId}`,
    method: 'get'
  })
}

export function getBudgetsBySubjectAndItem(subjectId, itemId) {
  return request({
    url: `/budg/budget/subject/${subjectId}?itemId=${itemId}`,
    method: 'get'
  })
}

export function checkBudgetAmount(budgetId, amount) {
  return request({
    url: `/budg/budget/check?budgetId=${budgetId}&amount=${amount}`,
    method: 'get'
  })
}

export function getBudgetRemainingAmount(budgetId) {
  return request({
    url: `/budg/budget/${budgetId}/remaining`,
    method: 'get'
  })
}

export function saveBudget(data) {
  return request({
    url: '/budg/budget',
    method: 'post',
    data
  })
}

// 预算执行
export function getBudgetExecutions() {
  return request({
    url: '/budg/execution/list',
    method: 'get'
  })
}

export function getBudgetExecutionsByBudget(budgetId) {
  return request({
    url: `/budg/execution/budget/${budgetId}`,
    method: 'get'
  })
}

// 预算申请
export function getBudgetApplies(params) {
  // 如果是旧式调用（page, size），转换为对象
  if (typeof params === 'number') {
    params = { page: params, size: arguments[1] }
  }
  // 移除null值
  const cleanParams = {}
  Object.keys(params).forEach(key => {
    if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
      cleanParams[key] = params[key]
    }
  })
  return request({
    url: '/budg/apply/list',
    method: 'get',
    params: cleanParams
  })
}

export function getBudgetApplyById(id) {
  return request({
    url: `/budg/apply/${id}`,
    method: 'get'
  })
}

export function saveBudgetApply(data) {
  return request({
    url: '/budg/apply',
    method: 'post',
    data
  })
}

export function updateBudgetApply(data) {
  return request({
    url: '/budg/apply',
    method: 'put',
    data
  })
}

export function submitBudgetApply(applyId) {
  return request({
    url: `/budg/apply/submit/${applyId}`,
    method: 'post'
  })
}

export function deleteBudgetApply(applyId) {
  return request({
    url: `/budg/apply/${applyId}`,
    method: 'delete'
  })
}

export function withdrawBudgetApply(applyId) {
  return request({
    url: `/budg/apply/withdraw/${applyId}`,
    method: 'post'
  })
}

export function approveBudgetApply(applyId, opinion, approverSignature) {
  const data = { opinion }
  if (approverSignature) {
    data.approverSignature = approverSignature
  }
  return request({
    url: `/budg/apply/approve/${applyId}`,
    method: 'post',
    data
  })
}

export function rejectBudgetApply(applyId, opinion) {
  return request({
    url: `/budg/apply/reject/${applyId}`,
    method: 'post',
    data: { opinion }
  })
}

export function returnBudgetApply(applyId, returnType, opinion) {
  return request({
    url: `/budg/apply/return/${applyId}`,
    method: 'post',
    data: { returnType, opinion }
  })
}

// 预算明细
export function getBudgetDetails(page, size, params) {
  return request({
    url: '/budg/detail/list',
    method: 'get',
    params: { page, size, ...params }
  })
}

export function getBudgetExecutionDetails(itemId, subjectId) {
  return request({
    url: '/budg/detail/execution',
    method: 'get',
    params: { itemId, subjectId }
  })
}

export function getBudgetApplyDetails(itemId, subjectId) {
  return request({
    url: '/budg/detail/apply',
    method: 'get',
    params: { itemId, subjectId }
  })
}

export function getBudgetApplyExecutionDetails(itemId, subjectId, applyNo) {
  return request({
    url: '/budg/detail/apply-execution',
    method: 'get',
    params: { itemId, subjectId, applyNo }
  })
}

// 预算分类
export function getBudgetCategories() {
  return request({
    url: '/budg/category/list',
    method: 'get'
  })
}

// ==================== 分类管理 API ====================
// 分页查询一级分类
export function getLevel1CategoriesPage(page, size, budgetYear, categoryType, isStop = null) {
  const params = { page, size }
  if (budgetYear) params.budgetYear = budgetYear
  if (categoryType) params.categoryType = categoryType
  if (isStop !== null && isStop !== undefined) params.isStop = isStop
  return request({
    url: '/budg/category/level1/page',
    method: 'get',
    params
  })
}

// 分页查询二级分类
export function getLevel2CategoriesPage(page, size, budgetYear, categoryType, parentCategoryId, isStop = null) {
  const params = { page, size }
  if (budgetYear) params.budgetYear = budgetYear
  if (categoryType) params.categoryType = categoryType
  if (parentCategoryId) params.parentCategoryId = parentCategoryId
  if (isStop !== null && isStop !== undefined) params.isStop = isStop
  return request({
    url: '/budg/category/level2/page',
    method: 'get',
    params
  })
}

// 查询所有一级分类（用于下拉选择）
export function getLevel1CategoriesList(budgetYear, categoryType, isStop = null) {
  const params = {}
  if (budgetYear) params.budgetYear = budgetYear
  if (categoryType) params.categoryType = categoryType
  if (isStop !== null && isStop !== undefined) params.isStop = isStop
  return request({
    url: '/budg/category/level1/list',
    method: 'get',
    params
  })
}

// 查询所有二级分类（用于下拉选择）
export function getLevel2CategoriesList(budgetYear, categoryType, parentCategoryId, isStop = null) {
  const params = {}
  if (budgetYear) params.budgetYear = budgetYear
  if (categoryType) params.categoryType = categoryType
  if (parentCategoryId) params.parentCategoryId = parentCategoryId
  if (isStop !== null && isStop !== undefined) params.isStop = isStop
  return request({
    url: '/budg/category/level2/list',
    method: 'get',
    params
  })
}

// 新增一级分类
export function saveLevel1Category(data) {
  return request({
    url: '/budg/category/level1',
    method: 'post',
    data
  })
}

// 新增二级分类
export function saveLevel2Category(data) {
  return request({
    url: '/budg/category/level2',
    method: 'post',
    data
  })
}

// 更新分类
export function updateBudgetCategory(data) {
  return request({
    url: '/budg/category',
    method: 'put',
    data
  })
}

// 删除分类
export function deleteBudgetCategory(id) {
  return request({
    url: `/budg/category/${id}`,
    method: 'delete'
  })
}

// 停用分类
export function stopBudgetCategory(id) {
  return request({
    url: `/budg/category/stop/${id}`,
    method: 'post'
  })
}

// 启用分类
export function startBudgetCategory(id) {
  return request({
    url: `/budg/category/start/${id}`,
    method: 'post'
  })
}

// 预算调整
export function getBudgetAdjustmentsPage(page, size, params) {
  return request({
    url: '/budg/adjustment/page',
    method: 'get',
    params: { page, size, ...params }
  })
}

export function getBudgetAdjustmentById(id) {
  return request({
    url: `/budg/adjustment/${id}`,
    method: 'get'
  })
}

export function saveBudgetAdjustment(data) {
  return request({
    url: '/budg/adjustment',
    method: 'post',
    data
  })
}

export function updateBudgetAdjustment(data) {
  return request({
    url: '/budg/adjustment',
    method: 'put',
    data
  })
}

export function deleteBudgetAdjustment(id) {
  return request({
    url: `/budg/adjustment/${id}`,
    method: 'delete'
  })
}

export function submitBudgetAdjustment(data) {
  return request({
    url: '/budg/adjustment/submit',
    method: 'post',
    data
  })
}

export function saveAndSubmitBudgetAdjustment(data) {
  return request({
    url: '/budg/adjustment/save-and-submit',
    method: 'post',
    data
  })
}

// 根据预算项目ID查询申请单（用于申请冲销）
export function getAppliesByItemId(itemId) {
  return request({
    url: `/budg/apply/item/${itemId}`,
    method: 'get'
  })
}

// 根据预算项目ID查询报账单（用于报账冲销）
export function getPayoutsByItemId(itemId) {
  return request({
    url: `/reimb/payout/item/${itemId}/payout`,
    method: 'get'
  })
}

// 根据subject_code和item_code查询申请单（用于申请冲销）
export function getAppliesBySubjectAndItem(subjectCode, itemCode) {
  return request({
    url: `/budg/detail/applies`,
    method: 'get',
    params: { subjectCode, itemCode }
  })
}

