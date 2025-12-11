<template>
  <div class="permission-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>权限管理</span>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="角色权限" name="role">
          <el-table :data="roleTableData" border style="width: 100%" v-loading="roleLoading">
            <el-table-column prop="roleCode" label="角色编码" width="120"></el-table-column>
            <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
            <el-table-column prop="roleDesc" label="角色描述"></el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleAssignMenu(scope.row)">分配菜单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="用户权限" name="user">
          <div style="margin-bottom: 15px;">
            <el-button type="primary" @click="handleBatchAssignMenu" :disabled="selectedUsers.length === 0">批量分配菜单</el-button>
            <span style="margin-left: 10px; color: #909399;">已选择 {{ selectedUsers.length }} 个用户</span>
          </div>
          <el-table 
            :data="userTableData" 
            border 
            style="width: 100%" 
            v-loading="userLoading"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="account" label="账号" width="120"></el-table-column>
            <el-table-column prop="name" label="用户名" width="120"></el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleAssignRole(scope.row)">分配角色</el-button>
                <el-button size="mini" type="success" @click="handleAssignMenu(scope.row)">分配菜单</el-button>
              </template>
            </el-table-column>
          </el-table>
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
      title="分配角色"
      :visible.sync="roleDialogVisible"
      width="500px"
    >
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.roleId" :label="role.roleId">
          {{ role.roleName }}
        </el-checkbox>
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoleAssign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRoleList, getRoleMenus, assignRoleMenus, getUserList, getUserRoles, assignUserRoles, getUserMenuIds, assignUserMenus } from '@/api/user'
import { getMenuTree } from '@/api/menu'

export default {
  name: 'PermissionManagement',
  data() {
    return {
      activeTab: 'role',
      roleLoading: false,
      userLoading: false,
      roleTableData: [],
      userTableData: [],
      selectedUsers: [],
      menuDialogVisible: false,
      menuDialogTitle: '分配菜单',
      menuTreeData: [],
      checkedMenuIds: [],
      currentAssignId: null,
      assignType: '', // 'role' or 'user' or 'batch'
      roleDialogVisible: false,
      allRoles: [],
      selectedRoleIds: [],
      currentUserId: null
    }
  },
  mounted() {
    this.loadRoleData()
    this.loadUserData()
    this.loadAllRoles()
    this.loadMenuTree()
  },
  methods: {
    loadRoleData() {
      this.roleLoading = true
      getRoleList().then(response => {
        if (response.code === 200) {
          this.roleTableData = response.data || []
        }
        this.roleLoading = false
      }).catch(() => {
        this.roleLoading = false
      })
    },
    loadUserData() {
      this.userLoading = true
      getUserList().then(response => {
        if (response.code === 200) {
          this.userTableData = response.data || []
        }
        this.userLoading = false
      }).catch(() => {
        this.userLoading = false
      })
    },
    loadAllRoles() {
      getRoleList().then(response => {
        if (response.code === 200) {
          this.allRoles = response.data || []
        }
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
        // 角色分配菜单
        this.menuDialogTitle = `为角色"${row.roleName}"分配菜单`
        this.assignType = 'role'
        this.currentAssignId = row.roleId
        // 先清空勾选
        this.checkedMenuIds = []
        // 获取角色已有菜单
        getRoleMenus(row.roleId).then(response => {
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
      getUserRoles(row.id).then(response => {
        if (response.code === 200) {
          this.selectedRoleIds = (response.data || []).map(item => item.roleId)
        }
        this.roleDialogVisible = true
      })
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
      
      if (this.assignType === 'role') {
        // 角色分配菜单
        assignRoleMenus(this.currentAssignId, allKeys).then(response => {
          if (response.code === 200) {
            this.$message.success('分配成功')
            this.menuDialogVisible = false
          } else {
            this.$message.error(response.message || '分配失败')
          }
        }).catch(error => {
          this.$message.error('分配失败：' + (error.message || '未知错误'))
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
      assignUserRoles(this.currentUserId, this.selectedRoleIds).then(response => {
        if (response.code === 200) {
          this.$message.success('分配成功')
          this.roleDialogVisible = false
        } else {
          this.$message.error(response.message || '分配失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.permission-management {
  padding: 20px;
}
</style>



