<template>
  <div class="attendance-statistics" v-loading="importLoading">
    <el-card>
      <div slot="header" class="clearfix">
        <span>考勤统计</span>
        <div style="float: right;">
          <el-button type="text" @click="handleDownloadTemplate">下载导入模板</el-button>
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-left: 10px;"
          >
            <el-button type="text">批量导入</el-button>
          </el-upload>
          <el-button type="primary" style="margin-left: 10px;" @click="openCalcDialog">统计考勤</el-button>
        </div>
      </div>
      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="工号:">
          <el-input v-model="searchForm.empCode" placeholder="请输入工号" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="姓名:">
          <el-input v-model="searchForm.empName" placeholder="请输入姓名" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="科室:">
          <el-select v-model="searchForm.deptId" placeholder="全部" clearable filterable style="width: 160px;">
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤月份:">
          <el-date-picker
            v-model="searchForm.attMonth"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 160px;"
            clearable
          />
        </el-form-item>
        <el-form-item label="台账状态:">
          <el-select v-model="searchForm.ledgerStatus" placeholder="全部" clearable style="width: 140px;">
            <el-option
              v-for="opt in ledgerStatusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="empCode" label="工号" width="120" />
        <el-table-column prop="empName" label="姓名" width="120" />
        <el-table-column prop="deptName" label="科室" width="150" />
        <el-table-column prop="attMonth" label="考勤月份" width="100" />
        <el-table-column prop="monthWorkDays" label="工作日" width="80" align="center" />
        <el-table-column prop="attDays" label="正常出勤" width="100" align="center" />
        <el-table-column prop="leaveDays" label="请假天数" width="100" align="center" />
        <el-table-column prop="overtimeDays" label="加班天数" width="100" align="center" />
        <el-table-column prop="absentDays" label="旷工天数" width="100" align="center" />
        <el-table-column prop="ledgerStatus" label="台账状态" width="100" align="center">
          <template slot-scope="scope">
            {{ getLedgerStatusText(scope.row.ledgerStatus) }}
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

    <!-- 统计考勤对话框 -->
    <el-dialog title="统计考勤" :visible.sync="calcDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="calcForm" ref="calcFormRef" label-width="100px" :rules="calcRules">
        <el-form-item label="考勤月份" prop="attMonth">
          <el-date-picker
            v-model="calcForm.attMonth"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="统计周期" prop="dateRange">
          <el-date-picker
            v-model="calcForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="工作日" prop="monthWorkDays">
          <el-input-number v-model="calcForm.monthWorkDays" :min="0" :max="31" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="calcDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="calcLoading" @click="handleCalc">开始统计</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getHrAttLedgerPage, calculateHrAttLedger, downloadHrAttLedgerTemplate, importHrAttLedger } from '@/api/hr'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getDeptList } from '@/api/user'
import { exportExcel } from '@/api/common'
import Cookies from 'js-cookie'

