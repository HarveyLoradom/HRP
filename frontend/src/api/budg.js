import request from './request'

// 预算主体
export function getBudgetSubjects() {
  return request({
    url: '/budg/subject/list',
    method: 'get'
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
  return request({
    url: '/budg/item/page',
    method: 'get',
    params: { page, size, ...params }
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
export function getBudgetApplies(page, size) {
  return request({
    url: '/budg/apply/list',
    method: 'get',
    params: { page, size }
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

export function approveBudgetApply(applyId, opinion) {
  return request({
    url: `/budg/apply/approve/${applyId}`,
    method: 'post',
    data: { opinion }
  })
}

export function rejectBudgetApply(applyId, opinion) {
  return request({
    url: `/budg/apply/reject/${applyId}`,
    method: 'post',
    data: { opinion }
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

// 预算分类
export function getBudgetCategories() {
  return request({
    url: '/budg/category/list',
    method: 'get'
  })
}

// ==================== 分类管理 API ====================
// 分页查询一级分类
export function getLevel1CategoriesPage(page, size, budgetYear, categoryType) {
  return request({
    url: '/budg/category/level1/page',
    method: 'get',
    params: { page, size, budgetYear, categoryType }
  })
}

// 分页查询二级分类
export function getLevel2CategoriesPage(page, size, budgetYear, categoryType, parentCategoryId) {
  return request({
    url: '/budg/category/level2/page',
    method: 'get',
    params: { page, size, budgetYear, categoryType, parentCategoryId }
  })
}

// 查询所有一级分类（用于下拉选择）
export function getLevel1CategoriesList(budgetYear, categoryType) {
  return request({
    url: '/budg/category/level1/list',
    method: 'get',
    params: { budgetYear, categoryType }
  })
}

// 查询所有二级分类（用于下拉选择）
export function getLevel2CategoriesList(budgetYear, categoryType, parentCategoryId) {
  return request({
    url: '/budg/category/level2/list',
    method: 'get',
    params: { budgetYear, categoryType, parentCategoryId }
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

