<template>
  <div>
    <!-- 操作工具栏 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <el-button
            type="primary"
            :icon="Plus"
            @click="openAddDailyDialog">
          新增订单
        </el-button>
      </el-col>

      <el-col :span="12" class="flex items-center justify-end">
        <div class="flex gap-2">
          <el-input
              v-model="querySparePartName"
              placeholder="请输入备件名称"
              clearable
              style="width: 200px"/>
          <el-button
              type="primary"
              :icon="Search"
              @click="handleSearch">
            查询
          </el-button>
          <el-button @click="clearSearch">
            清空
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table
        :data="dailyList"
        stripe
        style="width: 100%"
        @row-click="handleRowClick">
      <el-table-column
          prop="order_id" label="采购订单id"/>
      <el-table-column
          prop="applicant_id" label="申请人id"/>
      <el-table-column
          prop="station" label="需求车站"/>
      <el-table-column
          prop="workshop" label="所属工区"/>
      <el-table-column
          prop="spare_part_name" label="备件名称"/>
      <el-table-column
          prop="spare_part_model" label="备件型号"/>
      <el-table-column
          prop="number" label="数量"/>
      <el-table-column
          prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column
          prop="created_at"
          label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.created_at) }}
        </template>
      </el-table-column>

      <el-table-column
          prop="completed_at"
          label="完成时间">
        <template #default="{ row }">
          {{ formatDate(row.completed_at) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
          label="操作"
          width="180">
        <template #default="{ row }">
          <div class="flex gap-2">
            <el-button
                size="small"
                :disabled="!canEditStatus(row.status)"
                @click.stop="openStatusDialog(row, $event)"> <!-- 添加 $event 参数 -->
              更新状态
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 流程跟踪对话框 -->
    <el-dialog
        v-model="flowDialogVisible"
        title="采购流程跟踪"
        width="800px">
      <div class="flow-container">
        <el-steps
            :active="currentStep"
            align-center
            finish-status="success">
          <el-step
              v-for="(step, index) in STATUS_FLOW"
              :key="index"
              :title="step"
              :status="getStepStatus(index)">
            <!-- 修改el-step的描述部分 -->
            <template #description>
              <div v-if="historyMap[step] && historyMap[step].changedAt">
                <div>{{ formatDate(historyMap[step].changedAt) }}</div>
                <!-- 待上会状态显示申请人 -->
                <div v-if="step === '待上会'" class="operator-text">
                  申请人：{{ currentUser.name }}
                </div>
                <!-- 其他状态显示操作人 -->
                <div v-else-if="historyMap[step].operator" class="operator-text">
                  操作人：{{ historyMap[step].operator }}
                </div>
              </div>
              <div v-else class="text-gray-400">
                待完成
              </div>
            </template>
          </el-step>
        </el-steps>
      </div>
    </el-dialog>

    <!-- 状态更新对话框 -->
    <el-dialog
        v-model="statusDialogVisible"
        title="更新采购状态">
      <el-form>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentOrder.status)">
            {{ currentOrder.status }}
          </el-tag>
        </el-form-item>

        <el-form-item label="新状态">
          <el-select v-model="selectedStatus">
            <el-option
                v-for="status in nextStatusOptions"
                :key="status"
                :label="status"
                :value="status"/>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="statusDialogVisible = false">
          取消
        </el-button>
        <el-button
            type="primary"
            @click="updateOrderStatus">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增订单对话框 -->
    <el-dialog
        v-model="addDailyDialogVisible"
        title="新增订单"
        @close="clearAddDailyForm">
      <el-form
          ref="addDailyFormRef"
          :model="addDailyForm"
          :rules="addDailyFormRules"
          label-width="100px">
        <el-form-item
            label="需求车站"
            prop="station">
          <el-input
              v-model="addDailyForm.station"
              placeholder="请输入需求车站"
              clearable/>
        </el-form-item>

        <el-form-item
            label="所属工区"
            prop="workshop">
          <el-input
              v-model="addDailyForm.workshop"
              placeholder="请输入所属工区"
              clearable/>
        </el-form-item>

        <el-form-item
            label="备件名称"
            prop="spare_part_name">
          <el-input
              v-model="addDailyForm.spare_part_name"
              placeholder="请输入备件名称"
              clearable/>
        </el-form-item>

        <el-form-item
            label="备件型号"
            prop="spare_part_model">
          <el-input
              v-model="addDailyForm.spare_part_model"
              placeholder="请输入备件型号"
              clearable/>
        </el-form-item>

        <el-form-item
            label="数量"
            prop="number">
          <el-input
              v-model="addDailyForm.number"
              placeholder="请输入备件数量"
              clearable/>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDailyDialogVisible = false">
            取消
          </el-button>
          <el-button
              type="primary"
              @click="submitAddDaily">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import axios from 'axios'
import router from '@/router.js'

/* 常量定义 */
const API_BASE = 'http://localhost:8080/purchase_order'
const STATUS_FLOW = [
  '待上会',     // 初始状态
  '待招标',     // 第一步
  '采购中',     // 第二步
  '待发货',     // 第三步
  '待收货',     // 第四步
  '已完成'      // 最终状态
]

/* 响应式状态 */
const querySparePartName = ref('')
const currentUser = ref({})
const dailyList = ref([])
const statusHistory = ref([])
const handleRowClick = (row) => {
  showFlowDialog(row)
}
// 对话框相关
const addDailyDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const flowDialogVisible = ref(false)

// 表单相关
const addDailyForm = ref({
  station: '',
  workshop: '',
  spare_part_name: '',
  spare_part_model: '',
  number: '',
  status: '待上会'
})

const addDailyFormRef = ref(null)
const currentOrder = ref({})
const currentFlowOrder = ref(null)
const selectedStatus = ref('')

// 修正后的nextStatusOptions计算属性
const nextStatusOptions = computed(() => {
  const currentIndex = STATUS_FLOW.indexOf(currentOrder.value.status);
  return currentIndex >= 0 && currentIndex < STATUS_FLOW.length - 1
      ? [STATUS_FLOW[currentIndex + 1]]
      : [];
});


const historyMap = computed(() =>
    statusHistory.value.reduce((map, item) => {
      map[item.status] = {
        changedAt: item.changedAt,
        operator: item.operator
      }
      return map
    }, {})
)
// 修正后的currentStep计算属性
const currentStep = computed(() => {
  const status = currentFlowOrder.value?.status;
  const index = STATUS_FLOW.indexOf(status);
  return status === '已完成' ? STATUS_FLOW.length : index;
});

/* 生命周期钩子 */
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'))
  if (!user) {
    ElMessage.error('请先登录')
    await router.push('/login')
    return
  }
  currentUser.value = user
  await fetchOrders()
})

