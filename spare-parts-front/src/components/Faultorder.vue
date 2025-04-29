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
      <!-- 修改操作列按钮，添加.stop修饰符 -->
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <!-- 库管员验收按钮 -->
          <el-button
              v-if="currentUser.role === '库管员' && row.workOrderStatus === '待验收'"
              size="small"
              type="danger"
              @click.stop="handleAcceptance(row, $event)">
            验收
          </el-button>

          <!-- 管理员编辑按钮 -->
          <el-button
              v-if="currentUser.role === '管理员'"
              size="small"
              type="primary"
              @click.stop="openEditDialog(row, $event)">
            编辑
          </el-button>

          <!-- 现场工程师操作按钮 -->
          <template v-if="currentUser.role === '现场工程师'">
            <el-button
                v-if="row.workOrderStatus === '待处理'"
                size="small"
                type="success"
                @click.stop="handleAccept(row, $event)">
              接单
            </el-button>
            <el-button
                v-if="row.workOrderStatus === '处理中'"
                size="small"
                type="warning"
                @click.stop="handleReview(row, $event)">
              申请验收
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <!-- 申请验收弹窗 -->
    <el-dialog
        v-model="reviewDialogVisible"
        title="申请验收"
        width="800px"
    >
      <div class="upload-mode-selector">
        <el-radio-group v-model="uploadMode">
          <el-radio label="upload">文件上传</el-radio>
          <el-radio label="camera">摄像头拍摄</el-radio>
        </el-radio-group>
      </div>

      <!-- 文件上传模式 -->
      <div v-if="uploadMode === 'upload'">
        <el-upload
            class="upload-demo"
            :action="uploadAction"
            :headers="uploadHeaders"
            name="file"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :show-file-list="false"
        >
          <el-button type="primary">点击上传验收图片</el-button>
          <template #tip>
            <div class="el-upload__tip">
              请上传JPG/PNG格式的验收图片，大小不超过5MB
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 摄像头拍摄模式 -->
      <div v-else class="camera-container">
        <div class="camera-preview">
          <video ref="videoElement" class="video-preview" autoplay></video>
          <canvas ref="canvasElement" class="canvas-preview" style="display: none;"></canvas>
        </div>

        <div class="camera-controls">
          <el-button
              type="primary"
              @click="capturePhoto"
              :disabled="!cameraReady"
          >
            拍摄照片
          </el-button>
          <el-button
              type="info"
              @click="switchCamera"
              :disabled="!cameraReady"
          >
            切换摄像头
          </el-button>
        </div>
      </div>

      <!-- 通用预览 -->
      <div v-if="uploadedImage" class="preview-container">
        <h4>预览:</h4>
        <el-image
            :src="uploadedImage"
            fit="contain"
            style="max-height: 300px;"
        />
      </div>

      <template #footer>
        <el-button @click="closeReviewDialog">取消</el-button>
        <el-button
            type="primary"
            :disabled="!uploadedImage"
            @click="submitReview"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
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
    <!-- 新增验收弹窗 -->
    <el-dialog
        v-model="acceptanceDialogVisible"
        title="工单验收"
        width="500px"
    >
      <el-form :model="acceptanceForm" label-width="100px">
        <el-form-item label="处置类型" required>
          <el-select
              v-model="acceptanceForm.disposalType"
              placeholder="请选择处置类型"
          >
            <el-option
                v-for="type in disposalTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="验收结果" required>
          <el-radio-group v-model="acceptanceForm.reviewResult">
            <el-radio label="通过">通过</el-radio>
            <el-radio label="驳回">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="acceptanceDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            :disabled="!acceptanceForm.disposalType || !acceptanceForm.reviewResult"
            @click="submitAcceptance"
        >
          提交验收
        </el-button>
      </template>
    </el-dialog>
    <!-- 详情弹窗 -->
    <el-dialog
        v-model="detailVisible"
        title="故障工单详情"
        width="800px"
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
          <div v-if="currentDetail.repairResult">
            <el-image
                :src="currentDetail.repairResult"
                fit="contain"
                style="max-width: 100%; max-height: 300px;"
                :preview-src-list="[currentDetail.repairResult]"
            >
              <template #error>
                <div class="image-error">
                  <i class="el-icon-picture-outline"></i>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
            <div class="image-tips">
              <el-link type="primary" :href="currentDetail.repairResult" target="_blank">查看原图</el-link>
            </div>
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
/* 模块导入 */
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from "axios"
import router from "@/router.js"

