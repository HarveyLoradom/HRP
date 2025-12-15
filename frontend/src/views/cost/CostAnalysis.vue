<template>
  <div class="cost-analysis">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成本分析</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleCreate">新增分析</el-button>
      </div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="分析期间">
          <el-date-picker
            v-model="queryForm.analysisPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="analysisNo" label="分析编号" width="150"></el-table-column>
        <el-table-column prop="analysisName" label="分析名称" width="200"></el-table-column>
        <el-table-column prop="analysisPeriod" label="分析期间" width="120"></el-table-column>
        <el-table-column prop="totalCost" label="总成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalCost }}
          </template>
        </el-table-column>
        <el-table-column prop="costRatio" label="成本占比" width="100">
          <template slot-scope="scope">
            {{ scope.row.costRatio }}%
          </template>
        </el-table-column>
        <el-table-column prop="growthRate" label="增长率" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.growthRate >= 0 ? '#f56c6c' : '#67c23a' }">
              {{ scope.row.growthRate >= 0 ? '+' : '' }}{{ scope.row.growthRate }}%
            </span>
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
            <el-button size="mini" type="success" @click="handleCompare(scope.row)">对比</el-button>
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

    <!-- 新增分析对话框 -->
    <el-dialog title="新增成本分析" :visible.sync="createVisible" width="700px">
      <el-form :model="analysisForm" :rules="rules" ref="analysisForm" label-width="120px">
        <el-form-item label="分析名称" prop="analysisName">
          <el-input v-model="analysisForm.analysisName" placeholder="请输入分析名称"></el-input>
        </el-form-item>
        <el-form-item label="分析期间" prop="analysisPeriod">
          <el-date-picker
            v-model="analysisForm.analysisPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="分析维度">
          <el-checkbox-group v-model="analysisForm.dimensions">
            <el-checkbox label="DEPT">科室维度</el-checkbox>
            <el-checkbox label="EQUIPMENT">设备维度</el-checkbox>
            <el-checkbox label="PROJECT">项目维度</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="分析说明">
          <el-input type="textarea" v-model="analysisForm.analysisDesc" :rows="4" placeholder="请输入分析说明"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCreate">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看分析详情对话框 -->
    <el-dialog title="成本分析详情" :visible.sync="detailVisible" width="1000px">
      <div v-if="currentAnalysis">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="分析编号">{{ currentAnalysis.analysisNo }}</el-descriptions-item>
              <el-descriptions-item label="分析名称">{{ currentAnalysis.analysisName }}</el-descriptions-item>
              <el-descriptions-item label="分析期间">{{ currentAnalysis.analysisPeriod }}</el-descriptions-item>
              <el-descriptions-item label="总成本">¥{{ currentAnalysis.totalCost }}</el-descriptions-item>
              <el-descriptions-item label="成本占比">{{ currentAnalysis.costRatio }}%</el-descriptions-item>
              <el-descriptions-item label="增长率">
                <span :style="{ color: currentAnalysis.growthRate >= 0 ? '#f56c6c' : '#67c23a' }">
                  {{ currentAnalysis.growthRate >= 0 ? '+' : '' }}{{ currentAnalysis.growthRate }}%
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDate(currentAnalysis.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="分析说明" :span="2">{{ currentAnalysis.analysisDesc || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="成本明细" name="detail">
            <el-table :data="analysisDetails" border>
              <el-table-column prop="costItem" label="成本项目" width="150"></el-table-column>
              <el-table-column prop="costAmount" label="成本金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.costAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="costRatio" label="占比" width="100">
                <template slot-scope="scope">
                  {{ scope.row.costRatio }}%
                </template>
              </el-table-column>
              <el-table-column prop="lastPeriodAmount" label="上期金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.lastPeriodAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="growthRate" label="增长率" width="100">
                <template slot-scope="scope">
                  <span :style="{ color: scope.row.growthRate >= 0 ? '#f56c6c' : '#67c23a' }">
                    {{ scope.row.growthRate >= 0 ? '+' : '' }}{{ scope.row.growthRate }}%
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 对比分析对话框 -->
    <el-dialog title="成本对比分析" :visible.sync="compareVisible" width="1000px">
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="对比期间1">
          <el-date-picker
            v-model="compareForm.period1"
            type="month"
            placeholder="选择期间"
            format="yyyy-MM"
            value-format="yyyy-MM"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="对比期间2">
          <el-date-picker
            v-model="compareForm.period2"
            type="month"
            placeholder="选择期间"
            format="yyyy-MM"
            value-format="yyyy-MM"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleConfirmCompare">对比</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="compareData" border v-if="compareData.length > 0">
        <el-table-column prop="costItem" label="成本项目" width="150"></el-table-column>
        <el-table-column :label="compareForm.period1" width="150">
          <template slot-scope="scope">
            ¥{{ scope.row.amount1 }}
          </template>
        </el-table-column>
        <el-table-column :label="compareForm.period2" width="150">
          <template slot-scope="scope">
            ¥{{ scope.row.amount2 }}
          </template>
        </el-table-column>
        <el-table-column label="差额" width="150">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.diff >= 0 ? '#f56c6c' : '#67c23a' }">
              {{ scope.row.diff >= 0 ? '+' : '' }}¥{{ scope.row.diff }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变化率" width="100">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.changeRate >= 0 ? '#f56c6c' : '#67c23a' }">
              {{ scope.row.changeRate >= 0 ? '+' : '' }}{{ scope.row.changeRate }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCostAnalyses, saveCostAnalysis, getCostAnalysisDetail, getCostAnalysisCompare } from '@/api/cost'

export default {
  name: 'CostAnalysis',
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      createVisible: false,
      detailVisible: false,
      compareVisible: false,
      detailActiveTab: 'basic',
      queryForm: {
        analysisPeriod: ''
      },
      analysisForm: {
        analysisName: '',
        analysisPeriod: '',
        dimensions: [],
        analysisDesc: ''
      },
      rules: {
        analysisName: [{ required: true, message: '请输入分析名称', trigger: 'blur' }],
        analysisPeriod: [{ required: true, message: '请选择分析期间', trigger: 'change' }]
      },
      currentAnalysis: null,
      analysisDetails: [],
      compareForm: {
        period1: '',
        period2: ''
      },
      compareData: []
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
      getAllCostAnalyses().then(response => {
        if (response.code === 200) {
          let allData = response.data || []
          if (this.queryForm.analysisPeriod) {
            allData = allData.filter(item => item.analysisPeriod === this.queryForm.analysisPeriod)
          }
          this.tableData = allData
          this.pagination.total = allData.length
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
    handleQuery() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm = {
        analysisPeriod: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleCreate() {
      this.analysisForm = {
        analysisName: '',
        analysisPeriod: new Date().toISOString().slice(0, 7),
        dimensions: [],
        analysisDesc: ''
      }
      this.createVisible = true
    },
    handleConfirmCreate() {
      this.$refs.analysisForm.validate((valid) => {
        if (valid) {
          saveCostAnalysis(this.analysisForm).then(response => {
            if (response.code === 200) {
              this.$message.success('分析创建成功')
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
    async handleView(row) {
      try {
        const response = await getCostAnalysisDetail(row.analysisId)
        if (response.code === 200 && response.data) {
          this.currentAnalysis = response.data
          this.analysisDetails = response.data.details || []
        } else {
          this.currentAnalysis = row
          this.analysisDetails = []
        }
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载分析详情失败')
      }
    },
    handleCompare(row) {
      this.compareForm = {
        period1: row.analysisPeriod || '',
        period2: ''
      }
      this.compareData = []
      this.compareVisible = true
    },
    handleConfirmCompare() {
      if (!this.compareForm.period1 || !this.compareForm.period2) {
        this.$message.warning('请选择两个对比期间')
        return
      }
      getCostAnalysisCompare(this.compareForm).then(response => {
        if (response.code === 200) {
          this.compareData = response.data || []
        } else {
          this.$message.error(response.message || '获取对比数据失败')
        }
      })
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.cost-analysis {
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
