<template>
  <div class="attendance-query">
    <el-card>
      <div slot="header" class="clearfix">
        <span>考勤查询</span>
        <div style="float: right;">
          <el-button type="primary" @click="handleClock" :disabled="clockLoading">
            {{ clockButtonText }}
          </el-button>
          <el-button type="warning" @click="handleSupplementApply">补卡</el-button>
        </div>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="考勤日期:">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 250px;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="考勤状态:">
          <el-select v-model="searchForm.attStatus" placeholder="全部" clearable style="width: 150px;">
            <el-option
              v-for="option in attStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请类型:">
          <el-select v-model="searchForm.attType" placeholder="全部" clearable style="width: 150px;">
            <el-option
              v-for="option in attTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请子类型:">
          <el-select v-model="searchForm.attSubType" placeholder="全部" clearable style="width: 150px;">
            <el-option
              v-for="option in currentAttSubTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
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
        <el-table-column prop="recordId" label="打卡ID号" width="80" align="center"></el-table-column>
        <el-table-column prop="attDate" label="考勤日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.attDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="attStartTime" label="上班时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.attStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="attEndTime" label="下班时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.attEndTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="attStatus" label="考勤状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.attStatus)">
              {{ getStatusText(scope.row.attStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="day" label="天数" width="80" align="center"></el-table-column>
        <el-table-column prop="isSupplement" label="是否补卡" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isSupplement === 1 ? 'warning' : 'success'">
              {{ scope.row.isSupplement === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attType" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getAttTypeText(scope.row.attType) }}
          </template>
        </el-table-column>
        <el-table-column prop="attSubType" label="申请子类型" width="120">
          <template slot-scope="scope">
            {{ getAttSubTypeText(scope.row.attSubType, scope.row.attType) }}
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
    
    <!-- 补卡申请对话框 -->
    <el-dialog
      title="补卡申请"
      :visible.sync="supplementDialogVisible"
      width="1000px"
      :close-on-click-modal="false"
    >
      <el-form :model="supplementForm" :rules="supplementRules" ref="supplementForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申请人:" prop="empName">
              <el-input v-model="supplementForm.empName" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="科室:" prop="deptName">
              <el-input v-model="supplementForm.deptName" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机号:" prop="empPhone">
              <el-input v-model="supplementForm.empPhone" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开始时间:" prop="startTime">
              <el-date-picker
                v-model="supplementForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="yyyy-MM-dd HH:mm:ss"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间:" prop="endTime">
              <el-date-picker
                v-model="supplementForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="yyyy-MM-dd HH:mm:ss"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否护士:">
              <el-radio-group v-model="supplementForm.isNurse">
                <el-radio :label="0">否</el-radio>
                <el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板配置:">
              <el-input :value="getTemplateConfigName(6)" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="补卡ID号:">
              <el-input
                v-model="supplementForm.supplementId"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="补卡原因:" prop="applyReason">
              <el-input
                v-model="supplementForm.applyReason"
                type="textarea"
                :rows="3"
                placeholder="请输入补卡原因"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplementDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitSupplement" :loading="submitLoading">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getHrAttRecordPage, getTodayAttRecord, clockIn, clockOut, saveHrApply, submitHrApply } from '@/api/hr'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getTemplateConfigPage } from '@/api/templateConfig'
import { exportExcel } from '@/api/common'

export default {
  name: 'AttendanceQuery',
  data() {
    return {
      loading: false,
      clockLoading: false,
      submitLoading: false,
      exportLoading: false,
      searchForm: {
        dateRange: [],
        attStatus: '',
        attType: '',
        attSubType: ''
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      attStatusOptions: [],
      attTypeOptions: [],
      attSubTypeOptionsMap: {}, // 存储按考勤类型分组的子类型选项
      currentAttSubTypeOptions: [],
      todayRecord: null,
      clockButtonText: '上班打卡',
      supplementDialogVisible: false,
      supplementForm: {
        empId: null,
        empName: '',
        deptName: '',
        empPhone: '',
        startTime: '',
        endTime: '',
        isNurse: 0,
        applyReason: '',
        supplementId: '',
        templateConfigId: 6
      },
      templateConfigMap: {},
      supplementRules: {
      },
      exportLoading: false
    }
  },
  mounted() {
    this.loadAttStatusOptions()
    this.loadAttTypeOptions()
    this.loadTodayRecord()
    this.loadData()
  },
  watch: {
    'searchForm.attType'(newVal) {
      this.searchForm.attSubType = ''
      this.currentAttSubTypeOptions = []
      if (newVal) {
        this.currentAttSubTypeOptions = this.attSubTypeOptionsMap[newVal] || []
      }
    }
  },
  methods: {
    async loadAttStatusOptions() {
      this.attStatusOptions = await getCodeTypeOptions('HR_ATT_STATUS')
    },
    async loadAttTypeOptions() {
      try {
        this.attTypeOptions = await getCodeTypeOptions('HR_APPLY_TYPE')
        // 加载所有考勤类型的子类型选项
        if (this.attTypeOptions && this.attTypeOptions.length > 0) {
          for (const typeOption of this.attTypeOptions) {
            try {
              const subTypeOptions = await getCodeTypeOptions(typeOption.value + '_SUB_TYPE')
              this.$set(this.attSubTypeOptionsMap, typeOption.value, subTypeOptions || [])
            } catch (error) {
              console.error(`加载${typeOption.value}的子类型选项失败:`, error)
              this.$set(this.attSubTypeOptionsMap, typeOption.value, [])
            }
          }
        }
      } catch (error) {
        console.error('加载考勤类型选项失败:', error)
      }
    },
    async loadTodayRecord() {
      const userInfo = this.$store.state.user.userInfo || {}
      const empId = userInfo.empId
      if (!empId) {
        return
      }
      try {
        const response = await getTodayAttRecord(empId)
        if (response.code === 200) {
          this.todayRecord = response.data
          this.updateClockButton()
        }
      } catch (error) {
        console.error('加载今日打卡记录失败:', error)
      }
    },
    updateClockButton() {
      if (!this.todayRecord) {
        this.clockButtonText = '上班打卡'
      } else if (!this.todayRecord.attEndTime) {
        this.clockButtonText = '下班打卡'
      } else {
        this.clockButtonText = '今日已打卡'
      }
    },
    async handleClock() {
      const userInfo = this.$store.state.user.userInfo || {}
      const empId = userInfo.empId
      if (!empId) {
        this.$message.error('未获取到员工信息，请重新登录')
        return
      }
      
      this.clockLoading = true
      try {
        let response
        if (!this.todayRecord) {
          // 上班打卡
          response = await clockIn(empId)
        } else if (!this.todayRecord.attEndTime) {
          // 下班打卡
          response = await clockOut(empId)
        } else {
          this.$message.warning('今天已经完成打卡')
          this.clockLoading = false
          return
        }
        
        if (response.code === 200) {
          this.$message.success('打卡成功')
          this.loadTodayRecord()
          this.loadData()
        } else {
          this.$message.error(response.message || '打卡失败')
        }
      } catch (error) {
        this.$message.error('打卡失败：' + (error.message || '未知错误'))
      } finally {
        this.clockLoading = false
      }
    },
    async handleSupplementApply() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.supplementForm.empId = userInfo.empId
      this.supplementForm.empName = userInfo.empName || ''
      this.supplementForm.deptName = userInfo.deptName || ''
      this.supplementForm.empPhone = userInfo.empPhone || ''
      this.supplementForm.startTime = ''
      this.supplementForm.endTime = ''
      this.supplementForm.isNurse = userInfo.isNurse || 0
      this.supplementForm.applyReason = ''
      this.supplementForm.supplementId = ''
      this.supplementForm.templateConfigId = 6
      // 加载模板配置（用于显示名称）
      await this.loadTemplateConfigs()
      this.supplementDialogVisible = true
    },
    async loadTemplateConfigs() {
      try {
        const res = await getTemplateConfigPage('HR_TYPE', 1, 1, 100)
        if (res.code === 200 && res.data) {
          const configs = res.data.records || res.data.list || []
          this.templateConfigMap = configs.reduce((map, config) => {
            map[config.configId] = config
            return map
          }, {})
        }
      } catch (error) {
        console.error('加载模板配置失败:', error)
      }
    },
    getTemplateConfigName(configId) {
      if (!configId) return ''
      const config = this.templateConfigMap[configId]
      if (config) {
        return config.businessTypeName || config.businessTypeValue || config.businessType || ''
      }
      return ''
    },
    async handleSubmitSupplement() {
      this.$refs.supplementForm.validate(async (valid) => {
        if (!valid) {
          return false
        }
        
        this.submitLoading = true
        try {
          // 构建业务申请数据，申请类型固定为SUPPLY（补卡）
          const applyData = {
            hrApplyType: 'SUPPLY',
            hrApplySubType: '-', // 补卡申请默认子类型为SUPPLY
            empId: this.supplementForm.empId,
            startTime: this.supplementForm.startTime,
            endTime: this.supplementForm.endTime,
            isNurse: this.supplementForm.isNurse,
            applyReason: this.supplementForm.applyReason,
            supplementId: this.supplementForm.supplementId ? parseInt(this.supplementForm.supplementId) : null, // 补卡ID号（转为数字）
            templateConfigId: this.supplementForm.templateConfigId,
            status: 'DRAFT',
            applyDay: '0' // 默认天数，审批后会重新计算
          }
          
          // 先保存申请
          const saveResponse = await saveHrApply(applyData)
          if (saveResponse.code === 200 && saveResponse.data && saveResponse.data.applyId) {
            // 保存成功后，提交申请
            const applyId = saveResponse.data.applyId
            const submitResponse = await submitHrApply(applyId)
            if (submitResponse.code === 200) {
              this.$message.success('补卡申请已提交')
              this.supplementDialogVisible = false
            } else {
              this.$message.error(submitResponse.message || '提交失败')
            }
          } else {
            this.$message.error(saveResponse.message || '保存失败')
          }
        } catch (error) {
          this.$message.error('提交失败：' + (error.message || '未知错误'))
        } finally {
          this.submitLoading = false
        }
      })
    },
    loadData() {
      this.loading = true
      const userInfo = this.$store.state.user.userInfo || {}
      const empId = userInfo.empId
      
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        empId: empId
      }
      
      if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
        params.startDate = this.searchForm.dateRange[0]
        params.endDate = this.searchForm.dateRange[1]
      }
      
      if (this.searchForm.attStatus) {
        params.attStatus = this.searchForm.attStatus
      }
      
      if (this.searchForm.attType) {
        params.attType = this.searchForm.attType
      }
      
      if (this.searchForm.attSubType) {
        params.attSubType = this.searchForm.attSubType
      }
      
      getHrAttRecordPage(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '加载失败')
        }
      }).catch(error => {
        this.$message.error('加载失败：' + (error.message || '未知错误'))
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm.dateRange = []
      this.searchForm.attStatus = ''
      this.searchForm.attType = ''
      this.searchForm.attSubType = ''
      this.currentAttSubTypeOptions = []
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
    formatDate(date) {
      if (!date) return '-'
      if (typeof date === 'string') {
        return date.split('T')[0]
      }
      return date
    },
    formatDateTime(datetime) {
      if (!datetime) return '-'
      if (typeof datetime === 'string') {
        return datetime.replace('T', ' ').substring(0, 19)
      }
      return datetime
    },
    getStatusText(status) {
      const option = this.attStatusOptions.find(opt => opt.value === status)
      return option ? option.label : status || '-'
    },
    getStatusType(status) {
      const typeMap = {
        'NORMAL': 'success',
        'LATE': 'warning',
        'EARLY_LEAVE': 'danger'
      }
      return typeMap[status] || ''
    },
    getAttTypeText(attType) {
      if (!attType) return '-'
      const option = this.attTypeOptions.find(opt => opt.value === attType)
      return option ? option.label : attType
    },
    getAttSubTypeText(attSubType, attType) {
      if (!attSubType || !attType) return attSubType || '-'
      const subTypeOptions = this.attSubTypeOptionsMap[attType] || []
      const option = subTypeOptions.find(item => item.value === attSubType)
      return option ? option.label : attSubType
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportAttendance(false)
      } else if (command === 'all') {
        this.handleExportAttendance(true)
      }
    },
    // 导出考勤记录
    async handleExportAttendance(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const userInfo = this.$store.state.user.userInfo || {}
          const empId = userInfo.empId
          
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            empId: empId
          }
          
          if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
            params.startDate = this.searchForm.dateRange[0]
            params.endDate = this.searchForm.dateRange[1]
          }
          
          if (this.searchForm.attStatus) {
            params.attStatus = this.searchForm.attStatus
          }
          
          if (this.searchForm.attType) {
            params.attType = this.searchForm.attType
          }
          
          if (this.searchForm.attSubType) {
            params.attSubType = this.searchForm.attSubType
          }
          
          const res = await getHrAttRecordPage(params)
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
        const headers = ['打卡ID号', '考勤日期', '上班时间', '下班时间', '考勤状态', '天数', '是否补卡', '申请类型', '申请子类型']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.recordId || ''),
            String(this.formatDate(item.attDate) || ''),
            String(this.formatDateTime(item.attStartTime) || ''),
            String(this.formatDateTime(item.attEndTime) || ''),
            String(this.getStatusText(item.attStatus) || ''),
            String(item.day || ''),
            String(item.isSupplement === 1 ? '是' : '否'),
            String(this.getAttTypeText(item.attType) || ''),
            String(this.getAttSubTypeText(item.attSubType, item.attType) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '考勤记录' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '考勤记录' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.attendance-query {
  padding: 20px;
}
</style>