export default {
  name: 'AttendanceStatistics',
  data() {
    return {
      loading: false,
      calcLoading: false,
      importLoading: false,
      exportLoading: false,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      searchForm: {
        empCode: '',
        empName: '',
        deptId: null,
        attMonth: '',
        ledgerStatus: ''
      },
      ledgerStatusOptions: [],
      deptOptions: [],
      calcDialogVisible: false,
      calcForm: {
        attMonth: '',
        dateRange: [],
        monthWorkDays: 0
      },
      calcRules: {
        attMonth: [{ required: true, message: '请选择考勤月份', trigger: 'change' }],
        dateRange: [{ type: 'array', required: true, message: '请选择统计周期', trigger: 'change' }],
        monthWorkDays: [{ required: true, message: '请输入工作日', trigger: 'change' }]
      },
      uploadUrl: '/api/hr/attendance/ledger/import',
      uploadHeaders: {}
    }
  },
  mounted() {
    this.loadLedgerStatusOptions()
    this.loadDeptOptions()
    this.loadData()
    // 设置上传请求头
    const token = this.$store.state.user.token || Cookies.get('token')
    if (token) {
      this.uploadHeaders['Authorization'] = 'Bearer ' + token
    }
  },
  methods: {
    async loadDeptOptions() {
      try {
        const res = await getDeptList(0)
        if (res.code === 200 && res.data) {
          this.deptOptions = res.data || []
        } else {
          this.deptOptions = []
        }
      } catch (e) {
        console.error('加载科室失败:', e)
        this.deptOptions = []
      }
    },
    async loadLedgerStatusOptions() {
      try {
        this.ledgerStatusOptions = await getCodeTypeOptions('HR_LEDGER_STATUS')
      } catch (e) {
        this.ledgerStatusOptions = []
      }
    },
    getLedgerStatusText(value) {
      if (!value) return '-'
      const opt = (this.ledgerStatusOptions || []).find(x => x.value === value)
      return opt ? opt.label : value
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        attMonth: this.searchForm.attMonth || null,
        ledgerStatus: this.searchForm.ledgerStatus || null,
        empCode: this.searchForm.empCode || null,
        empName: this.searchForm.empName || null,
        deptId: this.searchForm.deptId || null
      }
      getHrAttLedgerPage(params).then(res => {
        if (res.code === 200 && res.data) {
          this.tableData = res.data.list || res.data.records || []
          this.pagination.total = res.data.total || 0
        } else {
          this.$message.error(res.message || res.msg || '加载失败')
        }
      }).catch(err => {
        this.$message.error('加载失败：' + (err.message || '未知错误'))
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm.empCode = ''
      this.searchForm.empName = ''
      this.searchForm.deptId = null
      this.searchForm.attMonth = ''
      this.searchForm.ledgerStatus = ''
      this.handleSearch()
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    openCalcDialog() {
      this.calcForm = { attMonth: '', dateRange: [], monthWorkDays: 0 }
      this.calcDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.calcFormRef) this.$refs.calcFormRef.clearValidate()
      })
    },
    handleCalc() {
      this.$refs.calcFormRef.validate(async valid => {
        if (!valid) return
        const [startDate, endDate] = this.calcForm.dateRange || []
        this.calcLoading = true
        try {
          const res = await calculateHrAttLedger(this.calcForm.attMonth, startDate, endDate, this.calcForm.monthWorkDays)
          if (res.code === 200) {
            this.$message.success(`统计完成，已更新${res.data || 0}条台账`)
            this.calcDialogVisible = false
            // 刷新主列表
            this.handleSearch()
          } else {
            this.$message.error(res.message || res.msg || '统计失败')
          }
        } catch (e) {
          this.$message.error('统计失败：' + (e.message || '未知错误'))
        } finally {
          this.calcLoading = false
        }
      })
    },
    handleDownloadTemplate() {
      downloadHrAttLedgerTemplate()
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                      file.type === 'application/vnd.ms-excel'
      if (!isExcel) {
        this.$message.error('只能上传Excel文件！')
        return false
      }
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB！')
        return false
      }
      // 校验通过后开启导入加载动画
      this.importLoading = true
      return true
    },
    handleUploadSuccess(response) {
      let res = response

      // 如果是字符串，尝试解析为 JSON
      if (typeof res === 'string') {
        try {
          res = JSON.parse(res)
        } catch (e) {
          this.$message.error('导入失败：响应格式错误')
          this.importLoading = false
          return
        }
      }

      // 如果是 axios 响应，可能在 data 里再包一层
      if (res && res.data && typeof res.data === 'object' && typeof res.code === 'undefined') {
        res = res.data
      }

      if (res && res.code === 200) {
        this.$message.success(res.message || '导入成功')
        // 重新加载数据，保证导入结果立即体现在列表上
        this.loadData()
      } else {
        this.$message.error((res && res.message) || '导入失败')
      }
      // 无论成功失败，关闭加载动画
      this.importLoading = false
    },
    handleUploadError(error) {
      this.$message.error('导入失败：' + (error.message || '未知错误'))
      this.importLoading = false
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportAttendance(false)
      } else if (command === 'all') {
        this.handleExportAttendance(true)
      }
    },
    // 导出考勤统计数据
    async handleExportAttendance(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            attMonth: this.searchForm.attMonth || null,
            ledgerStatus: this.searchForm.ledgerStatus || null,
            empCode: this.searchForm.empCode || null,
            empName: this.searchForm.empName || null,
            deptId: this.searchForm.deptId || null
          }
          
          const res = await getHrAttLedgerPage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.list || res.data.records || []
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
        const headers = ['工号', '姓名', '科室', '考勤月份', '工作日', '正常出勤', '请假天数', '加班天数', '旷工天数', '台账状态']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.empCode || ''),
            String(item.empName || ''),
            String(item.deptName || ''),
            String(item.attMonth || ''),
            String(item.monthWorkDays || 0),
            String(item.attDays || 0),
            String(item.leaveDays || 0),
            String(item.overtimeDays || 0),
            String(item.absentDays || 0),
            String(this.getLedgerStatusText(item.ledgerStatus) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '考勤统计' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 检查响应是否是blob类型
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        // 检查是否是错误响应（blob类型的错误响应需要读取）
        if (response.data instanceof Blob && response.data.type === 'application/json') {
          const text = await response.data.text()
          const errorData = JSON.parse(text)
          this.$message.error('导出失败：' + (errorData.message || '服务器错误'))
          return
        }
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '考勤统计' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        // 如果是blob类型的错误响应，尝试读取错误信息
        if (error.response && error.response.data instanceof Blob) {
          try {
            const text = await error.response.data.text()
            const errorData = JSON.parse(text)
            this.$message.error('导出失败：' + (errorData.message || '服务器错误'))
          } catch (e) {
            this.$message.error('导出失败：服务器响应错误')
          }
        } else {
          this.$message.error('导出失败：' + (error.message || '未知错误'))
        }
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.attendance-statistics {
  padding: 20px;
}
</style>

