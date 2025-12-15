<template>
  <div class="business-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>业务申请</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleAdd">新增申请</el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我的申请" name="my"></el-tab-pane>
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
      </el-tabs>

      <el-table :data="paginatedData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="applyNo" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="applyType" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getApplyTypeName(scope.row.applyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyTitle" label="申请标题" width="200"></el-table-column>
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
            {{ scope.row.duration || '-' }}
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
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'DRAFT' || scope.row.status === 'WITHDRAWN'" 
                       size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'DRAFT' || scope.row.status === 'WITHDRAWN'" 
                       size="mini" type="success" @click="handleSubmit(scope.row)">提交</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" 
                       size="mini" type="warning" @click="handleWithdraw(scope.row)">撤回</el-button>
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.status === 'DRAFT' || scope.row.status === 'WITHDRAWN'" 
                       size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请类型" prop="applyType">
              <el-select v-model="form.applyType" placeholder="请选择申请类型" style="width: 100%" @change="handleTypeChange">
                <el-option label="请假" value="LEAVE"></el-option>
                <el-option label="入职申请" value="ENTRY"></el-option>
                <el-option label="离职申请" value="EXIT"></el-option>
                <el-option label="转岗申请" value="TRANSFER"></el-option>
                <el-option label="调岗申请" value="ADJUST"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请标题" prop="applyTitle">
              <el-input v-model="form.applyTitle" placeholder="请输入申请标题"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.applyType === 'LEAVE'">
          <el-col :span="12">
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
                <el-option label="年假" value="ANNUAL"></el-option>
                <el-option label="病假" value="SICK"></el-option>
                <el-option label="事假" value="PERSONAL"></el-option>
                <el-option label="产假" value="MATERNITY"></el-option>
                <el-option label="陪产假" value="PATERNITY"></el-option>
                <el-option label="婚假" value="MARRIAGE"></el-option>
                <el-option label="丧假" value="BEREAVEMENT"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.applyType === 'LEAVE'">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" 
                              format="yyyy-MM-dd" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" 
                              format="yyyy-MM-dd" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申请事由" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="4" placeholder="请输入申请事由"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button v-if="!isEdit" type="success" @click="handleSaveAndSubmit">保存并提交</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="申请详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请单号">{{ currentDetail.applyNo }}</el-descriptions-item>
              <el-descriptions-item label="申请类型">{{ getApplyTypeName(currentDetail.applyType) }}</el-descriptions-item>
              <el-descriptions-item label="申请标题">{{ currentDetail.applyTitle }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="开始日期" v-if="currentDetail.startDate">{{ formatDate(currentDetail.startDate) }}</el-descriptions-item>
              <el-descriptions-item label="结束日期" v-if="currentDetail.endDate">{{ formatDate(currentDetail.endDate) }}</el-descriptions-item>
              <el-descriptions-item label="天数" v-if="currentDetail.duration">{{ currentDetail.duration }}天</el-descriptions-item>
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
  getBusinessApplyMyList,
  saveBusinessApply,
  updateBusinessApply,
  deleteBusinessApply,
  submitBusinessApply,
  withdrawBusinessApply,
  getBusinessApplyDetail,
  getBusinessApplyRecords
} from '@/api/hr'

export default {
  name: 'BusinessApply',
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
      dialogVisible: false,
      dialogTitle: '新增申请',
      isEdit: false,
      detailVisible: false,
      currentDetail: null,
      approvalRecords: [],
      form: {
        applyId: null,
        applyType: '',
        applyTitle: '',
        leaveType: '',
        startDate: '',
        endDate: '',
        duration: 0,
        applyReason: '',
        applicantId: null,
        status: 'DRAFT'
      },
      rules: {
        applyType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        applyTitle: [{ required: true, message: '请输入申请标题', trigger: 'blur' }],
        applyReason: [{ required: true, message: '请输入申请事由', trigger: 'blur' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
      },
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
      getBusinessApplyMyList(empId).then(response => {
        if (response.code === 200) {
          const allData = response.data || []
          if (this.activeTab === 'my') {
            const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
            const applicantId = this.$store.state.user.userInfo.empId
            this.tableData = allData.filter(item => {
              return (item.applicantId === applicantId) || (item.createUser === userId)
            })
          } else if (this.activeTab === 'pending') {
            this.tableData = allData.filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'approved') {
            this.tableData = allData.filter(item => item.status === 'APPROVED')
          } else if (this.activeTab === 'rejected') {
            this.tableData = allData.filter(item => item.status === 'REJECTED')
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
    handleTypeChange() {
      if (this.form.applyType !== 'LEAVE') {
        this.form.startDate = ''
        this.form.endDate = ''
        this.form.duration = 0
      }
    },
    handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      this.form = {
        applyId: null,
        applyType: '',
        applyTitle: '',
        leaveType: '',
        startDate: '',
        endDate: '',
        duration: 0,
        applyReason: '',
        applicantId: this.$store.state.user.userInfo.empId || null,
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的申请单才能编辑')
        return
      }
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.form.startDate && this.form.endDate) {
            const start = new Date(this.form.startDate)
            const end = new Date(this.form.endDate)
            this.form.duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
          }
          const api = this.isEdit ? updateBusinessApply : saveBusinessApply
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '保存成功')
              this.dialogVisible = false
              this.pagination.page = 1
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    handleSaveAndSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.form.startDate && this.form.endDate) {
            const start = new Date(this.form.startDate)
            const end = new Date(this.form.endDate)
            this.form.duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
          }
          saveBusinessApply(this.form).then(response => {
            if (response.code === 200) {
              const applyId = response.data || this.form.applyId
              submitBusinessApply(applyId).then(submitRes => {
                if (submitRes.code === 200) {
                  this.$message.success('保存并提交成功')
                  this.dialogVisible = false
                  this.pagination.page = 1
                  this.loadData()
                } else {
                  this.$message.error(submitRes.message || '提交失败')
                }
              })
            } else {
              this.$message.error(response.message || '保存失败')
            }
          })
        }
      })
    },
    handleSubmit(row) {
      this.$confirm('确认提交申请？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitBusinessApply(row.applyId).then(response => {
          if (response.code === 200) {
            this.$message.success('提交成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      this.$confirm('确认撤回申请？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawBusinessApply(row.applyId).then(response => {
          if (response.code === 200) {
            this.$message.success('撤回成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '撤回失败')
          }
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除申请？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBusinessApply(row.applyId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    async handleView(row) {
      try {
        const [detailRes, recordsRes] = await Promise.all([
          getBusinessApplyDetail(row.applyId),
          getBusinessApplyRecords(row.applyId)
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
    getApplyTypeName(type) {
      const typeMap = {
        'LEAVE': '请假',
        'ENTRY': '入职申请',
        'EXIT': '离职申请',
        'TRANSFER': '转岗申请',
        'ADJUST': '调岗申请'
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
.business-apply {
  padding: 20px;
}
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
</style>



