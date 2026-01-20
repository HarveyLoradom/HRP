<template>
  <div class="salary-calculate">
    <el-card>
      <div slot="header" class="clearfix">
        <span>薪酬计算</span>
        <div style="float: right;">
          <el-button type="primary" @click="handleCalculate" :loading="calculating">计算薪酬</el-button>
          <el-button type="success" @click="handleBatchPay" :disabled="selectedRows.length === 0" :loading="paying" style="margin-left: 10px;">
            发放薪酬
          </el-button>
        </div>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="核算月份:">
          <el-date-picker
            v-model="searchForm.calcMonth"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            clearable
            style="width: 160px;"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="工号:">
          <el-input v-model="searchForm.empCode" placeholder="请输入工号" clearable style="width: 140px;" @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="姓名:">
          <el-input v-model="searchForm.empName" placeholder="请输入姓名" clearable style="width: 140px;" @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="状态:">
          <el-select v-model="searchForm.calcStatus" placeholder="请选择状态" clearable style="width: 120px;" @change="handleSearch">
            <el-option label="未核算" value="UNCALC"></el-option>
            <el-option label="已核算" value="CALC"></el-option>
            <el-option label="已发放" value="PAID"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="empCode" label="工号" width="100"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="calcMonth" label="核算月份" width="110"></el-table-column>
        <el-table-column prop="basicSalary" label="基本工资" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.basicSalary) }}
          </template>
        </el-table-column>
        <el-table-column prop="postAllowance" label="岗位津贴" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.postAllowance) }}
          </template>
        </el-table-column>
        <el-table-column prop="overtimeSalary" label="加班工资" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.overtimeSalary) }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveDeduct" label="请假扣薪" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.leaveDeduct) }}
          </template>
        </el-table-column>
        <el-table-column prop="absentDeduct" label="旷工扣罚" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.absentDeduct) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalIncome" label="应发工资" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.totalIncome) }}
          </template>
        </el-table-column>
        <el-table-column prop="socialSecurity" label="个人社保" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.socialSecurity) }}
          </template>
        </el-table-column>
        <el-table-column prop="providentFund" label="个人公积金" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.providentFund) }}
          </template>
        </el-table-column>
        <el-table-column prop="personalTax" label="个人所得税" width="110" align="right">
          <template slot-scope="scope">
            {{ formatMoney(scope.row.personalTax) }}
          </template>
        </el-table-column>
        <el-table-column prop="netSalary" label="实发工资" width="110" align="right">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ formatMoney(scope.row.netSalary) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="calcStatus" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.calcStatus)">
              {{ getStatusText(scope.row.calcStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.calcStatus === 'CALC'"
              size="mini"
              type="success"
              @click="handlePay(scope.row)"
            >
              发放
            </el-button>
            <el-button
              v-else-if="scope.row.calcStatus === 'PAID'"
              size="mini"
              type="info"
              disabled
            >
              已发放
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页和导出 -->
      <div style="margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
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
    
    <!-- 计算薪酬对话框 -->
    <el-dialog title="计算薪酬" :visible.sync="calcDialogVisible" width="500px">
      <el-form :model="calcForm" label-width="120px">
        <el-form-item label="核算月份:" required>
          <el-date-picker
            v-model="calcForm.calcMonth"
            type="month"
            placeholder="选择月份"
            value-format="yyyy-MM"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="calcDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCalculate" :loading="calculating">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getHrSalCalculatePage, calculateSalary, batchCalculateSalary, paySalary, batchPaySalary, getHrAttLedgerPage } from '@/api/hr'
import { exportExcel } from '@/api/common'

export default {
  name: 'SalaryCalculate',
  data() {
    return {
      loading: false,
      calculating: false,
      paying: false,
      exportLoading: false,
      tableData: [],
      selectedRows: [],
      searchForm: {
        calcMonth: '',
        empCode: '',
        empName: '',
        calcStatus: ''
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      calcDialogVisible: false,
      calcForm: {
        calcMonth: ''
      }
    }
  },
  mounted() {
    // 默认显示当前月份
    const now = new Date()
    const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    this.searchForm.calcMonth = currentMonth
    this.calcForm.calcMonth = currentMonth
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size
        }
        
        if (this.searchForm.calcMonth) {
          params.calcMonth = this.searchForm.calcMonth
        }
        if (this.searchForm.empCode) {
          params.empCode = this.searchForm.empCode
        }
        if (this.searchForm.empName) {
          params.empName = this.searchForm.empName
        }
        if (this.searchForm.calcStatus) {
          params.calcStatus = this.searchForm.calcStatus
        }
        
        const res = await getHrSalCalculatePage(params)
        if (res.code === 200 && res.data) {
          this.tableData = res.data.records || res.data.list || []
          this.pagination.total = res.data.total || 0
        } else {
          this.$message.error(res.message || '加载数据失败')
          this.tableData = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
        this.tableData = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },
    // 查询
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    // 重置
    handleReset() {
      this.searchForm = {
        calcMonth: '',
        empCode: '',
        empName: '',
        calcStatus: ''
      }
      const now = new Date()
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      this.searchForm.calcMonth = currentMonth
      this.handleSearch()
    },
    // 分页
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    // 选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    // 打开计算对话框
    handleCalculate() {
      const now = new Date()
      this.calcForm.calcMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      this.calcDialogVisible = true
    },
    // 确认计算
    async confirmCalculate() {
      if (!this.calcForm.calcMonth) {
        this.$message.warning('请选择核算月份')
        return
      }
      
      this.calculating = true
      try {
        // 获取该月份所有未核算的考勤台账
        const ledgerRes = await getHrAttLedgerPage({
          attMonth: this.calcForm.calcMonth,
          ledgerStatus: 'UNCALC',
          page: 1,
          size: 10000
        })
        
        if (ledgerRes.code !== 200 || !ledgerRes.data || !ledgerRes.data.records || ledgerRes.data.records.length === 0) {
          this.$message.warning('该月份没有未核算的考勤台账，无法计算薪酬')
          this.calcDialogVisible = false
          return
        }
        
        const empIds = ledgerRes.data.records.map(ledger => ledger.empId)
        
        // 批量计算薪酬
        const res = await batchCalculateSalary(this.calcForm.calcMonth, empIds)
        if (res.code === 200) {
          this.$message.success('薪酬计算完成')
          this.calcDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(res.message || '计算失败')
        }
      } catch (error) {
        console.error('计算失败:', error)
        this.$message.error('计算失败：' + (error.message || '未知错误'))
      } finally {
        this.calculating = false
      }
    },
    // 发放薪酬（单个）
    async handlePay(row) {
      this.$confirm(`确定要发放 ${row.empName} 在 ${row.calcMonth} 的薪酬吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await paySalary(row.calcId)
          if (res.code === 200) {
            this.$message.success('发放成功')
            this.loadData()
          } else {
            this.$message.error(res.message || '发放失败')
          }
        } catch (error) {
          console.error('发放失败:', error)
          this.$message.error('发放失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },
    // 批量发放薪酬
    async handleBatchPay() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要发放的记录')
        return
      }
      
      // 检查是否都是已核算状态
      const notCalcRows = this.selectedRows.filter(row => row.calcStatus !== 'CALC')
      if (notCalcRows.length > 0) {
        this.$message.warning('只能发放已核算状态的薪酬')
        return
      }
      
      this.$confirm(`确定要发放选中的 ${this.selectedRows.length} 条薪酬记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.paying = true
        try {
          const calcIds = this.selectedRows.map(row => row.calcId)
          const res = await batchPaySalary(calcIds)
          if (res.code === 200) {
            this.$message.success('批量发放成功')
            this.selectedRows = []
            this.loadData()
          } else {
            this.$message.error(res.message || '批量发放失败')
          }
        } catch (error) {
          console.error('批量发放失败:', error)
          this.$message.error('批量发放失败：' + (error.message || '未知错误'))
        } finally {
          this.paying = false
        }
      }).catch(() => {})
    },
    // 格式化金额
    formatMoney(value) {
      const num = Number(value || 0)
      if (isNaN(num)) return '0.00'
      return num.toFixed(2)
    },
    // 获取状态类型
    getStatusType(status) {
      switch (status) {
        case 'UNCALC':
          return 'info'
        case 'CALC':
          return 'warning'
        case 'PAID':
          return 'success'
        default:
          return ''
      }
    },
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'UNCALC':
          return '未核算'
        case 'CALC':
          return '已核算'
        case 'PAID':
          return '已发放'
        default:
          return status || ''
      }
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportSalary(false)
      } else if (command === 'all') {
        this.handleExportSalary(true)
      }
    },
    // 导出薪酬数据
    async handleExportSalary(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000 // 设置一个很大的值以获取所有数据
          }
          
          if (this.searchForm.calcMonth) {
            params.calcMonth = this.searchForm.calcMonth
          }
          if (this.searchForm.empCode) {
            params.empCode = this.searchForm.empCode
          }
          if (this.searchForm.empName) {
            params.empName = this.searchForm.empName
          }
          if (this.searchForm.calcStatus) {
            params.calcStatus = this.searchForm.calcStatus
          }
          
          const res = await getHrSalCalculatePage(params)
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
        const headers = ['工号', '姓名', '核算月份', '基本工资', '岗位津贴', '加班工资', '请假扣薪', '旷工扣罚', '应发工资', '个人社保', '个人公积金', '个人所得税', '实发工资', '状态']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.empCode || '',
            item.empName || '',
            item.calcMonth || '',
            this.formatMoney(item.basicSalary),
            this.formatMoney(item.postAllowance),
            this.formatMoney(item.overtimeSalary),
            this.formatMoney(item.leaveDeduct),
            this.formatMoney(item.absentDeduct),
            this.formatMoney(item.totalIncome),
            this.formatMoney(item.socialSecurity),
            this.formatMoney(item.providentFund),
            this.formatMoney(item.personalTax),
            this.formatMoney(item.netSalary),
            this.getStatusText(item.calcStatus)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '薪酬计算' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '薪酬计算' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.salary-calculate {
  padding: 20px;
}
</style>
