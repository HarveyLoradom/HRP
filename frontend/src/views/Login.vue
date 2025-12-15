<template>
  <div class="login-container">
    <!-- 背景装饰图标 -->
    <div class="background-icons">
      <div class="icon-item icon-1"><i class="el-icon-user-solid"></i></div>
      <div class="icon-item icon-2"><i class="el-icon-data-line"></i></div>
      <div class="icon-item icon-3"><i class="el-icon-first-aid-kit"></i></div>
      <div class="icon-item icon-4"><i class="el-icon-document"></i></div>
      <div class="icon-item icon-5"><i class="el-icon-money"></i></div>
      <div class="icon-item icon-6"><i class="el-icon-medicine-box"></i></div>
      <div class="icon-item icon-7"><i class="el-icon-collection"></i></div>
      <div class="icon-item icon-8"><i class="el-icon-data-analysis"></i></div>
    </div>
    
    <!-- 居中登录区域 -->
    <div class="login-wrapper">
      <div class="login-box">
      <div class="login-header">
        <h2 class="main-title">HRP医疗资源管理</h2>
        <p class="sub-title">Hospital Resource Planning System</p>
        <div class="header-divider"></div>
        <h3>用户登录</h3>
      </div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        auto-complete="on"
        label-position="left"
      >
        <el-form-item prop="account">
          <div class="input-wrapper">
            <span class="svg-container">
              <i class="el-icon-user"></i>
            </span>
            <el-input
              ref="account"
              v-model="loginForm.account"
              placeholder="请输入工号"
              name="account"
              type="text"
              tabindex="1"
              auto-complete="on"
            />
          </div>
        </el-form-item>

        <el-form-item prop="password">
          <div class="input-wrapper">
            <span class="svg-container">
              <i class="el-icon-lock"></i>
            </span>
            <el-input
              :key="passwordType"
              ref="password"
              v-model="loginForm.password"
              :type="passwordType"
              placeholder="请输入密码"
              name="password"
              tabindex="2"
              auto-complete="on"
              @keyup.enter.native="handleLogin"
            />
            <span class="show-pwd" @click="showPwd" :title="passwordType === 'password' ? '显示密码' : '隐藏密码'">
              <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-close'"></i>
            </span>
          </div>
        </el-form-item>

        <el-form-item v-if="needCaptcha" prop="captchaCode">
          <div class="captcha-container">
            <div class="input-wrapper" style="width: 60%;">
              <span class="svg-container">
                <i class="el-icon-picture"></i>
              </span>
              <el-input
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
              />
            </div>
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else>点击刷新</span>
            </div>
          </div>
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="login-button"
          @click.native.prevent="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>
      <div class="login-footer">
        <p>© 2025 HRP系统. All rights reserved.</p>
      </div>
      </div>
    </div>

    <!-- 强制修改密码对话框 -->
    <el-dialog
      title="修改密码"
      :visible.sync="changePasswordDialogVisible"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      width="450px"
      center
    >
      <el-form
        ref="changePasswordForm"
        :model="changePasswordForm"
        :rules="changePasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          :loading="changePasswordLoading"
          type="primary"
          @click="handleChangePassword"
        >
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { login, getCaptcha, forceChangePassword } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.changePasswordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        account: '',
        password: '',
        captchaCode: '',
        captchaKey: ''
      },
      loginRules: {
        account: [{ required: true, trigger: 'blur', message: '请输入工号' }],
        password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
      },
      loading: false,
      passwordType: 'password',
      needCaptcha: false,
      captchaImage: '',
      changePasswordDialogVisible: false,
      changePasswordLoading: false,
      changePasswordForm: {
        userId: '',
        newPassword: '',
        confirmPassword: ''
      },
      changePasswordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度必须在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      pendingLoginData: null // 保存待登录的数据
    }
  },
  mounted() {
    if (this.$store.state.user.token) {
      this.$router.push('/hrp')
    }
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    async refreshCaptcha() {
      try {
        const response = await getCaptcha()
        if (response.code === 200) {
          this.captchaImage = response.data.image
          this.loginForm.captchaKey = response.data.key
        }
      } catch (error) {
        this.$message.error('获取验证码失败')
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          const loginData = {
            account: this.loginForm.account,
            password: this.loginForm.password
          }

          if (this.needCaptcha) {
            loginData.captchaCode = this.loginForm.captchaCode
            loginData.captchaKey = this.loginForm.captchaKey
          }

          this.$store.dispatch('user/login', loginData)
            .then(response => {
              if (response && response.data && response.data.needCaptcha) {
                this.needCaptcha = true
                this.$message.warning(response.data.message || '需要验证码')
                this.refreshCaptcha()
                this.loading = false
                return
              }

              // 检查是否需要强制修改密码
              if (response && response.data && response.data.needChangePassword) {
                this.changePasswordForm.userId = response.data.userId
                this.changePasswordDialogVisible = true
                this.pendingLoginData = loginData // 保存登录数据，修改密码后重新登录
                this.loading = false
                this.$message.warning(response.data.message || '请先修改密码')
                return
              }

              if (response && response.data) {
                const userId = response.data.userId
                this.$store.dispatch('menu/getMenus', userId).then(() => {
                  this.$router.push('/hrp')
                  this.$message.success('登录成功')
                }).catch(() => {
                  this.$router.push('/hrp')
                  this.$message.success('登录成功')
                })
              }
            })
            .catch(error => {
              // 调试：打印完整的错误信息
              console.log('登录错误详情:', error)
              console.log('error.message:', error.message)
              console.log('error.response:', error.response)
              console.log('error.response.data:', error.response?.data)
              
              // 优先使用error.message（已经被request.js处理过的）
              // 其次使用error.response.data.message（后端返回的错误消息）
              let errorMessage = ''
              
              // 如果error.message存在且不是浏览器默认的错误消息，直接使用
              if (error.message && 
                  error.message !== 'Request failed with status code 500' && 
                  error.message !== 'Internal Server Error' &&
                  error.message !== '' &&
                  !error.message.includes('Network Error')) {
                errorMessage = error.message
              }
              
              // 如果error.message为空或无效，尝试从响应体中提取错误消息
              if (!errorMessage && error.response && error.response.data) {
                let errorData = error.response.data
                console.log('原始errorData:', errorData, '类型:', typeof errorData)
                
                // 如果响应体是字符串，尝试解析为JSON
                if (typeof errorData === 'string') {
                  try {
                    errorData = JSON.parse(errorData)
                    console.log('解析后的errorData:', errorData)
                  } catch (e) {
                    // 如果解析失败，使用原始字符串（如果不是空字符串）
                    if (errorData && errorData.trim()) {
                      errorMessage = errorData
                    }
                  }
                }
                
                // 如果是对象，提取错误消息
                if (!errorMessage && errorData && typeof errorData === 'object') {
                  console.log('errorData的字段:', Object.keys(errorData))
                  
                  // 检查是否是Spring Boot默认错误格式（有status和error字段）
                  if (errorData.status !== undefined && errorData.error !== undefined) {
                    // Spring Boot默认错误格式
                    if (errorData.message && errorData.message.trim() !== '') {
                      errorMessage = String(errorData.message)
                    } else if (errorData.error && errorData.error.trim() !== '') {
                      // 如果message为空，使用error字段，但需要根据状态码推断更友好的消息
                      if (errorData.status === 401 || errorData.status === 500) {
                        errorMessage = '用户名或密码错误'
                      } else if (errorData.status === 403) {
                        errorMessage = '该用户已被停用'
                      } else {
                        errorMessage = String(errorData.error)
                      }
                    }
                  } else {
                    // Result格式或其他格式
                    // 优先使用Result格式的message字段
                    if (errorData.message !== undefined && errorData.message !== null && errorData.message !== '') {
                      errorMessage = String(errorData.message)
                    } else if (errorData.error && errorData.error !== '') {
                      errorMessage = String(errorData.error)
                    } else if (errorData.msg && errorData.msg !== '') {
                      errorMessage = String(errorData.msg)
                    } else if (errorData.data && errorData.data.message) {
                      errorMessage = String(errorData.data.message)
                    }
                  }
                }
              }
              
              // 如果还是获取不到错误消息，使用默认提示
              if (!errorMessage || errorMessage.trim() === '') {
                errorMessage = '用户名或密码错误，请检查后重试'
              }
              
              console.log('最终错误消息:', errorMessage)
              
              // 处理验证码需求
              const errorData = error.response && error.response.data
              if (errorData && errorData.data && errorData.data.needCaptcha) {
                this.needCaptcha = true
                this.$message.warning(errorData.data.message || '需要验证码')
                this.refreshCaptcha()
              } else {
                this.$message.error(errorMessage)
              }
              
              this.loading = false
            })
        } else {
          return false
        }
      })
    },
    handleChangePassword() {
      this.$refs.changePasswordForm.validate(valid => {
        if (valid) {
          this.changePasswordLoading = true
          const data = {
            userId: this.changePasswordForm.userId,
            newPassword: this.changePasswordForm.newPassword
          }

          forceChangePassword(data)
            .then(response => {
              if (response.code === 200) {
                this.$message.success('密码修改成功，请重新登录')
                this.changePasswordDialogVisible = false
                this.changePasswordForm.newPassword = ''
                this.changePasswordForm.confirmPassword = ''
                // 清空登录表单
                this.loginForm.password = ''
                // 如果有待登录数据，重新登录
                if (this.pendingLoginData) {
                  this.pendingLoginData = null
                  this.$message.info('请使用新密码重新登录')
                }
              } else {
                this.$message.error(response.message || '修改密码失败')
              }
              this.changePasswordLoading = false
            })
            .catch(error => {
              const errorData = error.response && error.response.data
              this.$message.error(error.message || errorData?.message || '修改密码失败')
              this.changePasswordLoading = false
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景装饰圆形 */
.login-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  top: -200px;
  right: -200px;
  z-index: 0;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  bottom: -150px;
  left: -150px;
  z-index: 0;
}

/* 背景装饰图标 */
.background-icons {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.icon-item {
  position: absolute;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  opacity: 0.6;
  animation: floatIcon 20s infinite ease-in-out;
}

.icon-item i {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.8);
}

.icon-1 { top: 15%; left: 10%; animation-delay: 0s; }
.icon-2 { top: 25%; right: 15%; animation-delay: 2s; }
.icon-3 { top: 50%; left: 8%; animation-delay: 4s; }
.icon-4 { top: 60%; right: 12%; animation-delay: 6s; }
.icon-5 { bottom: 25%; left: 15%; animation-delay: 8s; }
.icon-6 { bottom: 20%; right: 10%; animation-delay: 10s; }
.icon-7 { top: 35%; left: 20%; animation-delay: 12s; }
.icon-8 { bottom: 35%; right: 20%; animation-delay: 14s; }

@keyframes floatIcon {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.8;
  }
}

/* 登录包装器 */
.login-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 480px;
}

.login-box {
  width: 100%;
  padding: 50px 45px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header .main-title {
  color: #333;
  font-size: 35px;
  font-weight: 700;
  margin: 0 0 10px 0;
  letter-spacing: 3px;
  background: linear-gradient(135deg, #1565c0 0%, #0d47a1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header .sub-title {
  color: #909399;
  font-size: 14px;
  margin: 0 0 25px 0;
  font-weight: 300;
}

.header-divider {
  width: 50px;
  height: 3px;
  background: linear-gradient(135deg, #1565c0 0%, #0d47a1 100%);
  border-radius: 2px;
  margin: 0 auto 25px;
}

.login-header h3 {
  color: #333;
  font-size: 22px;
  font-weight: 500;
  margin: 0;
  letter-spacing: 0.5px;
}

.login-form {
  margin-top: 20px;
}

.login-form .el-form-item {
  margin-bottom: 22px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.input-wrapper:hover {
  border-color: #c0c4cc;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.svg-container {
  padding: 0 15px;
  color: #909399;
  font-size: 18px;
  display: flex;
  align-items: center;
}

.login-form .el-input {
  flex: 1;
}

.login-form .el-input input {
  background: transparent;
  border: 0;
  padding: 12px 45px 12px 0;
  color: #333;
  font-size: 14px;
}

.login-form .el-input input::placeholder {
  color: #c0c4cc;
}

.show-pwd {
  position: absolute;
  right: 15px;
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  user-select: none;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  z-index: 10;
}

.show-pwd:hover {
  color: #667eea;
}

.show-pwd i {
  font-size: 18px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-image {
  width: 35%;
  height: 47px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #667eea;
  background: #fff;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.captcha-image span {
  color: #909399;
  font-size: 12px;
}

.login-button {
  width: 100%;
  height: 50px;
  margin-top: 10px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.login-footer p {
  color: #909399;
  font-size: 12px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    padding: 40px 30px;
  }
  
  .login-header .main-title {
    font-size: 36px;
    letter-spacing: 2px;
  }
  
  .icon-item {
    width: 50px;
    height: 50px;
  }
  
  .icon-item i {
    font-size: 20px;
  }
}

.dialog-footer {
  text-align: center;
}
</style>
