<template>
  <div class="procurement-query">
    <el-card>
      <div slot="header">
        <span>采购查询（采购需求）</span>
      </div>
      
      <el-form :inline="true" :model="queryForm" class="demo-form-inline" style="margin-bottom: 20px;">
        <el-form-item label="需求类型">
          <el-select v-model="queryForm.requirementType" placeholder="请选择需求类型" clearable style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="设备" value="EQUIPMENT"></el-option>
            <el-option label="耗材" value="CONSUMABLE"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
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
        <el-table-column prop="requirementNo" label="需求单号" width="150"></el-table-column>
        <el-table-column prop="requirementName" label="需求名称" width="200"></el-table-column>
        <el-table-column prop="requirementType" label="需求类型" width="120">
          <template slot-scope="scope">
            {{ getRequirementTypeName(scope.row.requirementType) }}
          </template>
        </el-table-column>
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.estimatedAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="120"></el-table-column>
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
    <el-dialog title="需求详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="需求单号">{{ currentDetail.requirementNo }}</el-descriptions-item>
              <el-descriptions-item label="需求名称">{{ currentDetail.requirementName }}</el-descriptions-item>
              <el-descriptions-item label="需求类型">{{ getRequirementTypeName(currentDetail.requirementType) }}</el-descriptions-item>
              <el-descriptions-item label="预估金额">¥{{ currentDetail.estimatedAmount }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ currentDetail.deptName }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="需求描述" :span="2">{{ currentDetail.requirementDesc }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="审批记录" name="approval">
            <el-timeline>
              <el-timeline-item
                v-for="approval in approvalRecords"
                :key="approval.approvalId"
                :timestamp="formatDate(approval.approvalTime)"
                placement="top"
              >
                <el-card>
                  <h4>{{ approval.approverName }} 
                    <el-tag :type="approval.approvalResult === 'APPROVED' ? 'success' : 'danger'" size="small" style="margin-left: 10px;">
                      {{ approval.approvalResult === 'APPROVED' ? '通过' : '驳回' }}
                    </el-tag>
                  </h4>
                  <p v-if="approval.approvalOpinion" style="margin-top: 10px;">{{ approval.approvalOpinion }}</p>
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
  getProcurementMyListPage,
  getProcurementDetail,
  getProcurementApprovals
} from '@/api/asset'

export default {
  name: 'ProcurementQuery',
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
        requirementType: '',
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
      getProcurementMyListPage(empId, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let records = response.data.records || []
          // 根据需求类型和状态过滤（前端过滤，如果数据量大可以移到后端）
          if (this.queryForm.requirementType) {
            records = records.filter(item => item.requirementType === this.queryForm.requirementType)
          }
          if (this.queryForm.status) {
            records = records.filter(item => item.status === this.queryForm.status)
          }
          this.tableData = records
          this.pagination.total = (this.queryForm.requirementType || this.queryForm.status) ? records.length : (response.data.total || 0)
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
        requirementType: '',
        status: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    async handleView(row) {
      try {
        const [detailRes, approvalRes] = await Promise.all([
          getProcurementDetail(row.requirementId),
          getProcurementApprovals(row.requirementId)
        ])
        
        if (detailRes.code === 200 && detailRes.data && detailRes.data.requirement) {
          this.currentDetail = detailRes.data.requirement
        } else {
          this.currentDetail = row
        }
        
        if (approvalRes.code === 200) {
          this.approvalRecords = approvalRes.data || []
        } else if (detailRes.code === 200 && detailRes.data && detailRes.data.approvals) {
          this.approvalRecords = detailRes.data.approvals || []
        } else {
          this.approvalRecords = []
        }
        
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    getRequirementTypeName(type) {
      const typeMap = {
        'EQUIPMENT': '设备',
        'CONSUMABLE': '耗材',
        'OTHER': '其他'
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
.procurement-query {
  padding: 20px;
}
</style>



