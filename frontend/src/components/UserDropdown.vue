<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-dropdown">
      <el-avatar :size="32" :src="avatarUrl" @error="handleAvatarError">
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
      <el-dropdown-item  command="logout">
        <i class="el-icon-switch-button"></i>
        <span>退出登录</span>
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  name: 'UserDropdown',
  data() {
    return {
      avatarError: false // 头像加载错误标记
    }
  },
  mounted() {
    // 组件挂载时，检查userInfo是否有photo字段
    const userInfo = this.$store.state.user.userInfo
    console.log('UserDropdown mounted - userInfo:', userInfo)
    console.log('UserDropdown mounted - photo:', userInfo.photo)
    
    // 如果没有photo字段，可能是旧数据，建议用户重新登录
    if (!userInfo.photo && userInfo.userId) {
      console.warn('用户信息中没有photo字段，建议重新登录以获取最新信息')
    }
  },
  computed: {
    userName() {
      // 优先显示empName（员工姓名），如果没有才显示其他字段
      return this.$store.state.user.userInfo.empName || 
             this.$store.state.user.userInfo.realName || 
             this.$store.state.user.userInfo.name || 
             this.$store.state.user.userInfo.account || 
             '用户'
    },
    avatarUrl() {
      // 如果头像加载失败，不显示图片，使用默认图标
      if (this.avatarError) {
        return ''
      }
      
      const photo = this.$store.state.user.userInfo.photo
      const userInfo = this.$store.state.user.userInfo
      
      // 调试信息
      console.log('=== UserDropdown avatarUrl ===')
      console.log('userInfo:', userInfo)
      console.log('photo:', photo)
      console.log('photo type:', typeof photo)
      console.log('photo trimmed:', photo ? photo.trim() : 'null/undefined')
      
      if (!photo || !photo.trim()) {
        // 如果照片为空，返回空字符串使用el-avatar的默认图标（统一的默认头像）
        console.log('照片为空，使用默认图标')
        return ''
      }
      
      // 处理照片路径，参考UserManagement.vue中的getPhotoUrl方法
      if (photo.startsWith('http://') || photo.startsWith('https://')) {
        // 完整的URL直接返回
        return photo
      }
      
      // photoPath 格式应该是：employee-photos/xxx.jpg
      // 需要构建为：/api/uploads/employee-photos/xxx.jpg
      
      // 如果路径已经包含 /uploads/，直接使用
      if (photo.includes('/uploads/')) {
        // 如果已经是完整路径，直接返回
        if (photo.startsWith('/')) {
          return photo.startsWith('/api/') ? photo : `/api${photo}`
        }
        // 否则添加 /api 前缀
        return '/api/' + photo
      }
      
      // 默认情况：photoPath 是相对路径（如 employee-photos/xxx.jpg）
      // 构建为 /api/uploads/employee-photos/xxx.jpg
      const finalUrl = '/api/uploads/' + photo
      console.log('最终照片URL:', finalUrl)
      return finalUrl
    }
  },
  watch: {
    // 监听userInfo变化，重置头像错误标记
    '$store.state.user.userInfo': {
      handler() {
        this.avatarError = false
      },
      deep: true
    }
  },
  methods: {
    handleAvatarError() {
      // 当头像图片加载失败时，使用默认图标
      this.avatarError = true
    },
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
          this.$router.push('/login').catch(() => {})
          this.$message.success('退出成功')
        }).catch(() => {
          // 即使出错也跳转到登录页
          this.$router.push('/login').catch(() => {})
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
  text-align: center;
  font-size: 16px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.user-dropdown-menu .el-dropdown-menu__item span {
  flex: 1;
  text-align: left;
  display: inline-flex;
  align-items: center;
}
</style>

