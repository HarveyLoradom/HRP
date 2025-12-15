<template>
  <div class="procurement-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>需求发起</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleAdd">新增需求</el-button>
      </div>
      
      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
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
        <el-table-column prop="createTime" label="创建日期" width="150">
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
        <el-table-column label="操作" width="300">
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
            <el-form-item label="需求名称" prop="requirementName">
              <el-input v-model="form.requirementName" placeholder="请输入需求名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求类型" prop="requirementType">
              <el-select v-model="form.requirementType" placeholder="请选择需求类型" style="width: 100%">
                <el-option label="设备" value="EQUIPMENT"></el-option>
                <el-option label="耗材" value="CONSUMABLE"></el-option>
                <el-option label="其他" value="OTHER"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估金额" prop="estimatedAmount">
              <el-input-number v-model="form.estimatedAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="需求描述" prop="requirementDesc">
          <el-input type="textarea" v-model="form.requirementDesc" :rows="4" placeholder="请输入需求描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button v-if="!isEdit" type="success" @click="handleSaveAndSubmit">保存并提交</el-button>
      </div>
    </el-dialog>

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
              <el-descriptions-item label="创建日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
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
  getProcurementMyList,
  saveProcurement,
  updateProcurement,
  deleteProcurement,
  submitProcurement,
  withdrawProcurement,
  getProcurementDetail,
  getProcurementApprovals
} from '@/api/asset'

export default {
  name: 'ProcurementApply',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增需求',
      isEdit: false,
      detailVisible: false,
      detailActiveTab: 'basic',
      currentDetail: null,
      approvalRecords: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      form: {
        requirementId: null,
        requirementName: '',
        requirementType: '',
        estimatedAmount: 0,
        requirementDesc: '',
        applicantId: null,
        status: 'DRAFT'
      },
      rules: {
        requirementName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }],
        requirementType: [{ required: true, message: '请选择需求类型', trigger: 'change' }],
        estimatedAmount: [{ required: true, message: '请输入预估金额', trigger: 'blur' }],
        requirementDesc: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
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
      getProcurementMyList(empId).then(response => {
        if (response.code === 200) {
          // 只显示我发起的
          const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
          const applicantId = this.$store.state.user.userInfo.empId
          const allData = response.data || []
          this.tableData = allData.filter(item => {
            return (item.applicantId === applicantId) || 
                   (item.createUser === userId)
          })
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
    handleAdd() {
      this.dialogTitle = '新增需求'
      this.isEdit = false
      this.form = {
        requirementId: null,
        requirementName: '',
        requirementType: '',
        estimatedAmount: 0,
        requirementDesc: '',
        applicantId: this.$store.state.user.userInfo.empId || null,
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的需求单才能编辑')
        return
      }
      this.dialogTitle = '编辑需求'
      this.isEdit = true
      this.form = {
        requirementId: row.requirementId,
        requirementName: row.requirementName,
        requirementType: row.requirementType,
        estimatedAmount: row.estimatedAmount,
        requirementDesc: row.requirementDesc,
        applicantId: row.applicantId,
        status: row.status
      }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const api = this.isEdit ? updateProcurement : saveProcurement
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
          saveProcurement(this.form).then(response => {
            if (response.code === 200) {
              const requirementId = response.data || this.form.requirementId
              submitProcurement(requirementId).then(submitRes => {
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
      this.$confirm('确认提交需求单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitProcurement(row.requirementId).then(response => {
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
      this.$confirm('确认撤回需求单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawProcurement(row.requirementId).then(response => {
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
      this.$confirm('确认删除需求单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteProcurement(row.requirementId).then(response => {
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
.procurement-apply {
  padding: 20px;
}
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
</style>



