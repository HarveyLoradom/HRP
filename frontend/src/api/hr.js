import request from './request'

export function getAllSalaries() {
  return request({
    url: '/hr/salary/list',
    method: 'get'
  })
}

export function getSalariesByEmpId(empId) {
  return request({
    url: `/hr/salary/emp/${empId}`,
    method: 'get'
  })
}

export function getSalariesByMonth(salaryMonth) {
  return request({
    url: `/hr/salary/month/${salaryMonth}`,
    method: 'get'
  })
}

export function getSalaryById(id) {
  return request({
    url: `/hr/salary/${id}`,
    method: 'get'
  })
}

export function saveSalary(data) {
  return request({
    url: '/hr/salary',
    method: 'post',
    data
  })
}

export function calculateSalary(empId, salaryMonth) {
  return request({
    url: `/hr/salary/${empId}/calculate/${salaryMonth}`,
    method: 'post'
  })
}

