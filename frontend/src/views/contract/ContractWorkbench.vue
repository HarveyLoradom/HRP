<template>
  <div class="contract-workbench">
    <el-card>
      <div slot="header">
        <span>工作台</span>
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
              <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 100%;">
                <el-option
                  v-for="option in applyStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
        </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同类型:" label-width="100px">
              <el-select v-model="searchForm.contractType" placeholder="请选择状态:" clearable style="width: 100%;">
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

      <el-table :data="paginatedTableData" border style="width: 100%" v-loading="loading">
        <el-table-column label="合同编号:" width="160">
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
        <el-table-column prop="empName" label="申请人:" width="130">
          <template slot-scope="scope">
            {{ scope.row.empName || scope.row.empId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门:" width="150">
          <template slot-scope="scope">
            {{ scope.row.deptName || scope.row.deptId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contractAmount" label="合同金额:" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.contractAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态:" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.createTime) }}
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
      source-type="workbench"
      :contract-id="selectedContractId"
      @print="handleDetailPrint"
    />
  </div>
</template>

<script>
import { getAllContractsPage, getContractByNo } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { getUserById, getUserByAccount } from '@/api/user'
import { exportExcel } from '@/api/common'
import ContractDetail from '@/views/contract/ContractDetail.vue'

export default {
  name: 'ContractWorkbench',
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
      applyStatusOptions: [],
      executionStatusOptions: [],
      searchForm: {
        contractNo: '',
        contractName: '',
        status: '',
        contractType: ''
      },
      detailVisible: false,
      selectedContractId: null
    }
  },
  computed: {
    filteredTableData() {
      let filtered = [...this.tableData]
      
      if (this.searchForm.contractNo) {
        filtered = filtered.filter(item => 
          item.contractNo && item.contractNo.includes(this.searchForm.contractNo)
        )
      }
      if (this.searchForm.contractName) {
        filtered = filtered.filter(item => 
          item.contractName && item.contractName.includes(this.searchForm.contractName)
        )
      }
      if (this.searchForm.status) {
        filtered = filtered.filter(item => item.status === this.searchForm.status)
      }
      if (this.searchForm.contractType) {
        filtered = filtered.filter(item => item.contractType === this.searchForm.contractType)
      }
      
      return filtered
    },
    paginatedTableData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.filteredTableData.slice(start, end)
    },
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
      this.executionStatusOptions = await getCodeTypeOptions('EXECUTION_STATUS')
    },
    loadData() {
      this.loading = true
      // 先加载所有数据（因为后端暂时没有多条件查询接口，使用前端过滤）
      getAllContractsPage(1, 10000).then(response => {
        if (response.code === 200 && response.data) {
          let allData = response.data.records || []
          
          // 加载申请人、部门信息
          this.loadUserInfoForContracts(allData).then(() => {
            this.tableData = allData
            // 更新总数（基于过滤后的数据）
            this.$nextTick(() => {
              this.pagination.total = this.filteredTableData.length
            })
            this.loading = false
          })
        } else {
          this.tableData = []
          this.pagination.total = 0
          this.loading = false
        }
      }).catch(() => {
        this.tableData = []
        this.pagination.total = 0
        this.loading = false
      })
    },
    // 为合同列表加载申请人、部门信息
    async loadUserInfoForContracts(contracts) {
      for (const contract of contracts) {
        if (contract.empId && !contract.empName) {
          try {
            const userResponse = await getUserById(contract.empId)
            if (userResponse.code === 200 && userResponse.data) {
              contract.empName = userResponse.data.name || userResponse.data.empName || ''
              contract.deptName = userResponse.data.deptName || ''
            }
          } catch (error) {
            console.error('加载用户信息失败', error)
          }
        }
      }
    },
    handleSearch() {
      this.pagination.page = 1
      // 更新总数（基于过滤后的数据）
      this.$nextTick(() => {
        this.pagination.total = this.filteredTableData.length
      })
    },
    handleReset() {
      this.searchForm = {
        contractNo: '',
        contractName: '',
        status: '',
        contractType: ''
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
    async handleViewDetail(row) {
      this.selectedContractId = row.pactId
      this.detailVisible = true
    },
    handleDetailPrint() {
      // 打印功能由统一组件处理，这里只需要刷新数据（如果需要）
      // this.loadData()
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
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
        'EXECUTING': '',
        'COMPLETED': 'success',
        'ARCHIVED': 'info'
      }
      return typeMap[status] || ''
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
    formatDateTime(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
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
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportWorkbench(false)
      } else if (command === 'all') {
        this.handleExportWorkbench(true)
      }
    },
    // 导出工作台数据
    async handleExportWorkbench(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，重新查询并应用筛选条件
          const res = await getAllContractsPage(1, 10000)
          if (res.code === 200 && res.data) {
            let allData = res.data.records || []
            // 应用筛选条件
            let filtered = [...allData]
            if (this.searchForm.contractNo) {
              filtered = filtered.filter(item => 
                item.contractNo && item.contractNo.includes(this.searchForm.contractNo)
              )
            }
            if (this.searchForm.contractName) {
              filtered = filtered.filter(item => 
                item.contractName && item.contractName.includes(this.searchForm.contractName)
              )
            }
            if (this.searchForm.status) {
              filtered = filtered.filter(item => item.status === this.searchForm.status)
            }
            if (this.searchForm.contractType) {
              filtered = filtered.filter(item => item.contractType === this.searchForm.contractType)
            }
            dataToExport = filtered
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据（使用前端分页后的数据）
          dataToExport = this.paginatedTableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['合同编号', '合同名称', '合同类型', '申请人', '部门', '合同金额', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.contractNo || ''),
            String(item.contractName || ''),
            String(this.getContractTypeName(item.contractType) || ''),
            String(item.empName || item.empId || '-'),
            String(item.deptName || item.deptId || '-'),
            String(item.contractAmount ? '¥' + item.contractAmount : ''),
            String(this.getStatusText(item.status) || ''),
            String(this.formatDateOnly(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '合同工作台' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '合同工作台' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.contract-workbench {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
