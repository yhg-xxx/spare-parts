<!-- ReturnAudit.vue 返还审核组件 -->
<template>
  <div>
    <el-table
        :data="stockouts"
        stripe
        style="width: 100%"
        @selection-change="handleAuditSelect"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="出库单号" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="sn" label="SN码" />
      <el-table-column label="当前状态">
        <template #default="{row}">
          <el-tag type="warning">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作员ID">
        <template #default="{row}">
          {{ row.operatorId }}
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-4">
      <el-button
          type="success"
          :disabled="selectedAudits.length === 0"
          @click="handleBatchApprove"
      >
        批量通过审核（{{ selectedAudits.length }}）
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import router from '@/router.js'
// 在ReturnAudit.vue的<script setup>部分顶部添加
import qs from 'qs'
const stockouts = ref([])
const selectedAudits = ref([])
// 新增用户信息声明
const currentUser = ref(null)

const fetchData = async () => {
  try {
    const stockRes = await axios.get('/api/stockouts/all', {
      params: {
        status: [ // 直接使用数组，不添加[]后缀
          '返还审核-新好件',
          '返还审核-修好件',
          '返还审核-坏件'
        ]
      },
      paramsSerializer: params => {
        return qs.stringify(params, { arrayFormat: 'repeat' })
      }
    })
    stockouts.value = stockRes.data
  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  }
}

// ReturnAudit.vue 修改批量审核方法
// 修改 handleBatchApprove 方法中的查询逻辑
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确定通过${selectedAudits.value.length}项审核？`)

    const requests = selectedAudits.value.map(async item => {
      // 1. 解析返还状态（格式："返还审核-状态"）
      const [_, partStatus] = item.status.split('-') // 示例结果：["返还审核", "修好件"]

      // 2. 更新出库单状态
      await axios.put(`/api/stockouts/${item.id}/status`, null, {
        params: { status: '已入库' }
      })

      // 3. 获取备件完整信息
      const spareRes = await axios.get(`/spare_part/bySn/${item.sn}`)
      if (spareRes.data?.error) {
        throw new Error(spareRes.data.error)
      }

      // 4. 构造更新数据（保留原有字段）
      const updateData = {
        ...spareRes.data,                // 保留原有数据
        status: '在库',                  // 更新库存状态
        sparePartStatus: partStatus      // 更新备件质量状态
      }

      // 5. 调用完整更新接口
      await axios.put(`/spare_part/${spareRes.data.partId}`, updateData)
    })

    await Promise.all(requests)
    ElMessage.success(`已成功入库${selectedAudits.value.length}项备件`)
    await fetchData()
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(`操作失败: ${error.response.data.message}`)
    } else if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const handleAuditSelect = (selection) => {
  selectedAudits.value = selection
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
  await fetchData()
})
</script>