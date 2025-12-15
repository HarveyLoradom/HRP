<template>
  <div class="asset-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>{{ approvalTypeName }}</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleAdd">新增审批单</el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="我的发起" name="my"></el-tab-pane>
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="approvalNo" label="审批单号" width="150"></el-table-column>
        <el-table-column prop="approvalTitle" label="审批标题" width="200"></el-table-column>
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
        <el-table-column label="操作" width="380">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'DRAFT' || scope.row.status === 'WITHDRAWN'" 
                       size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'DRAFT' || scope.row.status === 'WITHDRAWN'" 
                       size="mini" type="success" @click="handleSubmit(scope.row)">提交</el-button>
            <el-button v-if="scope.row.status === 'PENDING' && isMyApproval(scope.row)" 
                       size="mini" type="warning" @click="handleWithdraw(scope.row)">撤回</el-button>
            <el-button v-if="scope.row.status === 'PENDING' && !isMyApproval(scope.row)" 
                       size="mini" type="success" @click="handleApprove(scope.row)">审批</el-button>
            <el-button v-if="scope.row.status === 'PENDING' && !isMyApproval(scope.row)" 
                       size="mini" type="danger" @click="handleReject(scope.row)">驳回</el-button>
            <el-button v-if="scope.row.status === 'PENDING' && !isMyApproval(scope.row)" 
                       size="mini" type="warning" @click="handleAddSign(scope.row)">加签</el-button>
            <el-button size="mini" type="info" icon="el-icon-printer" @click="handlePrint(scope.row)">打印</el-button>
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
        <el-form-item label="审批标题" prop="approvalTitle">
          <el-input v-model="form.approvalTitle" placeholder="请输入审批标题"></el-input>
        </el-form-item>
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

    <!-- 审批对话框 -->
    <el-dialog :title="approvalTitle" :visible.sync="approvalVisible" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approvalForm.opinion" :rows="4" placeholder="请输入审批意见（可选）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApproval">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="审批单详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="审批单号">{{ currentDetail.approvalNo }}</el-descriptions-item>
              <el-descriptions-item label="审批类型">{{ approvalTypeName }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ currentDetail.deptName }}</el-descriptions-item>
              <el-descriptions-item label="审批标题">{{ currentDetail.approvalTitle }}</el-descriptions-item>
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

    <!-- 加签对话框 -->
    <el-dialog title="加签" :visible.sync="addSignVisible" width="500px">
      <el-form :model="addSignForm" :rules="addSignRules" ref="addSignForm" label-width="100px">
        <el-form-item label="被加签人" prop="targetEmpCode">
          <el-autocomplete
            v-model="addSignForm.targetEmpCode"
            :fetch-suggestions="searchEmployee"
            placeholder="请输入工号或姓名搜索"
            value-key="empCode"
            @select="handleAddSignEmployeeSelect"
            style="width: 100%"
          >
            <template slot-scope="{ item }">
              <div>{{ item.empCode }} - {{ item.empName }}</div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="加签原因" prop="addsignReason">
          <el-input type="textarea" v-model="addSignForm.addsignReason" :rows="4" placeholder="请输入加签原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addSignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAddSign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAssetApprovalMyListPage, 
  saveAssetApproval, 
  updateAssetApproval,
  deleteAssetApproval,
  submitAssetApproval,
  withdrawAssetApproval,
  approveAssetApproval,
  rejectAssetApproval,
  getAssetApprovalDetail,
  getAssetApprovalRecords
} from '@/api/asset'
import { createAddSign, getProcessTaskByBusinessKey } from '@/api/process'
import { getEmployeeList } from '@/api/user'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'

