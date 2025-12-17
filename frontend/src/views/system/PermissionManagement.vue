<template>
  <div class="permission-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>权限管理</span>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="角色权限" name="role">
          <el-table :data="roleTableData" border style="width: 100%" v-loading="roleLoading">
            <el-table-column prop="codeValue" label="用户类型值" width="120"></el-table-column>
            <el-table-column prop="codeName" label="用户类型名称" width="150"></el-table-column>
            <el-table-column prop="codeTypeName" label="类型说明"></el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleAssignMenu(scope.row)">设置菜单权限</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="用户权限" name="user">
          <!-- 搜索条件 -->
          <el-form :inline="true" :model="userSearchForm" class="search-form" style="margin-bottom: 15px;">
            <el-form-item label="工号/姓名">
              <el-input v-model="userSearchForm.keyword" placeholder="请输入工号或姓名" clearable @keyup.enter.native="handleUserSearch"></el-input>
            </el-form-item>
            <el-form-item label="部门">
              <el-select v-model="userSearchForm.deptCode" placeholder="请选择部门" clearable style="width: 200px;">
                <el-option
                  v-for="dept in deptOptions"
                  :key="dept.deptCode"
                  :label="dept.deptName"
                  :value="dept.deptCode"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUserSearch">查询</el-button>
              <el-button @click="handleUserReset">重置</el-button>
            </el-form-item>
          </el-form>
          
          <div style="margin-bottom: 15px;">
            <el-button type="primary" @click="handleBatchAssignMenu" :disabled="selectedUsers.length === 0">批量分配菜单</el-button>
            <el-button type="success" @click="handleBatchAssignRole" :disabled="selectedUsers.length === 0" style="margin-left: 10px;">批量分配角色</el-button>
            <span style="margin-left: 10px; color: #909399;">已选择 {{ selectedUsers.length }} 个用户</span>
          </div>
          <el-table 
            :data="paginatedUserTableData" 
            border 
            style="width: 100%" 
            v-loading="userLoading"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="account" label="账号" width="120"></el-table-column>
            <el-table-column prop="name" label="用户名" width="120"></el-table-column>
            <el-table-column prop="deptName" label="部门" width="150">
              <template slot-scope="scope">
                <span v-if="scope.row.deptName">{{ scope.row.deptName }}</span>
                <span v-else style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="用户类型" width="120">
              <template slot-scope="scope">
                <span>{{ getUserTypeName(scope.row.type) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleAssignRole(scope.row)">分配角色</el-button>
                <el-button size="mini" type="success" @click="handleAssignMenu(scope.row)">分配菜单</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container" style="margin-top: 20px; text-align: right;">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="pagination.page"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total">
            </el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 分配菜单对话框 -->
    <el-dialog
      :title="menuDialogTitle"
      :visible.sync="menuDialogVisible"
      width="600px"
      @open="handleMenuDialogOpen"
    >
      <el-tree
        ref="menuTree"
        :data="menuTreeData"
        show-checkbox
        node-key="id"
        :props="{ children: 'children', label: 'menuName' }"
        :default-checked-keys="checkedMenuIds"
      ></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenuAssign">确定</el-button>
      </div>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog
      :title="roleDialogTitle"
      :visible.sync="roleDialogVisible"
      width="500px"
      @open="handleRoleDialogOpen"
    >
      <div v-if="userTypeOptions.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        暂无用户类型数据
      </div>
      <el-radio-group v-model="selectedUserType" v-else>
        <el-radio v-for="type in userTypeOptions" :key="type.value" :label="type.value">
          {{ type.label }}
        </el-radio>
      </el-radio-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoleAssign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRoleList, getUserList, getUserById, getUserRoles, assignUserRoles, batchAssignUserRoles, getUserMenuIds, assignUserMenus, getCodeByType, getUserTypeMenus, assignUserTypeMenus, updateUser, getDeptList } from '@/api/user'
import { getMenuTree } from '@/api/menu'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'PermissionManagement',
  mixins: [paginationMixin],
  data() {
    return {
      activeTab: 'role',
      roleLoading: false,
      userLoading: false,
      roleTableData: [],
      selectedUsers: [],
      menuDialogVisible: false,
      menuDialogTitle: '分配菜单',
      menuTreeData: [],
      checkedMenuIds: [],
      currentAssignId: null,
      assignType: '', // 'role' or 'user' or 'batch'
      roleDialogVisible: false,
      roleDialogTitle: '分配角色',
      allRoles: [],
      selectedRoleIds: [],
      currentUserId: null,
      isBatchRoleAssign: false,
      userTypeOptions: [],
      selectedUserType: null,
      userSearchForm: {
        keyword: '',
        deptCode: ''
      },
      deptOptions: [],
      allUserTableData: []
    }
  },
  computed: {
    filteredUserTableData() {
      let filtered = [...this.allUserTableData]
      
      // 按工号/姓名筛选
      if (this.userSearchForm.keyword && this.userSearchForm.keyword.trim()) {
        const keyword = this.userSearchForm.keyword.trim().toLowerCase()
        filtered = filtered.filter(user => 
          (user.account && user.account.toLowerCase().includes(keyword)) ||
          (user.name && user.name.toLowerCase().includes(keyword))
        )
      }
      
      // 按部门筛选
      if (this.userSearchForm.deptCode) {
        filtered = filtered.filter(user => user.deptCode === this.userSearchForm.deptCode)
      }
      
      return filtered
    },
    // 分页后的用户数据
    paginatedUserTableData() {
      return this.getPaginatedData(this.filteredUserTableData)
    }
  },
  mounted() {
    this.loadRoleData()
    this.loadUserData()
    this.loadAllRoles()
    this.loadMenuTree()
    this.loadUserTypeOptions()
    this.loadDeptList()
  },
  methods: {
    loadRoleData() {
      this.roleLoading = true
      // 从sys_code中获取USER_TYPE类型的用户类型
      getCodeByType('USER_TYPE').then(response => {
        if (response.code === 200) {
          // 只显示未停用的用户类型
          this.roleTableData = (response.data || []).filter(item => item.isStop === 0 || item.isStop === '0')
        } else {
          this.roleTableData = []
        }
        this.roleLoading = false
      }).catch(() => {
        this.roleTableData = []
        this.roleLoading = false
      })
    },
    loadUserData() {
      this.userLoading = true
      getUserList().then(response => {
        if (response.code === 200) {
          this.allUserTableData = response.data || []
          // 初始化分页
          this.pagination.total = this.filteredUserTableData.length
          this.resetPagination()
        }
        this.userLoading = false
      }).catch(() => {
        this.userLoading = false
      })
    },
    loadDeptList() {
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptOptions = response.data || []
        }
      }).catch(() => {
        this.deptOptions = []
      })
    },
    handleUserSearch() {
      // 触发计算属性重新过滤，并重置分页
      this.resetPagination()
      this.pagination.total = this.filteredUserTableData.length
    },
    handleUserReset() {
      this.userSearchForm.keyword = ''
      this.userSearchForm.deptCode = ''
      this.resetPagination()
      this.pagination.total = this.filteredUserTableData.length
    },
    loadAllRoles() {
      return getRoleList().then(response => {
        if (response.code === 200) {
          this.allRoles = response.data || []
          console.log('角色列表加载成功:', this.allRoles)
          return this.allRoles
        } else {
          this.$message.error(response.message || '获取角色列表失败')
          this.allRoles = []
          return []
        }
      }).catch(error => {
        console.error('获取角色列表失败:', error)
        this.$message.error('获取角色列表失败：' + (error.message || '未知错误'))
        this.allRoles = []
        return []
      })
    },
    loadUserTypeOptions() {
      getCodeByType('USER_TYPE').then(response => {
        if (response.code === 200) {
          this.userTypeOptions = (response.data || [])
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        } else {
          this.userTypeOptions = []
        }
      }).catch(() => {
        this.userTypeOptions = []
      })
    },
    loadMenuTree() {
      getMenuTree().then(response => {
        if (response.code === 200) {
          this.menuTreeData = response.data || []
        }
      })
    },
    handleAssignMenu(row) {
      if (this.activeTab === 'role') {
        // 用户类型分配菜单
        this.menuDialogTitle = `为用户类型"${row.codeName}"设置菜单权限`
        this.assignType = 'userType'
        this.currentAssignId = row.codeValue // 使用codeValue作为用户类型标识
        // 先清空勾选
        this.checkedMenuIds = []
        // 获取用户类型已有菜单
        getUserTypeMenus(row.codeValue).then(response => {
          if (response.code === 200) {
            this.checkedMenuIds = (response.data || []).map(item => item.menuId)
          }
          this.menuDialogVisible = true
        }).catch(() => {
          this.checkedMenuIds = []
          this.menuDialogVisible = true
        })
      } else {
        // 用户分配菜单（直接通过用户菜单关联）
        this.menuDialogTitle = `为用户"${row.name}"分配菜单`
        this.assignType = 'user'
        this.currentAssignId = row.id
        // 先清空勾选
        this.checkedMenuIds = []
        // 获取用户已有菜单权限
        getUserMenuIds(row.id).then(response => {
          if (response.code === 200) {
            this.checkedMenuIds = response.data || []
          } else {
            this.checkedMenuIds = []
          }
          this.menuDialogVisible = true
        }).catch(() => {
          this.checkedMenuIds = []
          this.menuDialogVisible = true
        })
      }
    },
    handleMenuDialogOpen() {
      // 对话框打开时，设置菜单树的勾选状态
      this.$nextTick(() => {
        if (this.$refs.menuTree && this.checkedMenuIds && this.checkedMenuIds.length > 0) {
          this.$refs.menuTree.setCheckedKeys(this.checkedMenuIds)
        } else if (this.$refs.menuTree) {
          this.$refs.menuTree.setCheckedKeys([])
        }
      })
    },
    handleAssignRole(row) {
      this.currentUserId = row.id
      this.isBatchRoleAssign = false
      this.roleDialogTitle = `为用户"${row.name}"分配角色`
      // 确保用户类型列表已加载
      if (!this.userTypeOptions || this.userTypeOptions.length === 0) {
        this.loadUserTypeOptions()
      }
      // 获取用户当前类型
      if (row.type) {
        this.selectedUserType = String(row.type)
      } else {
        getUserById(row.id).then(response => {
          if (response.code === 200 && response.data && response.data.type) {
            this.selectedUserType = String(response.data.type)
          } else {
            this.selectedUserType = null
          }
          this.roleDialogVisible = true
        }).catch(() => {
          this.selectedUserType = null
          this.roleDialogVisible = true
        })
      }
      if (this.selectedUserType) {
        this.roleDialogVisible = true
      }
    },
    handleBatchAssignRole() {
      if (this.selectedUsers.length === 0) {
        this.$message.warning('请先选择用户')
        return
      }
      this.isBatchRoleAssign = true
      this.roleDialogTitle = `为 ${this.selectedUsers.length} 个用户批量分配角色`
      // 确保用户类型列表已加载
      if (!this.userTypeOptions || this.userTypeOptions.length === 0) {
        this.loadUserTypeOptions()
      }
      this.selectedUserType = null
      this.roleDialogVisible = true
    },
    handleRoleDialogOpen() {
      // 对话框打开时，如果角色列表为空，重新加载
      if (!this.allRoles || this.allRoles.length === 0) {
        this.loadAllRoles()
      }
    },
    getUserTypeName(type) {
      if (!type) return '-'
      const typeStr = String(type)
      const userType = this.userTypeOptions.find(opt => opt.value === typeStr)
      return userType ? userType.label : '-'
    },
    handleSelectionChange(selection) {
      this.selectedUsers = selection
    },
    handleBatchAssignMenu() {
      if (this.selectedUsers.length === 0) {
        this.$message.warning('请先选择用户')
        return
      }
      this.menuDialogTitle = `为 ${this.selectedUsers.length} 个用户批量分配菜单`
      this.assignType = 'batch'
      this.checkedMenuIds = []
      this.menuDialogVisible = true
    },
    handleSaveMenuAssign() {
      const checkedKeys = this.$refs.menuTree.getCheckedKeys()
      const halfCheckedKeys = this.$refs.menuTree.getHalfCheckedKeys()
      // 合并完全勾选和半勾选的菜单ID
      const allKeys = [...checkedKeys, ...halfCheckedKeys]
      
      if (this.assignType === 'userType') {
        // 用户类型分配菜单
        assignUserTypeMenus(this.currentAssignId, allKeys).then(response => {
          if (response.code === 200) {
            this.$message.success('设置成功')
            this.menuDialogVisible = false
          } else {
            this.$message.error(response.message || '设置失败')
          }
        }).catch(error => {
          this.$message.error('设置失败：' + (error.message || '未知错误'))
        })
      } else if (this.assignType === 'batch') {
        // 批量分配菜单
        const userIds = this.selectedUsers.map(user => user.id)
        const promises = userIds.map(userId => assignUserMenus(userId, checkedKeys))
        Promise.all(promises).then(results => {
          const successCount = results.filter(r => r.code === 200).length
          if (successCount === userIds.length) {
            this.$message.success(`成功为 ${successCount} 个用户分配菜单`)
            this.menuDialogVisible = false
            this.selectedUsers = []
          } else {
            this.$message.warning(`部分用户分配失败，成功：${successCount}，失败：${userIds.length - successCount}`)
          }
        }).catch(error => {
          this.$message.error('批量分配失败：' + (error.message || '未知错误'))
        })
      } else {
        // 用户菜单分配（只保存完全勾选的菜单，不包含半选状态的父菜单）
        assignUserMenus(this.currentAssignId, checkedKeys).then(response => {
          if (response.code === 200) {
            this.$message.success('分配成功')
            this.menuDialogVisible = false
          } else {
            this.$message.error(response.message || '分配失败')
          }
        }).catch(error => {
          this.$message.error('分配失败：' + (error.message || '未知错误'))
        })
      }
    },
    handleSaveRoleAssign() {
      if (!this.selectedUserType) {
        this.$message.warning('请选择用户类型')
        return
      }

      if (this.isBatchRoleAssign) {
        // 批量分配用户类型
        const userIds = this.selectedUsers.map(user => user.id)
        const updatePromises = userIds.map(userId => {
          // 获取用户信息
          return getUserById(userId).then(response => {
            if (response.code === 200 && response.data) {
              // 更新用户类型
              const userData = { ...response.data, type: parseInt(this.selectedUserType) }
              return updateUser(userData)
            }
            return Promise.resolve({ code: 500, message: '用户不存在' })
          })
        })

        Promise.all(updatePromises).then(results => {
          const successCount = results.filter(r => r.code === 200).length
          if (successCount === userIds.length) {
            this.$message.success(`成功为 ${successCount} 个用户分配用户类型`)
            this.roleDialogVisible = false
            this.selectedUsers = []
            this.loadUserData() // 刷新用户列表
          } else {
            this.$message.warning(`部分用户分配失败，成功：${successCount}，失败：${userIds.length - successCount}`)
          }
        }).catch(error => {
          this.$message.error('批量分配失败：' + (error.message || '未知错误'))
        })
      } else {
        // 单个用户分配用户类型
        getUserById(this.currentUserId).then(response => {
          if (response.code === 200 && response.data) {
            // 更新用户类型
            const userData = { ...response.data, type: parseInt(this.selectedUserType) }
            updateUser(userData).then(updateResponse => {
              if (updateResponse.code === 200) {
                this.$message.success('分配成功')
                this.roleDialogVisible = false
                this.loadUserData() // 刷新用户列表
              } else {
                this.$message.error(updateResponse.message || '分配失败')
              }
            }).catch(error => {
              this.$message.error('分配失败：' + (error.message || '未知错误'))
            })
          } else {
            this.$message.error('用户不存在')
          }
        }).catch(error => {
          this.$message.error('获取用户信息失败：' + (error.message || '未知错误'))
        })
      }
    }
  }
}
</script>

<style scoped>
.permission-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 15px;
}
</style>



