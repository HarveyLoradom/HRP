<template>
  <div class="contract-execution">
    <el-card>
      <div slot="header">
        <span>合同执行</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="执行中" name="EXECUTING"></el-tab-pane>
        <el-tab-pane label="已归档" name="ARCHIVED"></el-tab-pane>
      </el-tabs>

      <el-table :data="paginatedData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="partyA" label="甲方" width="150"></el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.contractAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'EXECUTING'" size="mini" @click="handleChange(scope.row)">变更</el-button>
            <el-button v-if="scope.row.status === 'EXECUTING'" size="mini" type="primary" @click="handleArchive(scope.row)">归档</el-button>
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

    <!-- 合同变更对话框 -->
    <el-dialog title="合同变更" :visible.sync="changeVisible" width="700px">
      <el-form :model="changeForm" :rules="changeRules" ref="changeForm" label-width="100px">
        <el-form-item label="变更内容" prop="changeContent">
          <el-input type="textarea" v-model="changeForm.changeContent" :rows="4" placeholder="请输入变更内容"></el-input>
        </el-form-item>
        <el-form-item label="变更原因" prop="changeReason">
          <el-input type="textarea" v-model="changeForm.changeReason" :rows="3" placeholder="请输入变更原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="changeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmChange">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="合同详情" :visible.sync="detailVisible" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="合同编号">{{ currentDetail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ currentDetail.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ getContractTypeName(currentDetail.contractType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="甲方">{{ currentDetail.partyA }}</el-descriptions-item>
        <el-descriptions-item label="乙方">{{ currentDetail.partyB }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">¥{{ currentDetail.contractAmount }}</el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ formatDate(currentDetail.signDate) }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ formatDate(currentDetail.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ formatDate(currentDetail.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getContractsByStatus, archiveContract, getContractById, updateContract } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ContractExecution',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      activeTab: 'EXECUTING',
      tableData: [],
      allData: [],
      contractTypeOptions: [],
      changeVisible: false,
      changeForm: {
        contractId: null,
        changeContent: '',
        changeReason: ''
      },
      changeRules: {
        changeContent: [{ required: true, message: '请输入变更内容', trigger: 'blur' }],
        changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
      },
      detailVisible: false,
      currentDetail: null
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
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
    },
    loadData() {
      this.loading = true
      getContractsByStatus(this.activeTab).then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.tableData = this.allData
          this.pagination.total = this.tableData.length
          this.pagination.page = 1
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleTabClick() {
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleChange(row) {
      this.changeForm = {
        contractId: row.pactId,
        changeContent: '',
        changeReason: ''
      }
      this.changeVisible = true
    },
    handleConfirmChange() {
      this.$refs.changeForm.validate(valid => {
        if (valid) {
          // TODO: 实现合同变更逻辑（可以创建变更记录表）
          this.$message.success('合同变更功能待实现（需要创建合同变更记录表）')
          this.changeVisible = false
        }
      })
    },
    handleArchive(row) {
      this.$confirm('确认归档该合同吗？归档后合同将不能再变更。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        archiveContract(row.pactId).then(response => {
          if (response.code === 200) {
            this.$message.success('归档成功')
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '归档失败')
          }
        })
      })
    },
    handleView(row) {
      getContractById(row.pactId).then(response => {
        if (response.code === 200) {
          this.currentDetail = response.data
          this.detailVisible = true
        }
      })
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'EXECUTING': '执行中',
        'ARCHIVED': '已归档'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'EXECUTING': '',
        'ARCHIVED': 'info'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.contract-execution {
  padding: 20px;
}
</style>

