<template>
  <div class="attendance-manage">
    <el-card>
      <div slot="header">
        <span>考勤管理</span>
      </div>
      
      <el-form :inline="true" :model="queryForm" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="月份">
          <el-date-picker
            v-model="queryForm.month"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            @change="handleQuery"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="attendanceDate" label="日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.attendanceDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="上班时间" width="120">
          <template slot-scope="scope">
            {{ formatTime(scope.row.checkInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="下班时间" width="120">
          <template slot-scope="scope">
            {{ formatTime(scope.row.checkOutTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工作时长" width="100">
          <template slot-scope="scope">
            {{ scope.row.workHours || '-' }}小时
          </template>
        </el-table-column>
        <el-table-column prop="attendanceStatus" label="考勤状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.attendanceStatus)">
              {{ getStatusText(scope.row.attendanceStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
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
import { getMyAttendanceList } from '@/api/hr'

export default {
  name: 'AttendanceManage',
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      queryForm: {
        month: new Date().toISOString().slice(0, 7) // 默认当前月份
      }
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
      const empId = this.$store.state.user.userInfo.empId || null
      getMyAttendanceList(empId, this.queryForm.month).then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.tableData = this.allData
          this.pagination.total = this.tableData.length
          this.pagination.page = 1
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleQuery() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm.month = new Date().toISOString().slice(0, 7)
      this.pagination.page = 1
      this.loadData()
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN').split(' ')[0]
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    getStatusText(status) {
      const statusMap = {
        'NORMAL': '正常',
        'LATE': '迟到',
        'EARLY': '早退',
        'ABSENT': '缺勤',
        'LEAVE': '请假',
        'OVERTIME': '加班'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'NORMAL': 'success',
        'LATE': 'warning',
        'EARLY': 'warning',
        'ABSENT': 'danger',
        'LEAVE': 'info',
        'OVERTIME': ''
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.attendance-manage {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>



