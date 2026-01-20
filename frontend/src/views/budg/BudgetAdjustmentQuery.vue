<template>
  <div class="budget-adjustment-query">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算调整查询</span>
      </div>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="调整单号">
          <el-input 
            v-model="searchForm.adjustmentNo" 
            placeholder="请输入调整单号" 
            style="width: 200px"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="调整类型">
          <el-select v-model="searchForm.adjustmentType" placeholder="请选择" style="width: 150px" clearable>
            <el-option
              v-for="option in adjustmentTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预算主体">
          <el-select 
            v-model="searchForm.subjectId" 
            placeholder="请选择预算主体" 
            style="width: 200px" 
            filterable
            clearable
          >
            <el-option
              v-for="subject in budgetSubjects"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预算项目">
          <el-select 
            v-model="searchForm.itemId" 
            placeholder="请选择预算项目" 
            style="width: 200px" 
            filterable
            clearable
          >
            <el-option
              v-for="item in budgetItems"
              :key="item.itemId"
              :label="item.itemName"
              :value="item.itemId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="adjustmentNo" label="调整单号" width="180">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.adjustmentNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="adjustmentType" label="调整类型" width="120">
          <template slot-scope="scope">
            {{ getAdjustmentTypeName(scope.row.adjustmentType) }}
          </template>
        </el-table-column>
        <el-table-column prop="relatedBillNo" label="关联单据" width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.relatedBillNo">{{ scope.row.relatedBillNo }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="150"></el-table-column>
        <el-table-column prop="itemName" label="预算项目" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="adjustmentAmount" label="调整金额" width="120" align="right">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.adjustmentAmount < 0 ? 'red' : 'green' }">
              {{ scope.row.adjustmentAmount >= 0 ? '+' : '' }}¥{{ formatAmount(scope.row.adjustmentAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="originalAmount" label="调整前金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.originalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="adjustedAmount" label="调整后金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.adjustedAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="调整人" width="100"></el-table-column>
        <el-table-column prop="createTime" label="调整时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
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

    <!-- 详情对话框 -->
    <el-dialog title="调整单详情" :visible.sync="detailDialogVisible" width="1000px">
      <el-form :model="detailData" label-width="120px" :disabled="true">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="调整单号">
              <span>{{ detailData.adjustmentNo || '-' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整类型">
              <span>{{ getAdjustmentTypeName(detailData.adjustmentType) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联单据">
              <span>{{ detailData.relatedBillNo || '-' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预算主体">
              <span>{{ detailData.subjectName || '-' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算项目">
              <span>{{ detailData.itemName || '-' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算">
              <span>{{ detailData.budgetName || '-' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="调整前金额">
              <span>¥{{ formatAmount(detailData.originalAmount) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整金额">
              <span :style="{ color: detailData.adjustmentAmount < 0 ? 'red' : 'green' }">
                {{ detailData.adjustmentAmount >= 0 ? '+' : '' }}¥{{ formatAmount(detailData.adjustmentAmount) }}
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整后金额">
              <span>¥{{ formatAmount(detailData.adjustedAmount) }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="调整人">
              <span>{{ detailData.applicantName || '-' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整时间">
              <span>{{ formatDate(detailData.createTime) }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整原因">
              <span>{{ detailData.adjustmentReason || '-' }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer" style="text-align: right;">
        <el-button type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetAdjustmentsPage, getBudgetAdjustmentById, deleteBudgetAdjustment } from '@/api/budg'
import { getBudgetSubjects, getBudgetItems } from '@/api/budg'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'

export default {
  name: 'BudgetAdjustmentQuery',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      budgetSubjects: [],
      budgetItems: [],
      adjustmentTypeOptions: [],
      searchForm: {
        adjustmentNo: '',
        adjustmentType: '',
        subjectId: null,
        itemId: null
      },
      detailDialogVisible: false,
      detailData: {}
    }
  },
  mounted() {
    this.loadData()
    this.loadBudgetSubjects()
    this.loadBudgetItems()
    this.loadAdjustmentTypeOptions()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {}
      if (this.searchForm.adjustmentNo) {
        params.adjustmentNo = this.searchForm.adjustmentNo
      }
      if (this.searchForm.adjustmentType) {
        params.adjustmentType = this.searchForm.adjustmentType
      }
      if (this.searchForm.subjectId) {
        params.subjectId = this.searchForm.subjectId
      }
      if (this.searchForm.itemId) {
        params.itemId = this.searchForm.itemId
      }
      // 只查询审批通过的调整单（额度调整）和已完成的调整单（冲销调整）
      params.onlyApproved = true
      
      getBudgetAdjustmentsPage(this.pagination.page, this.pagination.size, params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.tableData = []
        this.pagination.total = 0
        this.loading = false
      })
    },
    async loadBudgetSubjects() {
      try {
        const response = await getBudgetSubjects()
        if (response.code === 200) {
          this.budgetSubjects = response.data || []
        }
      } catch (error) {
        console.error('加载预算主体失败', error)
      }
    },
    async loadBudgetItems() {
      try {
        const response = await getBudgetItems()
        if (response.code === 200) {
          this.budgetItems = response.data || []
        }
      } catch (error) {
        console.error('加载预算项目失败', error)
      }
    },
    async loadAdjustmentTypeOptions() {
      this.adjustmentTypeOptions = await getCodeTypeOptions('ADJUSTMENT_TYPE')
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        adjustmentNo: '',
        adjustmentType: '',
        subjectId: null,
        itemId: null
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
    getAdjustmentTypeName(type) {
      const option = this.adjustmentTypeOptions.find(item => item.value === type)
      return option ? option.label : type
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    async handleViewDetail(row) {
      try {
        const response = await getBudgetAdjustmentById(row.adjustmentId)
        if (response.code === 200) {
          this.detailData = response.data || {}
          this.detailDialogVisible = true
        } else {
          this.$message.error(response.message || '加载详情失败')
        }
      } catch (error) {
        console.error('加载详情失败', error)
        this.$message.error('加载详情失败')
      }
    },
    handleDelete() {
      if (!this.detailData.adjustmentId) {
        this.$message.warning('调整单ID不存在')
        return
      }
      
      this.$confirm('删除后将还原相关操作，确定要删除该调整单吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteBudgetAdjustment(this.detailData.adjustmentId)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.detailDialogVisible = false
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败', error)
          this.$message.error('删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 用户取消删除
      })
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportAdjustment(false)
      } else if (command === 'all') {
        this.handleExportAdjustment(true)
      }
    },
    // 导出预算调整查询数据
    async handleExportAdjustment(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {}
          if (this.searchForm.adjustmentNo) {
            params.adjustmentNo = this.searchForm.adjustmentNo
          }
          if (this.searchForm.adjustmentType) {
            params.adjustmentType = this.searchForm.adjustmentType
          }
          if (this.searchForm.subjectId) {
            params.subjectId = this.searchForm.subjectId
          }
          if (this.searchForm.itemId) {
            params.itemId = this.searchForm.itemId
          }
          params.onlyApproved = true
          
          const res = await getBudgetAdjustmentsPage(1, 10000, params)
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
        const headers = ['调整单号', '调整类型', '关联单据', '预算主体', '预算项目', '调整金额', '调整前金额', '调整后金额', '调整人', '调整时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.adjustmentNo || ''),
            String(this.getAdjustmentTypeName(item.adjustmentType) || ''),
            String(item.relatedBillNo || '-'),
            String(item.subjectName || ''),
            String(item.itemName || ''),
            String(item.adjustmentAmount >= 0 ? '+' : '') + '¥' + String(this.formatAmount(item.adjustmentAmount)),
            String('¥' + this.formatAmount(item.originalAmount)),
            String('¥' + this.formatAmount(item.adjustedAmount)),
            String(item.applicantName || ''),
            String(this.formatDate(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '预算调整查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '预算调整查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.budget-adjustment-query {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>

