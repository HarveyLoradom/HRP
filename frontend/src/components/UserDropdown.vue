<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-dropdown">
      <el-avatar :size="32" :src="avatarUrl">
        <i class="el-icon-user-solid"></i>
      </el-avatar>
      <span class="username">{{ userName }}</span>
      <i class="el-icon-arrow-down"></i>
    </div>
    <el-dropdown-menu slot="dropdown" class="user-dropdown-menu">
      <el-dropdown-item command="profile">
        <i class="el-icon-user"></i>
        <span>个人中心</span>
      </el-dropdown-item>
      <el-dropdown-item command="password">
        <i class="el-icon-lock"></i>
        <span>修改密码</span>
      </el-dropdown-item>
      <el-dropdown-item divided command="logout">
        <i class="el-icon-switch-button"></i>
        <span>退出登录</span>
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
          this.$emit('show-profile')
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

/* 下拉菜单样式 */
.user-dropdown-menu {
  min-width: 150px;
}

.user-dropdown-menu .el-dropdown-menu__item {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  justify-content: flex-start;
}

.user-dropdown-menu .el-dropdown-menu__item i {
  margin-right: 8px;
  width: 16px;
  text-align: left;
  font-size: 16px;
}

.user-dropdown-menu .el-dropdown-menu__item span {
  flex: 1;
  text-align: left;
}
</style>

