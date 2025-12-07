import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      // 系统平台
      {
        path: '/dashboard/clinic',
        name: 'ClinicManagement',
        component: () => import('../views/system/ClinicManagement.vue')
      },
      {
        path: '/dashboard/role',
        name: 'RoleManagement',
        component: () => import('../views/system/RoleManagement.vue')
      },
      {
        path: '/dashboard/dept',
        name: 'DeptManagement',
        component: () => import('../views/system/DeptManagement.vue')
      },
      {
        path: '/dashboard/employee',
        name: 'EmployeeManagement',
        component: () => import('../views/system/EmployeeManagement.vue')
      },
      {
        path: '/dashboard/code',
        name: 'CodeManagement',
        component: () => import('../views/system/CodeManagement.vue')
      },
      // 智能报账
      {
        path: '/dashboard/reimb/my-apply',
        name: 'MyReimbApply',
        component: () => import('../views/reimb/MyReimbApply.vue')
      },
      {
        path: '/dashboard/reimb/approval',
        name: 'ReimbApproval',
        component: () => import('../views/reimb/ReimbApproval.vue')
      },
      {
        path: '/dashboard/reimb/query',
        name: 'ReimbQuery',
        component: () => import('../views/reimb/ReimbQuery.vue')
      },
      // 合同管理
      {
        path: '/dashboard/contract/workbench',
        name: 'ContractWorkbench',
        component: () => import('../views/contract/ContractWorkbench.vue')
      },
      {
        path: '/dashboard/contract/draft',
        name: 'ContractDraft',
        component: () => import('../views/contract/ContractDraft.vue')
      },
      {
        path: '/dashboard/contract/approval',
        name: 'ContractApproval',
        component: () => import('../views/contract/ContractApproval.vue')
      },
      {
        path: '/dashboard/contract/execution',
        name: 'ContractExecution',
        component: () => import('../views/contract/ContractExecution.vue')
      },
      // 固定资产
      {
        path: '/dashboard/asset/approval',
        name: 'AssetApproval',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      {
        path: '/dashboard/asset/procurement',
        name: 'ProcurementRequirement',
        component: () => import('../views/asset/ProcurementRequirement.vue')
      },
      {
        path: '/dashboard/asset/query',
        name: 'AssetQuery',
        component: () => import('../views/asset/AssetQuery.vue')
      },
      // 全景人力
      {
        path: '/dashboard/hr/salary',
        name: 'SalaryManagement',
        component: () => import('../views/hr/SalaryManagement.vue')
      },
      {
        path: '/dashboard/hr/salary-calc',
        name: 'SalaryCalculation',
        component: () => import('../views/hr/SalaryCalculation.vue')
      },
      // DIP成本
      {
        path: '/dashboard/cost/report',
        name: 'CostReport',
        component: () => import('../views/cost/CostReport.vue')
      },
      {
        path: '/dashboard/cost/analysis',
        name: 'CostAnalysis',
        component: () => import('../views/cost/CostAnalysis.vue')
      },
      {
        path: '/dashboard/cost/accounting',
        name: 'CostAccounting',
        component: () => import('../views/cost/CostAccounting.vue')
      },
      // 单机效能
      {
        path: '/dashboard/efficiency/collection',
        name: 'DataCollection',
        component: () => import('../views/efficiency/DataCollection.vue')
      },
      {
        path: '/dashboard/efficiency/income',
        name: 'IncomeData',
        component: () => import('../views/efficiency/IncomeData.vue')
      },
      {
        path: '/dashboard/efficiency/cost',
        name: 'CostData',
        component: () => import('../views/efficiency/CostData.vue')
      },
      {
        path: '/dashboard/efficiency/equipment-report',
        name: 'EquipmentAnalysisReport',
        component: () => import('../views/efficiency/EquipmentAnalysisReport.vue')
      },
      {
        path: '/dashboard/efficiency/investment',
        name: 'InvestmentReturnAnalysis',
        component: () => import('../views/efficiency/InvestmentReturnAnalysis.vue')
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.user.token
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
