<template>
  <div class="cost-dept">
    <el-card>
      <div slot="header" class="clearfix">
        <span>科室成本</span>
      </div>

      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="核算周期">
          <el-select v-model="searchForm.cycleId" placeholder="请选择周期" clearable filterable style="width: 150px;">
            <el-option
              v-for="cycle in cycleList"
              :key="cycle.cycleId"
              :label="cycle.cycleName"
              :value="cycle.cycleId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成本要素">
          <el-select v-model="searchForm.elementType" placeholder="请选择成本要素" clearable filterable style="width: 150px;">
            <el-option
              v-for="item in elementTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="costNo" label="成本编号" width="180"></el-table-column>
        <el-table-column prop="cycleName" label="核算周期" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="elementType" label="成本要素" width="120">
          <template slot-scope="scope">
            {{ getElementTypeName(scope.row.elementType) }}
          </template>
        </el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.costAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="occurDate" label="发生日期" width="120"></el-table-column>
        <el-table-column prop="payType" label="付款方式" width="120">
          <template slot-scope="scope">
            {{ getPayTypeName(scope.row.payType) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        ></el-pagination>
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
  </div>
</template>

<script>
import { getCostCycleList, getCostLedgerList } from '@/api/cost'
import { getCodeByType } from '@/api/user'
import { exportExcel } from '@/api/common'
import { mapState } from 'vuex'

export default {
  name: 'CostDept',
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        cycleId: null,
        elementType: ''
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      cycleList: [],
      elementTypeOptions: [], // 成本要素选项
      payTypeOptions: [] // 付款方式选项
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadCycleList()
    this.loadElementTypeOptions()
    this.loadPayTypeOptions()
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        cycleId: this.searchForm.cycleId,
        elementType: this.searchForm.elementType || null
      }
      // 使用台账接口，后端会根据登录用户所在科室过滤
      getCostLedgerList(params).then(response => {
        if (response.code === 200) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '加载失败')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    loadCycleList() {
      getCostCycleList({ status: 1 }).then(response => {
        if (response.code === 200) {
          this.cycleList = response.data || []
        }
      })
    },
    loadElementTypeOptions() {
      getCodeByType('ELEMENT_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.elementTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        this.elementTypeOptions = []
      })
    },
    loadPayTypeOptions() {
      getCodeByType('PAYMENT_METHOD').then(response => {
        if (response.code === 200 && response.data) {
          this.payTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        this.payTypeOptions = []
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        cycleId: null,
        elementType: ''
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    getElementTypeName(codeValue) {
      if (!codeValue) return ''
      const option = this.elementTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getPayTypeName(codeValue) {
      if (!codeValue) return ''
      const option = this.payTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportCost(false)
      } else if (command === 'all') {
        this.handleExportCost(true)
      }
    },
    // 导出成本数据
    async handleExportCost(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            cycleId: this.searchForm.cycleId,
            elementType: this.searchForm.elementType || null
          }
          
          const res = await getCostLedgerList(params)
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
        const headers = ['成本编号', '核算周期', '部门', '成本要素', '成本金额', '发生日期', '付款方式', '备注']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.costNo || ''),
            String(item.cycleName || ''),
            String(item.deptName || ''),
            String(this.getElementTypeName(item.elementType) || ''),
            String(item.costAmount ? '¥' + item.costAmount : ''),
            String(item.occurDate || ''),
            String(this.getPayTypeName(item.payType) || ''),
            String(item.remark || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '科室成本' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '科室成本' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
    },
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style scoped>
.cost-dept {
  padding: 20px;
}
</style>