export default {
  name: 'AssetApproval',
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
      approvalType: '',
      approvalTypeName: '',
      dialogVisible: false,
      dialogTitle: '新增审批单',
      isEdit: false,
      approvalVisible: false,
      approvalTitle: '',
      detailVisible: false,
      currentDetail: null,
      approvalRecords: [],
      form: {
        approvalId: null,
        approvalType: '',
        approvalTitle: '',
        applyReason: '',
        applicantId: null,
        status: 'DRAFT'
      },
      approvalForm: {
        approvalId: null,
        opinion: ''
      },
      addSignVisible: false,
      addSignForm: {
        parentTaskId: null,
        targetUserId: '',
        targetUserName: '',
        targetEmpCode: '',
        addsignReason: ''
      },
      addSignRules: {
        targetEmpCode: [{ required: true, message: '请选择被加签人', trigger: 'change' }],
        addsignReason: [{ required: true, message: '请输入加签原因', trigger: 'blur' }]
      },
      employeeList: [],
      rules: {
        approvalTitle: [{ required: true, message: '请输入审批标题', trigger: 'blur' }],
        applyReason: [{ required: true, message: '请输入申请事由', trigger: 'blur' }]
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
  mounted() {
    this.initApprovalType()
    this.loadData()
    this.loadEmployeeList()
  },
  watch: {
    '$route'(to, from) {
      if (to.path !== from.path) {
        this.initApprovalType()
        this.loadData()
      }
    }
  },
  methods: {
    initApprovalType() {
      const path = this.$route.path
      if (path.includes('/transfer')) {
        this.approvalType = 'TRANSFER'
        this.approvalTypeName = '资产调拨审批'
      } else if (path.includes('/disposal')) {
        this.approvalType = 'DISPOSAL'
        this.approvalTypeName = '资产处置审批'
      } else if (path.includes('/inventory')) {
        this.approvalType = 'INVENTORY_DIFF'
        this.approvalTypeName = '盘点差异审批'
      } else if (path.includes('/change')) {
        this.approvalType = 'CHANGE'
        this.approvalTypeName = '资产变动审批'
      }
    },
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || null
      getAssetApprovalMyListPage(this.approvalType, empId, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let records = response.data.records || []
          // 根据当前tab过滤数据（前端过滤，如果数据量大可以移到后端）
          if (this.activeTab === 'my') {
            // 我的发起：只显示我发起的
            const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
            const applicantId = this.$store.state.user.userInfo.empId
            records = records.filter(item => {
              return (item.applicantId === applicantId) || 
                     (item.createUser === userId)
            })
          } else if (this.activeTab === 'pending') {
            records = records.filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'approved') {
            records = records.filter(item => item.status === 'APPROVED')
          } else if (this.activeTab === 'rejected') {
            records = records.filter(item => item.status === 'REJECTED')
          }
          this.tableData = records
          this.pagination.total = records.length > 0 ? records.length : (response.data.total || 0)
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
    handleTabClick() {
      this.pagination.page = 1
      this.loadData()
    },
    isMyApproval(row) {
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      const applicantId = this.$store.state.user.userInfo.empId
      return (row.applicantId === applicantId) || (row.createUser === userId)
    },
    handleAdd() {
      this.dialogTitle = '新增审批单'
      this.isEdit = false
      this.form = {
        approvalId: null,
        approvalType: this.approvalType,
        approvalTitle: '',
        applyReason: '',
        applicantId: this.$store.state.user.userInfo.empId || null,
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的审批单才能编辑')
        return
      }
      this.dialogTitle = '编辑审批单'
      this.isEdit = true
      this.form = {
        approvalId: row.approvalId,
        approvalType: row.approvalType,
        approvalTitle: row.approvalTitle,
        applyReason: row.applyReason,
        applicantId: row.applicantId,
        status: row.status
      }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const api = this.isEdit ? updateAssetApproval : saveAssetApproval
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
          saveAssetApproval(this.form).then(response => {
            if (response.code === 200) {
              const approvalId = response.data || this.form.approvalId
              submitAssetApproval(approvalId).then(submitRes => {
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
      this.$confirm('确认提交审批单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitAssetApproval(row.approvalId).then(response => {
          if (response.code === 200) {
            this.$message.success('提交成功')
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      this.$confirm('确认撤回审批单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawAssetApproval(row.approvalId).then(response => {
          if (response.code === 200) {
            this.$message.success('撤回成功')
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '撤回失败')
          }
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除审批单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAssetApproval(row.approvalId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleApprove(row) {
      this.approvalTitle = '审批通过'
      this.approvalForm = {
        approvalId: row.approvalId,
        opinion: ''
      }
      this.approvalVisible = true
    },
    handleReject(row) {
      this.approvalTitle = '驳回审批'
      this.approvalForm = {
        approvalId: row.approvalId,
        opinion: ''
      }
      this.approvalVisible = true
    },
    handleConfirmApproval() {
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      const isApprove = this.approvalTitle === '审批通过'
      const api = isApprove ? approveAssetApproval : rejectAssetApproval
      const data = {
        approvalId: this.approvalForm.approvalId,
        userId: userId,
        opinion: this.approvalForm.opinion
      }
      api(data).then(response => {
        if (response.code === 200) {
          this.$message.success(isApprove ? '审批通过' : '已驳回')
          this.approvalVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      })
    },
    loadEmployeeList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
    },
    searchEmployee(queryString, cb) {
      const results = this.employeeList.filter(emp => {
        return (emp.empCode && emp.empCode.includes(queryString)) ||
               (emp.empName && emp.empName.includes(queryString))
      }).map(emp => ({
        empCode: emp.empCode,
        empName: emp.empName,
        empId: emp.empId
      }))
      cb(results)
    },
    handleAddSignEmployeeSelect(item) {
      this.addSignForm.targetUserId = item.empId
      this.addSignForm.targetUserName = item.empName
      this.addSignForm.targetEmpCode = item.empCode
    },
    async handleAddSign(row) {
      // 根据业务主键获取当前任务
      try {
        const taskResponse = await getProcessTaskByBusinessKey(row.approvalNo)
        if (taskResponse.code === 200 && taskResponse.data && taskResponse.data.length > 0) {
          // 找到当前用户的任务
          const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
          const currentTask = taskResponse.data.find(t => t.assigneeUserId === userId && t.taskStatus === 'PENDING')
          if (currentTask) {
            this.addSignForm.parentTaskId = currentTask.taskId
            this.addSignForm.targetUserId = ''
            this.addSignForm.targetUserName = ''
            this.addSignForm.targetEmpCode = ''
            this.addSignForm.addsignReason = ''
            this.addSignVisible = true
          } else {
            this.$message.warning('未找到当前用户的待办任务')
          }
        } else {
          this.$message.warning('未找到相关流程任务')
        }
      } catch (error) {
        this.$message.error('获取任务信息失败')
      }
    },
    handleConfirmAddSign() {
      this.$refs.addSignForm.validate(valid => {
        if (valid) {
          const userInfo = this.$store.state.user.userInfo
          const addsignData = {
            parentTaskId: this.addSignForm.parentTaskId,
            targetUserId: this.addSignForm.targetUserId,
            targetUserName: this.addSignForm.targetUserName,
            targetEmpCode: this.addSignForm.targetEmpCode,
            addsignUserId: userInfo.userId || userInfo.id,
            addsignUserName: userInfo.name || userInfo.userName,
            addsignEmpCode: userInfo.empCode || userInfo.account,
            addsignReason: this.addSignForm.addsignReason
          }
          createAddSign(addsignData).then(response => {
            if (response.code === 200) {
              this.$message.success('加签成功')
              this.addSignVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '加签失败')
            }
          }).catch(() => {
            this.$message.error('加签失败')
          })
        }
      })
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
.asset-approval {
  padding: 20px;
}
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
</style>
