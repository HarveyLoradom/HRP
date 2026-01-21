<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="dashboard-header">
        <div class="header-left">
          <el-button
            type="text"
            icon="el-icon-menu"
            class="menu-toggle"
            @click="toggleSidebar"
          ></el-button>
          <el-button
            type="text"
            class="home-button"
            @click="goHome"
          >
            <i class="el-icon-s-home"></i>
            <span>首页</span>
          </el-button>
          <div class="system-title">
            <i class="el-icon-hospital"></i>
            <span>HRP医院资源规划系统</span>
          </div>
        </div>
        <div class="header-right">
          <UserDropdown 
            @change-password="showChangePassword = true"
            @show-profile="showProfile = true"
          />
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside :width="sidebarWidth" class="dashboard-aside">
          <div class="menu-search" v-if="!isCollapse">
            <el-input
              v-model="menuSearchText"
              placeholder="搜索菜单"
              prefix-icon="el-icon-search"
              clearable
              @input="handleMenuSearch"
            />
          </div>
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            :collapse="isCollapse"
            :unique-opened="true"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#fff"
          >
            <menu-item
              v-for="menu in filteredMenus"
              :key="menu.id"
              :menu="menu"
            />
          </el-menu>
        </el-aside>

        <!-- 主内容区域 -->
        <el-main class="dashboard-main">
          <router-view v-if="$route.path !== '/hrp'" />
          <div v-else class="welcome-page">
            <div class="welcome-content">
              <div class="medical-icon-wrapper">
                <i class="el-icon-hospital welcome-icon"></i>
                <div class="icon-pulse"></div>
              </div>
              <h1 class="medical-title">HRP医院资源规划系统</h1>
              <div class="medical-subtitle">
                <div class="subtitle-line"></div>
                <span>Hospital Resource Planning System</span>
                <div class="subtitle-line"></div>
              </div>
              <p class="medical-description">专业、高效、安全的医院资源管理平台</p>
              <div class="medical-features">
                <div class="feature-item">
                  <i class="el-icon-folder-opened"></i>
                  <span>资源整合</span>
                </div>
                <div class="feature-item">
                  <i class="el-icon-data-analysis"></i>
                  <span>数据分析</span>
                </div>
                <div class="feature-item">
                  <i class="el-icon-lock"></i>
                  <span>安全可靠</span>
                </div>
                <div class="feature-item">
                  <i class="el-icon-coordinate"></i>
                  <span>精细管理</span>
                </div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 修改密码对话框 -->
    <ChangePassword v-model="showChangePassword" />
    
    <!-- 个人信息对话框 -->
    <el-dialog
      title="个人信息"
      :visible.sync="showProfile"
      width="900px"
      :close-on-click-modal="false"
      @close="handleProfileClose"
    >
      <Profile v-if="showProfile" ref="profile" @close="showProfile = false" />
    </el-dialog>
  </div>
</template>

<script>
import MenuItem from '@/components/MenuItem.vue'
import UserDropdown from '@/components/UserDropdown.vue'
import ChangePassword from '@/components/ChangePassword.vue'
import Profile from '@/views/Profile.vue'

