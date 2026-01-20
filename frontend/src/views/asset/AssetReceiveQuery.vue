<template>
  <div class="asset-receive-query">
    <el-card>
      <div slot="header" class="clearfix">
        <span>资产领用查询</span>
      </div>
      
      <!-- 查询条件 -->
      <div class="toolbar" style="margin-bottom: 10px;">
        <el-form :inline="true" :model="searchForm" size="small">
          <el-form-item label="领用单号:">
            <el-input v-model="searchForm.receiveNo" placeholder="请输入领用单号" clearable style="width: 200px" @keyup.enter.native="loadData" />
          </el-form-item>
          <el-form-item label="领用科室:">
            <el-select v-model="searchForm.deptId"  clearable filterable style="width: 200px">
              <el-option
                v-for="dept in deptOptions"
                :key="dept.deptId"
                :label="dept.deptName"
                :value="dept.deptId">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="领用人:">
            <el-input v-model="searchForm.applyEmpName" placeholder="请输入领用人姓名" clearable style="width: 180px" @keyup.enter.native="loadData" />
          </el-form-item>
          <el-form-item label="操作人:">
            <el-input v-model="searchForm.operatorName" placeholder="请输入操作人姓名" clearable style="width: 180px" @keyup.enter.native="loadData" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="loadData">查询</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="receiveNo" label="领用单号" width="180">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.receiveNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="领用科室" width="150"></el-table-column>
        <el-table-column prop="applyEmpName" label="领用人" width="120"></el-table-column>
        <el-table-column prop="receiveDate" label="领用日期" width="120">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.receiveDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="receiveReason" label="领用原因" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页和导出 -->
      <div class="pagination" style="display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
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
    
    <!-- 领用详情对话框 -->
    <ReceiveDetailDialog
      :visible.sync="detailDialogVisible"
      :receive-id="currentReceiveId"
    />
  </div>
</template>

<script>
import { getAssetReceivePage } from '@/api/asset'
import { getDeptList } from '@/api/user'
import { exportExcel } from '@/api/common'
import ReceiveDetailDialog from './ReceiveDetailDialog.vue'

export default {
  name: 'AssetReceiveQuery',
  components: {
    ReceiveDetailDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        receiveNo: '',
        deptId: null,
        applyEmpName: '',
        operatorName: ''
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      deptOptions: [],
      detailDialogVisible: false,
      currentReceiveId: null
    }
  },
  mounted() {
    this.loadDepts()
    this.setDefaultDept()
    this.loadData()
  },
  methods: {
    // 加载部门列表
    async loadDepts() {
      try {
        const res = await getDeptList()
        if (res.code === 200 && res.data) {
          this.deptOptions = res.data || []
          // 部门列表加载完成后，设置默认部门
          this.setDefaultDept()
        }
      } catch (error) {
        console.error('加载部门列表失败:', error)
      }
    },
    // 设置默认领用科室（当前登录人的科室）
    setDefaultDept() {
      const userInfo = this.$store.state.user.userInfo || {}
      const deptId = userInfo.deptId || userInfo.dept_id || null
      if (deptId && !this.searchForm.deptId) {
        this.searchForm.deptId = deptId
      }
    },
    // 加载数据
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        receiveNo: this.searchForm.receiveNo || null,
        deptId: this.searchForm.deptId || null,
        applyEmpId: null, // 根据领用人姓名搜索，后端需要支持按姓名搜索
        operatorCode: null // 根据操作人姓名搜索，后端需要支持按姓名搜索
      }
      
      // TODO: 后端需要支持按姓名搜索，这里暂时先按单号搜索
      getAssetReceivePage(params).then(res => {
        if (res.code === 200 && res.data) {
          let list = res.data.records || res.data.list || []
          // 前端过滤：按领用人姓名和操作人姓名
          if (this.searchForm.applyEmpName) {
            list = list.filter(item => 
              item.applyEmpName && item.applyEmpName.includes(this.searchForm.applyEmpName)
            )
          }
          if (this.searchForm.operatorName) {
            list = list.filter(item => 
              item.operatorName && item.operatorName.includes(this.searchForm.operatorName)
            )
          }
          this.tableData = list
          // 如果进行了前端过滤，使用过滤后的长度；否则使用后端返回的total
          if (this.searchForm.applyEmpName || this.searchForm.operatorName) {
            this.pagination.total = list.length
          } else {
            this.pagination.total = res.data.total || 0
          }
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
      }).catch(err => {
        console.error('查询失败:', err)
        this.$message.error('查询失败')
        this.tableData = []
        this.pagination.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    // 重置查询条件
    handleReset() {
      // 重置时，领用科室保留为当前登录人的科室
      const userInfo = this.$store.state.user.userInfo || {}
      const defaultDeptId = userInfo.deptId || userInfo.dept_id || null
      
      this.searchForm = {
        receiveNo: '',
        deptId: defaultDeptId,
        applyEmpName: '',
        operatorName: ''
      }
      this.loadData()
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    // 查看详情
    handleViewDetail(row) {
      this.currentReceiveId = row.id
      this.detailDialogVisible = true
    },
    // 格式化日期（只显示日期部分）
    formatDateOnly(date) {
      if (!date) return ''
      if (typeof date === 'string') {
        return date.substring(0, 10)
      }
      return date
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      if (typeof dateTime === 'string') {
        return dateTime.substring(0, 19).replace('T', ' ')
      }
      return dateTime
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportReceive(false)
      } else if (command === 'all') {
        this.handleExportReceive(true)
      }
    },
    // 导出资产领用数据
    async handleExportReceive(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000,
            receiveNo: this.searchForm.receiveNo || null,
            deptId: this.searchForm.deptId || null,
            applyEmpId: null,
            operatorCode: null
          }
          
          const res = await getAssetReceivePage(params)
          if (res.code === 200 && res.data) {
            let list = res.data.records || res.data.list || []
            // 前端过滤：按领用人姓名和操作人姓名
            if (this.searchForm.applyEmpName) {
              list = list.filter(item => 
                item.applyEmpName && item.applyEmpName.includes(this.searchForm.applyEmpName)
              )
            }
            if (this.searchForm.operatorName) {
              list = list.filter(item => 
                item.operatorName && item.operatorName.includes(this.searchForm.operatorName)
              )
            }
            dataToExport = list
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
        const headers = ['领用单号', '领用科室', '领用人', '领用日期', '领用原因', '操作人', '创建时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.receiveNo || ''),
            String(item.deptName || ''),
            String(item.applyEmpName || ''),
            String(this.formatDateOnly(item.receiveDate) || ''),
            String(item.receiveReason || ''),
            String(item.operatorName || ''),
            String(this.formatDateTime(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '资产领用查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '资产领用查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.asset-receive-query {
  padding: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
