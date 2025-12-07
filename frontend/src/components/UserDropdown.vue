<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-dropdown">
      <el-avatar :size="32" :src="avatarUrl">
        <i class="el-icon-user-solid"></i>
      </el-avatar>
      <span class="username">{{ userName }}</span>
      <i class="el-icon-arrow-down"></i>
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="profile">
        <i class="el-icon-user"></i> 个人中心
      </el-dropdown-item>
      <el-dropdown-item command="password">
        <i class="el-icon-lock"></i> 修改密码
      </el-dropdown-item>
      <el-dropdown-item divided command="logout">
        <i class="el-icon-switch-button"></i> 退出登录
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  name: 'UserDropdown',
  computed: {
    userName() {
      return this.$store.state.user.userInfo.name || 
             this.$store.state.user.userInfo.account || 
             '用户'
    },
    avatarUrl() {
      return this.$store.state.user.userInfo.avatar || ''
    }
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$message.info('个人中心功能开发中')
          break
        case 'password':
          this.$emit('change-password')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },
    handleLogout() {
      this.$confirm('确认退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('user/logout').then(() => {
          this.$router.push('/login')
          this.$message.success('退出成功')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 100%;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-dropdown .username {
  margin: 0 8px;
  color: #fff;
  font-size: 14px;
}

.user-dropdown .el-icon-arrow-down {
  color: #fff;
  font-size: 12px;
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
}

.el-dropdown-menu__item i {
  margin-right: 8px;
}
</style>

