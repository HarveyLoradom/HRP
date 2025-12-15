<template>
  <div class="cost-report">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成本报表</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleGenerate">生成报表</el-button>
      </div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="报表类型">
          <el-select v-model="queryForm.reportType" placeholder="请选择报表类型" clearable style="width: 150px;">
            <el-option label="全部" value=""></el-option>
            <el-option label="月度报表" value="MONTHLY"></el-option>
            <el-option label="季度报表" value="QUARTERLY"></el-option>
            <el-option label="年度报表" value="YEARLY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报表期间">
          <el-date-picker
            v-model="queryForm.reportPeriod"
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
        <el-table-column prop="reportNo" label="报表编号" width="150"></el-table-column>
        <el-table-column prop="reportName" label="报表名称" width="200"></el-table-column>
        <el-table-column prop="reportType" label="报表类型" width="120">
          <template slot-scope="scope">
            {{ getReportTypeName(scope.row.reportType) }}
          </template>
        </el-table-column>
        <el-table-column prop="reportPeriod" label="报表期间" width="120"></el-table-column>
        <el-table-column prop="totalCost" label="总成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalCost }}
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
        <el-table-column prop="createTime" label="生成时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleView(scope.row)">查看</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 生成报表对话框 -->
    <el-dialog title="生成成本报表" :visible.sync="generateVisible" width="600px">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item label="报表类型" required>
          <el-select v-model="generateForm.reportType" placeholder="请选择报表类型" style="width: 100%">
            <el-option label="月度报表" value="MONTHLY"></el-option>
            <el-option label="季度报表" value="QUARTERLY"></el-option>
            <el-option label="年度报表" value="YEARLY"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报表期间" required>
          <el-date-picker
            v-model="generateForm.reportPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="报表名称">
          <el-input v-model="generateForm.reportName" placeholder="自动生成，可手动修改"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmGenerate">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看报表详情对话框 -->
    <el-dialog title="成本报表详情" :visible.sync="detailVisible" width="1200px">
      <div v-if="currentReport">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="报表编号">{{ currentReport.reportNo }}</el-descriptions-item>
              <el-descriptions-item label="报表名称">{{ currentReport.reportName }}</el-descriptions-item>
              <el-descriptions-item label="报表类型">{{ getReportTypeName(currentReport.reportType) }}</el-descriptions-item>
              <el-descriptions-item label="报表期间">{{ currentReport.reportPeriod }}</el-descriptions-item>
              <el-descriptions-item label="总成本">¥{{ currentReport.totalCost }}</el-descriptions-item>
              <el-descriptions-item label="直接成本">¥{{ currentReport.directCost }}</el-descriptions-item>
              <el-descriptions-item label="间接成本">¥{{ currentReport.indirectCost }}</el-descriptions-item>
              <el-descriptions-item label="生成时间">{{ formatDate(currentReport.createTime) }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="图表分析" name="chart">
            <div style="display: flex; flex-wrap: wrap; gap: 20px;">
              <!-- 成本结构饼图 -->
              <div style="flex: 1; min-width: 400px;">
                <div style="text-align: center; margin-bottom: 10px; font-weight: bold;">成本结构分析</div>
                <div ref="pieChart" style="width: 100%; height: 400px;"></div>
              </div>
              <!-- 成本趋势柱状图 -->
              <div style="flex: 1; min-width: 400px;">
                <div style="text-align: center; margin-bottom: 10px; font-weight: bold;">成本趋势分析</div>
                <div ref="barChart" style="width: 100%; height: 400px;"></div>
              </div>
            </div>
            <!-- 成本明细折线图 -->
            <div style="margin-top: 20px;">
              <div style="text-align: center; margin-bottom: 10px; font-weight: bold;">成本明细趋势</div>
              <div ref="lineChart" style="width: 100%; height: 400px;"></div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCostReports, generateCostReport, getCostReportDetail, deleteCostReport } from '@/api/cost'
import * as echarts from 'echarts'

