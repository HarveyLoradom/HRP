<template>
  <div class="procurement-requirement">
    <el-card>
      <div slot="header">
        <span>采购需求</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增需求</el-button>
      </div>
      <el-table :data="paginatedData" border v-loading="loading">
        <el-table-column prop="requirementNo" label="需求单号" width="150"></el-table-column>
        <el-table-column prop="requirementName" label="需求名称" width="200"></el-table-column>
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.estimatedAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
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
  </div>
</template>

<script>
import { getAllProcurements, saveProcurement } from '@/api/asset'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ProcurementRequirement',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: []
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
      getAllProcurements().then(response => {
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
    handleAdd() {
      this.$message.info('新增需求功能待实现')
    },
    handleEdit(row) {
      this.$message.info('编辑需求功能待实现')
    }
  }
}
</script>

<style scoped>
.procurement-requirement { padding: 20px; }
</style>

