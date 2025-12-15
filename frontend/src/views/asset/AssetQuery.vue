<template>
  <div class="asset-query">
    <el-card>
      <div slot="header">
        <span>资产查询（业务审批）</span>
      </div>
      
      <el-form :inline="true" :model="queryForm" class="demo-form-inline" style="margin-bottom: 20px;">
        <el-form-item label="审批类型">
          <el-select v-model="queryForm.approvalType" placeholder="请选择审批类型" clearable style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="资产调拨" value="TRANSFER"></el-option>
            <el-option label="资产处置" value="DISPOSAL"></el-option>
            <el-option label="盘点差异" value="INVENTORY_DIFF"></el-option>
            <el-option label="资产变动" value="CHANGE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="草稿" value="DRAFT"></el-option>
            <el-option label="待审批" value="PENDING"></el-option>
            <el-option label="已审批" value="APPROVED"></el-option>
            <el-option label="已拒绝" value="REJECTED"></el-option>
            <el-option label="已撤回" value="WITHDRAWN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="approvalNo" label="审批单号" width="150"></el-table-column>
        <el-table-column prop="approvalTitle" label="审批标题" width="200"></el-table-column>
        <el-table-column prop="approvalType" label="审批类型" width="120">
          <template slot-scope="scope">
            {{ getApprovalTypeName(scope.row.approvalType) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="120"></el-table-column>
        <el-table-column prop="applyReason" label="申请事由" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog title="审批单详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="审批单号">{{ currentDetail.approvalNo }}</el-descriptions-item>
              <el-descriptions-item label="审批类型">{{ getApprovalTypeName(currentDetail.approvalType) }}</el-descriptions-item>
              <el-descriptions-item label="审批标题">{{ currentDetail.approvalTitle }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ currentDetail.deptName }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="审批记录" name="approval">
            <el-timeline>
              <el-timeline-item
                v-for="record in approvalRecords"
                :key="record.recordId"
                :timestamp="formatDate(record.approvalTime)"
                placement="top"
              >
                <el-card>
                  <h4>{{ record.approverName }} 
                    <el-tag :type="record.approvalResult === 'APPROVED' ? 'success' : 'danger'" size="small" style="margin-left: 10px;">
                      {{ record.approvalResult === 'APPROVED' ? '通过' : '驳回' }}
                    </el-tag>
                  </h4>
                  <p v-if="record.approvalOpinion" style="margin-top: 10px;">{{ record.approvalOpinion }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="approvalRecords.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无审批记录</div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAssetApprovalMyListPage,
  getAssetApprovalDetail,
  getAssetApprovalRecords
} from '@/api/asset'

export default {
  name: 'AssetQuery',
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      queryForm: {
        approvalType: '',
        status: ''
      },
      detailVisible: false,
      detailActiveTab: 'basic',
      currentDetail: null,
      approvalRecords: [],
      statusMap: {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝',
        'WITHDRAWN': '已撤回'
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || null
      const approvalType = this.queryForm.approvalType || null
      getAssetApprovalMyListPage(approvalType, empId, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let records = response.data.records || []
          // 根据状态过滤（前端过滤，如果数据量大可以移到后端）
          if (this.queryForm.status) {
            records = records.filter(item => item.status === this.queryForm.status)
          }
          this.tableData = records
          this.pagination.total = this.queryForm.status ? records.length : (response.data.total || 0)
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
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleQuery() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm = {
        approvalType: '',
        status: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    async handleView(row) {
      try {
        const [detailRes, recordsRes] = await Promise.all([
          getAssetApprovalDetail(row.approvalId),
          getAssetApprovalRecords(row.approvalId)
        ])
        
        if (detailRes.code === 200 && detailRes.data && detailRes.data.approval) {
          this.currentDetail = detailRes.data.approval
        } else {
          this.currentDetail = row
        }
        
        if (recordsRes.code === 200) {
          this.approvalRecords = recordsRes.data || []
        } else if (detailRes.code === 200 && detailRes.data && detailRes.data.records) {
          this.approvalRecords = detailRes.data.records || []
        } else {
          this.approvalRecords = []
        }
        
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    getApprovalTypeName(type) {
      const typeMap = {
        'TRANSFER': '资产调拨',
        'DISPOSAL': '资产处置',
        'INVENTORY_DIFF': '盘点差异',
        'CHANGE': '资产变动'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      return this.statusMap[status] || status
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
    }
  }
}
</script>

<style scoped>
.asset-query {
  padding: 20px;
}
</style>