/* 通用方法 */
const formatDate = (timestamp) => {
  if (!timestamp || timestamp === 'null') return '-';
  const date = new Date(timestamp);
  return isNaN(date) ? '-' : date.toLocaleString();
}

const getStatusTagType = (status) =>
    ({
      '待上会': 'info',
      '待招标': '',
      '采购中': 'warning',
      '待发货': 'primary',
      '待收货': 'success',
      '已完成': 'success'
    }[status] || '')

/* 订单列表相关 */
const fetchOrders = async () => {
  try {
    const params = {
      spare_part_name: querySparePartName.value
    }

    const { data } = await axios.get(`${API_BASE}/a`, {
      params,
      withCredentials: true
    })

    dailyList.value = data
  } catch (error) {
    handleError('获取订单列表失败', error)
  }
}

const handleSearch = () => fetchOrders()
const clearSearch = () => {
  querySparePartName.value = ''
  fetchOrders()
}

/* 订单状态相关 */
const canEditStatus = (status) =>
    !['已完成', '已入库'].includes(status) &&
    currentUser.value.role === '库管员'

const updateOrderStatus = async () => {
  try {
    await axios.put(
        `${API_BASE}/${currentOrder.value.order_id}/status`,
        {
          newStatus: selectedStatus.value,
          operatorId: currentUser.value.user_id
        },
        { withCredentials: true }
    )

    await fetchOrders()
    statusDialogVisible.value = false
    ElMessage.success('状态更新成功')
  } catch (error) {
    handleError('更新失败', error)
  }
}

