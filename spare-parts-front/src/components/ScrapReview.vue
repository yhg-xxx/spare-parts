<template>
  <div class="app-container">
    <div class="main-content">
    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-select
          id="scrap-review-status-select"
          v-model="selectedStatus"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择状态"
          style="width: 400px; margin-right: 15px"
          @change="handleStatusChange"
      >
        <el-option
            v-for="status in enhancedStatusOptions"
            :key="status.value"
            :label="status.label"
            :value="status.value"
        />
      </el-select>
    </div>

    <!-- 审核列表 -->
    <el-table :data="filteredList" border highlight-current-row @row-click="handleRowClick" :row-class-name="tableRowClassName">
      <el-table-column prop="sn" label="SN号" width="180" />
      <el-table-column prop="scrapReason" label="报废原因" />
      <el-table-column prop="applyTime" label="申请时间" width="180" />
      <el-table-column prop="applicantId" label="申请人ID" width="120" />
      <el-table-column label="损坏照片" width="120">
        <template #default="{row}">
          <el-image
              style="width: 100px; height: 100px"
              :src="getImageUrl(row.damagePhoto)"
              :preview-src-list="[getImageUrl(row.damagePhoto)]"
              fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="partStatus" label="当前状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.partStatus)">
            {{ row.partStatus }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审核对话框 -->
    <el-dialog
        v-model="dialogVisible"
        title="报废审核"
        width="40%"
    >
      <el-form :model="currentRecord" label-width="100px">
        <el-form-item label="SN号">
          <el-input v-model="currentRecord.sn" disabled />
        </el-form-item>

        <el-form-item label="报废原因">
          <el-input v-model="currentRecord.scrapReason" disabled />
        </el-form-item>

        <el-form-item label="损坏照片">
          <el-image
              v-if="currentRecord.damagePhoto"
              style="width: 200px; height: 200px"
              :src="getImageUrl(currentRecord.damagePhoto)"
              :preview-src-list="[getImageUrl(currentRecord.damagePhoto)]"
              fit="cover"
          />
          <span v-else>无照片</span>
        </el-form-item>

        <el-form-item label="审核状态" prop="partStatus" required>
          <el-select id="scrap-review-status-input" v-model="currentRecord.partStatus">
            <el-option
                v-for="status in statusOptions"
                :key="status"
                :label="status"
                :value="status"
            />
          </el-select>
        </el-form-item>

        <el-form-item
            v-if="currentRecord.partStatus === '已报废'"
            label="处置方式"
            prop="disposalMethod"
            required
        >
          <el-input
              id="scrap-review-disposal"
              v-model="currentRecord.disposalMethod"
              placeholder="请输入处置方式"
          />
        </el-form-item>

        <el-form-item
            v-if="currentRecord.partStatus === '已报废'"
            label="执行人"
            prop="executor"
            required
        >
          <el-input
              id="scrap-review-executor"
              v-model="currentRecord.executor"
              placeholder="请输入执行人"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button id="scrap-review-cancel-btn" @click="dialogVisible = false">取消</el-button>
        <el-button id="scrap-review-submit-btn" type="primary" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
  </div>
</template>

<script setup>
import {ref, reactive, computed, watch, onMounted} from 'vue'
import axios from 'axios'
import {ElMessage} from "element-plus";

const apiUrl = 'http://localhost:8080/scrapRecord/scrap-records'
// 获取当前登录用户信息
const getCurrentUser = () => {
  try {
    const userData = sessionStorage.getItem('user')
    return userData ? JSON.parse(userData) : null
  } catch (e) {
    console.error('用户信息解析失败:', e)
    return null
  }
}
// 状态选项
const statusOptions = ['待审核', '已报废', '驳回']
const statusConfig = {
  all: { label: '全部', value: '' },
  pending: { label: '待审核', value: '待审核' },
  scrapped: { label: '已报废', value: '已报废' },
  rejected: { label: '驳回', value: '驳回' }
}
const finalStatuses = ['已报废', '驳回']
// 响应式数据
const allData = ref([]) // 存储所有数据
const selectedStatus = ref([statusConfig.pending.value])
const dialogVisible = ref(false)
const currentRecord = ref({
  orderId: '',
  sn: '',
  scrapReason: '',
  damagePhoto: '',
  partStatus: '',
  disposalMethod: '',
  executor: ''
})

// 计算筛选后的列表（前端过滤）
const filteredList = computed(() => {
  // 包含"全部"或空选时返回所有数据
  if (selectedStatus.value.includes(statusConfig.all.value) ||
      selectedStatus.value.length === 0) {
    return allData.value
  }

  // 根据选中状态过滤数据
  return allData.value.filter(item =>
      selectedStatus.value.includes(item.partStatus)
  )
})

// 修改后的状态选项（包含全部）
const enhancedStatusOptions = ref([
  statusConfig.all,
  statusConfig.pending,
  statusConfig.scrapped,
  statusConfig.rejected
])


// 获取图片完整URL（添加时间戳防缓存）
const getImageUrl = (url) => {
  if (!url) return '';
  // 直接使用后端返回的完整URL，添加时间戳防止缓存
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}t=${new Date().getTime()}`;
}

// 图片加载错误处理
const handleImageError = (e) => {
  console.error('图片加载失败:', e)
}
// 状态标签样式
const statusTagType = (status) => {
  switch(status) {
    case '待审核': return 'warning'
    case '已报废': return 'success'
    case '驳回': return 'danger'
    default: return 'info'
  }
}

// 获取数据
const fetchAllData = async () => {
  try {
    const response = await axios.get(apiUrl)
    allData.value = response.data
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  }
}
// 点击行显示审核对话框
const handleRowClick = (row) => {
  if (finalStatuses.includes(row.partStatus)) {
    ElMessage.warning('该记录已审核完成，不可修改')
    return
  }
  currentRecord.value = { ...row,
    executor: getCurrentUser()?.name || ''}
  dialogVisible.value = true
}
const tableRowClassName = ({ row }) => {
  return finalStatuses.includes(row.partStatus) ? 'disabled-row' : ''
}
// 提交审核
const handleSubmit = async () => {
  try {
    const updates = {
      partStatus: currentRecord.value.partStatus,
      disposalMethod: currentRecord.value.disposalMethod,
      executor: currentRecord.value.executor
    }

    // 1. 先提交报废记录审核
    await axios.put(`${apiUrl}/${currentRecord.value.orderId}/review`, updates)

    // 2. 如果审核通过为"已报废"，更新备件状态
    if (currentRecord.value.partStatus === '已报废') {
      // 2.1 通过SN码查询备件信息
      const sparePartRes = await axios.get(`http://localhost:8080/spare_part/bySn/${currentRecord.value.sn}`)

      if (!sparePartRes.data) {
        throw new Error('未找到对应备件')
      }

      // 2.2 创建要更新的备件对象（保留原始数据）
      const updatedPart = {
        ...sparePartRes.data,
        sparePartStatus: '已报废' // 修改状态字段
      }

      // 2.3 调用现有更新接口
      await axios.put(`http://localhost:8080/spare_part/${sparePartRes.data.partId}`, updatedPart)
    }

    ElMessage.success('审核提交成功')
    dialogVisible.value = false
    await fetchAllData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(`操作失败: ${error.response?.data?.message || error.message}`)
  }
}
const handleStatusChange = (values) => {
  const lastValue = values[values.length - 1]

  // 处理"全部"选项逻辑
  if (lastValue === statusConfig.all.value) {
    selectedStatus.value = [statusConfig.all.value]
  } else {
    selectedStatus.value = values.filter(v => v !== statusConfig.all.value)
    // 空选时自动选择"全部"
    if (selectedStatus.value.length === 0) {
      selectedStatus.value = [statusConfig.all.value]
    }
  }
}
// 初始化加载数据
onMounted(() => {
  fetchAllData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  display: flex;
  justify-content: center; /* 确保内容水平居中 */
}

.main-content {
  width: 1200px; /* 根据实际需要调整这个宽度 */
}

.filter-container {
  margin-bottom: 20px;
}

/* 修改选择框样式 */
.el-select {
  width: 100%; /* 让选择框填满容器 */
}

.el-table {
  width: 100%;
  margin: 0 auto; /* 表格水平居中 */
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.main-content {
  width: 1200px; /* 根据实际需求调整宽度 */
}

.filter-container {
  margin-bottom: 20px;
}

/* 添加禁用行样式 */
:deep(.disabled-row) {
  cursor: not-allowed;
  background-color: #f5f7fa;

  /* 禁用悬停高亮 */
  &:hover > td {
    background-color: inherit !important;
  }

  /* 禁用当前行高亮 */
  &.current-row > td {
    background-color: inherit !important;
  }
}
/* 保持原有图片样式 */
.el-image {
  cursor: pointer;
  border-radius: 4px;
  transition: transform 0.3s ease; /* 添加悬停动画 */
}

.el-image:hover {
  transform: scale(1.05); /* 图片悬停放大效果 */
}

/* 优化对话框样式 */
.el-dialog {
  border-radius: 8px;
}

.el-form-item {
  margin-bottom: 22px;
}
</style>