<template>
  <div class="role-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>角色管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增角色</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="roleCode" label="角色编码" width="120"></el-table-column>
        <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
        <el-table-column prop="roleDesc" label="角色描述"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" @click="handleAssignMenu(scope.row)">分配菜单</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input type="textarea" v-model="form.roleDesc"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 分配菜单对话框 -->
    <el-dialog title="分配菜单" :visible.sync="menuDialogVisible" width="600px">
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
        <el-button type="primary" @click="handleMenuSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRoleList, saveRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/user'
import { getAllMenus } from '@/api/menu'

export default {
  name: 'RoleManagement',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      menuDialogVisible: false,
      dialogTitle: '新增角色',
      isEdit: false,
      currentRole: null,
      form: {
        roleId: null,
        roleCode: '',
        roleName: '',
        roleDesc: ''
      },
      rules: {
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      },
      menuTreeData: [],
      checkedMenuIds: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getRoleList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增角色'
      this.isEdit = false
      this.form = {
        roleId: null,
        roleCode: '',
        roleName: '',
        roleDesc: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑角色'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确定要删除该角色吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteRole(row.roleId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleAssignMenu(row) {
      this.currentRole = row
      this.menuDialogVisible = true
      this.loadMenus()
      this.loadRoleMenus(row.roleId)
    },
    loadMenus() {
      getAllMenus().then(response => {
        if (response.code === 200) {
          this.menuTreeData = response.data
        }
      })
    },
    loadRoleMenus(roleId) {
      getRoleMenus(roleId).then(response => {
        if (response.code === 200) {
          this.checkedMenuIds = response.data.map(item => item.menuId)
          this.$nextTick(() => {
            this.$refs.menuTree.setCheckedKeys(this.checkedMenuIds)
          })
        }
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateRole : saveRole
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    handleMenuSubmit() {
      const checkedKeys = this.$refs.menuTree.getCheckedKeys()
      const halfCheckedKeys = this.$refs.menuTree.getHalfCheckedKeys()
      const menuIds = [...checkedKeys, ...halfCheckedKeys]
      
      assignRoleMenus(this.currentRole.roleId, menuIds).then(response => {
        if (response.code === 200) {
          this.$message.success('分配成功')
          this.menuDialogVisible = false
        } else {
          this.$message.error(response.message || '分配失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.role-management {
  padding: 20px;
}
</style>
