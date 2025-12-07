<template>
  <div class="procurement-requirement">
    <el-card>
      <div slot="header">
        <span>采购需求</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增需求</el-button>
      </div>
      <el-table :data="tableData" border>
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
    </el-card>
  </div>
</template>

<script>
import { getAllProcurements, saveProcurement } from '@/api/asset'

export default {
  name: 'ProcurementRequirement',
  data() {
    return { tableData: [] }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getAllProcurements().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
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

