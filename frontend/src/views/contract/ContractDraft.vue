<template>
  <div class="contract-draft">
    <el-card>
      <div slot="header" class="clearfix">
        <span>合同起草</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增合同</el-button>
      </div>
      
      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
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
          <template slot-scope="scope">¥{{ scope.row.contractAmount }}</template>
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
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="primary" @click="handleSubmit(scope.row)">提交</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="warning" @click="handleWithdraw(scope.row)">撤回</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="合同类型" prop="contractType">
          <el-select v-model="form.contractType" placeholder="请选择合同类型" style="width: 100%">
            <el-option
              v-for="option in contractTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="合同名称" prop="contractName">
          <el-input v-model="form.contractName" placeholder="请输入合同名称"></el-input>
        </el-form-item>
        <el-form-item label="甲方" prop="partyA">
          <el-input v-model="form.partyA" placeholder="请输入甲方"></el-input>
        </el-form-item>
        <el-form-item label="乙方" prop="partyB">
          <el-input v-model="form.partyB" placeholder="请输入乙方"></el-input>
        </el-form-item>
        <el-form-item label="合同金额" prop="contractAmount">
          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="签订日期" prop="signDate">
          <el-date-picker v-model="form.signDate" type="datetime" placeholder="选择签订日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="datetime" placeholder="选择开始日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="datetime" placeholder="选择结束日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getContractsByStatus, saveContract, updateContract, deleteContract, submitContract, withdrawContract } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ContractDraft',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      contractTypeOptions: [],
      dialogVisible: false,
      dialogTitle: '新增合同',
      isEdit: false,
      form: {
        pactId: null,
        contractNo: '',
        contractName: '',
        contractType: '',
        partyA: '',
        partyB: '',
        contractAmount: 0,
        signDate: null,
        startDate: null,
        endDate: null,
        remark: '',
        status: 'DRAFT'
      },
      rules: {
        contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
        contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
        partyA: [{ required: true, message: '请输入甲方', trigger: 'blur' }],
        partyB: [{ required: true, message: '请输入乙方', trigger: 'blur' }],
        contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }]
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
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
    },
    loadData() {
      this.loading = true
      getContractsByStatus('DRAFT').then(response => {
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
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    handleAdd() {
      this.dialogTitle = '新增合同'
      this.isEdit = false
      this.form = {
        pactId: null,
        contractNo: '',
        contractName: '',
        contractType: '',
        partyA: '',
        partyB: '',
        contractAmount: 0,
        signDate: null,
        startDate: null,
        endDate: null,
        remark: '',
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的合同才能编辑')
        return
      }
      this.dialogTitle = '编辑合同'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      if (row.status !== 'DRAFT') {
        this.$message.warning('只有草稿状态的合同才能删除')
        return
      }
      this.$confirm('确认删除该合同吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteContract(row.pactId).then(response => {
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
    handleSubmit(row) {
      if (row.status !== 'DRAFT') {
        this.$message.warning('只有草稿状态的合同才能提交')
        return
      }
      this.$confirm('确认提交该合同吗？提交后将进入审批流程。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitContract(row.pactId).then(response => {
          if (response.code === 200) {
            const message = response.message || '提交成功'
            this.$message.success(message)
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      if (row.status !== 'PENDING') {
        this.$message.warning('只有待审批状态的合同才能撤回')
        return
      }
      this.$confirm('确认撤回该合同吗？撤回后可以重新编辑。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawContract(row.pactId).then(response => {
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
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateContract : saveContract
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.pagination.page = 1
              this.loadData()
            }
          })
        }
      })
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
.contract-draft {
  padding: 20px;
}
</style>

