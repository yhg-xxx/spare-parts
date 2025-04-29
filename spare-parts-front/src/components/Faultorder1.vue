<template>
  <div class="fault-order-container">
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
      <!-- 正确写法 -->
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button
              size="small"
              type="primary"
              @click.stop="showTrack(row)">
            流程跟踪
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 流程跟踪弹窗 -->
    <el-dialog v-model="trackVisible" title="流程跟踪" width="600px">
      <el-timeline v-if="currentTrackData.length">
        <el-timeline-item
            v-for="(step, index) in currentTrackData"
            :key="index"
            :timestamp="formatTimelineTime(step.time)"
            placement="top">
          <div class="track-step">
            <el-tag
                :type="getStepTagType(step.status)"
                size="small">
              {{ step.status }}
            </el-tag>
            <span class="operator">{{ step.operator }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>

      <div v-else class="empty-tip">暂无流程记录</div>

      <template #footer>
        <el-button @click="trackVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="故障工单详情" width="800px">
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
          <div v-if="currentDetail.repairResult">
            <el-image
                :src="currentDetail.repairResult"
                fit="contain"
                style="max-width: 100%; max-height: 300px;"
                :preview-src-list="[currentDetail.repairResult]">
              <template #error>
                <div class="image-error">
                  <i class="el-icon-picture-outline"></i>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
          <span v-else>暂无维修结果</span>
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
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from "axios"

const detailVisible = ref(false)
const currentDetail = ref({})
const tableData = ref([])

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 状态标签样式
const statusTagType = (status) => {
  const map = {
    '待处理': 'warning',
    '处理中': 'primary',
    '待验收': 'primary',
    '已关闭': 'success',
    '已返厂': 'info',
    '已报废': 'danger'
  }
  return map[status] || 'info'
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

// 加载数据
const loadData = async () => {
  try {
    const response = await request.get('/fault-orders')
    tableData.value = response.data
  } catch (error) {
    ElMessage.error('数据加载失败')
  }
}

// 查看详情
const handleRowClick = async (row) => {
  try {
    const response = await request.get(`/fault-orders/${row.faultId}`)
    currentDetail.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 初始化加载
onMounted(async () => {
  await loadData()
})
// 错误：getHours() 拼写错误 & 缺少getMinutes()
const formatTimelineTime = (timeStr) => {
  const date = new Date(timeStr)
  return `${date.toLocaleDateString()} ${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`
  // 原代码中存在拼写错误：date.toHours() → date.getHours()
}

// 新增状态
const trackVisible = ref(false)
const currentTrackData = ref([])

// 显示流程跟踪
const showTrack = async (row) => {
  try {
    const response = await request.get(`/fault-orders/${row.faultId}/timeline`)
    currentTrackData.value = response.data
    trackVisible.value = true
  } catch (error) {
    ElMessage.error('获取流程数据失败')
  }
}

// 步骤标签样式
const getStepTagType = (status) => {
  const typeMap = {
    '工单创建': 'info',
    '开始处理': 'primary',
    '验收通过': 'success',
    '验收驳回': 'danger',
    '返厂维修完成': 'warning',
    '备件已报废': 'danger'
  }
  return typeMap[status] || ''
}
</script>

<style scoped>
/* 保留原有样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #999;
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
/* 分类标题样式增强 */
:deep(.full-row) {
  grid-column: 1 / -1;
  background-color: #f0faff; /* 更明亮的背景色 */
  margin: 12px 0 8px; /* 增加上下间距 */
}
:deep(.el-descriptions__body .el-descriptions-item__cell) {
  padding: 12px;
}
.timeline-section {
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.timeline-node {
  display: flex;
  align-items: center;
  gap: 8px;

  .status-label {
    font-weight: 500;
    color: #2c3e50;
  }

  .el-tag {
    flex-shrink: 0;
  }
}
.track-step {
  display: flex;
  align-items: center;
  gap: 8px;

  .operator {
    color: #666;
    font-size: 14px;
  }
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>