<template>
  <div class="investment-return-analysis">
    <el-card>
      <div slot="header" class="clearfix">
        <span>投资收益分析</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleAdd">新增分析</el-button>
      </div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="设备编码">
          <el-input v-model="queryForm.equipmentCode" placeholder="请输入设备编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="analysisNo" label="分析编号" width="150"></el-table-column>
        <el-table-column prop="analysisName" label="分析名称" width="200"></el-table-column>
        <el-table-column prop="equipmentCode" label="设备编码" width="120"></el-table-column>
        <el-table-column prop="equipmentName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="investmentAmount" label="投资金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.investmentAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="returnAmount" label="回报金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.returnAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="returnRate" label="回报率" width="100">
          <template slot-scope="scope">
            {{ scope.row.returnRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="analysisPeriod" label="分析期间" width="150"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleView(scope.row)">查看</el-button>
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
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="设备编码" prop="equipmentCode">
          <el-select v-model="form.equipmentCode" placeholder="请选择设备" filterable style="width: 100%" @change="handleEquipmentChange">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.equipmentCode"
              :label="`${equipment.equipmentName} (${equipment.equipmentCode})`"
              :value="equipment.equipmentCode"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分析名称" prop="analysisName">
          <el-input v-model="form.analysisName" placeholder="请输入分析名称"></el-input>
        </el-form-item>
        <el-form-item label="投资金额" prop="investmentAmount">
          <el-input-number v-model="form.investmentAmount" :min="0" :precision="2" style="width: 100%" @change="handleAmountChange"></el-input-number>
        </el-form-item>
        <el-form-item label="回报金额" prop="returnAmount">
          <el-input-number v-model="form.returnAmount" :min="0" :precision="2" style="width: 100%" @change="handleAmountChange"></el-input-number>
        </el-form-item>
        <el-form-item label="回报率" prop="returnRate">
          <el-input v-model="form.returnRate" disabled>
            <template slot="append">%</template>
          </el-input>
        </el-form-item>
        <el-form-item label="分析期间">
          <el-input v-model="form.analysisPeriod" placeholder="例如：2024年度"></el-input>
        </el-form-item>
        <el-form-item label="分析说明">
          <el-input type="textarea" v-model="form.analysisDesc" :rows="4" placeholder="请输入分析说明"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="投资收益分析详情" :visible.sync="detailVisible" width="800px">
      <div v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分析编号">{{ currentDetail.analysisNo }}</el-descriptions-item>
          <el-descriptions-item label="分析名称">{{ currentDetail.analysisName }}</el-descriptions-item>
          <el-descriptions-item label="设备编码">{{ currentDetail.equipmentCode }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentDetail.equipmentName }}</el-descriptions-item>
          <el-descriptions-item label="投资金额">¥{{ currentDetail.investmentAmount }}</el-descriptions-item>
          <el-descriptions-item label="回报金额">¥{{ currentDetail.returnAmount }}</el-descriptions-item>
          <el-descriptions-item label="回报率">{{ currentDetail.returnRate }}%</el-descriptions-item>
          <el-descriptions-item label="分析期间">{{ currentDetail.analysisPeriod }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="分析说明" :span="2">{{ currentDetail.analysisDesc || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getInvestmentAnalyses, saveInvestmentAnalysis, getInvestmentAnalysisDetail, calculateReturnRate, getAllEquipments } from '@/api/efficiency'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'InvestmentReturnAnalysis',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      equipmentList: [],
      dialogVisible: false,
      dialogTitle: '新增分析',
      isEdit: false,
      detailVisible: false,
      currentDetail: null,
      queryForm: {
        equipmentCode: ''
      },
      form: {
        analysisId: null,
        equipmentCode: '',
        equipmentName: '',
        analysisName: '',
        investmentAmount: 0,
        returnAmount: 0,
        returnRate: 0,
        analysisPeriod: '',
        analysisDesc: ''
      },
      rules: {
        equipmentCode: [{ required: true, message: '请选择设备', trigger: 'change' }],
        analysisName: [{ required: true, message: '请输入分析名称', trigger: 'blur' }],
        investmentAmount: [{ required: true, message: '请输入投资金额', trigger: 'blur' }],
        returnAmount: [{ required: true, message: '请输入回报金额', trigger: 'blur' }]
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
    this.loadEquipmentList()
  },
  methods: {
    loadData() {
      this.loading = true
      getInvestmentAnalyses().then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.handleSearch()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.queryForm.equipmentCode) {
        filtered = filtered.filter(item => item.equipmentCode && item.equipmentCode.includes(this.queryForm.equipmentCode))
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    loadEquipmentList() {
      getAllEquipments().then(response => {
        if (response.code === 200) {
          this.equipmentList = response.data || []
        }
      })
    },
    handleQuery() {
      this.pagination.page = 1
      this.handleSearch()
    },
    handleReset() {
      this.queryForm = {
        equipmentCode: ''
      }
      this.pagination.page = 1
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleAdd() {
      this.dialogTitle = '新增分析'
      this.isEdit = false
      this.form = {
        analysisId: null,
        equipmentCode: '',
        equipmentName: '',
        analysisName: '',
        investmentAmount: 0,
        returnAmount: 0,
        returnRate: 0,
        analysisPeriod: new Date().getFullYear() + '年度',
        analysisDesc: ''
      }
      this.dialogVisible = true
    },
    handleEquipmentChange(val) {
      const equipment = this.equipmentList.find(item => item.equipmentCode === val)
      if (equipment) {
        this.form.equipmentName = equipment.equipmentName
      }
    },
    handleAmountChange() {
      if (this.form.investmentAmount > 0 && this.form.returnAmount > 0) {
        calculateReturnRate(this.form.investmentAmount, this.form.returnAmount).then(response => {
          if (response.code === 200 && response.data) {
            this.form.returnRate = response.data.returnRate
          } else {
            // 手动计算
            this.form.returnRate = ((this.form.returnAmount / this.form.investmentAmount) * 100).toFixed(2)
          }
        }).catch(() => {
          // 手动计算
          this.form.returnRate = ((this.form.returnAmount / this.form.investmentAmount) * 100).toFixed(2)
        })
      } else {
        this.form.returnRate = 0
      }
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          saveInvestmentAnalysis(this.form).then(response => {
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
    async handleView(row) {
      try {
        const response = await getInvestmentAnalysisDetail(row.analysisId)
        if (response.code === 200 && response.data) {
          this.currentDetail = response.data
        } else {
          this.currentDetail = row
        }
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.investment-return-analysis { 
  padding: 20px; 
}
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
.search-form {
  margin-bottom: 20px;
}
</style>
