<template>
  <div class="fault-order-container">
    <!-- 操作栏 -->
    <div class="mb-4">
      <el-button type="primary" @click="openAddDialog">新增故障工单</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" border style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="faultId" label="故障单ID" width="120" />
      <el-table-column prop="sn" label="SN号" width="200" />
      <el-table-column prop="faultDescription" label="故障描述" />
      <el-table-column prop="workOrderStatus" label="工单状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.workOrderStatus)">
            {{ row.workOrderStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <!-- 管理员显示编辑按钮 -->
          <el-button
              v-if="currentUser.role === '管理员'"
              size="small"
              type="primary"
              @click="openEditDialog(row, $event)">
            编辑
          </el-button>

          <!-- 维修人员显示操作按钮 -->
          <template v-if="currentUser.role === '现场工程师'">
            <el-button
                v-if="row.workOrderStatus === '待处理'"
                size="small"
                type="success"
                @click="handleAccept(row, $event)">
              接单
            </el-button>
            <el-button
                v-if="row.workOrderStatus === '处理中'"
                size="small"
                type="warning"
                @click="handleReview(row)">
              申请验收
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑故障工单' : '新增故障工单'"
        width="600px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="SN号" required>
          <el-input v-model="formData.sn" />
        </el-form-item>

        <el-form-item label="故障描述" required>
          <el-input
              v-model="formData.faultDescription"
              type="textarea"
              :rows="3"
          />
        </el-form-item>

        <el-form-item label="工单状态" required>
          <el-select
              v-model="formData.workOrderStatus"
              placeholder="请选择状态"
          >
            <el-option
                v-for="status in statusOptions"
                :key="status"
                :label="status"
                :value="status"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
    <!-- 详情弹窗 -->
    <el-dialog
        v-model="detailVisible"
        title="故障工单详情"
        width="700px"
    >
      <el-descriptions :column="2" border>
        <!-- 基础信息 -->
        <el-descriptions-item :span="2" class-name="full-row">
          <template #label>
            <span class="title-text">基础信息</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="故障单ID">{{ currentDetail.faultId }}</el-descriptions-item>
        <el-descriptions-item label="备件ID">{{ currentDetail.sparePart.partId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="SN号">{{ currentDetail.sn }}</el-descriptions-item>
        <el-descriptions-item label="故障时间">{{ formatTime(currentDetail.faultTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentDetail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="报告人">{{ currentDetail.reportedBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">
          {{ currentDetail.faultDescription }}
        </el-descriptions-item>

        <!-- 状态信息 -->

        <el-descriptions-item :span="2" label="当前状态">
          <el-tag :type="statusTagType(currentDetail.workOrderStatus)">
            {{ currentDetail.workOrderStatus }}
          </el-tag>
        </el-descriptions-item>

        <!-- 维修信息 -->
        <el-descriptions-item :span="2" class-name="full-row">
          <template #label>
            <span class="title-text">维修信息</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ currentDetail.repairBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ formatTime(currentDetail.processedAt) }}</el-descriptions-item>
        <el-descriptions-item label="维修结果" :span="2">
          {{ currentDetail.repairResult || '暂无维修结果' }}
        </el-descriptions-item>

        <!-- 验收信息 -->
        <el-descriptions-item :span="2" class-name="full-row">
          <template #label>
            <span class="title-text">验收信息</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="验收结果">
          <el-tag v-if="currentDetail.reviewResult"
                  :type="currentDetail.reviewResult === '通过' ? 'success' : 'danger'">
            {{ currentDetail.reviewResult }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="验收人">{{ currentDetail.reviewBy || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="验收时间">{{ formatTime(currentDetail.reviewAt) }}</el-descriptions-item>

        <!-- 处置信息 -->

        <el-descriptions-item label="处置类型" :span="2">
          {{ currentDetail.disposalType || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from "axios";
import router from "@/router.js";
const detailVisible = ref(false)
const currentDetail = ref({})
// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})
// 新增用户数据响应式对象

// 统一响应处理
request.interceptors.response.use(
    response => response.data,
    error => {
      ElMessage.error(error.message)
      return Promise.reject(error)
    }
)
// 接单处理方法
// 接单处理方法（已实现）
const handleAccept = async  (row, event) => {
  event.stopPropagation()
  try {
    await api.accept(row.faultId, {  // 调用更新接口
      repairBy: currentUser.value.name, // 维修人员
    })
    ElMessage.success('接单成功')
    await loadData() // 刷新列表
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
// 修改API定义
const api = {
  getList: (params) => request.get('/fault-orders', { params }),
  create: (data) => request.post('/fault-orders', data),
  update: (id, data) => request.put(`/fault-orders/${id}`, data),
  getDetail: (id) => request.get(`/fault-orders/${id}`),
  accept: (id, data) => request.put(`/fault-orders/${id}/accept`, data)
}
// 状态定义
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formData = reactive({
  sn: '',
  faultDescription: '',
  workOrderStatus: ''
})
// 修改表格行点击处理
const handleRowClick = async (row) => {
  try {
    const detail = await api.getDetail(row.faultId)
    currentDetail.value = detail
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}
// 时间格式化方法
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}
const currentUser = ref(null);
// 状态选项
const statusOptions = ['待处理', '处理中', '已验收', '已返厂', '已报废', '已关闭']

// 生命周期
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    await router.push('/');
    return;
  }
  currentUser.value = user;
  await loadData();
});
// 加载数据
const loadData = async () => {
  try {
    const response = await api.getList()
    tableData.value = response
  } catch (error) {
    ElMessage.error('数据加载失败')
  }
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
// 阻止编辑按钮冒泡
const openEditDialog = (row, event) => {
  event.stopPropagation()
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    if (isEdit.value) {
      await api.update(formData.faultId, formData)
    } else {
      await api.create(formData)
    }

    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    sn: '',
    faultDescription: '',
    workOrderStatus: ''
  })
}

// 状态标签样式
// 状态标签样式 (script部分修改)
const statusTagType = (status) => {
  const map = {
    '待处理': 'warning',
    '处理中': 'primary',
    '已验收': 'success',  // 新增
    '已关闭': 'success',
    '已返厂': 'info',
    '已报废': 'danger'
  }
  return map[status] || 'info' // 设置默认类型
}

</script>

<style scoped>


/* 分类标题样式增强 */
:deep(.full-row) {
  grid-column: 1 / -1;
  background-color: #f0faff; /* 更明亮的背景色 */
  margin: 12px 0 8px; /* 增加上下间距 */
}
.section-title {
  font-weight: 600;
  font-size: 15px; /* 增大字号 */
  color: #1d2c40; /* 更深的文字颜色 */
  padding: 10px 0;
  border-left: 4px solid #1890ff; /* 更亮的蓝色边框 */
  padding-left: 16px; /* 增加左边距 */
  width: 100%;
  background: linear-gradient(to right, #f0faff 0%, #f6fbff 100%); /* 渐变背景 */
  border-radius: 4px; /* 圆角 */
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1); /* 添加浅色阴影 */
}
/* 标题文字装饰 */
.title-text {
  position: relative;
  padding-left: 8px;
}

.title-text::before {
  content: "";
  position: absolute;
  left: -4px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: #1890ff;
  border-radius: 2px;
}
/* 调整描述项标签对齐 */
:deep(.el-descriptions__body .el-descriptions-item__cell) {
  padding: 12px;
}

</style>