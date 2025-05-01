<template>
  <div class="detail-container">
    <el-page-header @back="goBack">
      <template #content>
        <span class="text-large font-600 mr-3"> 出库详情 #{{ stockout.id }} </span>
      </template>
    </el-page-header>

    <el-descriptions
        class="mt-4"
        title="基本信息"
        :column="2"
        border
    >
      <el-descriptions-item label="备件名称">{{ stockout.partName }}</el-descriptions-item>
      <el-descriptions-item label="备件型号">{{ stockout.partModel }}</el-descriptions-item>
      <el-descriptions-item label="SN码">{{ stockout.sn }}</el-descriptions-item>
      <el-descriptions-item label="当前状态">
        <el-tag :type="getStatusTagType(stockout.status)">
          {{ stockout.status }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="操作员ID">{{ stockout.operatorId }}</el-descriptions-item>
      <el-descriptions-item label="仓库名称">{{ stockout.locationName }}</el-descriptions-item>
      <el-descriptions-item label="出库时间">{{ formatDate(stockout.outTime) }}</el-descriptions-item>
      <el-descriptions-item label="关联领用单">
        <el-link v-if="stockout.requestId" type="primary">
          #{{ stockout.requestId }}
        </el-link>
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const stockout = ref({})

const getStatusTagType = (status) => {
  if (status?.startsWith('返还审核-')) return 'warning'
  if (status === '已入库') return 'success'
  return ''
}
import { watch } from 'vue'

// 监听路由参数变化
watch(
    () => route.params.id,
    (newId) => {
      if (newId) fetchDetail()
    }
)
const formatDate = (timeString) => {
  return new Date(timeString).toLocaleString('zh-CN')
}

const fetchDetail = async () => {
  try {
    const res = await axios.get(`/api/stockouts/${route.params.id}`)
    stockout.value = res.data
  } catch (error) {
    ElMessage.error('获取详情失败')
    router.go(-1)
  }
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
</style>