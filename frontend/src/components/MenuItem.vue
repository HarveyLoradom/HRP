<template>
  <div>
    <el-submenu v-if="menu.children && menu.children.length > 0" :index="String(menu.id)">
      <template slot="title">
        <i :class="menu.icon || 'el-icon-menu'"></i>
        <span>{{ menu.menuName }}</span>
      </template>
      <menu-item
        v-for="child in menu.children"
        :key="child.id"
        :menu="child"
      />
    </el-submenu>
    <el-menu-item v-else :index="getMenuPath(menu)" @click="handleMenuClick(menu)">
      <i :class="menu.icon || 'el-icon-menu'"></i>
      <span slot="title">{{ menu.menuName }}</span>
    </el-menu-item>
  </div>
</template>

<script>
export default {
  name: 'MenuItem',
  props: {
    menu: {
      type: Object,
      required: true
    }
  },
  methods: {
    getMenuPath(menu) {
      if (menu.path) {
        // 如果path以/开头，直接使用；否则拼接/hrp/
        return menu.path.startsWith('/') ? menu.path : `/hrp/${menu.path}`
      }
      return String(menu.id)
    },
    handleMenuClick(menu) {
      // 如果是目录类型（menu_type=1）且没有实际组件，不进行路由跳转
      if (menu.menuType === 1 && (!menu.component || menu.component === 'Layout' || menu.component === '')) {
        // 目录类型的菜单不跳转，只是分组
        return
      }
      // 其他情况由el-menu的router属性自动处理
    }
  }
}
</script>

<style scoped>
.el-menu-item,
.el-submenu__title {
  color: #bfcbd9;
}

.el-menu-item:hover,
.el-submenu__title:hover {
  background-color: #263445 !important;
  color: #fff;
}

.el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #fff;
}
</style>
