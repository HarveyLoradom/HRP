<template>
  <div class="contract-draft">
    <el-card>
      <div slot="header">
        <span>合同起草</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增合同</el-button>
      </div>
      <el-table :data="tableData" border>
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.contractAmount }}</template>
        </el-table-column>
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
import { getContractsByStatus } from '@/api/contract'

export default {
  name: 'ContractDraft',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getContractsByStatus('DRAFT').then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    handleAdd() {
      this.$message.info('新增合同功能待实现')
    },
    handleEdit(row) {
      this.$message.info('编辑合同功能待实现')
    }
  }
}
</script>

<style scoped>
.contract-draft { padding: 20px; }
</style>

