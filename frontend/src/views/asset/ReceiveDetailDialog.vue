<template>
  <el-dialog
    title="领用详情"
    :visible.sync="dialogVisible"
    width="1200px"
    @close="handleClose"
  >
    <div v-if="loading" style="text-align: center; padding: 40px;">
      <i class="el-icon-loading" style="font-size: 24px;"></i>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="receiveData">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="receiveData" label-width="140px" style="margin-top: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="领用单号:">
                  <span>{{ receiveData.receiveNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="领用日期:">
                  <span>{{ formatDateOnly(receiveData.receiveDate) }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="领用科室:">
                  <span>{{ receiveData.deptName || '-' }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="领用人:">
                  <span>{{ receiveData.applyEmpName || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人:">
                  <span>{{ receiveData.operatorName || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="创建时间:">
                  <span>{{ formatDateTime(receiveData.createTime) }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="领用原因:">
              <span>{{ receiveData.receiveReason || '-' }}</span>
            </el-form-item>
            <el-form-item label="更新时间:">
              <span>{{ formatDateTime(receiveData.updateTime) }}</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 资产信息 -->
        <el-tab-pane label="资产信息" name="detail">
          <el-table :data="detailList" border style="width: 100%; margin-top: 20px;" v-if="detailList && detailList.length > 0">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
            <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
            <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
            <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80" align="center"></el-table-column>
            <el-table-column prop="receiveNum" label="领用数量" width="100" align="center"></el-table-column>
          </el-table>
          <div v-else style="text-align: center; color: #999; padding: 20px;">暂无资产信息</div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <div v-else style="text-align: center; color: #999; padding: 40px;">
      暂无数据
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getAssetReceiveById } from '@/api/asset'

export default {
  name: 'ReceiveDetailDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    receiveId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      receiveData: null,
      activeTab: 'basic'
    }
  },
  computed: {
    detailList() {
      return this.receiveData && this.receiveData.details ? this.receiveData.details : []
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && this.receiveId) {
        this.loadData()
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false)
      }
    },
    receiveId(val) {
      if (val && this.dialogVisible) {
        this.loadData()
      }
    }
  },
  methods: {
    // 加载领用详情数据
    loadData() {
      if (!this.receiveId) return
      
      this.loading = true
      getAssetReceiveById(this.receiveId).then(res => {
        if (res.code === 200 && res.data) {
          this.receiveData = res.data
        } else {
          this.$message.error(res.msg || '加载失败')
          this.receiveData = null
        }
      }).catch(err => {
        console.error('加载领用详情失败:', err)
        this.$message.error('加载失败')
        this.receiveData = null
      }).finally(() => {
        this.loading = false
      })
    },
    // 关闭对话框
    handleClose() {
      this.dialogVisible = false
      this.receiveData = null
    },
    // 格式化日期（只显示日期部分）
    formatDateOnly(date) {
      if (!date) return ''
      if (typeof date === 'string') {
        return date.substring(0, 10)
      }
      return date
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      if (typeof dateTime === 'string') {
        return dateTime.substring(0, 19).replace('T', ' ')
      }
      return dateTime
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>

