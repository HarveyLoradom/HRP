<template>
  <div class="cost-accounting">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成本核算</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleCreate">新增核算</el-button>
      </div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="核算期间">
          <el-date-picker
            v-model="queryForm.accountingPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="queryForm.deptCode" placeholder="请输入科室编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="accountingNo" label="核算编号" width="150"></el-table-column>
        <el-table-column prop="accountingName" label="核算名称" width="200"></el-table-column>
        <el-table-column prop="accountingPeriod" label="核算期间" width="120"></el-table-column>
        <el-table-column prop="deptCode" label="科室编码" width="120"></el-table-column>
        <el-table-column prop="deptName" label="科室名称" width="150"></el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.costAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="directCost" label="直接成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.directCost }}
          </template>
        </el-table-column>
        <el-table-column prop="indirectCost" label="间接成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.indirectCost }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="success" @click="handleCalculate(scope.row)">核算</el-button>
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

    <!-- 新增核算对话框 -->
    <el-dialog title="新增成本核算" :visible.sync="createVisible" width="700px">
      <el-form :model="accountingForm" :rules="rules" ref="accountingForm" label-width="120px">
        <el-form-item label="核算名称" prop="accountingName">
          <el-input v-model="accountingForm.accountingName" placeholder="请输入核算名称"></el-input>
        </el-form-item>
        <el-form-item label="核算期间" prop="accountingPeriod">
          <el-date-picker
            v-model="accountingForm.accountingPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="核算维度">
          <el-radio-group v-model="accountingForm.accountingDimension">
            <el-radio label="DEPT">科室维度</el-radio>
            <el-radio label="PROJECT">项目维度</el-radio>
            <el-radio label="EQUIPMENT">设备维度</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="accountingForm.accountingDimension === 'DEPT'" label="选择科室">
          <el-input v-model="accountingForm.deptCode" placeholder="请输入科室编码"></el-input>
        </el-form-item>
        <el-form-item label="核算说明">
          <el-input type="textarea" v-model="accountingForm.accountingDesc" :rows="4" placeholder="请输入核算说明"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCreate">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看核算详情对话框 -->
    <el-dialog title="成本核算详情" :visible.sync="detailVisible" width="1000px">
      <div v-if="currentAccounting">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="核算编号">{{ currentAccounting.accountingNo }}</el-descriptions-item>
              <el-descriptions-item label="核算名称">{{ currentAccounting.accountingName }}</el-descriptions-item>
              <el-descriptions-item label="核算期间">{{ currentAccounting.accountingPeriod }}</el-descriptions-item>
              <el-descriptions-item label="核算维度">{{ getDimensionName(currentAccounting.accountingDimension) }}</el-descriptions-item>
              <el-descriptions-item label="科室编码">{{ currentAccounting.deptCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="科室名称">{{ currentAccounting.deptName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="成本金额">¥{{ currentAccounting.costAmount }}</el-descriptions-item>
              <el-descriptions-item label="直接成本">¥{{ currentAccounting.directCost }}</el-descriptions-item>
              <el-descriptions-item label="间接成本">¥{{ currentAccounting.indirectCost }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentAccounting.status)">{{ getStatusText(currentAccounting.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDate(currentAccounting.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="核算说明" :span="2">{{ currentAccounting.accountingDesc || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="成本明细" name="detail">
            <el-table :data="accountingDetails" border>
              <el-table-column prop="costItemCode" label="成本项目编码" width="150"></el-table-column>
              <el-table-column prop="costItemName" label="成本项目名称" width="150"></el-table-column>
              <el-table-column prop="costType" label="成本类型" width="120">
                <template slot-scope="scope">
                  {{ getCostTypeName(scope.row.costType) }}
                </template>
              </el-table-column>
              <el-table-column prop="costAmount" label="成本金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.costAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="allocationMethod" label="分摊方式" width="120">
                <template slot-scope="scope">
                  {{ getAllocationMethodName(scope.row.allocationMethod) }}
                </template>
              </el-table-column>
              <el-table-column prop="allocationBase" label="分摊基数" width="120">
                <template slot-scope="scope">
                  {{ scope.row.allocationBase || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCostAccountings, getCostAccountingsByPeriod, saveCostAccounting, getCostAccountingDetail, calculateCost } from '@/api/cost'

export default {
  name: 'CostAccounting',
  data() {
    return {
      loading: false,
      tableData: [],
      createVisible: false,
      detailVisible: false,
      detailActiveTab: 'basic',
      queryForm: {
        accountingPeriod: '',
        deptCode: ''
      },
      accountingForm: {
        accountingName: '',
        accountingPeriod: '',
        accountingDimension: 'DEPT',
        deptCode: '',
        accountingDesc: ''
      },
      rules: {
        accountingName: [{ required: true, message: '请输入核算名称', trigger: 'blur' }],
        accountingPeriod: [{ required: true, message: '请选择核算期间', trigger: 'change' }]
      },
      currentAccounting: null,
      accountingDetails: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
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
      if (this.queryForm.accountingPeriod) {
        getCostAccountingsByPeriod(this.queryForm.accountingPeriod).then(response => {
          if (response.code === 200) {
            let allData = response.data || []
            if (this.queryForm.deptCode) {
              allData = allData.filter(item => item.deptCode === this.queryForm.deptCode)
            }
            this.tableData = allData
            this.pagination.total = allData.length
            this.pagination.page = 1
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        getAllCostAccountings().then(response => {
          if (response.code === 200) {
            let allData = response.data || []
            if (this.queryForm.deptCode) {
              allData = allData.filter(item => item.deptCode === this.queryForm.deptCode)
            }
            this.tableData = allData
            this.pagination.total = allData.length
            this.pagination.page = 1
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleQuery() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm = {
        accountingPeriod: '',
        deptCode: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleCreate() {
      this.accountingForm = {
        accountingName: '',
        accountingPeriod: new Date().toISOString().slice(0, 7),
        accountingDimension: 'DEPT',
        deptCode: '',
        accountingDesc: ''
      }
      this.createVisible = true
    },
    handleConfirmCreate() {
      this.$refs.accountingForm.validate((valid) => {
        if (valid) {
          saveCostAccounting(this.accountingForm).then(response => {
            if (response.code === 200) {
              this.$message.success('核算创建成功')
              this.createVisible = false
              this.pagination.page = 1
              this.loadData()
            } else {
              this.$message.error(response.message || '创建失败')
            }
          })
        }
      })
    },
    handleCalculate(row) {
      this.$confirm('确认执行成本核算吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        calculateCost({ accountingId: row.accountingId }).then(response => {
          if (response.code === 200) {
            this.$message.success('核算成功')
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '核算失败')
          }
        })
      })
    },
    async handleView(row) {
      try {
        const response = await getCostAccountingDetail(row.accountingId)
        if (response.code === 200 && response.data) {
          this.currentAccounting = response.data
          this.accountingDetails = response.data.details || []
        } else {
          this.currentAccounting = row
          this.accountingDetails = []
        }
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载核算详情失败')
      }
    },
    getDimensionName(dimension) {
      const dimensionMap = {
        'DEPT': '科室维度',
        'PROJECT': '项目维度',
        'EQUIPMENT': '设备维度'
      }
      return dimensionMap[dimension] || dimension
    },
    getCostTypeName(type) {
      const typeMap = {
        'DIRECT': '直接成本',
        'INDIRECT': '间接成本'
      }
      return typeMap[type] || type
    },
    getAllocationMethodName(method) {
      const methodMap = {
        'AVERAGE': '平均分摊',
        'PROPORTION': '按比例',
        'WORKLOAD': '按工作量',
        'AREA': '按面积',
        'MANPOWER': '按人员'
      }
      return methodMap[method] || method
    },
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'CALCULATED': '已核算',
        'CONFIRMED': '已确认'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'CALCULATED': 'success',
        'CONFIRMED': 'success'
      }
      return typeMap[status] || ''
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.cost-accounting {
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
