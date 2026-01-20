<template>
  <el-dialog
    title="采购单详情"
    :visible.sync="dialogVisible"
    width="1200px"
    @close="handleClose"
  >
    <div v-if="loading" style="text-align: center; padding: 40px;">
      <i class="el-icon-loading" style="font-size: 24px;"></i>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="purchaseData">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="purchaseData" label-width="140px" style="margin-top: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="操作人:">
                  <span>{{ purchaseData.operatorName || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人科室:">
                  <span>{{ purchaseData.operatorDeptName || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人手机号:">
                  <span>{{ purchaseData.operatorPhone || '-' }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="采购单号:">
                  <span>{{ purchaseData.orderNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请单号:">
                  <span>{{ purchaseData.applyNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="采购日期:">
                  <span>{{ formatDateOnly(purchaseData.purchaseDate) }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="采购状态:">
                  <el-tag :type="getPurchaseStatusType(purchaseData.purchaseStatus)">
                    {{ getPurchaseStatusText(purchaseData.purchaseStatus) }}
                  </el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="合同编号:">
                  <span>{{ purchaseData.contractNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="供应商:">
                  <span>{{ purchaseData.supplierName || '-' }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="采购总金额:">
                  <span style="font-weight: bold; color: #409EFF;">¥{{ formatMoney(purchaseData.totalAmount) }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注:">
              <span>{{ purchaseData.remark || '-' }}</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 采购明细 -->
        <el-tab-pane label="采购明细" name="detail">
          <el-table :data="detailList" border style="width: 100%; margin-top: 20px;" v-if="detailList && detailList.length > 0">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
            <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
            <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
            <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="applyQuantity" label="申请数量" width="100"></el-table-column>
            <el-table-column prop="purchaseQuantity" label="采购数量" width="100"></el-table-column>
            <el-table-column prop="price" label="实际单价" width="120">
              <template slot-scope="scope">
                <span>¥{{ formatMoney(scope.row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="120">
              <template slot-scope="scope">
                <span>¥{{ formatMoney(scope.row.totalPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" width="150"></el-table-column>
          </el-table>
          <div v-if="detailList && detailList.length > 0" style="margin-top: 10px; text-align: right; padding-right: 10px;">
            <strong>合计金额：¥{{ formatMoney(purchaseData.totalAmount) }}</strong>
          </div>
          <div v-else style="text-align: center; color: #999; padding: 20px;">暂无明细数据</div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 操作按钮 -->
      <div slot="footer" class="dialog-footer" style="margin-top: 20px; text-align: right;">
        <el-button v-if="purchaseData.purchaseStatus === 'PURCHASING'" type="primary" @click="handleCompletePurchase">完成采购</el-button>
        <el-button v-if="purchaseData.purchaseStatus !== 'COMPLETED'" @click="handleEdit">编辑</el-button>
        <el-button v-if="purchaseData.purchaseStatus !== 'COMPLETED'" type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </div>
    
  </el-dialog>
</template>

<script>
import { getAssetPurchaseById, completeAssetPurchase, deleteAssetPurchase } from '@/api/asset'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'PurchaseDetailDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    purchaseId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      activeTab: 'basic',
      purchaseData: null,
      detailList: [],
      purchaseStatusOptions: []
    }
  },
  async mounted() {
    await this.loadStatusOptions()
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && this.purchaseId) {
        this.loadPurchaseDetail()
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false)
      }
    },
    purchaseId(val) {
      if (val && this.dialogVisible) {
        this.loadPurchaseDetail()
      }
    }
  },
  methods: {
    // 加载状态选项
    async loadStatusOptions() {
      try {
        this.purchaseStatusOptions = await getCodeTypeOptions('PURCHASE_STATUS')
      } catch (error) {
        console.error('加载状态选项失败:', error)
        // 使用默认值
        this.purchaseStatusOptions = [
          { label: '采购中', value: 'PURCHASING' },
          { label: '已完成', value: 'COMPLETED' },
          { label: '已取消', value: 'CANCELLED' }
        ]
      }
    },
    loadPurchaseDetail() {
      if (!this.purchaseId) return
      
      this.loading = true
      getAssetPurchaseById(this.purchaseId).then(res => {
        if (res.code === 200 && res.data) {
          this.purchaseData = res.data.purchase || res.data
          this.detailList = res.data.details || []
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error('加载采购单详情失败:', err)
        this.$message.error('加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    async handleCompletePurchase() {
      // 检查是否有合同号
      if (!this.purchaseData.contractNo || this.purchaseData.contractNo.trim() === '') {
        this.$message.warning('请先编辑采购单，填写合同编号后再完成采购')
        // 关闭详情对话框，触发编辑
        this.handleClose()
        this.$emit('edit', this.purchaseId)
        return
      }
      
      // 确认完成采购
      this.$confirm('确定要完成采购吗？完成后将自动生成入库单。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeAssetPurchase(this.purchaseId, this.purchaseData.contractNo).then(res => {
          if (res.code === 200) {
            this.$message.success('完成采购成功，已自动生成入库单')
            this.handleClose()
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '完成采购失败')
          }
        }).catch(err => {
          console.error('完成采购失败:', err)
          this.$message.error('完成采购失败：' + (err.response?.data?.msg || err.message || '未知错误'))
        })
      }).catch(() => {})
    },
    handleEdit() {
      this.handleClose()
      this.$emit('edit', this.purchaseId)
    },
    handleDelete() {
      this.$confirm('确定要删除该采购单吗？删除后无法恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAssetPurchase(this.purchaseId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.handleClose()
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        }).catch(err => {
          console.error('删除采购单失败:', err)
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    },
    handleClose() {
      this.dialogVisible = false
      this.purchaseData = null
      this.detailList = []
      this.activeTab = 'basic'
    },
    getPurchaseStatusText(status) {
      const option = this.purchaseStatusOptions.find(opt => opt.value === status)
      return option ? option.label : status
    },
    getPurchaseStatusType(status) {
      const typeMap = {
        'PURCHASING': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'info'
      }
      return typeMap[status] || ''
    },
    formatDateOnly(date) {
      if (!date) return ''
      if (typeof date === 'string') {
        return date.substring(0, 10)
      }
      return date
    },
    formatMoney(amount) {
      if (amount == null) return '0.00'
      return parseFloat(amount).toFixed(2)
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

