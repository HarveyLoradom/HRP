<template>
  <div class="user-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>用户管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增用户</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="account" label="账号" width="120"></el-table-column>
        <el-table-column prop="name" label="用户名" width="120"></el-table-column>
        <el-table-column prop="type" label="用户类型" width="100">
          <template slot-scope="scope">
            {{ scope.row.type === 1 ? '管理员' : scope.row.type === 2 ? '普通用户' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="320">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="info" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button size="mini" :type="scope.row.isStop === 0 ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
              {{ scope.row.isStop === 0 ? '停用' : '启用' }}
            </el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" :disabled="isEdit" placeholder="请输入账号（工号）"></el-input>
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="用户类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择用户类型">
            <el-option label="管理员" :value="1"></el-option>
            <el-option label="普通用户" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（默认123456）"></el-input>
        </el-form-item>
        <el-form-item label="密码" v-else>
          <el-input v-model="form.password" type="password" placeholder="留空则不修改密码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, getUserById, saveUser, updateUser, deleteUser, toggleUserStatus, resetUserPassword } from '@/api/user'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增用户',
      isEdit: false,
      form: {
        id: null,
        account: '',
        name: '',
        type: 1,
        phone: '',
        password: '123456'
      },
      rules: {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        type: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getUserList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.isEdit = false
      this.form = {
        id: null,
        account: '',
        name: '',
        type: 1,
        phone: '',
        password: '123456'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.isEdit = true
      this.form = {
        id: row.id,
        account: row.account,
        name: row.name,
        type: row.type,
        phone: row.phone || '',
        password: ''
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateUser : saveUser
          const formData = { ...this.form }
          // 编辑时如果密码为空，则不传密码字段
          if (this.isEdit && !formData.password) {
            delete formData.password
          }
          api(formData).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteUser(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      }).catch(() => {})
    },
    handleToggleStatus(row) {
      const action = row.isStop === 0 ? '停用' : '启用'
      this.$confirm(`确定要${action}该用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        toggleUserStatus(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.loadData()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        })
      }).catch(() => {})
    },
    handleResetPassword(row) {
      this.$confirm(`确定要重置用户"${row.name}"的密码吗？密码将重置为系统配置的原始密码。`, '重置密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        resetUserPassword(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('密码重置成功，密码已重置为系统配置的原始密码')
            this.loadData()
          } else {
            this.$message.error(response.message || '密码重置失败')
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}
</style>
