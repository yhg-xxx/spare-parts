<!-- ReturnApply.vue 返还申请组件 -->
<template>
  <div>
    <!-- 返还申请表格 -->
    <el-table
        :data="filteredStockouts"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="出库单号" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="sn" label="SN码" />
      <el-table-column prop="outTime" label="出库时间">
        <template #default="{row}">
          {{ formatDate(row.outTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="当前状态">
        <template #default="{row}">
          <el-tag type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量操作 -->
    <div class="mt-4">
      <el-button
          type="warning"
          :disabled="selectedItems.length === 0"
          @click="openReturnDialog"
      >
        批量返还申请（{{ selectedItems.length }}）
      </el-button>
    </div>
    <!-- 新增的返还申请记录表格 -->
    <div class="mt-6">
      <h3 class="mb-4 text-lg font-semibold">我的返还申请记录</h3>
      <el-table
          :data="returnApplications"
          stripe
          style="width: 100%"
      >
        <el-table-column prop="id" label="申请单号" />
        <el-table-column prop="partName" label="备件名称" />
        <el-table-column prop="sn" label="SN码" />
        <el-table-column label="申请时间">
          <template #default="{row}">
            {{ formatDate(row.outTime) }}
          </template>
        </el-table-column>
        <el-table-column label="处理状态">
          <template #default="{row}">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 返还申请对话框 -->
    <el-dialog v-model="returnDialogVisible" title="备件返还申请">
      <el-form :model="returnForm">
        <el-form-item
            label="备件状态"
            prop="sparePartStatus"
            label-width="100px"
        >
          <el-select
              v-model="returnForm.sparePartStatus"
              placeholder请选择返还后状态
          >
            <el-option
                v-for="status in partStatusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            :loading="isSubmitting"
            @click="submitReturnApply"
        >
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
// 新增用户信息处理
const currentUser = ref(null)
// 响应式数据
const stockouts = ref([])
const selectedItems = ref([])
const returnDialogVisible = ref(false)
const isSubmitting = ref(false)
const returnForm = ref({
  sparePartStatus: ''
})

// 备件状态选项
const partStatusOptions = [
  { value: '新好件', label: '新好件（未使用）' },
  { value: '修好件', label: '修好件（已修复）' },
  { value: '坏件', label: '坏件（需维修）' }
]

// 增强后的计算属性
const filteredStockouts = computed(() => {
  if (!currentUser.value?.user_id) return []

  return stockouts.value.filter(item =>
      item.operatorId === currentUser.value.user_id &&
      item.status === '已出库'
  )
})

// 获取出库记录
const fetchStockouts = async () => {
  try {
    const res = await axios.get('/api/stockouts/all')
    stockouts.value = res.data
  } catch (error) {
    ElMessage.error('获取出库记录失败')
  }
}

// 提交返还申请
const submitReturnApply = async () => {
  try {
    isSubmitting.value = true

    const requests = selectedItems.value.map(async item => {
      // 1. 更新出库单状态（添加状态后缀）
      await axios.put(`/api/stockouts/${item.id}/status`, null, {
        params: {
          status: `返还审核-${returnForm.value.sparePartStatus}`
        }
      })

      // 2. 获取备件完整信息
      const spareRes = await axios.get(`/spare_part?sn=${item.sn}`)
      if (spareRes.data.length > 0) {
        const part = spareRes.data[0]
        const updateData = {
          ...part,
          sparePartStatus: returnForm.value.sparePartStatus
        }
        await axios.put(`/spare_part/${part.partId}`, updateData)
      }
    })

    await Promise.all(requests)
    ElMessage.success(`成功提交${selectedItems.value.length}项返还申请`)
    returnDialogVisible.value = false
    await fetchStockouts()
  } catch (error) {
    ElMessage.error('提交失败：' + error.message)
  } finally {
    isSubmitting.value = false
  }
}

// 初始化加载用户信息
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'))
  if (!user) {
    ElMessage.error('请先登录')
    await router.push('/')
    return
  }
  currentUser.value = user
  await fetchStockouts()  // 确保在用户验证后加载数据
})
// 新增日期格式化工具
const formatDate = (timeString) => {
  try {
    return new Date(timeString).toLocaleString('zh-CN')
  } catch (e) {
    return '无效日期'
  }
}
// 新增选择处理函数
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 新增打开对话框方法
const openReturnDialog = () => {
  if (selectedItems.value.length === 0) return
  returnDialogVisible.value = true
}
// 新增计算属性 - 返还申请记录
const returnApplications = computed(() => {
  if (!currentUser.value?.user_id) return []

  return stockouts.value.filter(item =>
      item.operatorId === currentUser.value.user_id &&
      (
          /^返还审核-/.test(item.status) ||  // 包含所有返还审核状态
          item.status === '已入库'            // 包含已入库状态
      )
  )
})
// 新增状态标签类型方法
const getStatusTagType = (status) => {
  const statusMap = {
    '返还审核-修好件': 'success',
    '返还审核-新好件': '',
    '返还审核-坏件': 'danger',
    '已入库': 'success'  // 新增已入库状态颜色
  }
  return statusMap[status] || 'info'
}

</script>