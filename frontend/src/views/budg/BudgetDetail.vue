<template>
  <div class="budget-detail">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算明细</span>
      </div>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预算主体">
          <el-select 
            v-model="searchForm.subjectId" 
            placeholder="请选择预算主体" 
            style="width: 200px" 
            filterable
            clearable
          >
            <el-option
              v-for="subject in subjectOptions"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-date-picker
            v-model="searchForm.budgetYear"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            style="width: 150px"
            clearable
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预算分类">
          <el-select v-model="searchForm.categoryType" placeholder="请选择" style="width: 150px" clearable>
            <el-option label="收入预算" value="INCOME"></el-option>
            <el-option label="支出预算" value="EXPENSE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input 
            v-model="searchForm.itemName" 
            placeholder="请输入项目名称" 
            style="width: 200px"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="itemName" label="项目名称" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="150"></el-table-column>
        <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="categoryType" label="预算分类" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.categoryType === 'INCOME' ? 'success' : 'warning'">
              {{ scope.row.categoryType === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="budgetAmount" label="预算总额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.budgetAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executedAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleViewExecution(scope.row)">
              ¥{{ formatAmount(scope.row.executedAmount) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="appliedAmount" label="申请金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleViewApply(scope.row)">
              ¥{{ formatAmount(scope.row.appliedAmount) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150" align="right">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
              ¥{{ formatAmount(scope.row.remainingAmount) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
        <el-dropdown @command="handleExportCommand" :disabled="exportLoading">
          <el-button size="big" type="text" :loading="exportLoading">
            导出<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="current">导出本页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-card>

    <!-- 执行金额明细对话框 -->
    <el-dialog title="执行金额明细" :visible.sync="executionDialogVisible" width="900px">
      <el-table :data="executionDetails" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="executionAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.executionAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionDate" label="执行日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.executionDate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 申请金额明细对话框 -->
    <el-dialog title="申请金额明细" :visible.sync="applyDialogVisible" width="900px">
      <el-table :data="applyDetails" border style="width: 100%">
        <el-table-column prop="applyNo" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.applyAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executedAmount" label="已执行金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link 
              type="primary" 
              @click="handleViewApplyExecution(scope.row)"
              v-if="scope.row.executedAmount && scope.row.executedAmount > 0"
            >
              ¥{{ formatAmount(scope.row.executedAmount) }}
            </el-link>
            <span v-else>¥{{ formatAmount(scope.row.executedAmount || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 申请单已执行金额明细对话框 -->
    <el-dialog title="已执行金额明细" :visible.sync="applyExecutionDialogVisible" width="900px">
      <el-table :data="applyExecutionDetails" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="executionAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.executionAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionDate" label="执行日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.executionDate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetDetails, getBudgetExecutionDetails, getBudgetApplyDetails, getBudgetApplyExecutionDetails, getBudgetSubjects } from '@/api/budg'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'

export default {
  name: 'BudgetDetail',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      subjectOptions: [],
      searchForm: {
        subjectId: null,
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        itemName: ''
      },
      executionDialogVisible: false,
      applyDialogVisible: false,
      applyExecutionDialogVisible: false,
      executionDetails: [],
      applyDetails: [],
      applyExecutionDetails: [],
      currentRow: {}
    }
  },
  mounted() {
    this.loadData()
    this.loadBudgetSubjects()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetDetails(this.pagination.page, this.pagination.size, this.searchForm).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadBudgetSubjects() {
      getBudgetSubjects(0).then(response => {
        if (response.code === 200) {
          this.subjectOptions = response.data || []
        }
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        subjectId: null,
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        itemName: ''
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleViewExecution(row) {
      this.currentRow = row
      getBudgetExecutionDetails(row.itemId, row.subjectId).then(response => {
        if (response.code === 200) {
          this.executionDetails = response.data || []
          this.executionDialogVisible = true
        }
      })
    },
    handleViewApply(row) {
      this.currentRow = row
      getBudgetApplyDetails(row.itemId, row.subjectId).then(response => {
        if (response.code === 200) {
          this.applyDetails = response.data || []
          this.applyDialogVisible = true
        }
      })
    },
    handleViewApplyExecution(row) {
      getBudgetApplyExecutionDetails(this.currentRow.itemId, this.currentRow.subjectId, row.applyNo).then(response => {
        if (response.code === 200) {
          this.applyExecutionDetails = response.data || []
          this.applyExecutionDialogVisible = true
        }
      })
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝'
      }
      return map[status] || status
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportDetail(false)
      } else if (command === 'all') {
        this.handleExportDetail(true)
      }
    },
    // 导出预算明细数据
    async handleExportDetail(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const res = await getBudgetDetails(1, 10000, this.searchForm)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.tableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['项目名称', '预算主体', '年度', '预算分类', '预算总额', '执行金额', '申请金额', '剩余可执行金额']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.itemName || ''),
            String(item.subjectName || ''),
            String(item.budgetYear || ''),
            String(item.categoryType === 'INCOME' ? '收入' : '支出'),
            String('¥' + this.formatAmount(item.budgetAmount)),
            String('¥' + this.formatAmount(item.executedAmount)),
            String('¥' + this.formatAmount(item.appliedAmount)),
            String('¥' + this.formatAmount(item.remainingAmount))
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '预算明细' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '预算明细' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.budget-detail {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>

