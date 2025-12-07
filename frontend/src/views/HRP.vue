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
            active-text-color="#409EFF"
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
              <i class="el-icon-hospital welcome-icon"></i>
              <h1>欢迎使用HRP医院资源规划系统</h1>
              <p>请从左侧菜单选择功能模块</p>
              <div class="quick-actions">
                <el-card class="action-card" shadow="hover" @click.native="goToMenu('reimb/my-apply')">
                  <i class="el-icon-money"></i>
                  <p>我的申请</p>
                </el-card>
                <el-card class="action-card" shadow="hover" @click.native="goToMenu('contract/workbench')">
                  <i class="el-icon-document-copy"></i>
                  <p>合同管理</p>
                </el-card>
                <el-card class="action-card" shadow="hover" @click.native="goToMenu('asset/query')">
                  <i class="el-icon-box"></i>
                  <p>固定资产</p>
                </el-card>
                <el-card class="action-card" shadow="hover" @click.native="goToMenu('hr/salary')">
                  <i class="el-icon-coin"></i>
                  <p>薪酬管理</p>
                </el-card>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 修改密码对话框 -->
    <ChangePassword v-model="showChangePassword" />
    
    <!-- 个人中心对话框 -->
    <el-dialog
      title="个人中心"
      :visible.sync="showProfile"
      width="900px"
      :close-on-click-modal="false"
      @close="showProfile = false"
    >
      <Profile v-if="showProfile" />
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
    goToMenu(path) {
      this.$router.push(`/hrp/${path}`)
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
}

.welcome-content {
  text-align: center;
  max-width: 800px;
}

.welcome-icon {
  font-size: 80px;
  color: #667eea;
  margin-bottom: 20px;
}

.welcome-content h1 {
  color: #333;
  font-size: 32px;
  font-weight: 600;
  margin: 20px 0;
}

.welcome-content p {
  color: #909399;
  font-size: 16px;
  margin-bottom: 40px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-top: 40px;
}

.action-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  border: none;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15) !important;
}

.action-card i {
  font-size: 40px;
  color: #667eea;
  margin-bottom: 10px;
}

.action-card p {
  margin: 0;
  color: #333;
  font-size: 14px;
  font-weight: 500;
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
