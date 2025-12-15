<template>
  <div class="equipment-analysis-report">
    <el-card>
      <div slot="header" class="clearfix">
        <span>设备分析报告</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleGenerate">生成报告</el-button>
      </div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="设备编码">
          <el-input v-model="queryForm.equipmentCode" placeholder="请输入设备编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="报告期间">
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
        <el-table-column prop="reportNo" label="报告编号" width="150"></el-table-column>
        <el-table-column prop="reportName" label="报告名称" width="200"></el-table-column>
        <el-table-column prop="equipmentCode" label="设备编码" width="120"></el-table-column>
        <el-table-column prop="equipmentName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="reportPeriod" label="报告期间" width="120"></el-table-column>
        <el-table-column prop="totalIncome" label="总收入" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalIncome }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="总成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalCost }}
          </template>
        </el-table-column>
        <el-table-column prop="netProfit" label="净利润" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.netProfit }}
          </template>
        </el-table-column>
        <el-table-column prop="utilizationRate" label="使用率" width="100">
          <template slot-scope="scope">
            {{ scope.row.utilizationRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="150">
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

    <!-- 生成报告对话框 -->
    <el-dialog title="生成设备分析报告" :visible.sync="generateVisible" width="600px">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item label="设备编码" required>
          <el-select v-model="generateForm.equipmentCode" placeholder="请选择设备" filterable style="width: 100%" @change="handleEquipmentChange">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.equipmentCode"
              :label="`${equipment.equipmentName} (${equipment.equipmentCode})`"
              :value="equipment.equipmentCode"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报告期间" required>
          <el-date-picker
            v-model="generateForm.reportPeriod"
            type="month"
            placeholder="选择月份"
            format="yyyy-MM"
            value-format="yyyy-MM"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="报告名称">
          <el-input v-model="generateForm.reportName" placeholder="自动生成，可手动修改"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="generateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmGenerate">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看报告详情对话框 -->
    <el-dialog title="设备分析报告详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentReport">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="报告编号">{{ currentReport.reportNo }}</el-descriptions-item>
              <el-descriptions-item label="报告名称">{{ currentReport.reportName }}</el-descriptions-item>
              <el-descriptions-item label="设备编码">{{ currentReport.equipmentCode }}</el-descriptions-item>
              <el-descriptions-item label="设备名称">{{ currentReport.equipmentName }}</el-descriptions-item>
              <el-descriptions-item label="报告期间">{{ currentReport.reportPeriod }}</el-descriptions-item>
              <el-descriptions-item label="生成时间">{{ formatDate(currentReport.createTime) }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="收入分析" name="income">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总收入">¥{{ currentReport.totalIncome }}</el-descriptions-item>
              <el-descriptions-item label="设备收入">¥{{ currentReport.equipmentIncome || 0 }}</el-descriptions-item>
              <el-descriptions-item label="其他收入">¥{{ currentReport.otherIncome || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="成本分析" name="cost">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总成本">¥{{ currentReport.totalCost }}</el-descriptions-item>
              <el-descriptions-item label="维护成本">¥{{ currentReport.maintenanceCost || 0 }}</el-descriptions-item>
              <el-descriptions-item label="能源成本">¥{{ currentReport.energyCost || 0 }}</el-descriptions-item>
              <el-descriptions-item label="人力成本">¥{{ currentReport.personnelCost || 0 }}</el-descriptions-item>
              <el-descriptions-item label="折旧成本">¥{{ currentReport.depreciationCost || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="效益分析" name="profit">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="净利润">¥{{ currentReport.netProfit }}</el-descriptions-item>
              <el-descriptions-item label="使用率">{{ currentReport.utilizationRate }}%</el-descriptions-item>
              <el-descriptions-item label="投资回报率">{{ currentReport.returnRate || 0 }}%</el-descriptions-item>
              <el-descriptions-item label="成本回收期">{{ currentReport.paybackPeriod || '-' }}个月</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEquipmentReports, generateEquipmentReport, getEquipmentReportDetail, getAllEquipments } from '@/api/efficiency'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'EquipmentAnalysisReport',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      equipmentList: [],
      generateVisible: false,
      detailVisible: false,
      detailActiveTab: 'basic',
      queryForm: {
        equipmentCode: '',
        reportPeriod: ''
      },
      generateForm: {
        equipmentCode: '',
        reportPeriod: new Date().toISOString().slice(0, 7),
        reportName: ''
      },
      currentReport: null
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
      getEquipmentReports().then(response => {
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
      if (this.queryForm.reportPeriod) {
        filtered = filtered.filter(item => item.reportPeriod === this.queryForm.reportPeriod)
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
        equipmentCode: '',
        reportPeriod: ''
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
    handleGenerate() {
      this.generateForm = {
        equipmentCode: '',
        reportPeriod: new Date().toISOString().slice(0, 7),
        reportName: ''
      }
      this.generateVisible = true
    },
    handleEquipmentChange(val) {
      const equipment = this.equipmentList.find(item => item.equipmentCode === val)
      if (equipment) {
        this.generateForm.reportName = `${equipment.equipmentName}-${this.generateForm.reportPeriod}-分析报告`
      }
    },
    handleConfirmGenerate() {
      if (!this.generateForm.equipmentCode) {
        this.$message.warning('请选择设备')
        return
      }
      if (!this.generateForm.reportPeriod) {
        this.$message.warning('请选择报告期间')
        return
      }
      generateEquipmentReport(this.generateForm).then(response => {
        if (response.code === 200) {
          this.$message.success('报告生成成功')
          this.generateVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '报告生成失败')
        }
      })
    },
    async handleView(row) {
      try {
        const response = await getEquipmentReportDetail(row.reportId)
        if (response.code === 200 && response.data) {
          this.currentReport = response.data
        } else {
          this.currentReport = row
        }
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载报告详情失败')
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
.equipment-analysis-report { 
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
