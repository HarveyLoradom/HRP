<template>
  <div v-loading="loading" class="profile-dialog-content">
    <div v-if="employeeInfo">
      <el-form
        ref="editForm"
        :model="editForm"
        :rules="editRules"
        label-width="120px"
      >
            <!-- 照片放在第一行 -->
            <el-row :gutter="20">
              <el-col :span="24">
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
                      <img 
                        v-if="photoUrl" 
                        :src="photoUrl" 
                        class="avatar" 
                        @error="handleImageError"
                        @load="handleImageLoad"
                      />
                      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    </el-upload>
                    <div style="margin-left: 20px; flex: 1;">
                      <p style="margin: 0; color: #909399; font-size: 12px;">上传大头像照片</p>
                      <p style="margin: 5px 0 0 0; color: #909399; font-size: 12px;">支持 jpg、png、gif 格式，建议尺寸 200x200</p>
                    </div>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="工号">
                  <el-input v-model="employeeInfo.empCode" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="empName">
                  <el-input v-model="editForm.empName" placeholder="请输入姓名"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="性别" prop="empSex">
                  <el-radio-group v-model="editForm.empSex">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
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
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="手机号" prop="empPhone">
                  <el-input v-model="editForm.empPhone" placeholder="请输入手机号"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="empEmail">
                  <el-input v-model="editForm.empEmail" placeholder="请输入邮箱"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身份证号">
                  <el-input v-model="employeeInfo.idCard" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="部门">
                  <el-input v-model="employeeInfo.deptName" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户类型">
                  <el-input v-model="employeeInfo.userTypeName" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="职工类型">
                  <el-input v-model="employeeInfo.empTypeName" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-tag :type="employeeInfo.isStop === 0 ? 'success' : 'danger'">
                    {{ employeeInfo.isStop === 0 ? '正常' : '停用' }}
                  </el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="创建时间">
                  <el-input :value="formatDate(employeeInfo.createTime)" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="银行卡号" prop="bankAccount">
                  <el-input v-model="editForm.bankAccount" placeholder="请输入银行卡号"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="银行名称" prop="bankName">
                  <el-input v-model="editForm.bankName" placeholder="请输入银行名称"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <!-- 按钮区域，放在右下角 -->
          <div class="profile-form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          </div>
    </div>
    
    <div v-else class="no-data">
      <i class="el-icon-warning"></i>
      <p>未找到员工信息，请联系管理员</p>
    </div>
  </div>
</template>

<script>
import { getEmployeeByCode, updateEmployee, uploadEmployeePhoto, deleteEmployeePhoto } from '@/api/user'
import request from '@/api/request'
import Cookies from 'js-cookie'

