<template>
  <div class="leave-manage">
    <el-card>
      <div slot="header">
        <span>请假管理</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我的请假" name="my"></el-tab-pane>
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
      </el-tabs>

      <el-table :data="paginatedData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="leaveNo" label="请假单号" width="150"></el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="120">
          <template slot-scope="scope">
            {{ getLeaveTypeName(scope.row.leaveType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="天数" width="80">
          <template slot-scope="scope">
            {{ scope.row.duration }}天
          </template>
        </el-table-column>
        <el-table-column prop="leaveReason" label="请假事由" width="200" show-overflow-tooltip></el-table-column>
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
    <el-dialog title="请假详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="请假单号">{{ currentDetail.leaveNo }}</el-descriptions-item>
              <el-descriptions-item label="请假类型">{{ getLeaveTypeName(currentDetail.leaveType) }}</el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ formatDate(currentDetail.startDate) }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ formatDate(currentDetail.endDate) }}</el-descriptions-item>
              <el-descriptions-item label="请假天数">{{ currentDetail.duration }}天</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="请假事由" :span="2">{{ currentDetail.leaveReason }}</el-descriptions-item>
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
import { getMyLeaveList, getBusinessApplyDetail, getBusinessApplyRecords } from '@/api/hr'

export default {
  name: 'LeaveManage',
  data() {
    return {
      loading: false,
      activeTab: 'my',
      detailActiveTab: 'basic',
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
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
      getMyLeaveList(empId).then(response => {
        if (response.code === 200) {
          const allData = response.data || []
          if (this.activeTab === 'my') {
            const applicantId = this.$store.state.user.userInfo.empId
            this.tableData = allData.filter(item => item.applicantId === applicantId)
          } else if (this.activeTab === 'pending') {
            this.tableData = allData.filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'approved') {
            this.tableData = allData.filter(item => item.status === 'APPROVED')
          }
          this.pagination.total = this.tableData.length
          this.pagination.page = 1
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleTabClick() {
      this.pagination.page = 1
      this.loadData()
    },
    async handleView(row) {
      try {
        const [detailRes, recordsRes] = await Promise.all([
          getBusinessApplyDetail(row.applyId || row.leaveId),
          getBusinessApplyRecords(row.applyId || row.leaveId)
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
    getLeaveTypeName(type) {
      const typeMap = {
        'ANNUAL': '年假',
        'SICK': '病假',
        'PERSONAL': '事假',
        'MATERNITY': '产假',
        'PATERNITY': '陪产假',
        'MARRIAGE': '婚假',
        'BEREAVEMENT': '丧假'
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
.leave-manage {
  padding: 20px;
}
</style>



