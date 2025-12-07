<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <i class="el-icon-hospital"></i>
        </div>
        <h2>HRP医院资源规划系统</h2>
        <p>Hospital Resource Planning System</p>
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
            <span class="show-pwd" @click="showPwd">
              <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'"></i>
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
        <p>© 2024 HRP系统. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script>
import { login, getCaptcha } from '@/api/auth'

export default {
  name: 'Login',
  data() {
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
      captchaImage: ''
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
              const errorData = error.response && error.response.data
              if (errorData && errorData.data && errorData.data.needCaptcha) {
                this.needCaptcha = true
                this.$message.warning(errorData.data.message || '需要验证码')
                this.refreshCaptcha()
              } else {
                this.$message.error(error.message || errorData?.message || '登录失败')
              }
              this.loading = false
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
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.background-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: -50px;
  animation-delay: 5s;
}

.shape-3 {
  width: 250px;
  height: 250px;
  bottom: -50px;
  left: 20%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.login-box {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.logo i {
  font-size: 40px;
  color: #fff;
}

.login-header h2 {
  color: #333;
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-top: 30px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
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
  padding: 12px 15px 12px 0;
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
}

.show-pwd:hover {
  color: #667eea;
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
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.login-footer p {
  color: #909399;
  font-size: 12px;
  margin: 0;
}
</style>
