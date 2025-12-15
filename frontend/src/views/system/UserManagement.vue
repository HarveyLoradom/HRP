<template>
  <div class="user-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>用户管理</span>
        <div style="float: right;">
          <el-button type="text" @click="handleDownloadTemplate">下载导入模板</el-button>
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-left: 10px;"
          >
            <el-button type="text">批量导入</el-button>
          </el-upload>
          <el-button style="margin-left: 10px; padding: 3px 0" type="text" @click="handleAdd">新增用户</el-button>
        </div>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工号/姓名">
          <el-input v-model="searchForm.keyword" placeholder="请输入工号或姓名" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptCode" placeholder="请选择部门" clearable style="width: 200px;">
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptCode"
              :label="dept.deptName"
              :value="dept.deptCode"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="empCode" label="工号" width="120">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              @click="handleViewEmployee(scope.row.empCode)"
              style="color: #409EFF;"
            >
              {{ scope.row.empCode || '-' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.deptName">{{ scope.row.deptName }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="锁定状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.locked === 1" type="danger">已锁定</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="info" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button v-if="scope.row.locked === 1" size="mini" type="success" @click="handleUnlock(scope.row)">解锁</el-button>
            <el-button size="mini" :type="scope.row.isStop === 0 ? 'warning' : 'success'" @click="handleToggleStatus(scope.row)">
              {{ scope.row.isStop === 0 ? '停用' : '启用' }}
            </el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查看员工信息对话框 -->
    <el-dialog title="员工信息" :visible.sync="employeeDialogVisible" width="700px">
      <div v-loading="!employeeInfo">
      <el-descriptions :column="2" border v-if="employeeInfo">
        <el-descriptions-item label="工号">{{ employeeInfo.empCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ employeeInfo.empName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ employeeInfo.empSex === 1 ? '男' : employeeInfo.empSex === 2 ? '女' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ employeeInfo.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">
          {{ formatDate(employeeInfo.empBirthday) }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">{{ employeeInfo.empPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ employeeInfo.empEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ employeeInfo.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职工类型">{{ employeeInfo.empTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ employeeInfo.userTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="employeeInfo.isStop === 0 ? 'success' : 'danger'">
            {{ employeeInfo.isStop === 0 ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(employeeInfo.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ employeeInfo.createUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(employeeInfo.updateTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="银行卡号">{{ employeeInfo.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行名称">{{ employeeInfo.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="照片" :span="2">
          <img v-if="employeeInfo.photo" :src="getPhotoUrl(employeeInfo.photo)" style="width: 120px; height: 120px; object-fit: cover; border-radius: 4px;" />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
        <div v-if="!employeeInfo" style="text-align: center; padding: 40px; color: #909399;">
          正在加载员工信息...
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="employeeDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" @close="handleDialogCancel">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <!-- 照片放在第一行 -->
        <el-form-item label="照片">
          <div style="display: flex; align-items: flex-start;">
            <el-upload
              class="avatar-uploader"
              :action="uploadPhotoUrl"
              :show-file-list="false"
              :on-success="handlePhotoSuccess"
              :on-error="handlePhotoError"
              :before-upload="beforePhotoUpload"
              :headers="uploadHeaders"
            >
              <img v-if="photoUrl" :src="photoUrl" class="avatar" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div style="margin-left: 20px; flex: 1;">
              <p style="margin: 0; color: #909399; font-size: 12px;">上传大头像照片</p>
              <p style="margin: 5px 0 0 0; color: #909399; font-size: 12px;">支持 jpg、png、gif 格式，建议尺寸 200x200</p>
            </div>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号（账号）" prop="account">
              <el-input v-model="form.account" :disabled="isEdit" placeholder="请输入工号，将作为登录账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="empSex">
              <el-radio-group v-model="form.empSex">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期" prop="empBirthday">
              <el-date-picker
                v-model="form.empBirthday"
                type="date"
                placeholder="选择日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="empEmail">
              <el-input v-model="form.empEmail" placeholder="请输入邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%" clearable>
                <el-option
                  v-for="item in deptList"
                  :key="item.deptId"
                  :label="item.deptName"
                  :value="item.deptId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择用户类型" style="width: 100%">
                <el-option
                  v-for="item in userTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职工类型" prop="empTypeId">
              <el-select v-model="form.empTypeId" placeholder="请选择职工类型" style="width: 100%">
                <el-option
                  v-for="item in empTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="银行卡号" prop="bankAccount">
              <el-input v-model="form.bankAccount" placeholder="请输入银行卡号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行名称" prop="bankName">
              <el-input v-model="form.bankName" placeholder="请输入银行名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item 
          label="新密码" 
          v-if="isEdit"
          :rules="[{ min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }]"
          prop="password"
        >
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="留空则不修改密码"
            show-password
            clearable
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, getUserById, saveUser, updateUser, deleteUser, toggleUserStatus, resetUserPassword, unlockUser, getAllEmployeesWithUser, searchEmployees, getEmployeeByCode, getCodeByType, getDeptList, getUserByAccount, uploadEmployeePhoto, deleteEmployeePhoto } from '@/api/user'
import { getCodeTypeOptions } from '@/utils/codeType'
import request from '@/api/request'
import Cookies from 'js-cookie'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      allEmployees: [],
      searchForm: {
        keyword: '',
        deptCode: ''
      },
      deptOptions: [],
      dialogVisible: false,
      dialogTitle: '新增用户',
      isEdit: false,
      form: {
        id: null,
        account: '',
        name: '',
        empSex: 1,
        idCard: '',
        empBirthday: '',
        phone: '',
        empEmail: '',
        deptId: null,
        type: null,
        empTypeId: null,
        password: '', // 新增时不传密码，后端使用默认密码
        bankAccount: '',
        bankName: '',
        photo: ''
      },
      photoUrl: '',
      uploadPhotoUrl: '/api/auth/attachment/upload-photo',
      rules: {
        account: [{ required: true, message: '请输入工号（账号）', trigger: 'blur' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        empSex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        type: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
        empTypeId: [{ required: true, message: '请选择职工类型', trigger: 'change' }],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        empEmail: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        idCard: [
          { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
        ]
      },
      employeeDialogVisible: false,
      employeeInfo: null,
      userTypeOptions: [],
      empTypeOptions: [],
      deptList: [],
      uploadUrl: '/api/auth/user/import',
      uploadHeaders: {}
    }
  },
  mounted() {
    this.loadData()
    this.loadCodeOptions()
    this.loadDeptList()
    // 设置上传请求头
    const token = this.$store.state.user.token || Cookies.get('token')
    if (token) {
      this.uploadHeaders['Authorization'] = 'Bearer ' + token
    }
  },
  methods: {
    loadData() {
      this.loading = true
      getAllEmployeesWithUser().then(response => {
        if (response.code === 200) {
          this.allEmployees = response.data || []
          this.handleSearch()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadCodeOptions() {
      // 加载用户类型选项（从sys_code表，codeType为USER_TYPE）
      getCodeByType('USER_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.userTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        // 如果获取失败，使用默认值
        this.userTypeOptions = [
          { label: '管理员', value: '1' },
          { label: '普通用户', value: '2' }
        ]
      })
      
      // 加载职工类型选项（从sys_code表，codeType为EMP_TYPE）
      getCodeByType('EMP_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.empTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        this.empTypeOptions = []
      })
    },
    loadDeptList() {
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptList = response.data || []
          this.deptOptions = response.data || []
        }
      })
    },
    handleSearch() {
      let filtered = [...this.allEmployees]
      
      // 按工号/姓名筛选
      if (this.searchForm.keyword && this.searchForm.keyword.trim()) {
        const keyword = this.searchForm.keyword.trim().toLowerCase()
        filtered = filtered.filter(emp => 
          (emp.empCode && emp.empCode.toLowerCase().includes(keyword)) ||
          (emp.empName && emp.empName.toLowerCase().includes(keyword))
        )
      }
      
      // 按部门筛选
      if (this.searchForm.deptCode) {
        filtered = filtered.filter(emp => emp.deptCode === this.searchForm.deptCode)
      }
      
      this.tableData = filtered
    },
    handleReset() {
      this.searchForm.keyword = ''
      this.searchForm.deptCode = ''
      this.tableData = this.allEmployees
    },
    handleViewEmployee(empCode) {
      if (!empCode) {
        this.$message.warning('工号不能为空，无法查看员工信息')
        return
      }
      // 使用对话框的loading，而不是全局loading，避免影响表格显示
          this.employeeDialogVisible = true
      // 先清空之前的数据
      this.employeeInfo = null
      
      // 获取员工信息
      getEmployeeByCode(empCode).then(empResponse => {
        if (empResponse.code === 200 && empResponse.data) {
          this.employeeInfo = empResponse.data
        } else {
          this.$message.error(empResponse.message || '获取员工信息失败')
          this.employeeDialogVisible = false
        }
      }).catch(error => {
        this.$message.error('获取员工信息失败：' + (error.message || '未知错误'))
        this.employeeDialogVisible = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.isEdit = false
      this.photoUrl = ''
      this.uploadedPhotoPath = null
      this.originalPhotoPath = null
      this.form = {
        id: null,
        account: '',
        name: '',
        empSex: 1,
        idCard: '',
        empBirthday: '',
        phone: '',
        empEmail: '',
        deptId: null,
        type: null,
        empTypeId: null,
        password: '', // 新增时不传密码，后端使用默认密码
        bankAccount: '',
        bankName: '',
        photo: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (!row.userId) {
        this.$message.warning('该员工尚未创建用户账号')
        return
      }
      this.dialogTitle = '编辑用户'
      this.isEdit = true
      // 先获取员工详细信息
      if (row.empCode) {
        getEmployeeByCode(row.empCode).then(response => {
          if (response.code === 200 && response.data) {
            const emp = response.data
            this.form = {
              id: row.userId,
              account: row.account,
              name: row.empName || emp.empName || '',
              empSex: emp.empSex || 1,
              idCard: emp.idCard || '',
              empBirthday: this.formatDateForPicker(emp.empBirthday),
              phone: row.phone || emp.empPhone || '',
              empEmail: emp.empEmail || '',
              deptId: emp.deptId || null,
              type: row.userType ? String(row.userType) : null,
              empTypeId: emp.empTypeId ? String(emp.empTypeId) : null,
              password: '',
              bankAccount: emp.bankAccount || '',
              bankName: emp.bankName || '',
              photo: emp.photo || ''
            }
            this.photoUrl = emp.photo ? this.getPhotoUrl(emp.photo) : ''
            // 记录原始照片路径
            this.originalPhotoPath = emp.photo || null
            this.uploadedPhotoPath = null
          } else {
            // 如果获取员工信息失败，使用基本信息
            this.form = {
              id: row.userId,
              account: row.account,
              name: row.empName || '',
              empSex: 1,
              idCard: '',
              empBirthday: '',
              phone: row.phone || row.empPhone || '',
              empEmail: '',
              deptId: null,
              type: row.userType ? String(row.userType) : null,
              empTypeId: null,
              password: '',
              bankAccount: '',
              bankName: '',
              photo: ''
            }
            this.photoUrl = ''
          }
        })
      } else {
        this.form = {
          id: row.userId,
          account: row.account,
          name: row.empName || '',
          empSex: 1,
          idCard: '',
          empBirthday: '',
          phone: row.phone || row.empPhone || '',
          empEmail: '',
          deptId: null,
          type: row.userType ? String(row.userType) : null,
          empTypeId: null,
          password: '',
          bankAccount: '',
          bankName: '',
          photo: ''
        }
        this.photoUrl = ''
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateUser : saveUser
          const formData = { ...this.form }
          // 转换类型
          if (formData.type) {
            formData.type = Number(formData.type)
          }
          if (formData.empTypeId) {
            formData.empTypeId = Number(formData.empTypeId)
          }
          if (formData.empSex) {
            formData.empSex = Number(formData.empSex)
          }
          if (formData.deptId) {
            formData.deptId = Number(formData.deptId)
          }
          // 处理出生日期：如果为空字符串，设置为null
          if (formData.empBirthday === '') {
            formData.empBirthday = null
          }
          // 新增时不传密码字段，后端使用默认密码
          if (!this.isEdit) {
            delete formData.password
          }
          // 编辑时如果密码为空，则不传密码字段
          if (this.isEdit && !formData.password) {
            delete formData.password
          }
          api(formData).then(response => {
            if (response.code === 200) {
              // 保存成功，清空上传照片标记（已保存，不再需要删除）
              this.uploadedPhotoPath = null
              this.originalPhotoPath = null
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          }).catch(error => {
            this.$message.error('操作失败：' + (error.message || '未知错误'))
          })
        }
      })
    },
    async handleDialogCancel() {
      // 如果上传了新照片但没有保存，需要删除上传的文件
      if (this.uploadedPhotoPath && 
          this.originalPhotoPath !== this.uploadedPhotoPath) {
        try {
          await deleteEmployeePhoto(this.uploadedPhotoPath)
          console.log('已删除未保存的照片:', this.uploadedPhotoPath)
        } catch (error) {
          console.error('删除未保存的照片失败:', error)
          // 不显示错误，静默处理
        }
      }
      
      // 恢复原始照片显示
      if (this.originalPhotoPath) {
        this.form.photo = this.originalPhotoPath
        this.photoUrl = this.getPhotoUrl(this.originalPhotoPath)
      } else {
        this.form.photo = ''
        this.photoUrl = ''
      }
      
      // 重置上传照片路径标记
      this.uploadedPhotoPath = null
      
      // 关闭对话框
      this.dialogVisible = false
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
    },
    handleDelete(row) {
      if (!row.userId) {
        this.$message.warning('该员工尚未创建用户账号')
        return
      }
      this.$confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteUser(row.userId).then(response => {
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
      if (!row.userId) {
        this.$message.warning('该员工尚未创建用户账号')
        return
      }
      const action = row.isStop === 0 ? '停用' : '启用'
      this.$confirm(`确定要${action}该用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        toggleUserStatus(row.userId).then(response => {
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
      if (!row.userId) {
        this.$message.warning('该员工尚未创建用户账号')
        return
      }
      this.$confirm(`确定要重置用户"${row.empName}"的密码吗？密码将重置为系统配置的原始密码。`, '重置密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        resetUserPassword(row.userId).then(response => {
          if (response.code === 200) {
            this.$message.success('密码重置成功，密码已重置为系统配置的原始密码')
            this.loadData()
          } else {
            this.$message.error(response.message || '密码重置失败')
          }
        })
      }).catch(() => {})
    },
    handleUnlock(row) {
      if (!row.userId) {
        this.$message.warning('该员工尚未创建用户账号')
        return
      }
      this.$confirm(`确定要解锁用户"${row.empName}"吗？`, '解锁用户', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        unlockUser(row.userId).then(response => {
          if (response.code === 200) {
            this.$message.success('解锁成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '解锁失败')
          }
        })
      }).catch(() => {})
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
    handlePhotoSuccess(response, file, fileList) {
      console.log('上传成功响应:', response)
      console.log('响应类型:', typeof response)
      
      // el-upload 使用原生 XMLHttpRequest，响应可能是：
      // 1. 字符串（JSON字符串，需要解析）
      // 2. 对象（浏览器自动解析的JSON）
      // 3. { code: 200, data: "photo-path", message: "" }
      
      let res = response
      
      // 如果是字符串，尝试解析为JSON
      if (typeof response === 'string') {
        try {
          res = JSON.parse(response)
        } catch (e) {
          console.error('解析响应JSON失败:', e)
          this.$message.error('照片上传失败：响应格式错误')
          return
        }
      }
      
      // 如果响应有data属性，可能是axios包装的
      if (response && response.data && typeof response.data === 'object') {
        res = response.data
      }
      
      // 如果响应有data属性且是对象，可能需要进一步解析
      if (res && res.data && typeof res.data === 'object' && res.code === 200) {
        // 如果data是对象，可能data.data才是真正的数据
        if (res.data.data) {
          res = res.data
        }
      }
      
      // 如果响应有data属性且是对象，可能需要进一步解析
      if (res && res.data && typeof res.data === 'object' && res.code === 200) {
        // 如果data是对象，可能data.data才是真正的数据
        if (res.data.data) {
          res = res.data
        }
      }
      
      // 检查响应格式
      if (res && res.code === 200) {
        // 获取照片路径 - 确保是字符串类型
        let photoPath = res.data
        if (photoPath && typeof photoPath === 'object' && photoPath.data) {
          photoPath = photoPath.data
        }
        
        if (photoPath && typeof photoPath === 'string') {
          console.log('照片路径:', photoPath)
          // 记录新上传的照片路径（用于取消时删除）
          this.uploadedPhotoPath = photoPath
          this.form.photo = photoPath
          this.photoUrl = this.getPhotoUrl(photoPath)
          console.log('照片URL:', this.photoUrl)
          // 强制刷新图片显示
          this.$nextTick(() => {
            const img = this.$el.querySelector('.avatar')
            if (img) {
              // 先清空src，再设置新的src，强制重新加载
              img.src = ''
              setTimeout(() => {
                img.src = this.photoUrl
                img.onload = () => {
                  console.log('图片加载成功')
                }
                img.onerror = () => {
                  console.error('图片加载失败，URL:', this.photoUrl)
                }
              }, 50)
            }
          })
          this.$message.success('照片上传成功')
        } else {
          console.error('响应中没有照片路径或路径格式不正确, res:', res, 'photoPath:', photoPath)
          this.$message.error('照片上传成功，但未返回照片路径')
        }
      } else {
        const errorMsg = (res && res.message) || '照片上传失败'
        console.error('上传失败:', res)
        this.$message.error(errorMsg)
      }
    },
    handlePhotoError(err, file, fileList) {
      console.error('上传失败:', err)
      this.$message.error('照片上传失败：' + (err.message || '未知错误'))
    },
    beforePhotoUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
        return false
      }
      return true
    },
    getPhotoUrl(photoPath) {
      if (!photoPath) return ''
      // 如果是完整URL，直接返回
      if (photoPath.startsWith('http://') || photoPath.startsWith('https://')) {
        return photoPath
      }
      // photoPath 格式应该是：employee-photos/xxx.jpg
      // 需要构建为：/api/uploads/employee-photos/xxx.jpg
      
      // 如果路径已经包含 /uploads/，直接使用
      if (photoPath.includes('/uploads/')) {
        // 如果已经是完整路径，直接返回
        if (photoPath.startsWith('/')) {
          return photoPath
        }
        // 否则添加 /api 前缀
        return '/api/' + photoPath
      }
      
      // 默认情况：photoPath 是相对路径（如 employee-photos/xxx.jpg）
      // 构建为 /api/uploads/employee-photos/xxx.jpg
      const url = '/api/uploads/' + photoPath
      console.log('构建的照片URL:', url)
      return url
    },
    handleDownloadTemplate() {
      // 下载Excel模板
      window.open('/api/auth/user/template', '_blank')
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                      file.type === 'application/vnd.ms-excel'
      if (!isExcel) {
        this.$message.error('只能上传Excel文件！')
        return false
      }
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB！')
        return false
      }
      return true
    },
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.$message.success('导入成功')
        this.loadData()
      } else {
        this.$message.error(response.message || '导入失败')
      }
    },
    handleUploadError(error) {
      this.$message.error('导入失败：' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>
