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

      <el-table :data="tableData" border style="width: 100%">
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
    </el-card>
  </div>
</template>

<script>
import { getAllSalaries, getSalariesByMonth } from '@/api/hr'

export default {
  name: 'SalaryManagement',
  data() {
    return {
      tableData: [],
      searchMonth: ''
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getAllSalaries().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleSearch() {
      if (this.searchMonth) {
        getSalariesByMonth(this.searchMonth).then(response => {
          if (response.code === 200) {
            this.tableData = response.data
          }
        })
      } else {
        this.loadData()
      }
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