export default {
  name: 'Profile',
  data() {
    return {
      loading: false,
      saving: false,
      employeeInfo: null,
      originalFormData: null, // 保存原始数据，用于取消时恢复
      photoUrl: '',
      uploadedPhotoPath: null, // 记录新上传的照片路径，用于取消时删除
      uploadPhotoUrl: '/api/auth/attachment/upload-photo',
      uploadHeaders: {},
      editForm: {
        empId: null,
        empCode: '',
        empName: '',
        empSex: 1,
        empPhone: '',
        empEmail: '',
        empBirthday: null,
        bankAccount: '',
        bankName: '',
        photo: ''
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
    // 初始化上传请求头
    this.initUploadHeaders()
    // 加载员工信息
    this.loadEmployeeInfo()
  },
  methods: {
    initUploadHeaders() {
      // 设置上传请求头，使用响应式方式
      const token = Cookies.get('token') || localStorage.getItem('token')
      if (token) {
        this.$set(this.uploadHeaders, 'Authorization', 'Bearer ' + token)
      }
    },
    loadEmployeeInfo() {
      this.loading = true
      // 从 store 或 localStorage 获取账号
      const userInfo = this.$store.state.user.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
      const account = userInfo.account
      
      if (!account) {
        this.$message.error('未获取到用户账号信息，请重新登录')
        this.loading = false
        return
      }
      
      console.log('加载员工信息，账号:', account)
      getEmployeeByCode(account).then(response => {
        if (response.code === 200 && response.data) {
          this.employeeInfo = response.data
          // 初始化编辑表单
          this.editForm = {
            empId: this.employeeInfo.empId,
            empCode: this.employeeInfo.empCode,
            empName: this.employeeInfo.empName || '',
            empSex: this.employeeInfo.empSex || 1,
            empPhone: this.employeeInfo.empPhone || '',
            empEmail: this.employeeInfo.empEmail || '',
            empBirthday: this.formatDateForPicker(this.employeeInfo.empBirthday),
            bankAccount: this.employeeInfo.bankAccount || '',
            bankName: this.employeeInfo.bankName || '',
            photo: this.employeeInfo.photo || ''
          }
          // 保存原始数据，用于取消时恢复
          this.originalFormData = JSON.parse(JSON.stringify(this.editForm))
          // 重置上传照片路径标记
          this.uploadedPhotoPath = null
          // 设置照片显示
          console.log('员工信息加载完成，照片路径:', this.employeeInfo.photo)
          if (this.employeeInfo.photo) {
            this.photoUrl = this.getPhotoUrl(this.employeeInfo.photo)
            console.log('设置照片URL:', this.photoUrl)
          } else {
            this.photoUrl = ''
            console.log('员工没有照片')
          }
        } else {
          this.$message.warning('未找到员工信息，可能账号与工号不匹配')
        }
        this.loading = false
      }).catch(error => {
        this.$message.error('获取员工信息失败：' + (error.message || '未知错误'))
        this.loading = false
      })
    },
    async handleCancel() {
      await this.cleanupUnsavedPhoto()
      // 恢复原始数据
      if (this.originalFormData) {
        this.editForm = JSON.parse(JSON.stringify(this.originalFormData))
        // 恢复照片显示
        if (this.employeeInfo && this.employeeInfo.photo) {
          this.photoUrl = this.getPhotoUrl(this.employeeInfo.photo)
        } else {
          this.photoUrl = ''
        }
      }
      
      // 重置上传照片路径标记
      this.uploadedPhotoPath = null
      
      // 重置表单验证状态
      if (this.$refs.editForm) {
        this.$refs.editForm.clearValidate()
      }
      // 触发关闭事件
      this.$emit('close')
    },
    async cleanupUnsavedPhoto() {
      // 如果上传了新照片但没有保存，需要删除上传的文件
      if (this.uploadedPhotoPath && 
          this.originalFormData && 
          this.uploadedPhotoPath !== this.originalFormData.photo) {
        try {
          await deleteEmployeePhoto(this.uploadedPhotoPath)
          console.log('已删除未保存的照片:', this.uploadedPhotoPath)
        } catch (error) {
          console.error('删除未保存的照片失败:', error)
          // 不显示错误，静默处理
        }
      }
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
              // 更新本地显示的数据
              if (this.employeeInfo) {
                this.employeeInfo.empName = this.editForm.empName
                this.employeeInfo.empSex = this.editForm.empSex
                this.employeeInfo.empPhone = this.editForm.empPhone
                this.employeeInfo.empEmail = this.editForm.empEmail
                this.employeeInfo.empBirthday = this.editForm.empBirthday
                this.employeeInfo.bankAccount = this.editForm.bankAccount
                this.employeeInfo.bankName = this.editForm.bankName
                this.employeeInfo.photo = this.editForm.photo
              }
              // 更新原始数据
              this.originalFormData = JSON.parse(JSON.stringify(this.editForm))
              // 清空上传照片路径标记（已保存，不再需要删除）
              this.uploadedPhotoPath = null
              // 关闭对话框
              this.$emit('close')
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
          if (this.uploadedPhotoPath && this.uploadedPhotoPath !== this.originalFormData?.photo) {
            // 旧照片是新上传的，取消时会删除
          }
          this.uploadedPhotoPath = photoPath
          this.editForm.photo = photoPath
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
      let errorMsg = '照片上传失败'
      if (err && err.response) {
        const res = err.response.data
        if (res && res.message) {
          errorMsg = res.message
        } else if (res && res.data && res.data.message) {
          errorMsg = res.data.message
        }
      } else if (err && err.message) {
        errorMsg = err.message
      }
      this.$message.error(errorMsg)
    },
    handleImageError(event) {
      console.error('图片加载失败:', this.photoUrl)
      console.error('事件对象:', event)
      // 图片加载失败时，显示警告但不清空photoUrl，让用户可以重新上传
      // 如果需要，可以选择清空photoUrl来显示占位符
      // this.photoUrl = ''
    },
    handleImageLoad(event) {
      console.log('图片加载成功:', this.photoUrl)
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
      if (!photoPath) {
        console.log('照片路径为空')
        return ''
      }
      
      console.log('构建照片URL, 原始路径:', photoPath)
      
      // 如果是完整URL，直接返回
      if (photoPath.startsWith('http://') || photoPath.startsWith('https://')) {
        console.log('完整URL，直接返回:', photoPath)
        return photoPath
      }
      
      // photoPath 可能的格式：
      // 1. employee-photos/xxx.jpg (相对路径)
      // 2. /uploads/employee-photos/xxx.jpg (已包含uploads前缀)
      // 3. F:/data/uploads/employee-photos/xxx.jpg (完整Windows路径)
      
      // 处理Windows路径格式（包含盘符）
      if (photoPath.match(/^[A-Za-z]:[\\/]/)) {
        // 提取uploads之后的部分
        const uploadsIndex = photoPath.indexOf('uploads')
        if (uploadsIndex >= 0) {
          photoPath = photoPath.substring(uploadsIndex + 7) // 7 = 'uploads'.length
          if (photoPath.startsWith('/') || photoPath.startsWith('\\')) {
            photoPath = photoPath.substring(1)
          }
          photoPath = photoPath.replace(/\\/g, '/')
        } else {
          // 如果没有uploads，尝试提取最后一个路径部分
          const parts = photoPath.split(/[\\/]/)
          photoPath = parts[parts.length - 1]
        }
      }
      
      // 如果路径已经包含 /uploads/，提取uploads之后的部分
      if (photoPath.includes('/uploads/')) {
        const parts = photoPath.split('/uploads/')
        photoPath = parts[parts.length - 1]
      } else if (photoPath.includes('uploads')) {
        const parts = photoPath.split('uploads')
        photoPath = parts[parts.length - 1]
        if (photoPath.startsWith('/') || photoPath.startsWith('\\')) {
          photoPath = photoPath.substring(1)
        }
        photoPath = photoPath.replace(/\\/g, '/')
      }
      
      // 统一处理路径分隔符
      photoPath = photoPath.replace(/\\/g, '/')
      
      // 如果路径已经以 /api/uploads/ 开头，直接返回
      if (photoPath.startsWith('/api/uploads/')) {
        console.log('已是完整API路径，直接返回:', photoPath)
        return photoPath
      }
      
      // 如果路径已经以 /uploads/ 开头，添加 /api 前缀
      if (photoPath.startsWith('/uploads/')) {
        const url = '/api' + photoPath
        console.log('添加/api前缀:', url)
        return url
      }
      
      // 默认情况：photoPath 是相对路径（如 employee-photos/xxx.jpg）
      // 后端静态资源映射是 /uploads/**，前端代理会将 /api 转发到后端
      // 所以应该构建为 /api/uploads/employee-photos/xxx.jpg
      const url = '/api/uploads/' + photoPath
      console.log('构建的照片URL:', url)
      return url
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
.profile-dialog-content {
  min-height: 300px;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
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

.profile-form-actions {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
  margin-top: 20px;
}

.profile-form-actions .el-button {
  margin-left: 10px;
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