/* 订单创建相关 */
const addDailyFormRules = {
  station: { required: true, message: '请输入需求车站', trigger: 'blur' },
  workshop: { required: true, message: '请输入所属工区', trigger: 'blur' },
  spare_part_name: { required: true, message: '请输入备件名称', trigger: 'blur' },
  spare_part_model: { required: true, message: '请输入备件型号', trigger: 'blur' },
  number: { required: true, message: '请输入备件数量', trigger: 'blur' }
}

const submitAddDaily = async () => {
  try {
    await addDailyFormRef.value.validate()

    const formData = {
      ...addDailyForm.value,
      applicant_id: currentUser.value.user_id,
      created_at: new Date().toISOString()
    }

    await axios.post(API_BASE, formData, {
      withCredentials: true
    })

    await fetchOrders()
    addDailyDialogVisible.value = false
    ElMessage.success('采购订单新增成功！')
  } catch (error) {
    if (error.name !== 'ElFormValidateError') {
      handleError('提交失败', error)
    }
  }
}

/* 流程跟踪相关 */
const getStepStatus = (index) => {
  const status = currentFlowOrder.value?.status;
  // 如果当前状态是已完成，所有步骤标记为成功
  if (status === '已完成') {
    return 'success';
  }
  const currentIndex = STATUS_FLOW.indexOf(status);
  if (currentIndex === -1) return 'wait'; // 状态不存在于流程中
  return index < currentIndex ? 'success'
      : index === currentIndex ? 'process'
          : 'wait';
};


const showFlowDialog = async (order) => {
  try {
    currentFlowOrder.value = order;
    const { data } = await axios.get(
        `${API_BASE}/${order.order_id}/histories`
    );

    // 创建完整状态节点骨架
    const statusNodes = STATUS_FLOW.reduce((acc, status) => {
      acc[status] = {
        changedAt: null,
        operator: null
      };
      return acc;
    }, {});

    // 合并API数据（包含操作人信息）
    data.forEach(item => {
      if (statusNodes[item.status]) {
        statusNodes[item.status] = {
          changedAt: item.changedAt,
          operator: item.operator
        };
      }
    });

    // 添加初始状态（待上会），使用当前用户作为申请人
    statusNodes['待上会'] = {
      changedAt: order.created_at,
      operator: currentUser.value.name // 使用当前登录用户姓名
    };

    statusHistory.value = STATUS_FLOW.map(status => ({
      status,
      ...statusNodes[status]
    }));

    flowDialogVisible.value = true;
  } catch (error) {
    handleError('获取流程历史失败', error);
  }
};
/* 辅助方法 */
const handleError = (message, error) => {
  const errorMsg = error.response?.data?.message || error.message
  ElMessage.error(`${message}: ${errorMsg}`)
}

/* UI交互方法 */
const openAddDailyDialog = () =>
    addDailyDialogVisible.value = true

const clearAddDailyForm = () => {
  addDailyFormRef.value?.resetFields()
  addDailyForm.value = {
    station: '',
    workshop: '',
    spare_part_name: '',
    spare_part_model: '',
    number: '',
    status: '待上会'
  }
}

const openStatusDialog = (row, event) => {
  // 确保 event 存在再调用方法
  event?.stopPropagation()
  currentOrder.value = row
  selectedStatus.value = ''
  statusDialogVisible.value = true
}
</script>
<style scoped>
.dialog-footer {
  text-align: right;
}

.flow-container {
  padding: 20px 40px;
}

.timestamp {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

:deep(.el-step__head.is-process) {
  color: #409EFF;
  border-color: #409EFF;
}

:deep(.el-step__title.is-process) {
  color: #409EFF;
  font-weight: 500;
}

:deep(.el-step__description.is-process) {
  color: #666;
}
.operator-text {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

.flow-container .el-step__description {
  line-height: 1.5;
}
</style>