/* 响应式状态 - 数据相关 */
const tableData = ref([])
const formData = reactive({
  sn: '',
  faultDescription: '',
  workOrderStatus: ''
})
const currentDetail = ref({})
const currentUser = ref(null)
const currentReviewOrder = ref(null)
const currentAcceptanceOrder = ref(null)
/* 响应式状态 - 对话框控制 */
const detailVisible = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const reviewDialogVisible = ref(false)
const acceptanceDialogVisible = ref(false)

/* 响应式状态 - 文件上传 */
const uploadMode = ref('upload')
const uploadedImage = ref('')
const uploadAction = ref('http://localhost:8080/files/upload')
const uploadHeaders = ref({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
})

/* 响应式状态 - 摄像头相关 */
const videoElement = ref(null)
const canvasElement = ref(null)
const cameraReady = ref(false)
const currentStream = ref(null)
const cameraFacingMode = ref('environment')

/* 响应式状态 - 验收表单 */
const acceptanceForm = reactive({
  disposalType: '',
  reviewResult: ''
})
const disposalTypes = ref([
  { label: '修好件', value: '修好件' },
  { label: '报废', value: '报废' },
  { label: '返厂修', value: '返厂修' }
])

/* 常量定义 */
const statusOptions = ['待处理', '处理中', '已验收', '已返厂', '已报废', '已关闭']

/* 实例对象 */
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

