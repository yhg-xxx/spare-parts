<template>
  <div class="fault-order-container">
    <!-- 操作栏 -->
    <div class="mb-4">
      <el-button id="fault-add-btn" type="primary" @click="openAddDialog">新增故障工单</el-button>
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
              id="fault-acceptance-{{row.faultId}}"
              v-if="currentUser.role === '库管员' && row.workOrderStatus === '待验收'"
              size="small"
              type="danger"
              @click.stop="handleAcceptance(row, $event)">
            验收
          </el-button>

          <!-- 管理员编辑按钮 -->
          <el-button
              id="fault-edit-{{row.faultId}}"
              v-if="currentUser.role === '管理员'"
              size="small"
              type="primary"
              @click.stop="openEditDialog(row, $event)">
            编辑
          </el-button>

          <!-- 现场工程师操作按钮 -->
          <template v-if="currentUser.role === '二级维修人员'">
            <el-button
                id="fault-accept-{{row.faultId}}"
                v-if="row.workOrderStatus === '待处理'"
                size="small"
                type="success"
                @click.stop="handleAccept(row, $event)">
              接单
            </el-button>
            <el-button
                id="fault-review-{{row.faultId}}"
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
          <el-radio value="upload">文件上传</el-radio>
          <el-radio value="camera">摄像头拍摄</el-radio>
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
              请上传JPG/PNG格式的验收图片，大小不超过20MB
            </div>
          </template>
        </el-upload>
        <div v-if="uploading" class="location-loading" style="margin-top: 10px;">
          <el-icon class="loading-icon">
            <!-- 同定位加载图标 -->
          </el-icon>
          <span class="loading-text">正在上传图片...</span>
        </div>
      </div>

      <!-- 摄像头拍摄模式 -->
      <div v-else class="camera-container">
        <div class="camera-preview">
          <video ref="videoElement" class="video-preview" autoplay></video>
          <canvas ref="canvasElement" class="canvas-preview" style="display: none;"></canvas>

        </div>
        <div v-if="locating" class="location-loading">
          <el-icon class="loading-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" style="width: 1em; height: 1em;">
              <path fill="currentColor" d="M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32zm0 640a32 32 0 0 1 32 32v192a32 32 0 1 1-64 0V736a32 32 0 0 1 32-32zm448-192a32 32 0 0 1-32 32H736a32 32 0 1 1 0-64h192a32 32 0 0 1 32 32zm-640 0a32 32 0 0 1-32 32H96a32 32 0 0 1 0-64h192a32 32 0 0 1 32 32zM195.2 195.2a32 32 0 0 1 45.248 0L376.32 331.008a32 32 0 0 1-45.248 45.248L195.2 240.448a32 32 0 0 1 0-45.248zm452.544 452.544a32 32 0 0 1 45.248 0L828.8 783.552a32 32 0 0 1-45.248 45.248L647.744 692.992a32 32 0 0 1 0-45.248zM783.552 195.2a32 32 0 0 1 45.248 45.248L692.992 376.32a32 32 0 0 1-45.248-45.248L783.552 195.2zM331.008 647.744a32 32 0 0 1 45.248 45.248L240.448 828.8a32 32 0 0 1-45.248-45.248L331.008 647.744z"/>
            </svg>
          </el-icon>
          <span class="loading-text">正在获取地理位置...</span>
        </div>

        <div class="camera-controls">
          <el-button
              type="primary"
              @click="capturePhoto"
              :disabled="!cameraReady || locating"
              :loading="locating"
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

      <!-- 修改后的预览部分 -->
      <div v-if="uploadedImage" class="preview-container">
        <h4>预览（点击查看大图）:</h4>
        <el-image
            :src="uploadedImage"
            fit="contain"
            style="max-height: 300px; cursor: zoom-in;"
            :preview-src-list="[uploadedImage]"
            :zoom-rate="1.2"
            :max-scale="5"
            :min-scale="0.5"
            hide-on-click-modal
        >
          <template #error>
            <div class="image-error">
              <i class="el-icon-picture-outline"></i>
              <span>预览加载失败</span>
            </div>
          </template>
        </el-image>
      </div>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
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



        <el-form-item label="故障时间" required>
          <el-date-picker
              v-model="formData.faultTime"
              type="datetime"
              placeholder="选择故障时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
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
            <el-radio value="通过">通过</el-radio>
            <el-radio value="驳回">驳回</el-radio>
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

    <el-dialog
        v-model="acceptConfirmVisible"
        title="确认接单"
        width="500px"
    >
      <span>确定要接单吗？</span>
      <template #footer>
        <el-button @click="acceptConfirmVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAccept">确认</el-button>
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
import {ref, reactive, onMounted, watch, nextTick} from 'vue'
import {ElMessage} from 'element-plus'
import axios from "axios"
import router from "@/router.js"
import dayjs from "dayjs";
const uploading = ref(false)
/* 响应式状态 - 数据相关 */
const tableData = ref([])
const formData = reactive({
  sn: '',
  faultDescription: '',
  faultTime: '',
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
const acceptConfirmVisible = ref(false)
const currentAcceptOrder = ref(null)

/* 响应式状态 - 文件上传 */
const uploadMode = ref('upload')
const uploadedImage = ref('')
const uploadAction = ref('http://localhost:8080/files/upload')
const uploadHeaders = ref({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
})
const locating = ref(false);
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
  {label: '修好件', value: '修好件'},
  {label: '返厂修', value: '返厂修'}
])


