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
    redirect: '/hrp'
  },
  {
    path: '/hrp',
    name: 'HRP',
    component: () => import('../views/HRP.vue'),
    meta: { requiresAuth: true },
    children: [
      // 系统平台 - 管理平台
      {
        path: '/hrp/system/user',
        name: 'UserManagement',
        component: () => import('../views/system/UserManagement.vue')
      },
      {
        path: '/hrp/system/permission',
        name: 'PermissionManagement',
        component: () => import('../views/system/PermissionManagement.vue')
      },
      {
        path: '/hrp/system/dept',
        name: 'DeptManagement',
        component: () => import('../views/system/DeptManagement.vue')
      },
      {
        path: '/hrp/system/position',
        name: 'PositionManagement',
        component: () => import('../views/system/PositionManagement.vue')
      },
      {
        path: '/hrp/system/employee',
        name: 'EmployeeManagement',
        component: () => import('../views/system/EmployeeManagement.vue')
      },
      // 系统平台 - 系统设置
      {
        path: '/hrp/system/params',
        name: 'SystemParams',
        component: () => import('../views/system/SystemParams.vue')
      },
      // 系统平台 - 业务平台
      {
        path: '/hrp/business/process-definition',
        name: 'ProcessDefinition',
        component: () => import('../views/business/ProcessDefinition.vue')
      },
      {
        path: '/hrp/business/process-task',
        name: 'ProcessTask',
        component: () => import('../views/business/ProcessTask.vue')
      },
      {
        path: '/hrp/business/process-instance',
        name: 'ProcessInstance',
        component: () => import('../views/business/ProcessInstance.vue')
      },
      // 旧路由（保留兼容）
      {
        path: '/hrp/role',
        name: 'RoleManagement',
        component: () => import('../views/system/RoleManagement.vue')
      },
      // 智能报账 - 业务办理
      {
        path: '/hrp/reimb/my-apply',
        name: 'MyReimbApply',
        component: () => import('../views/reimb/MyReimbApply.vue')
      },
      {
        path: '/hrp/reimb/my-payout',
        name: 'MyReimbPayout',
        component: () => import('../views/reimb/MyReimbPayout.vue')
      },
      // 智能报账 - 业务审批
      {
        path: '/hrp/reimb/apply-approval',
        name: 'ApplyApproval',
        component: () => import('../views/reimb/ApplyApproval.vue')
      },
      {
        path: '/hrp/reimb/payout-approval',
        name: 'ReimbApproval',
        component: () => import('../views/reimb/ReimbApproval.vue')
      },
      // 智能报账 - 财务处理
      {
        path: '/hrp/reimb/apply-query',
        name: 'ApplyQuery',
        component: () => import('../views/reimb/ApplyQuery.vue')
      },
      {
        path: '/hrp/reimb/payout-query',
        name: 'ReimbQuery',
        component: () => import('../views/reimb/ReimbQuery.vue')
      },
      // 合同管理
      {
        path: '/hrp/contract/workbench',
        name: 'ContractWorkbench',
        component: () => import('../views/contract/ContractWorkbench.vue')
      },
      {
        path: '/hrp/contract/draft',
        name: 'ContractDraft',
        component: () => import('../views/contract/ContractDraft.vue')
      },
      {
        path: '/hrp/contract/approval',
        name: 'ContractApproval',
        component: () => import('../views/contract/ContractApproval.vue')
      },
      {
        path: '/hrp/contract/execution',
        name: 'ContractExecution',
        component: () => import('../views/contract/ContractExecution.vue')
      },
      // 预算管理 - 预算基础设置
      {
        path: '/hrp/budg/subject',
        name: 'BudgetSubject',
        component: () => import('../views/budg/BudgetSubject.vue')
      },
      {
        path: '/hrp/budg/item',
        name: 'BudgetItem',
        component: () => import('../views/budg/BudgetItem.vue')
      },
      // 固定资产
      {
        path: '/hrp/asset/approval',
        name: 'AssetApproval',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      {
        path: '/hrp/asset/procurement',
        name: 'ProcurementRequirement',
        component: () => import('../views/asset/ProcurementRequirement.vue')
      },
      {
        path: '/hrp/asset/query',
        name: 'AssetQuery',
        component: () => import('../views/asset/AssetQuery.vue')
      },
      // 全景人力
      {
        path: '/hrp/hr/salary',
        name: 'SalaryManagement',
        component: () => import('../views/hr/SalaryManagement.vue')
      },
      {
        path: '/hrp/hr/salary-calc',
        name: 'SalaryCalculation',
        component: () => import('../views/hr/SalaryCalculation.vue')
      },
      // DIP成本
      {
        path: '/hrp/cost/report',
        name: 'CostReport',
        component: () => import('../views/cost/CostReport.vue')
      },
      {
        path: '/hrp/cost/analysis',
        name: 'CostAnalysis',
        component: () => import('../views/cost/CostAnalysis.vue')
      },
      {
        path: '/hrp/cost/accounting',
        name: 'CostAccounting',
        component: () => import('../views/cost/CostAccounting.vue')
      },
      // 单机效能
      {
        path: '/hrp/efficiency/collection',
        name: 'DataCollection',
        component: () => import('../views/efficiency/DataCollection.vue')
      },
      {
        path: '/hrp/efficiency/income',
        name: 'IncomeData',
        component: () => import('../views/efficiency/IncomeData.vue')
      },
      {
        path: '/hrp/efficiency/cost',
        name: 'CostData',
        component: () => import('../views/efficiency/CostData.vue')
      },
      {
        path: '/hrp/efficiency/equipment-report',
        name: 'EquipmentAnalysisReport',
        component: () => import('../views/efficiency/EquipmentAnalysisReport.vue')
      },
      {
        path: '/hrp/efficiency/investment',
        name: 'InvestmentReturnAnalysis',
        component: () => import('../views/efficiency/InvestmentReturnAnalysis.vue')
      },
      // 固定资产 - 业务审批
      {
        path: '/hrp/asset/approval/transfer',
        name: 'AssetApprovalTransfer',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      {
        path: '/hrp/asset/approval/disposal',
        name: 'AssetApprovalDisposal',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      {
        path: '/hrp/asset/approval/inventory',
        name: 'AssetApprovalInventory',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      {
        path: '/hrp/asset/approval/change',
        name: 'AssetApprovalChange',
        component: () => import('../views/asset/AssetApproval.vue')
      },
      // 固定资产 - 采购需求
      {
        path: '/hrp/asset/procurement/apply',
        name: 'ProcurementApply',
        component: () => import('../views/asset/ProcurementApply.vue')
      },
      {
        path: '/hrp/asset/procurement/approval',
        name: 'ProcurementApproval',
        component: () => import('../views/asset/ProcurementApproval.vue')
      },
      // 固定资产 - 综合查询
      {
        path: '/hrp/asset/query/asset',
        name: 'AssetQuery',
        component: () => import('../views/asset/AssetQuery.vue')
      },
      {
        path: '/hrp/asset/query/procurement',
        name: 'ProcurementQuery',
        component: () => import('../views/asset/ProcurementQuery.vue')
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
    next('/hrp')
  } else {
    next()
  }
})

export default router
