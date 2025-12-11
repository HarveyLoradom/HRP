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

