<template>
  <div class="transfer-management">
    <el-card>
      <div slot="header">
        <span>入转调离管理</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我的申请" name="my"></el-tab-pane>
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
      </el-tabs>

      <el-table :data="paginatedData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="transferNo" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="transferType" label="类型" width="120">
          <template slot-scope="scope">
            {{ getTransferTypeName(scope.row.transferType) }}
          </template>
        </el-table-column>
        <el-table-column prop="empCode" label="工号" width="120"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="fromDeptName" label="原部门" width="150"></el-table-column>
        <el-table-column prop="toDeptName" label="目标部门" width="150"></el-table-column>
        <el-table-column prop="transferReason" label="申请事由" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.effectiveDate) }}
          </template>
        </el-table-column>
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

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog title="申请详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请单号">{{ currentDetail.transferNo }}</el-descriptions-item>
              <el-descriptions-item label="类型">{{ getTransferTypeName(currentDetail.transferType) }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ currentDetail.empCode }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ currentDetail.empName }}</el-descriptions-item>
              <el-descriptions-item label="原部门">{{ currentDetail.fromDeptName }}</el-descriptions-item>
              <el-descriptions-item label="目标部门">{{ currentDetail.toDeptName }}</el-descriptions-item>
              <el-descriptions-item label="生效日期">{{ formatDate(currentDetail.effectiveDate) }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请事由" :span="2">{{ currentDetail.transferReason }}</el-descriptions-item>
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
import { getMyTransferList, getBusinessApplyDetail, getBusinessApplyRecords } from '@/api/hr'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'TransferManagement',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      activeTab: 'my',
      detailActiveTab: 'basic',
      tableData: [],
      allData: [],
      detailVisible: false,
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
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || null
      getMyTransferList(empId).then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.filterAndPaginateData()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    filterAndPaginateData() {
      let filtered = [...this.allData]
      if (this.activeTab === 'my') {
        const applicantId = this.$store.state.user.userInfo.empId
        filtered = filtered.filter(item => item.applicantId === applicantId || item.empId === applicantId)
      } else if (this.activeTab === 'pending') {
        filtered = filtered.filter(item => item.status === 'PENDING')
      } else if (this.activeTab === 'approved') {
        filtered = filtered.filter(item => item.status === 'APPROVED')
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleTabClick() {
      this.pagination.page = 1
      this.filterAndPaginateData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    async handleView(row) {
      try {
        const [detailRes, recordsRes] = await Promise.all([
          getBusinessApplyDetail(row.transferId || row.applyId),
          getBusinessApplyRecords(row.transferId || row.applyId)
        ])
        
        if (detailRes.code === 200 && detailRes.data) {
          this.currentDetail = detailRes.data
        } else {
          this.currentDetail = row
        }
        
        if (recordsRes.code === 200) {
          this.approvalRecords = recordsRes.data || []
        } else {
          this.approvalRecords = []
        }
        
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    getTransferTypeName(type) {
      const typeMap = {
        'ENTRY': '入职',
        'TRANSFER': '转岗',
        'ADJUST': '调岗',
        'EXIT': '离职'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN').split(' ')[0]
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
.transfer-management {
  padding: 20px;
}
</style>



