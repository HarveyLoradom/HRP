<template>
  <div class="apply-query">
    <el-card>
      <div slot="header" class="clearfix">
        <span>申请查询</span>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="申请单号:" label-width="100px">
              <el-input v-model="searchForm.payoutBillcode" placeholder="请输入申请单号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请人:" label-width="100px">
              <el-input v-model="searchForm.empName" placeholder="请输入申请人" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请类型:" label-width="100px">
              <el-select v-model="searchForm.payoutTypeId" placeholder="请选择申请类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="申请时间:" label-width="100px">
              <el-date-picker
                v-model="searchForm.applyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
      >
        <el-table-column label="申请单号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.payoutBillcode }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="130"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
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

    <!-- 统一详情组件 -->
    <ApplyApplyDetail
      v-model="detailVisible"
      source-type="query"
      :payout-id="selectedPayoutId"
    />

  </div>
</template>

<script>
import { getPayoutList } from '@/api/reimb'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ApplyApplyDetail from '@/views/reimb/ApplyApplyDetail.vue'

export default {
  name: 'ApplyQuery',
  mixins: [paginationMixin],
  components: {
    ApplyApplyDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      searchForm: {
        payoutBillcode: '',
        empName: '',
        payoutTypeId: '',
        status: '',
        applyDateRange: null
      },
      applyTypeOptions: [],
      applyStatusOptions: [],
      detailVisible: false,
      selectedPayoutId: null
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.applyTypeOptions = await getCodeTypeOptions('APPLY_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        isApprovalList: false, // 查询列表，显示所有数据
        billTypePrefix: 'SQD', // 只显示申请单（SQD前缀）
        ...this.buildSearchParams()
      }
      
      getPayoutList(params).then(response => {
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
    buildSearchParams() {
      const params = {}
      if (this.searchForm.payoutBillcode) {
        params.payoutBillcode = this.searchForm.payoutBillcode
      }
      if (this.searchForm.empName) {
        params.empName = this.searchForm.empName
      }
      if (this.searchForm.payoutTypeId) {
        params.payoutTypeId = this.searchForm.payoutTypeId
      }
      if (this.searchForm.status) {
        params.status = this.searchForm.status
      }
      if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
        params.startDate = this.searchForm.applyDateRange[0]
        params.endDate = this.searchForm.applyDateRange[1]
      }
      return params
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        payoutBillcode: '',
        empName: '',
        payoutTypeId: '',
        status: '',
        applyDateRange: null
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
    handleViewDetail(row) {
      this.selectedPayoutId = row.payoutId
      this.detailVisible = true
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getPayoutTypeName(codeValue) {
      const option = this.applyTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      if (isNaN(d.getTime())) return date
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    getStatusText(status) {
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return typeMap[status] || ''
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApply(false)
      } else if (command === 'all') {
        this.handleExportApply(true)
      }
    },
    // 导出申请数据
    async handleExportApply(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            isApprovalList: false,
            billTypePrefix: 'SQD',
            ...this.buildSearchParams()
          }
          
          const res = await getPayoutList(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || []
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
        const headers = ['申请单号', '申请人', '部门', '申请类型', '申请金额', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.payoutBillcode || '',
            item.empName || '',
            item.deptName || '',
            this.getPayoutTypeName(item.payoutTypeId),
            item.applyAmount ? '¥' + item.applyAmount : '',
            this.getStatusText(item.status),
            this.formatDateOnly(item.applyDate || item.createTime)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '申请查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '申请查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.apply-query {
  padding: 20px;
}
</style>