export default {
  name: 'CostReport',
  data() {
    return {
      loading: false,
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      generateVisible: false,
      detailVisible: false,
      detailActiveTab: 'basic',
      queryForm: {
        reportType: '',
        reportPeriod: ''
      },
      generateForm: {
        reportType: '',
        reportPeriod: new Date().toISOString().slice(0, 7),
        reportName: ''
      },
      currentReport: null,
      pieChart: null,
      barChart: null,
      lineChart: null
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
  beforeDestroy() {
    if (this.pieChart) {
      this.pieChart.dispose()
    }
    if (this.barChart) {
      this.barChart.dispose()
    }
    if (this.lineChart) {
      this.lineChart.dispose()
    }
  },
  watch: {
    detailVisible(val) {
      if (val && this.detailActiveTab === 'chart') {
        this.$nextTick(() => {
          this.initCharts()
        })
      }
    },
    detailActiveTab(val) {
      if (val === 'chart' && this.detailVisible) {
        this.$nextTick(() => {
          this.initCharts()
        })
      }
    }
  },
  methods: {
    loadData() {
      this.loading = true
      getAllCostReports().then(response => {
        if (response.code === 200) {
          let allData = response.data || []
          if (this.queryForm.reportType) {
            allData = allData.filter(item => item.reportType === this.queryForm.reportType)
          }
          if (this.queryForm.reportPeriod) {
            allData = allData.filter(item => item.reportPeriod === this.queryForm.reportPeriod)
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
        reportType: '',
        reportPeriod: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleGenerate() {
      this.generateForm = {
        reportType: '',
        reportPeriod: new Date().toISOString().slice(0, 7),
        reportName: ''
      }
      this.generateVisible = true
    },
    handleConfirmGenerate() {
      if (!this.generateForm.reportType) {
        this.$message.warning('请选择报表类型')
        return
      }
      if (!this.generateForm.reportPeriod) {
        this.$message.warning('请选择报表期间')
        return
      }
      if (!this.generateForm.reportName) {
        const typeName = this.getReportTypeName(this.generateForm.reportType)
        this.generateForm.reportName = `${typeName}-${this.generateForm.reportPeriod}`
      }
      generateCostReport(this.generateForm).then(response => {
        if (response.code === 200) {
            this.$message.success('报表生成成功')
            this.generateVisible = false
            this.pagination.page = 1
            this.loadData()
        } else {
          this.$message.error(response.message || '报表生成失败')
        }
      })
    },
    async handleView(row) {
      try {
        const response = await getCostReportDetail(row.reportId)
        if (response.code === 200 && response.data) {
          this.currentReport = response.data
        } else {
          this.currentReport = row
        }
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载报表详情失败')
      }
    },
    handleDelete(row) {
      this.$confirm('确认删除该报表吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCostReport(row.reportId).then(response => {
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
    initCharts() {
      if (!this.currentReport) return
      
      // 成本结构饼图
      if (this.$refs.pieChart) {
        if (this.pieChart) {
          this.pieChart.dispose()
        }
        this.pieChart = echarts.init(this.$refs.pieChart)
        const pieOption = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [{
            name: '成本结构',
            type: 'pie',
            radius: '60%',
            data: [
              { value: this.currentReport.directCost || 0, name: '直接成本' },
              { value: this.currentReport.indirectCost || 0, name: '间接成本' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        }
        this.pieChart.setOption(pieOption)
      }

      // 成本趋势柱状图
      if (this.$refs.barChart) {
        if (this.barChart) {
          this.barChart.dispose()
        }
        this.barChart = echarts.init(this.$refs.barChart)
        const barOption = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          xAxis: {
            type: 'category',
            data: ['总成本', '直接成本', '间接成本']
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '¥{value}'
            }
          },
          series: [{
            data: [
              this.currentReport.totalCost || 0,
              this.currentReport.directCost || 0,
              this.currentReport.indirectCost || 0
            ],
            type: 'bar',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }]
        }
        this.barChart.setOption(barOption)
      }

      // 成本明细折线图（模拟数据）
      if (this.$refs.lineChart) {
        if (this.lineChart) {
          this.lineChart.dispose()
        }
        this.lineChart = echarts.init(this.$refs.lineChart)
        const lineOption = {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['直接成本', '间接成本']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['第1周', '第2周', '第3周', '第4周']
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '¥{value}'
            }
          },
          series: [
            {
              name: '直接成本',
              type: 'line',
              stack: 'Total',
              data: [
                (this.currentReport.directCost || 0) * 0.2,
                (this.currentReport.directCost || 0) * 0.3,
                (this.currentReport.directCost || 0) * 0.25,
                (this.currentReport.directCost || 0) * 0.25
              ]
            },
            {
              name: '间接成本',
              type: 'line',
              stack: 'Total',
              data: [
                (this.currentReport.indirectCost || 0) * 0.2,
                (this.currentReport.indirectCost || 0) * 0.3,
                (this.currentReport.indirectCost || 0) * 0.25,
                (this.currentReport.indirectCost || 0) * 0.25
              ]
            }
          ]
        }
        this.lineChart.setOption(lineOption)
      }
    },
    getReportTypeName(type) {
      const typeMap = {
        'MONTHLY': '月度报表',
        'QUARTERLY': '季度报表',
        'YEARLY': '年度报表'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.cost-report {
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
