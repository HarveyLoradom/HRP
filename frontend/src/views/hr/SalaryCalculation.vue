<template>
  <div class="salary-calculation">
    <el-card>
      <div slot="header"><span>薪酬计算</span></div>
      <el-form :inline="true">
        <el-form-item label="职工ID">
          <el-input v-model="empId" placeholder="请输入职工ID"></el-input>
        </el-form-item>
        <el-form-item label="薪酬月份">
          <el-date-picker v-model="salaryMonth" type="month" format="yyyy-MM" value-format="yyyy-MM"></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCalculate">计算</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { calculateSalary } from '@/api/hr'

export default {
  name: 'SalaryCalculation',
  data() {
    return {
      empId: '',
      salaryMonth: ''
    }
  },
  methods: {
    handleCalculate() {
      if (!this.empId || !this.salaryMonth) {
        this.$message.warning('请填写完整信息')
        return
      }
      calculateSalary(this.empId, this.salaryMonth).then(response => {
        if (response.code === 200) {
          this.$message.success('计算成功')
        }
      })
    }
  }
}
</script>

<style scoped>
.salary-calculation { padding: 20px; }
</style>

