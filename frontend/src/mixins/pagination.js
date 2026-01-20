/**
 * 分页混入
 * 使用方法：
 * 1. 在组件中引入：import { paginationMixin } from '@/mixins/pagination'
 * 2. 在组件中混入：mixins: [paginationMixin]
 * 3. 在data中定义：pagination: { page: 1, size: 10, total: 0 }
 * 4. 在computed中添加：paginatedData() { return this.getPaginatedData(this.tableData) }
 * 5. 在模板中使用：:data="paginatedData" 替代 :data="tableData"
 * 6. 在loadData方法中更新：this.pagination.total = allData.length
 */
export const paginationMixin = {
  data() {
    return {
      pagination: {
        page: 1,
        size: 10,
        total: 0
      }
    }
  },
  methods: {
    getPaginatedData(data) {
      if (!data || !Array.isArray(data)) {
        return []
      }
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return data.slice(start, end)
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      if (this.loadData) {
        this.loadData()
      }
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    resetPagination() {
      this.pagination.page = 1
    }
  }
}

// 为了兼容性，也导出为default
export default paginationMixin


