import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '../store'
import Cookies from 'js-cookie'

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
        component: () => import('../views/system/UserManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/permission',
        name: 'PermissionManagement',
        component: () => import('../views/system/PermissionManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/dept',
        name: 'DeptManagement',
        component: () => import('../views/system/DeptManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/position',
        name: 'PositionManagement',
        component: () => import('../views/system/PositionManagement.vue'),
        meta: { requiresAuth: true }
      },
      // 系统平台 - 系统设置
      {
        path: '/hrp/system/params',
        name: 'SystemParams',
        component: () => import('../views/system/SystemParams.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/print-settings',
        name: 'PrintSettings',
        component: () => import('../views/system/PrintSettings.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/print-preview',
        name: 'PrintPreview',
        component: () => import('../views/system/PrintPreview.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/hrp/system/template-config',
        name: 'TemplateConfig',
        component: () => import('../views/system/TemplateConfig.vue'),
        meta: { requiresAuth: true }
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
      {
        path: '/hrp/budg/category',
        name: 'BudgetCategory',
        component: () => import('../views/budg/BudgetCategory.vue')
      },
      {
        path: '/hrp/budg/project',
        name: 'BudgetProject',
        component: () => import('../views/budg/BudgetProject.vue')
      },
      // 预算综合管理
      {
        path: '/hrp/budg/apply',
        name: 'BudgetApply',
        component: () => import('../views/budg/BudgetApply.vue')
      },
      {
        path: '/hrp/budg/approval',
        name: 'BudgetApproval',
        component: () => import('../views/budg/BudgetApproval.vue')
      },
      {
        path: '/hrp/budg/detail',
        name: 'BudgetDetail',
        component: () => import('../views/budg/BudgetDetail.vue')
      },
      // 预算管理 - 预算流程配置与参数设置
      {
        path: '/hrp/budg/process',
        name: 'BudgetProcess',
        component: () => import('../views/budg/BudgetProcess.vue')
      },
      {
        path: '/hrp/budg/param',
        name: 'BudgetParam',
        component: () => import('../views/budg/BudgetParam.vue')
      },
      // 预算管理 - 预算执行与控制、调整与分析
      {
        path: '/hrp/budg/execution',
        name: 'BudgetExecution',
        component: () => import('../views/budg/BudgetExecution.vue')
      },
      {
        path: '/hrp/budg/adjustment-manage',
        name: 'BudgetAdjustmentManage',
        component: () => import('../views/budg/BudgetAdjustmentManage.vue')
      },
      {
        path: '/hrp/budg/analysis',
        name: 'BudgetAnalysis',
        component: () => import('../views/budg/BudgetAnalysis.vue')
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
        path: '/hrp/hr/business-apply',
        name: 'BusinessApply',
        component: () => import('../views/hr/BusinessApply.vue')
      },
      {
        path: '/hrp/hr/attendance/manage',
        name: 'AttendanceManage',
        component: () => import('../views/hr/AttendanceManage.vue')
      },
      {
        path: '/hrp/hr/attendance/leave',
        name: 'LeaveManage',
        component: () => import('../views/hr/LeaveManage.vue')
      },
      {
        path: '/hrp/hr/salary',
        name: 'SalaryManagement',
        component: () => import('../views/hr/SalaryManagement.vue')
      },
      {
        path: '/hrp/hr/performance',
        name: 'PerformanceManagement',
        component: () => import('../views/hr/PerformanceManagement.vue')
      },
      {
        path: '/hrp/hr/transfer',
        name: 'TransferManagement',
        component: () => import('../views/hr/TransferManagement.vue')
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

// 辅助函数：检查路径是否在菜单权限中
function isPathInMenus(path, menus) {
  if (!menus || menus.length === 0) {
    return false
  }
  
  // 递归检查菜单树
  function checkMenu(menuList) {
    for (const menu of menuList) {
      // 先检查子菜单
      if (menu.children && menu.children.length > 0) {
        if (checkMenu(menu.children)) {
          return true
        }
      }
      
      // 检查当前菜单的路径
      if (menu.path) {
        let menuPath = menu.path
        // 如果path不以/开头，拼接/hrp/
        if (!menuPath.startsWith('/')) {
          menuPath = `/hrp/${menuPath}`
        }
        // 标准化路径（移除末尾的斜杠）
        menuPath = menuPath.replace(/\/$/, '')
        const normalizedPath = path.replace(/\/$/, '')
        
        // 精确匹配
        if (menuPath === normalizedPath) {
          return true
        }
        // 路径前缀匹配（用于子路由）
        if (normalizedPath.startsWith(menuPath + '/')) {
          return true
        }
      }
    }
    return false
  }
  
  return checkMenu(menus)
}

// 路由守卫
router.beforeEach((to, from, next) => {
  // 从多个位置获取token
  const token = store.state.user.token || Cookies.get('token')
  const userInfo = store.state.user.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  // 如果访问登录页
  if (to.path === '/login') {
    // 如果已登录且有用户信息，跳转到主页（避免重复登录）
    if (token && userInfo && Object.keys(userInfo).length > 0) {
      next('/hrp')
    } else {
      // 清除可能存在的无效token
      if (token && (!userInfo || Object.keys(userInfo).length === 0)) {
        Cookies.remove('token')
        localStorage.removeItem('userInfo')
        store.commit('user/REMOVE_TOKEN')
        store.commit('user/REMOVE_USER_INFO')
      }
      // 如果未登录，允许访问登录页
      next()
    }
    return
  }
  
  // 检查是否需要认证（所有 /hrp 开头的路径都需要认证）
  const requiresAuth = to.meta.requiresAuth !== false && to.path.startsWith('/hrp')
  
  if (requiresAuth) {
    // 如果没有token或没有用户信息，跳转到登录页
    if (!token || !userInfo || Object.keys(userInfo).length === 0) {
      // 清除可能存在的无效数据
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
      store.commit('user/REMOVE_TOKEN')
      store.commit('user/REMOVE_USER_INFO')
      // 使用 replace 避免在历史记录中留下重定向记录
      next({
        path: '/login',
        replace: true
      })
      return
    }
    
    // 如果访问的是首页，直接允许
    if (to.path === '/hrp') {
      next()
      return
    }
    
    // 不允许通过直接输入URL的方式访问任何子页面，必须通过菜单点击
    // 判断是否为直接输入URL：
    // 1. from.name === null（首次访问或刷新）
    // 2. from.path === '/'（从根路径跳转）
    // 3. from.path === to.path（刷新当前页面，但这种情况应该允许）
    // 4. from.path 不在 /hrp 路径下（从外部跳转）
    const isDirectUrlAccess = 
      from.name === null || 
      from.path === '/' || 
      (from.path !== '/hrp' && !from.path.startsWith('/hrp/'))
    
    // 如果是直接输入URL访问子页面，重定向到首页
    if (isDirectUrlAccess) {
      next({
        path: '/hrp',
        replace: true
      })
      return
    }
    
    // 如果是从首页或其他页面跳转过来的，还需要检查路径是否在菜单权限中
    const menus = store.state.menu.menus || []
    
    // 如果菜单为空，可能是还没加载，允许访问（会在HRP组件中加载菜单）
    if (menus.length === 0) {
      next()
      return
    }
    
    // 检查路径是否在菜单权限中
    const isInMenus = isPathInMenus(to.path, menus)
    
    // 如果路径不在菜单权限中，重定向到首页
    if (!isInMenus) {
      next({
        path: '/hrp',
        replace: true
      })
      return
    }
  }
  
  // 允许访问
  next()
})

export default router
