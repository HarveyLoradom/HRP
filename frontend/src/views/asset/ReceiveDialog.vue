<template>
  <el-dialog
    title="新增领用"
    :visible.sync="dialogVisible"
    width="1000px"
    @close="handleClose"
  >
    <el-tabs v-model="activeTab">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
    <el-form :model="form" :rules="rules" ref="form" label-width="140px" style="margin-top: 20px;">
      <el-row :gutter="20">
        <el-col :span="8">
              <el-form-item label="操作人:">
                <el-input :value="form.operatorCode && form.operatorName ? `${form.operatorCode} - ${form.operatorName}` : (form.operatorName || form.operatorCode || '')" disabled></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="领用日期:" prop="receiveDate" required>
            <el-date-picker
              v-model="form.receiveDate"
              type="date"
              placeholder="选择领用日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              style="width: 100%;"
            ></el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="领用科室:" prop="deptId" required>
            <el-select
              v-model="form.deptId"
              placeholder="请选择领用科室"
              filterable
              style="width: 100%;"
            >
              <el-option
                v-for="dept in deptOptions"
                :key="dept.deptId"
                :label="dept.deptName"
                :value="dept.deptId">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="领用人:" prop="applyEmpId" required>
            <el-select
              v-model="form.applyEmpId"
              placeholder="请选择领用人"
              filterable
              remote
              :remote-method="searchEmployees"
              :loading="employeeLoading"
              style="width: 100%;"
            >
              <el-option
                v-for="emp in employeeOptions"
                :key="emp.empId"
                :label="emp.empName"
                :value="emp.empId.toString()">
                <span style="float: left">{{ emp.empName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ emp.empCode }}</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="领用原因:" prop="receiveReason" required>
        <el-input type="textarea" v-model="form.receiveReason" :rows="3" placeholder="请输入领用原因"></el-input>
      </el-form-item>
    </el-form>
      </el-tab-pane>
    
      <!-- 资产信息 -->
      <el-tab-pane label="资产信息" name="asset">
    <div style="margin-top: 20px;">
      <div style="margin-bottom: 10px;">
            <el-button type="primary" size="small" @click="handleOpenAssetDialog">选择资产</el-button>
      </div>
      <el-table :data="detailList" border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
        <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column label="库存数量" width="100" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.stockNum || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="领用数量" width="150">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.receiveNum"
              :min="1"
              :max="scope.row.stockNum || 0"
              :precision="0"
              style="width: 100%"
              :disabled="!scope.row.assetCode"
            ></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" style="color: #f56c6c;" @click="handleRemoveDetail(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 资产选择对话框 -->
    <el-dialog title="选择资产" :visible.sync="assetDialogVisible" width="1000px" :close-on-click-modal="false" :append-to-body="true" :modal="false">
      <div>
        <el-form :inline="true" style="margin-bottom: 10px;">
          <el-form-item label="资产名称">
            <el-input v-model="assetSearchForm.assetName" placeholder="请输入资产名称" clearable style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item label="资产编码">
            <el-input v-model="assetSearchForm.assetCode" placeholder="请输入资产编码" clearable style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item label="一级分类">
            <el-select v-model="assetSearchForm.level1Id" placeholder="全部" clearable style="width: 200px" @change="handleAssetLevel1Change">
              <el-option
                v-for="category in assetLevel1Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="二级分类">
            <el-select v-model="assetSearchForm.level2Id" placeholder="请先选择一级分类" clearable style="width: 200px" :disabled="!assetSearchForm.level1Id" @change="handleAssetLevel2Change">
              <el-option
                v-for="category in assetLevel2Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="三级分类">
            <el-select v-model="assetSearchForm.categoryId" placeholder="请先选择二级分类" clearable style="width: 200px" :disabled="!assetSearchForm.level2Id" @change="handleSearchAssets">
              <el-option
                v-for="category in assetLevel3Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearchAssets">查询</el-button>
            <el-button @click="handleResetAssetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table 
          ref="assetTable"
          :data="assetList" 
          border 
          style="width: 100%" 
          v-loading="assetListLoading"
          @selection-change="handleAssetSelectionChange"
        >
          <el-table-column type="selection" width="30"></el-table-column>
          <el-table-column type="index" label="序号" width="50" align="center"></el-table-column>
          <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
          <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
          <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
          <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
          <el-table-column prop="unit" label="单位" width="80"></el-table-column>
          <el-table-column prop="stockNum" label="库存数量" width="120" align="center">
            <template slot-scope="scope">
              <span style="color: #409EFF; font-weight: bold;">{{ scope.row.stockNum || 0 }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 10px; text-align: right;" v-if="assetPagination.total > 0">
          <el-pagination
            @size-change="handleAssetSizeChange"
            @current-change="handleAssetCurrentChange"
            :current-page="assetPagination.page"
            :page-sizes="[5, 10, 20, 50, 100]"
            :page-size="assetPagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="assetPagination.total">
          </el-pagination>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectedAssets">确定</el-button>
      </div>
    </el-dialog>
    
    <div slot="footer" class="dialog-footer" style="margin-top: 20px; text-align: right;">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { saveAssetReceive, generateReceiveNo, getAssetAccountPage, getAssetCategoryLevel1List, getAssetCategoryLevel2List, getAssetCategoryLevel3List } from '@/api/asset'
import { getDeptList, getUserById, getUserByAccount, searchEmployees, getAllEmployeesWithUser } from '@/api/user'

export default {
  name: 'ReceiveDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      activeTab: 'basic', // 当前激活的tab
      form: {
        receiveNo: '',
        receiveDate: new Date().toISOString().substring(0, 10),
        deptId: null,
        applyEmpId: '',
        receiveReason: '',
        operatorCode: '', // 操作人工号
        operatorName: '' // 操作人姓名
      },
      rules: {
        receiveDate: [
          { required: true, message: '请选择领用日期', trigger: 'change' }
        ],
        deptId: [
          { required: true, message: '请选择领用科室', trigger: 'change' }
        ],
        applyEmpId: [
          { required: true, message: '请选择领用人', trigger: 'change' }
        ],
        receiveReason: [
          { required: true, message: '请输入领用原因', trigger: 'blur' }
        ]
      },
      detailList: [],
      deptOptions: [],
      employeeOptions: [],
      employeeLoading: false,
      // 资产选择对话框相关
      assetDialogVisible: false,
      assetSearchForm: {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      },
      assetLevel1Options: [],
      assetLevel2Options: [],
      assetLevel3Options: [],
      assetList: [],
      assetListLoading: false,
      assetPagination: {
        page: 1,
        size: 10,
        total: 0
      },
      selectedAssetsInDialog: [] // 对话框中选中的资产
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.initForm()
        this.loadReceiveNo()
        this.loadDepts()
        this.loadDefaultDept()
        this.loadAllEmployees() // 加载所有员工列表
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false)
      }
    }
  },
  methods: {
    // 初始化表单
    initForm() {
      this.activeTab = 'basic'
      // 获取当前登录用户信息
      const userInfo = this.$store.state.user.userInfo || {}
      const operatorCode = userInfo.empCode || userInfo.emp_code || ''
      const operatorName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || ''
      
      this.form = {
        receiveNo: '',
        receiveDate: new Date().toISOString().substring(0, 10),
        deptId: null,
        applyEmpId: '',
        receiveReason: '',
        operatorCode: operatorCode,
        operatorName: operatorName
      }
      this.detailList = []
      this.assetDialogVisible = false
      this.selectedAssetsInDialog = []
      this.assetList = []
      this.assetSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetLevel1Options = []
      this.assetLevel2Options = []
      this.assetLevel3Options = []
      this.assetPagination = {
        page: 1,
        size: 10,
        total: 0
      }
    },
    // 加载领用单号
    async loadReceiveNo() {
      try {
        const res = await generateReceiveNo()
        if (res.code === 200 && res.data) {
          this.form.receiveNo = res.data
        }
      } catch (error) {
        console.error('生成领用单号失败:', error)
      }
    },
    // 加载部门列表
    async loadDepts() {
      try {
        const res = await getDeptList()
        if (res.code === 200 && res.data) {
          this.deptOptions = res.data || []
        }
      } catch (error) {
        console.error('加载部门列表失败:', error)
      }
    },
    // 加载默认部门（当前用户部门）和操作人信息
    async loadDefaultDept() {
      try {
        const userInfo = this.$store.state.user.userInfo || {}
        let deptId = userInfo.deptId || userInfo.dept_id || null
        let operatorCode = userInfo.empCode || userInfo.emp_code || ''
        let operatorName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || ''
        
        // 如果没有部门ID或操作人信息，尝试通过userId获取
        if ((!deptId || !operatorCode) && (userInfo.userId || userInfo.id)) {
          try {
            const userId = userInfo.userId || userInfo.id
            const response = await getUserById(userId)
            if (response.code === 200 && response.data) {
              const data = response.data
              if (!deptId) {
                deptId = data.deptId || data.dept_id || null
              }
              if (!operatorCode) {
                operatorCode = data.empCode || data.emp_code || ''
              }
              if (!operatorName) {
                operatorName = data.empName || data.emp_name || data.realName || data.name || ''
              }
            }
          } catch (err) {
            console.error('通过userId获取用户信息失败:', err)
          }
        }
        
        // 如果还没有部门ID或操作人信息，尝试通过account获取
        if ((!deptId || !operatorCode) && userInfo.account) {
          try {
            const response = await getUserByAccount(userInfo.account)
            if (response.code === 200 && response.data) {
              const data = response.data
              if (!deptId) {
                deptId = data.deptId || data.dept_id || null
              }
              if (!operatorCode) {
                operatorCode = data.empCode || data.emp_code || ''
              }
              if (!operatorName) {
                operatorName = data.empName || data.emp_name || data.realName || data.name || ''
              }
            }
          } catch (err) {
            console.error('通过account获取用户信息失败:', err)
          }
        }
        
        // 设置默认部门和操作人信息
        // 如果表单中还没有设置部门ID，则使用获取到的部门ID
        if (deptId && !this.form.deptId) {
          this.form.deptId = deptId
        }
        if (operatorCode) {
          this.form.operatorCode = operatorCode
        }
        if (operatorName) {
          this.form.operatorName = operatorName
        }
      } catch (error) {
        console.error('加载默认部门和操作人信息失败:', error)
      }
    },
    // 加载所有员工列表
    async loadAllEmployees() {
      try {
        this.employeeLoading = true
        // isStop: 0-启用, 1-停用, null-仅启用（默认）
        const res = await getAllEmployeesWithUser(0) // 0表示仅查询启用的员工
        if (res.code === 200 && res.data) {
          this.employeeOptions = res.data || []
        }
      } catch (err) {
        console.error('加载员工列表失败:', err)
        this.employeeOptions = []
      } finally {
        this.employeeLoading = false
      }
    },
    // 搜索员工
    searchEmployees(query) {
      if (query && query.trim() !== '') {
        this.employeeLoading = true
        searchEmployees(query).then(res => {
          if (res.code === 200 && res.data) {
            this.employeeOptions = res.data || []
          }
        }).catch(err => {
          console.error('搜索员工失败:', err)
          // 搜索失败时不清空列表，保持原有数据
        }).finally(() => {
          this.employeeLoading = false
        })
      } else {
        // 如果查询为空，重新加载所有员工
        this.loadAllEmployees()
      }
    },
    // 打开资产选择对话框
    async handleOpenAssetDialog() {
      // 加载一级分类选项
      this.loadAssetLevel1Options()
      this.assetDialogVisible = true
      this.selectedAssetsInDialog = []
      this.assetSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetLevel2Options = []
      this.assetLevel3Options = []
      this.assetPagination.page = 1
      await this.loadAssetList()
      // 清空表格选择状态
      this.$nextTick(() => {
        if (this.$refs.assetTable) {
          this.$refs.assetTable.clearSelection()
        }
      })
    },
    // 加载一级分类选项
    loadAssetLevel1Options() {
      getAssetCategoryLevel1List(1).then(res => {
        if (res.code === 200 && res.data) {
          this.assetLevel1Options = res.data || []
        }
      })
    },
    // 加载资产列表（只显示库存数量>0的）
    async loadAssetList() {
      this.assetListLoading = true
      try {
        const params = {
          page: this.assetPagination.page,
          size: this.assetPagination.size,
          hasStock: true // 只查询有库存的
        }
        if (this.assetSearchForm.assetName) {
          params.assetName = this.assetSearchForm.assetName
        }
        if (this.assetSearchForm.assetCode) {
          params.assetCode = this.assetSearchForm.assetCode
        }
        // 优先级：三级分类 > 二级分类 > 一级分类
        if (this.assetSearchForm.categoryId) {
          params.categoryId = this.assetSearchForm.categoryId
        } else if (this.assetSearchForm.level2Id) {
          params.level2Id = this.assetSearchForm.level2Id
        } else if (this.assetSearchForm.level1Id) {
          params.level1Id = this.assetSearchForm.level1Id
        }
        
        const res = await getAssetAccountPage(params)
        if (res.code === 200 && res.data) {
          // 过滤出库存数量>0的资产
          const allAssets = res.data.records || res.data.list || res.data.rows || []
          this.assetList = allAssets.filter(asset => (asset.stockNum || 0) > 0)
          this.assetPagination.total = res.data.total || this.assetList.length
        } else {
          this.assetList = []
          this.assetPagination.total = 0
        }
      } catch (error) {
        console.error('加载资产列表失败', error)
        this.$message.error('加载资产列表失败：' + (error.message || '未知错误'))
        this.assetList = []
        this.assetPagination.total = 0
      } finally {
        this.assetListLoading = false
      }
    },
    // 搜索资产
    handleSearchAssets() {
      this.assetPagination.page = 1
      this.loadAssetList()
    },
    // 重置搜索
    handleResetAssetSearch() {
      this.assetSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetLevel2Options = []
      this.assetLevel3Options = []
      this.assetPagination.page = 1
      this.loadAssetList()
    },
    // 一级分类变化
    handleAssetLevel1Change(level1Id) {
      this.assetSearchForm.level2Id = null
      this.assetSearchForm.categoryId = null
      this.assetLevel2Options = []
      this.assetLevel3Options = []
      if (level1Id) {
        getAssetCategoryLevel2List(level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.assetLevel2Options = res.data || []
          }
        })
      }
      this.assetPagination.page = 1
      this.loadAssetList()
    },
    // 二级分类变化
    handleAssetLevel2Change(level2Id) {
      this.assetSearchForm.categoryId = null
      this.assetLevel3Options = []
      if (level2Id) {
        getAssetCategoryLevel3List(level2Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.assetLevel3Options = res.data || []
          }
        })
      }
      this.assetPagination.page = 1
      this.loadAssetList()
    },
    // 资产选择变化
    handleAssetSelectionChange(selection) {
      this.selectedAssetsInDialog = selection
    },
    // 确认选择资产
    handleConfirmSelectedAssets() {
      if (this.selectedAssetsInDialog.length === 0) {
        this.$message.warning('请至少选择一个资产')
        return
      }
      
      let addedCount = 0
      let skippedCount = 0
      
      this.selectedAssetsInDialog.forEach(asset => {
        // 检查assetCode是否有效
        if (!asset.assetCode || asset.assetCode.toString().trim() === '') {
          skippedCount++
          return
        }
        
        // 检查是否已经添加过（通过assetCode判断）
        const exists = this.detailList.some(detail => detail.assetCode === asset.assetCode)
        
        if (exists) {
          skippedCount++
          return
        }
        
        // 检查库存数量是否大于0
        if ((asset.stockNum || 0) <= 0) {
          skippedCount++
          return
        }
        
        // 添加到明细列表
        this.detailList.push({
          assetCode: asset.assetCode,
          assetName: asset.assetName || '',
          spec: asset.spec || '',
          manufacturer: asset.manufacturer || '',
          unit: asset.unit || '',
          stockNum: asset.stockNum || 0,
          receiveNum: 1 // 默认领用数量为1
        })
        addedCount++
      })
      
      if (addedCount > 0) {
        this.$message.success(`成功添加 ${addedCount} 个资产${skippedCount > 0 ? `，跳过 ${skippedCount} 个已添加或库存为0的资产` : ''}`)
        this.assetDialogVisible = false
      } else {
        this.$message.warning('没有可添加的资产（可能已添加或库存为0）')
      }
    },
    // 分页大小变化
    handleAssetSizeChange(size) {
      this.assetPagination.size = size
      this.assetPagination.page = 1
      this.loadAssetList()
    },
    // 分页页码变化
    handleAssetCurrentChange(page) {
      this.assetPagination.page = page
      this.loadAssetList()
    },
    // 删除明细行
    handleRemoveDetail(index) {
      this.detailList.splice(index, 1)
    },
    // 保存
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (!valid) {
          return false
        }
        
        if (!this.detailList || this.detailList.length === 0) {
          this.$message.warning('请至少添加一条领用明细')
          return false
        }
        
        // 验证明细数据
        for (let i = 0; i < this.detailList.length; i++) {
          const detail = this.detailList[i]
          if (!detail.assetCode) {
            this.$message.warning(`第${i + 1}行请选择资产`)
            return false
          }
          if (!detail.receiveNum || detail.receiveNum <= 0) {
            this.$message.warning(`第${i + 1}行领用数量必须大于0`)
            return false
          }
          if (detail.receiveNum > detail.stockNum) {
            this.$message.warning(`第${i + 1}行领用数量不能超过库存数量`)
            return false
          }
        }
        
        const data = {
          receiveMain: {
            receiveNo: this.form.receiveNo,
            receiveDate: this.form.receiveDate,
            deptId: this.form.deptId,
            applyEmpId: this.form.applyEmpId,
            receiveReason: this.form.receiveReason,
            operatorCode: (this.form.operatorCode && this.form.operatorCode.toString().trim() !== '') ? String(this.form.operatorCode).trim() : null
          },
          details: this.detailList.map(detail => ({
            assetCode: detail.assetCode,
            assetName: detail.assetName,
            spec: detail.spec,
            unit: detail.unit,
            receiveNum: detail.receiveNum,
            manufacturer: detail.manufacturer
          }))
        }
        
        saveAssetReceive(data).then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.handleClose()
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '保存失败')
          }
        }).catch(err => {
          console.error('保存失败:', err)
          this.$message.error(err.msg || '保存失败')
        })
      })
    },
    // 关闭对话框
    handleClose() {
      this.dialogVisible = false
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
      this.detailList = []
      this.assetDialogVisible = false
      this.selectedAssetsInDialog = []
      this.assetList = []
      this.assetSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetLevel1Options = []
      this.assetLevel2Options = []
      this.assetLevel3Options = []
      this.assetPagination = {
        page: 1,
        size: 10,
        total: 0
      }
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