/* 实例对象 */
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

/* API配置 */
const api = {
  getList: (params) => request.get('/api/fault-orders', {params}),
  create: (data) => request.post('/api/fault-orders', data),
  update: (id, data) => request.put(`/api/fault-orders/${id}`, data),
  getDetail: (id) => request.get(`/api/fault-orders/${id}`),
  accept: (id, data) => request.put(`/api/fault-orders/${id}/accept`, data),
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: {'Content-Type': 'multipart/form-data'}
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
  if ('geolocation' in navigator) {
    navigator.permissions.query({name: 'geolocation'}).then(permissionStatus => {
      permissionStatus.onchange = () => {
        if (permissionStatus.state === 'denied') {
          ElMessage.warning('位置权限已被拒绝，水印将不显示位置信息')
        }
      }
    })
  }
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

})

/* 表单提交 */
const submitForm = async () => {
  try {
    // 转换时间格式
    const payload = {
      ...formData,
      faultTime: dayjs(formData.faultTime).format('YYYY-MM-DD HH:mm:ss'),
      sn: formData.sn,// 确保SN号存在
      ...(!isEdit.value && {reportedBy: currentUser.value.name})
    }

    isEdit.value
        ? await api.update(formData.faultId, payload)
        : await api.create(payload)

    ElMessage.success('操作成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

/* 摄像头方法 */
const initCamera = async () => {
  try {
    const constraints = {
      video: {
        facingMode: cameraFacingMode.value,
        width: {ideal: 1280},
        height: {ideal: 720}
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
    locating.value = true
    await fetchLocation()
    const video = videoElement.value
    const canvas = canvasElement.value
    const context = canvas.getContext('2d')

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    context.drawImage(video, 0, 0, canvas.width, canvas.height)

    // 添加完整水印（与processImage一致）
    const baseFontSize = Math.max(canvas.width * 0.04, 30)
    context.font = `bold ${baseFontSize}px Arial`
    context.fillStyle = 'rgba(255, 0, 0, 0.6)'
    context.textAlign = 'right'
    context.textBaseline = 'bottom'

    // 文字阴影
    context.shadowColor = 'rgba(0, 0, 0, 0.8)'
    context.shadowOffsetX = 2
    context.shadowOffsetY = 2
    context.shadowBlur = 3


    const lineHeight = baseFontSize * 1.2
    const padding = 10


    const locationStatus = () => {
      if (locating.value) return '定位中...'
      if (!currentLocation.value) return '未获取位置'
      return currentLocation.value.formatted_address || '未知位置'
    }

    const lines = [
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
      `操作人：${currentUser.value.name}`,
      `地址：${locationStatus()}`
    ].filter(Boolean);
    lines.forEach((text, index) => {
      context.fillText(
          text,
          canvas.width - padding,
          canvas.height - padding - (lines.length - index - 1) * lineHeight
      )
    })
    // 转换为高质量JPEG
    const blob = await new Promise(resolve =>
        canvas.toBlob(resolve, 'image/jpeg', 0.9)
    )

    // 创建文件对象
    const file = new File([blob], `capture_${Date.now()}.jpg`, {
      type: 'image/jpeg'
    })

    // 统一使用上传接口
    const response = await api.upload(file)

    if (response.code === 200) {
      uploadedImage.value = response.url
      ElMessage.success('照片上传成功')
    }
  } catch (error) {
    ElMessage.error(`拍摄失败：${error.message}`)
  } finally {
    locating.value = false  // 确保最终关闭提示
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

/* 修改后的图片处理函数 */
const processImage = (file) => {
  return new Promise(async (resolve, reject) => {
    await fetchLocation()
    const reader = new FileReader()

    reader.onload = async (e) => {
      const img = new Image()
      img.onload = async () => {
        try {
          const canvas = document.createElement('canvas')
          const ctx = canvas.getContext('2d')

          // 设置画布尺寸
          canvas.width = img.width
          canvas.height = img.height

          // 处理背景（仅限非透明格式）
          if (!file.type.includes('png')) {
            ctx.fillStyle = '#ffffff'
            ctx.fillRect(0, 0, canvas.width, canvas.height)
          }
          ctx.drawImage(img, 0, 0)

          // 添加水印
          const baseFontSize = Math.max(canvas.width * 0.04, 30)
          ctx.font = `bold ${baseFontSize}px Arial`
          ctx.fillStyle = 'rgba(255, 0, 0, 0.6)'
          ctx.textAlign = 'right'
          ctx.textBaseline = 'bottom'

          // 阴影效果
          ctx.shadowColor = 'rgba(0, 0, 0, 0.8)'
          ctx.shadowOffsetX = 2
          ctx.shadowOffsetY = 2
          ctx.shadowBlur = 3

          // 水印内容
          const timestamp = new Date().toLocaleString()
          const lineHeight = baseFontSize * 1.2
          const padding = 10

          // 绘制水印

          const locationStatus = () => {
            if (locating.value) return '定位中...'
            if (!currentLocation.value) return '未获取位置'
            return currentLocation.value.formatted_address || '未知位置'
          }


          const lines = [
            dayjs().format('YYYY-MM-DD HH:mm:ss'),
            `操作人：${currentUser.value.name}`,
            `地址：${locationStatus()}`
          ].filter(Boolean);
          lines.forEach((text, index) => {
            ctx.fillText(
                text,
                canvas.width - padding,
                canvas.height - padding - (lines.length - index - 1) * lineHeight
            )
          })

          // 转换回文件
          canvas.toBlob(blob => {
            resolve(new File([blob], file.name, {
              type: file.type || 'image/jpeg',
              lastModified: Date.now()
            }))
          }, file.type || 'image/jpeg', 0.85)

        } catch (error) {
          reject(error)
        }
      }
      img.onerror = reject
      img.src = e.target.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
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
const handleAccept = (row, event) => {
  event.stopPropagation()
  currentAcceptOrder.value = row
  acceptConfirmVisible.value = true
}

/* 新增确认接单方法 */
const confirmAccept = async () => {
  try {
    if (!currentAcceptOrder.value) return

    await api.accept(currentAcceptOrder.value.faultId, {
      repairBy: currentUser.value.name,
    })

    ElMessage.success('接单成功')
    acceptConfirmVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}
/* 新增上传成功处理 */
const handleUploadSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    uploadedImage.value = response.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}
const handleUploadError = (error) => {
  uploading.value = false
  ElMessage.error('上传失败: ' + error.message)
}
/* 修改上传前处理 */
const beforeUpload = async (file) => {
  uploading.value = true
  const isImage = file.type.startsWith('image/')
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过20MB')
    return false
  }

  // 处理图片并替换文件
  try {
    // 使用await等待处理完成
    const processedFile = await processImage(file)

    // 创建新文件对象替换原始文件
    return new File([processedFile], file.name, {
      type: file.type,
      lastModified: Date.now()
    })
  } catch (error) {
    uploading.value = false
    ElMessage.error('图片处理失败: ' + error.message)
    return false
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
// 新增响应式变量
const currentLocation = ref(null)

// 获取地理位置方法
const fetchLocation = async () => {
  const GEOLOCATION_TIMEOUT = 8000;
  const API_TIMEOUT = 5000;
  const MAX_RETRIES = 2;

  locating.value = true; // 开始定位标识

  try {
    let finalLocation = '';
    let position = null;

    // 带重试机制的定位（保持原逻辑）
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(
              resolve,
              reject,
              {
                enableHighAccuracy: true,
                timeout: GEOLOCATION_TIMEOUT,
                maximumAge: 0 // 关键点：禁用位置缓存
              }
          );
        });
        break;
      } catch (error) {
        if (attempt === MAX_RETRIES) throw error;
        await new Promise(resolve => setTimeout(resolve, 1000 * (attempt + 1)));
      }
    }

    // 强制发起新请求（移除缓存检查）
    const [_, geocodeRes] = await Promise.allSettled([
      new Promise(resolve => setTimeout(resolve, 1000)),
      request.get('/api/location', {
        params: {
          longitude: position.coords.longitude.toFixed(6),
          latitude: position.coords.latitude.toFixed(6)
        },
        timeout: API_TIMEOUT
      })
    ]);

    // 处理地理编码结果（保持原逻辑）
    if (geocodeRes.status === 'fulfilled') {
      finalLocation = {
        formatted_address: geocodeRes.value?.regeocode?.formatted_address,
        coordinates: `${position.coords.latitude.toFixed(4)},${position.coords.longitude.toFixed(4)}`
      };
    } else {
      finalLocation = {
        formatted_address: `纬度:${position.coords.latitude.toFixed(4)}, 经度:${position.coords.longitude.toFixed(4)}`,
        coordinates: ''
      };
    }

    currentLocation.value = finalLocation;
  } catch (error) {
    console.error('定位失败:', error);
    // 错误处理逻辑保持原样
  } finally {
    locating.value = false;
  }
};
</script>

<style scoped>

/* 新增加载动画样式 */
.location-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-top: 16px;
}

.loading-icon {
  animation: rotate 1.2s linear infinite;
  margin-right: 8px;
  color: #1890ff;
}

.loading-text {
  color: #606266;
  font-size: 14px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 修改水印中的定位状态显示 */
.image-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.location-status {
  margin-left: 4px;
}
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

/* 新增水印样式 */
.watermark {
  font-size: 18px;
  line-height: 1.5;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.8);
  max-width: 80%;
  word-break: break-all;
}

/* 新增预览相关样式 */
.preview-container h4 {
  color: #666;
  margin-bottom: 8px;
  font-size: 14px;
}

:deep(.el-image-viewer__mask) {
  background-color: rgba(0, 0, 0, 0.8);
}

:deep(.el-image-viewer__actions) {
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 20px;
  padding: 8px 16px;
}

:deep(.el-image-viewer__canvas) {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>