/* API配置 */
const api = {
  getList: (params) => request.get('/fault-orders', { params }),
  create: (data) => request.post('/fault-orders', data),
  update: (id, data) => request.put(`/fault-orders/${id}`, data),
  getDetail: (id) => request.get(`/fault-orders/${id}`),
  accept: (id, data) => request.put(`/fault-orders/${id}/accept`, data),
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

/* 生命周期 */
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

/* 数据方法 */
const loadData = async () => {
  try {
    const response = await api.getList()
    tableData.value = response
  } catch (error) {
    ElMessage.error('数据加载失败')
  }
}

/* 通用方法 */
const formatTime = (time) => time ? new Date(time).toLocaleString() : '-'
const statusTagType = (status) => ({
  '待处理': 'warning',
  '处理中': 'primary',
  '待验收': 'primary',
  '已验收': 'success',
  '已关闭': 'success',
  '已返厂': 'info',
  '已报废': 'danger'
})[status] || 'info'

/* 对话框方法 */
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row, event) => {
  event.stopPropagation()
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const resetForm = () => Object.assign(formData, {
  sn: '',
  faultDescription: '',
  workOrderStatus: ''
})

/* 表单提交 */
const submitForm = async () => {
  try {
    isEdit.value
        ? await api.update(formData.faultId, formData)
        : await api.create(formData)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

/* 摄像头方法 */
const initCamera = async () => {
  try {
    const constraints = {
      video: {
        facingMode: cameraFacingMode.value,
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    }

    const stream = await navigator.mediaDevices.getUserMedia(constraints)
    currentStream.value = stream
    videoElement.value.srcObject = stream
    cameraReady.value = true
  } catch (error) {
    ElMessage.error('摄像头访问失败: ' + error.message)
    cameraReady.value = false
  }
}
const capturePhoto = async () => {
  try {
    const video = videoElement.value
    const canvas = canvasElement.value
    const context = canvas.getContext('2d')

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    context.drawImage(video, 0, 0, canvas.width, canvas.height)

    // 转换为Blob并添加文件名
    const blob = await new Promise(resolve =>
        canvas.toBlob(resolve, 'image/jpeg', 0.9)
    )

    // 创建带文件名的File对象
    const file = new File([blob], 'capture.jpg', {
      type: 'image/jpeg',
      lastModified: Date.now()
    })

    // 创建FormData并正确附加文件
    const formData = new FormData()
    formData.append('file', file, 'capture.jpg')  // 必须明确指定文件名

    // 添加必要的请求头
    const config = {
      headers: {
        'Content-Type': 'multipart/form-data',
        'X-Requested-With': 'XMLHttpRequest'
      },
      withCredentials: true // 确保携带凭证
    }

    // 直接使用axios发送请求
    const response = await axios.post(
        '/files/upload',
        formData,
        config
    )

    handleUploadSuccess(response.data)
  } catch (error) {
    ElMessage.error(`上传失败：${error.response?.data?.msg || error.message}`)
  }
}
const switchCamera = async () => {
  cameraFacingMode.value = cameraFacingMode.value === 'environment'
      ? 'user'
      : 'environment'

  if (currentStream.value) {
    currentStream.value.getTracks().forEach(track => track.stop())
  }
  await initCamera()
}

/* 验收流程 */
const handleReview = (row, event) => {
  event?.stopPropagation() // 安全调用
  currentReviewOrder.value = row
  reviewDialogVisible.value = true
}
const submitReview = async () => {
  try {
    await api.update(currentReviewOrder.value.faultId, {
      workOrderStatus: '待验收',
      repairResult: uploadedImage.value // 确保使用正确的图片URL字段
    })
    ElMessage.success('验收申请已提交')
    reviewDialogVisible.value = false
    uploadedImage.value = ''
    await loadData()
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message)
  }
}
const handleAcceptance = (row, event) => {
  event?.stopPropagation()
  currentAcceptanceOrder.value = row
  acceptanceForm.disposalType = row.disposalType || ''
  acceptanceForm.reviewResult = row.reviewResult || ''
  acceptanceDialogVisible.value = true
}
const submitAcceptance = async () => {
  try {
    const newStatus = acceptanceForm.reviewResult === '通过' ? '已关闭' : '处理中'

    await api.update(currentAcceptanceOrder.value.faultId, {
      disposalType: acceptanceForm.disposalType,
      reviewResult: acceptanceForm.reviewResult,
      workOrderStatus: newStatus,
      reviewBy: currentUser.value.name,
      reviewAt: new Date().toISOString()
    })

    ElMessage.success('验收处理完成')
    acceptanceDialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message)
  }
}

/* 其他交互 */
const handleRowClick = async (row) => {
  try {
    const detail = await api.getDetail(row.faultId)
    currentDetail.value = detail
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}
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

/* 监听器 */
watch(reviewDialogVisible, (val) => {
  if (val && uploadMode.value === 'camera') {
    nextTick(() => {
      initCamera()
    })
  } else {
    if (currentStream.value) {
      currentStream.value.getTracks().forEach(track => track.stop())
      currentStream.value = null
    }
  }
})
watch(uploadMode, (newVal) => {
  if (newVal === 'camera') {
    nextTick(() => {
      initCamera()
    })
  } else {
    if (currentStream.value) {
      currentStream.value.getTracks().forEach(track => track.stop())
      currentStream.value = null
    }
  }
})

/* 请求拦截器 */
request.interceptors.response.use(
    response => response.data,
    error => {
      ElMessage.error(error.message)
      return Promise.reject(error)
    }
)
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
/* 图片错误提示 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #999;

  i {
    font-size: 48px;
    margin-bottom: 8px;
  }
}

/* 图片操作提示 */
.image-tips {
  margin-top: 8px;
  text-align: center;
}
/* 新增摄像头区域样式 */
.camera-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.camera-preview {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 比例 */
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}

.video-preview {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保持比例填充 */
}

.camera-controls {
  margin-top: 16px;
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .camera-container {
    max-width: 100%;
    padding: 12px;
  }

  .camera-preview {
    padding-top: 56.25%; /* 16:9 比例 */
  }
}
</style>