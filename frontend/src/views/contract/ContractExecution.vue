<template>
  <div class="contract-execution">
    <el-card>
      <div slot="header">
        <span>合同执行</span>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="合同编号:" label-width="100px">
              <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同名称:" label-width="100px">
              <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="执行状态:" label-width="100px">
              <el-select v-model="searchForm.executionStatus" placeholder="请选择执行状态" clearable style="width: 100%;">
                <el-option
                  v-for="option in executionStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同类型:" label-width="100px">
              <el-select v-model="searchForm.contractType" placeholder="请选择合同类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in contractTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
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
        style="width: 100%;" 
        v-loading="loading"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="contractNo" label="合同编号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.contractNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.signDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionStatus" label="执行状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getExecutionStatusType(scope.row.executionStatus)">
              {{ getExecutionStatusName(scope.row.executionStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="handleInvalidate(scope.row)">失效</el-button>
            <el-button 
              size="mini" 
              type="primary" 
              @click="handleArchive(scope.row)"
              :disabled="scope.row.executionStatus !== 'COMPLETED' && scope.row.executionStatus !== '已履约'"
            >归档</el-button>
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

    <!-- 统一详情组件 -->
    <ContractDetail
      v-model="detailVisible"
      source-type="query"
      :contract-id="selectedContractId"
    />
  </div>
</template>

<script>
import { getApprovedContractsPage, invalidateContract, archiveContractManual, getContractById, updateContractExecutionStatus } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getCodeByCodeName } from '@/api/user'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ContractDetail from '@/views/contract/ContractDetail.vue'

export default {
  name: 'ContractExecution',
  mixins: [paginationMixin],
  components: {
    ContractDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      contractTypeOptions: [],
      executionStatusOptions: [],
      contractExpireDays: 30, // 默认临期天数
      searchForm: {
        contractNo: '',
        contractName: '',
        executionStatus: '',
        contractType: ''
      },
      detailVisible: false,
      selectedContractId: null
    }
  },
  mounted() {
    // 进入页面时触发更新合同执行状态
    this.updateExecutionStatus()
    this.loadCodeTypeOptions()
    this.loadContractExpireDays()
    this.loadData()
  },
  methods: {
    /**
     * 更新合同执行状态
     */
    async updateExecutionStatus() {
      try {
        await updateContractExecutionStatus()
        // 静默执行，不显示成功提示
      } catch (error) {
        // 静默失败，不显示错误提示，避免影响用户体验
        console.error('更新合同执行状态失败:', error)
      }
    },
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      this.executionStatusOptions = await getCodeTypeOptions('EXECUTION_STATUS')
    },
    async loadContractExpireDays() {
      try {
        const response = await getCodeByCodeName('合同临期天数提醒')
        if (response.code === 200 && response.data && response.data.codeValue) {
          this.contractExpireDays = parseInt(response.data.codeValue) || 30
        }
      } catch (error) {
        console.error('获取合同临期天数失败:', error)
      }
    },
    loadData() {
      this.loading = true
      // 每次加载数据时重新获取临期天数参数（确保参数更新后立即生效）
      this.loadContractExpireDays()
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        contractNo: this.searchForm.contractNo || undefined,
        contractName: this.searchForm.contractName || undefined,
        executionStatus: this.searchForm.executionStatus || undefined,
        contractType: this.searchForm.contractType || undefined
      }
      getApprovedContractsPage(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.tableData = []
        this.pagination.total = 0
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        contractNo: '',
        contractName: '',
        executionStatus: '',
        contractType: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    getExecutionStatusName(codeValue) {
      if (!codeValue) return '-'
      const option = this.executionStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusType(codeValue) {
      // 根据执行状态返回对应的标签类型
      if (!codeValue) return 'info'
      const status = codeValue.toUpperCase()
      if (status === 'PENDING_EXECUTION' || status === '待履约') return 'warning'
      if (status === 'EXECUTING' || status === '履约中') return 'success'
      if (status === 'COMPLETED' || status === '已履约') return 'success'
      if (status === 'ARCHIVED' || status === '已归档') return 'info'
      if (status === 'INVALID' || status === '已失效') return 'danger'
      return 'info'
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleInvalidate(row) {
      this.$confirm('确认失效该合同吗？失效后合同将不能再执行。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        invalidateContract(row.pactId).then(response => {
          if (response.code === 200) {
            this.$message.success('失效成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '失效失败')
          }
        }).catch(error => {
          this.$message.error('失效失败：' + (error.message || '未知错误'))
        })
      }).catch(() => {})
    },
    handleArchive(row) {
      // 校验：只有已履约状态的合同才能归档
      const executionStatus = row.executionStatus
      if (!executionStatus || (executionStatus !== 'COMPLETED' && executionStatus !== '已履约')) {
        this.$message.warning('只有已履约状态的合同才能归档')
        return
      }
      
      this.$confirm('确认归档该合同吗？归档后合同将不能再变更。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        archiveContractManual(row.pactId).then(response => {
          if (response.code === 200) {
            this.$message.success('归档成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '归档失败')
          }
        }).catch(error => {
          this.$message.error('归档失败：' + (error.message || '未知错误'))
        })
      }).catch(() => {})
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusName(codeValue) {
      const option = this.executionStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusType(codeValue) {
      if (!codeValue) return 'info'
      const status = codeValue.toUpperCase()
      if (status === 'PENDING_EXECUTION' || status === '待履约') return 'info'
      if (status === 'EXECUTING' || status === '履约中') return 'success'
      if (status === 'COMPLETED' || status === '已履约') return 'success'
      if (status === 'ARCHIVED' || status === '已归档') return 'info'
      if (status === 'INVALID' || status === '已失效') return 'danger'
      return 'info'
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    // 判断行是否临期，临期则返回class名称用于标红
    getRowClassName({ row }) {
      if (row.endDate) {
        const endDate = new Date(row.endDate)
        const now = new Date()
        const diffTime = endDate.getTime() - now.getTime()
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
        
        // 如果距离结束日期 <= 临期天数，且还没有结束（diffDays > 0），则标红
        if (diffDays > 0 && diffDays <= this.contractExpireDays) {
          return 'expiring-row'
        }
      }
      return ''
    },
    handleViewDetail(row) {
      this.selectedContractId = row.pactId
      this.detailVisible = true
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportExecution(false)
      } else if (command === 'all') {
        this.handleExportExecution(true)
      }
    },
    // 导出合同执行数据
    async handleExportExecution(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            contractNo: this.searchForm.contractNo || undefined,
            contractName: this.searchForm.contractName || undefined,
            executionStatus: this.searchForm.executionStatus || undefined,
            contractType: this.searchForm.contractType || undefined
          }
          
          const res = await getApprovedContractsPage(params)
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
        const headers = ['合同编号', '合同名称', '合同类型', '签订日期', '执行状态', '开始日期', '结束日期']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.contractNo || ''),
            String(item.contractName || ''),
            String(this.getContractTypeName(item.contractType) || ''),
            String(this.formatDate(item.signDate) || ''),
            String(this.getExecutionStatusName(item.executionStatus) || ''),
            String(this.formatDate(item.startDate) || ''),
            String(this.formatDate(item.endDate) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '合同执行' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '合同执行' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.contract-execution {
  padding: 20px;
}

/* 临期合同整行标红 */
::v-deep .expiring-row {
  background-color: #fee !important;
}

::v-deep .expiring-row td {
  background-color: #fee !important;
}
</style>
