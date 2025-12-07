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
    <el-menu-item v-else :index="getMenuPath(menu)">
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
        // 如果path以/开头，直接使用；否则拼接/dashboard/
        return menu.path.startsWith('/') ? menu.path : `/dashboard/${menu.path}`
      }
      return String(menu.id)
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
