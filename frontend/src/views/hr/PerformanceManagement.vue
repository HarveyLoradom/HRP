<template>
  <div class="performance-management">
    <el-card>
      <div slot="header">
        <span>绩效管理</span>
      </div>
      
      <el-form :inline="true" :model="queryForm" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="年度">
          <el-date-picker
            v-model="queryForm.year"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            @change="handleQuery"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="季度">
          <el-select v-model="queryForm.quarter" placeholder="请选择季度" clearable @change="handleQuery">
            <el-option label="第一季度" value="Q1"></el-option>
            <el-option label="第二季度" value="Q2"></el-option>
            <el-option label="第三季度" value="Q3"></el-option>
            <el-option label="第四季度" value="Q4"></el-option>
            <el-option label="全年" value="YEAR"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="performanceNo" label="绩效编号" width="150"></el-table-column>
        <el-table-column prop="performanceYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="performanceQuarter" label="季度" width="100">
          <template slot-scope="scope">
            {{ getQuarterName(scope.row.performanceQuarter) }}
          </template>
        </el-table-column>
        <el-table-column prop="empCode" label="工号" width="120"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="performanceScore" label="绩效分数" width="120">
          <template slot-scope="scope">
            {{ scope.row.performanceScore }}
          </template>
        </el-table-column>
        <el-table-column prop="performanceLevel" label="绩效等级" width="120">
          <template slot-scope="scope">
            <el-tag :type="getLevelType(scope.row.performanceLevel)">
              {{ getLevelName(scope.row.performanceLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="performanceDesc" label="绩效说明" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
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

    <!-- 查看详情对话框 -->
    <el-dialog title="绩效详情" :visible.sync="detailVisible" width="800px">
      <div v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="绩效编号">{{ currentDetail.performanceNo }}</el-descriptions-item>
          <el-descriptions-item label="年度">{{ currentDetail.performanceYear }}</el-descriptions-item>
          <el-descriptions-item label="季度">{{ getQuarterName(currentDetail.performanceQuarter) }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ currentDetail.empCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentDetail.empName }}</el-descriptions-item>
          <el-descriptions-item label="绩效分数">{{ currentDetail.performanceScore }}</el-descriptions-item>
          <el-descriptions-item label="绩效等级">
            <el-tag :type="getLevelType(currentDetail.performanceLevel)">
              {{ getLevelName(currentDetail.performanceLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="绩效说明" :span="2">{{ currentDetail.performanceDesc }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPerformanceList } from '@/api/hr'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'PerformanceManagement',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      queryForm: {
        year: new Date().getFullYear().toString(),
        quarter: ''
      },
      detailVisible: false,
      currentDetail: null
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
      getMyPerformanceList(empId, this.queryForm.year, this.queryForm.quarter).then(response => {
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
      // 根据查询条件过滤
      if (this.queryForm.year) {
        filtered = filtered.filter(item => item.performanceYear === this.queryForm.year)
      }
      if (this.queryForm.quarter) {
        filtered = filtered.filter(item => item.performanceQuarter === this.queryForm.quarter)
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleQuery() {
      this.pagination.page = 1
      this.handleSearch()
    },
    handleReset() {
      this.queryForm = {
        year: new Date().getFullYear().toString(),
        quarter: ''
      }
      this.pagination.page = 1
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleView(row) {
      this.currentDetail = row
      this.detailVisible = true
    },
    getQuarterName(quarter) {
      const quarterMap = {
        'Q1': '第一季度',
        'Q2': '第二季度',
        'Q3': '第三季度',
        'Q4': '第四季度',
        'YEAR': '全年'
      }
      return quarterMap[quarter] || quarter
    },
    getLevelName(level) {
      const levelMap = {
        'EXCELLENT': '优秀',
        'GOOD': '良好',
        'QUALIFIED': '合格',
        'UNQUALIFIED': '不合格'
      }
      return levelMap[level] || level
    },
    getLevelType(level) {
      const typeMap = {
        'EXCELLENT': 'success',
        'GOOD': 'success',
        'QUALIFIED': 'info',
        'UNQUALIFIED': 'danger'
      }
      return typeMap[level] || ''
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.performance-management {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>