export default {
  name: 'HRP',
  components: {
    MenuItem,
    UserDropdown,
    ChangePassword,
    Profile
  },
  data() {
    return {
      isCollapse: false,
      menuSearchText: '',
      showChangePassword: false,
      showProfile: false,
      allMenus: []
    }
  },
  computed: {
    sidebarWidth() {
      return this.isCollapse ? '64px' : '200px'
    },
    userInfo() {
      return this.$store.state.user.userInfo
    },
    menus() {
      return this.$store.state.menu.menus
    },
    filteredMenus() {
      if (!this.menuSearchText) {
        return this.menus
      }
      return this.filterMenus(this.menus, this.menuSearchText.toLowerCase())
    },
    activeMenu() {
      return this.$route.path
    }
  },
  watch: {
    menus: {
      immediate: true,
      handler(newMenus) {
        this.allMenus = newMenus
      }
    }
  },
  mounted() {
    const userId = this.userInfo.userId || this.userInfo.id
    if (userId) {
      this.$store.dispatch('menu/getMenus', userId)
    }
  },
  methods: {
    toggleSidebar() {
      this.isCollapse = !this.isCollapse
    },
    goHome() {
      if (this.$route.path !== '/hrp') {
        this.$router.push('/hrp')
      }
    },
    handleMenuSearch() {
      // 搜索逻辑已在computed中处理
    },
    filterMenus(menus, searchText) {
      const result = []
      for (const menu of menus) {
        const menuName = (menu.menuName || '').toLowerCase()
        if (menuName.includes(searchText)) {
          result.push(menu)
        } else if (menu.children && menu.children.length > 0) {
          const filteredChildren = this.filterMenus(menu.children, searchText)
          if (filteredChildren.length > 0) {
            result.push({
              ...menu,
              children: filteredChildren
            })
          }
        }
      }
      return result
    },
    async handleProfileClose() {
      // 通知Profile组件清理未保存的数据
      if (this.$refs.profile) {
        // 通过ref调用组件方法
        await this.$refs.profile.cleanupUnsavedPhoto()
      }
      this.showProfile = false
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  overflow: hidden;
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu-toggle {
  color: #fff;
  font-size: 20px;
  padding: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.3s;
}

.menu-toggle:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.home-button {
  color: #fff;
  font-size: 14px;
  padding: 8px 15px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.home-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.system-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-left: 10px;
}

.system-title i {
  font-size: 24px;
}

.header-right {
  display: flex;
  align-items: center;
}

.dashboard-aside {
  background-color: #304156;
  overflow: hidden;
  transition: width 0.3s;
}

.menu-search {
  padding: 15px;
  background-color: #263445;
}

.menu-search .el-input {
  width: 100%;
}

.menu-search .el-input >>> .el-input__inner {
  background-color: #1f2d3d;
  border: 1px solid #475669;
  color: #bfcbd9;
}

.menu-search .el-input >>> .el-input__inner:focus {
  border-color: #409EFF;
}

.el-menu-vertical {
  border-right: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

/* 确保菜单文字颜色正确显示 */
.el-menu-vertical .el-menu-item,
.el-menu-vertical .el-submenu__title {
  color: #bfcbd9 !important;
}

.el-menu-vertical .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #fff !important;
}

.el-menu-vertical .el-menu-item.is-active span,
.el-menu-vertical .el-menu-item.is-active i {
  color: #fff !important;
}

.el-menu-vertical .el-menu-item:hover,
.el-menu-vertical .el-submenu__title:hover {
  background-color: #263445 !important;
  color: #fff !important;
}

.el-menu-vertical .el-menu-item:hover span,
.el-menu-vertical .el-menu-item:hover i,
.el-menu-vertical .el-submenu__title:hover span,
.el-menu-vertical .el-submenu__title:hover i {
  color: #fff !important;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.dashboard-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.welcome-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 100px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f8 100%);
  position: relative;
  overflow: hidden;
}

.welcome-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.05) 0%, transparent 70%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.welcome-content {
  text-align: center;
  max-width: 900px;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.medical-icon-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 30px;
}

.welcome-icon {
  font-size: 100px;
  color: #409EFF;
  margin-bottom: 0;
  position: relative;
  z-index: 2;
  filter: drop-shadow(0 4px 12px rgba(64, 158, 255, 0.3));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.icon-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  border: 3px solid #409EFF;
  border-radius: 50%;
  opacity: 0;
  animation: ripple 2s ease-out infinite;
}

@keyframes ripple {
  0% {
    width: 100px;
    height: 100px;
    opacity: 0.6;
  }
  100% {
    width: 180px;
    height: 180px;
    opacity: 0;
  }
}

.medical-title {
  color: #2c3e50;
  font-size: 42px;
  font-weight: 700;
  margin: 30px 0 20px 0;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.medical-subtitle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin: 20px 0 30px 0;
}

.subtitle-line {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #409EFF, transparent);
}

.medical-subtitle span {
  color: #606266;
  font-size: 16px;
  font-weight: 300;
  letter-spacing: 3px;
  font-style: italic;
}

.medical-description {
  color: #909399;
  font-size: 18px;
  margin: 30px 0 50px 0;
  letter-spacing: 1px;
}

.medical-features {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 50px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  min-width: 120px;
}

.feature-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
  background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
}

.feature-item i {
  font-size: 36px;
  color: #409EFF;
  transition: all 0.3s ease;
}

.feature-item:hover i {
  transform: scale(1.2);
  color: #67C23A;
}

.feature-item span {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
}

/* 滚动条样式 */
.el-menu-vertical::-webkit-scrollbar,
.dashboard-main::-webkit-scrollbar {
  width: 6px;
}

.el-menu-vertical::-webkit-scrollbar-track,
.dashboard-main::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.el-menu-vertical::-webkit-scrollbar-thumb,
.dashboard-main::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.el-menu-vertical::-webkit-scrollbar-thumb:hover,
.dashboard-main::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>



