<template>
  <div class="salary-management">
    <el-card>
      <div slot="header">
        <span>人员薪酬</span>
      </div>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="薪酬月份">
          <el-date-picker
            v-model="searchMonth"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            @change="handleSearch"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="empCode" label="工号" width="120"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="salaryMonth" label="薪酬月份" width="120"></el-table-column>
        <el-table-column prop="baseSalary" label="基本工资" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.baseSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="performanceSalary" label="绩效工资" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.performanceSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="应发工资" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="actualSalary" label="实发工资" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.actualSalary }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllSalaries, getSalariesByMonth } from '@/api/hr'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'SalaryManagement',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      searchMonth: ''
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getAllSalaries().then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.handleSearch()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.searchMonth) {
        filtered = filtered.filter(item => item.salaryMonth === this.searchMonth)
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待计算',
        'CALCULATED': '已计算',
        'PAID': '已发放'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'CALCULATED': 'success',
        'PAID': 'info'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.salary-management {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>

