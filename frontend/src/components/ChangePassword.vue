<template>
  <el-dialog
    title="修改密码"
    :visible.sync="visible"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="passwordForm"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          @keyup.enter.native="handleSubmit"
        />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { changePassword } from '@/api/user'

export default {
  name: 'ChangePassword',
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      visible: this.value,
      loading: false,
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    value(val) {
      this.visible = val
    },
    visible(val) {
      this.$emit('input', val)
      if (!val) {
        this.handleClose()
      }
    }
  },
  methods: {
    handleClose() {
      this.$refs.passwordForm.resetFields()
      this.form = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.visible = false
    },
    handleSubmit() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.loading = true
          const userInfo = this.$store.state.user.userInfo
          changePassword({
            userId: userInfo.userId || userInfo.id,
            oldPassword: this.form.oldPassword,
            newPassword: this.form.newPassword
          }).then(response => {
            if (response.code === 200) {
              this.$message.success('密码修改成功，请重新登录')
              this.handleClose()
              setTimeout(() => {
                this.$store.dispatch('user/logout').then(() => {
                  this.$router.push('/login')
                })
              }, 1500)
            } else {
              this.$message.error(response.message || '密码修改失败')
            }
            this.loading = false
          }).catch(error => {
            this.$message.error(error.message || '密码修改失败')
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>

