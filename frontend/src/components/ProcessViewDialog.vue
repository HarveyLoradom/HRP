<template>
  <el-dialog title="流程节点" :visible.sync="dialogVisible" width="90%" @close="handleClose">
    <div v-if="currentProcessDefinition">
      <el-alert
        v-if="processNodes && processNodes.length > 0"
        title="流程节点信息"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;">
        <template slot="default">
          <p>流程定义：{{ currentProcessDefinition.definitionName || '未命名流程' }}</p>
          <p>共 {{ processNodes.length }} 个流程节点</p>
        </template>
      </el-alert>
      <el-table v-if="processNodes && processNodes.length > 0" :data="processNodes" border style="width: 100%">
        <el-table-column prop="name" label="节点名称" width="200"></el-table-column>
        <el-table-column prop="type" label="节点类型" width="150">
          <template slot-scope="scope">
            {{ scope.row.type === 'userTask' ? '审批节点' : scope.row.type }}
          </template>
        </el-table-column>
        <el-table-column prop="assigneeTypeText" label="审批人类型" width="150">
          <template slot-scope="scope">
            {{ scope.row.assigneeTypeText || getAssigneeTypeName(scope.row.assigneeType) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="审批人" width="150">
          <template slot-scope="scope">
            {{ scope.row.assigneeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="加签" width="70">
          <template slot-scope="scope">
            {{ scope.row.allowAddsign === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="转办" width="70">
          <template slot-scope="scope">
            {{ scope.row.allowTransfer === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="退回" width="70">
          <template slot-scope="scope">
            {{ scope.row.allowReject === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="打印" width="70">
          <template slot-scope="scope">
            {{ scope.row.needPrint === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="printOrder" label="打印顺序" width="100">
          <template slot-scope="scope">
            {{ scope.row.printOrder || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.taskStatus" :type="getTaskStatusType(scope.row.taskStatus)" size="small">
              {{ getTaskStatusName(scope.row.taskStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="审批意见" min-width="200" v-if="showComment">
          <template slot-scope="scope">
            {{ scope.row.comment || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="completeTime" label="完成时间" width="160" v-if="showCompleteTime">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.completeTime) || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 50px; color: #999;">
        <p>流程节点数据为空，请检查流程定义配置</p>
      </div>
    </div>
    <div v-else style="text-align: center; padding: 50px; color: #999;">
      暂无流程信息
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getProcessDefinitionById, getProcessTaskByTaskKey, getProcessNodes, getProcessNodesWithBusiness } from '@/api/process'
import { getTemplateConfigById } from '@/api/templateConfig'

export default {
  name: 'ProcessViewDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    row: {
      type: [Object, null],
      required: false,
      default: null
    },
    templateConfigMap: {
      type: Object,
      default: () => ({})
    },
    // 业务键字段名（如 'payoutBillcode', 'contractNo', 'applyNo'）
    businessKeyField: {
      type: String,
      default: 'payoutBillcode'
    },
    // 是否显示审批意见和完成时间列（合同起草页面不显示）
    showComment: {
      type: Boolean,
      default: false
    },
    showCompleteTime: {
      type: Boolean,
      default: false
    },
    // 业务类型名称（用于错误提示，如 '申请', '报账', '合同', '预算申请'）
    businessTypeName: {
      type: String,
      default: '申请'
    }
  },
  data() {
    return {
      currentProcessDefinition: null,
      processNodes: [],
      loading: false
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.loadProcessInfo()
      } else {
        this.resetData()
      }
    }
  },
  methods: {
    resetData() {
      this.currentProcessDefinition = null
      this.processNodes = []
    },
    async loadProcessInfo() {
      if (!this.row) {
        return
      }

      // 获取流程定义ID
      let processDefinitionId = this.row.processDefinitionId
      
      // 如果没有processDefinitionId，尝试从templateConfigId获取
      if (!processDefinitionId && this.row.templateConfigId) {
        // 从本地映射或后端获取模板配置
        let config = this.templateConfigMap[this.row.templateConfigId]
        if (!config) {
          try {
            const configResponse = await getTemplateConfigById(this.row.templateConfigId)
            if (configResponse.code === 200 && configResponse.data) {
              config = configResponse.data
              // 更新本地映射（如果父组件提供了templateConfigMap）
              if (this.templateConfigMap) {
                this.$set(this.templateConfigMap, this.row.templateConfigId, config)
              }
            }
          } catch (error) {
            console.error('获取模板配置失败', error)
            // 不返回，继续尝试其他方式
          }
        }
        
        if (config) {
          processDefinitionId = config.processDefinitionId
        }
      }
      
      if (!processDefinitionId) {
        this.$message.warning(`该${this.businessTypeName}未关联流程定义，请检查模板配置`)
        return
      }
      
      try {
        this.loading = true
        // 获取流程定义基本信息
        const response = await getProcessDefinitionById(processDefinitionId)
        if (response.code === 200 && response.data) {
          this.currentProcessDefinition = response.data
          
          // 获取业务键
          const businessKey = this.row[this.businessKeyField]
          
          // 从wf_process_task表获取流程任务记录（新的基于数据库表的方式）
          try {
            let tasksResponse
            if (businessKey) {
              // 使用taskKey（即业务键）从数据库表查询任务记录
              tasksResponse = await getProcessTaskByTaskKey(businessKey)
            } else {
              // 如果没有业务键，使用原来的方式（从流程定义解析）
              let nodesResponse
              try {
                nodesResponse = await getProcessNodes(processDefinitionId)
                if (nodesResponse.code === 200 && nodesResponse.data && nodesResponse.data.length > 0) {
                  // 按打印顺序升序排序
                  this.processNodes = nodesResponse.data.slice().sort((a, b) => {
                    const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                    const bOrder = (b.printOrder != null ? b.printOrder : 999999)
                    return aOrder - bOrder
                  })
                }
                this.loading = false
                return
              } catch (nodesError) {
                this.processNodes = []
                const errorMsg = (nodesError.response && nodesError.response.data && nodesError.response.data.message) || nodesError.message || '网络错误'
                this.$message.error('获取流程节点失败：' + errorMsg)
                this.loading = false
                return
              }
            }
            
            if (tasksResponse.code === 200) {
              if (tasksResponse.data && tasksResponse.data.length > 0) {
                // 将ProcessTask转换为前端期望的格式（ProcessNodeInfo格式）
                // 按节点名称分组，同一节点只显示一行，审批人使用approverList（逗号拼接的多个审批人）
                const tasksByNodeName = {}
                tasksResponse.data.forEach(task => {
                  const nodeName = task.taskName
                  if (!tasksByNodeName[nodeName]) {
                    tasksByNodeName[nodeName] = {
                      id: task.taskId,
                      name: nodeName,
                      type: 'userTask',
                      assigneeType: task.assigneeType,
                      assigneeTypeText: this.getAssigneeTypeName(task.assigneeType),
                      assigneeId: task.assigneeUserId,
                      // 使用approverList（同一节点的所有审批人，用逗号拼接），如果没有则使用assigneeUserName
                      assigneeName: task.approverList || task.assigneeUserName || '-',
                      assigneeCode: task.assigneeEmpCode,
                      allowAddsign: task.allowAddsign,
                      allowTransfer: task.allowTransfer,
                      allowReject: task.allowReject,
                      needPrint: task.needPrint,
                      printOrder: task.printOrder,
                      taskStatus: task.taskStatus, // 使用第一个任务的状态
                      completeTime: task.completeTime,
                      comment: task.comment
                    }
                  }
                })
                // 转换为数组后，按照打印顺序升序排序（printOrder 小的在前，null 放到最后）
                this.processNodes = Object.values(tasksByNodeName).sort((a, b) => {
                  const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                  const bOrder = (b.printOrder != null ? b.printOrder : 999999)
                  return aOrder - bOrder
                })
              } else {
                // 如果没有任务记录，尝试从流程定义解析（首次加载）
                try {
                  let nodesResponse
                  if (businessKey) {
                    nodesResponse = await getProcessNodesWithBusiness(processDefinitionId, businessKey)
                  } else {
                    nodesResponse = await getProcessNodes(processDefinitionId)
                  }
                  if (nodesResponse.code === 200) {
                    if (nodesResponse.data && nodesResponse.data.length > 0) {
                      // 后端已经过滤了sequenceFlow，直接使用返回的数据
                      // 按打印顺序升序排序（printOrder 小的在前，null 放到最后）
                      this.processNodes = nodesResponse.data.slice().sort((a, b) => {
                        const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                        const bOrder = (b.printOrder != null ? b.printOrder : 999999)
                        return aOrder - bOrder
                      })
                    } else {
                      this.processNodes = []
                      this.$message.warning('流程节点数据为空，请检查流程定义配置')
                    }
                  } else {
                    this.processNodes = []
                    this.$message.error('获取流程节点失败：' + (nodesResponse.message || '未知错误'))
                  }
                } catch (nodesError) {
                  this.processNodes = []
                  const errorMsg = (nodesError.response && nodesError.response.data && nodesError.response.data.message) || nodesError.message || '网络错误'
                  this.$message.error('获取流程节点失败：' + errorMsg)
                }
              }
            } else {
              this.processNodes = []
              this.$message.error('获取流程任务失败：' + (tasksResponse.message || '未知错误'))
            }
          } catch (taskError) {
            this.processNodes = []
            const errorMsg = (taskError.response && taskError.response.data && taskError.response.data.message) || taskError.message || '网络错误'
            this.$message.error('获取流程任务失败：' + errorMsg)
          }
        } else {
          this.$message.error('获取流程定义失败：' + (response.message || '流程定义不存在'))
        }
      } catch (error) {
        this.$message.error('获取流程定义失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    getAssigneeTypeName(assigneeType) {
      const typeMap = {
        'user': '指定用户',
        'position': '指定岗位',
        'dept': '部门负责人',
        'manage_dept': '归口审批人',
        'initiator': '发起人',
        'previous': '上一节点审批人',
        'responsible': '负责人'
      }
      return typeMap[assigneeType] || assigneeType || '-'
    },
    getTaskStatusName(statusValue) {
      if (!statusValue) return '-'
      const statusMap = {
        'COMPLETED': '已完成',
        'PENDING': '待审批',
        'RETURNED': '已退回',
        'TERMINATED': '已终止',
        'TRANSFERRED': '已转办'
      }
      return statusMap[statusValue] || statusValue
    },
    getTaskStatusType(statusValue) {
      if (statusValue === 'COMPLETED') return 'success'
      if (statusValue === 'PENDING') return 'warning'
      if (statusValue === 'RETURNED') return 'danger'
      if (statusValue === 'TERMINATED') return 'info'
      if (statusValue === 'TRANSFERRED') return 'info'
      return 'info'
    },
    formatDateTime(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
</style>

