<template>
  <div class="profile-content-wrapper">
    <div v-loading="loading" class="profile-content">
      <div v-if="employeeInfo" class="profile-info">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="工号">{{ employeeInfo.empCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ employeeInfo.empName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ employeeInfo.empSex === 1 ? '男' : employeeInfo.empSex === 2 ? '女' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">
            {{ formatDate(employeeInfo.empBirthday) }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ employeeInfo.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ employeeInfo.empPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ employeeInfo.empEmail || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门编码">{{ employeeInfo.deptCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="职工类型ID">{{ employeeInfo.empTypeId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="employeeInfo.isStop === 0 ? 'success' : 'danger'">
              {{ employeeInfo.isStop === 0 ? '正常' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(employeeInfo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="profile-actions">
          <el-button type="primary" @click="handleEdit">编辑信息</el-button>
        </div>
      </div>
      
      <div v-else class="no-data">
        <i class="el-icon-warning"></i>
        <p>未找到员工信息，请联系管理员</p>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      title="编辑个人信息"
      :visible.sync="editVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="handleEditClose"
    >
      <el-form
        ref="editForm"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="empName">
          <el-input v-model="editForm.empName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="empSex">
          <el-radio-group v-model="editForm.empSex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="empPhone">
          <el-input v-model="editForm.empPhone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="empEmail">
          <el-input v-model="editForm.empEmail" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="出生日期" prop="empBirthday">
          <el-date-picker
            v-model="editForm.empBirthday"
            type="date"
            placeholder="选择日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmployeeByCode, updateEmployee } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    return {
      loading: false,
      saving: false,
      employeeInfo: null,
      editVisible: false,
      editForm: {
        empId: null,
        empName: '',
        empSex: 1,
        empPhone: '',
        empEmail: '',
        empBirthday: null
      },
      editRules: {
        empName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        empPhone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        empEmail: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadEmployeeInfo()
  },
  methods: {
    loadEmployeeInfo() {
      this.loading = true
      const account = this.$store.state.user.userInfo.account
      if (!account) {
        this.$message.error('未获取到用户账号信息')
        this.loading = false
        return
      }
      
      getEmployeeByCode(account).then(response => {
        if (response.code === 200 && response.data) {
          this.employeeInfo = response.data
        } else {
          this.$message.warning('未找到员工信息，可能账号与工号不匹配')
        }
        this.loading = false
      }).catch(error => {
        this.$message.error('获取员工信息失败：' + (error.message || '未知错误'))
        this.loading = false
      })
    },
    handleEdit() {
      if (!this.employeeInfo) {
        this.$message.warning('无法编辑，未找到员工信息')
        return
      }
      this.editForm = {
        empId: this.employeeInfo.empId,
        empName: this.employeeInfo.empName || '',
        empSex: this.employeeInfo.empSex || 1,
        empPhone: this.employeeInfo.empPhone || '',
        empEmail: this.employeeInfo.empEmail || '',
        empBirthday: this.formatDateForPicker(this.employeeInfo.empBirthday)
      }
      this.editVisible = true
    },
    handleEditClose() {
      this.$refs.editForm.resetFields()
    },
    handleSave() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.saving = true
          // 准备提交数据（日期格式后端会自动转换）
          const formData = { ...this.editForm }
          updateEmployee(formData).then(response => {
            if (response.code === 200) {
              this.$message.success('更新成功')
              this.editVisible = false
              this.loadEmployeeInfo()
            } else {
              this.$message.error(response.message || '更新失败')
            }
            this.saving = false
          }).catch(error => {
            this.$message.error('更新失败：' + (error.message || '未知错误'))
            this.saving = false
          })
        }
      })
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    formatDateForPicker(date) {
      if (!date) return null
      // 处理字符串格式的日期
      if (typeof date === 'string') {
        return date.split('T')[0]
      }
      const d = new Date(date)
      if (isNaN(d.getTime())) return null
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style scoped>
.profile-content-wrapper {
  padding: 0;
}

.profile-content {
  min-height: 400px;
}

.profile-info {
  padding: 20px 0;
}

.profile-actions {
  margin-top: 30px;
  text-align: center;
}

.profile-actions .el-button {
  margin: 0 10px;
}

.no-data {
  text-align: center;
  padding: 100px 20px;
  color: #909399;
}

.no-data i {
  font-size: 48px;
  margin-bottom: 20px;
  color: #E6A23C;
}

.no-data p {
  font-size: 16px;
  margin: 0;
}

.dialog-footer {
  text-align: right;
}
</style>
