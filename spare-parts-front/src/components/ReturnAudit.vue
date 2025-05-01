<!-- ReturnAudit.vue 返还审核组件 -->
<template>
  <div>
    <el-tabs v-model="activeTab" @tab-change="fetchData">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待审核" name="pending" />
    </el-tabs>

    <el-table
        :data="stockouts"
        stripe
        style="width: 100%"
        @selection-change="handleAuditSelect"
        @row-click="handleRowClick"
    >

      <el-table-column type="selection" width="55" />
      <!-- 修改出库单号列 -->
      <el-table-column prop="id" label="出库单号" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="sn" label="SN码" />
      <el-table-column label="当前状态">
        <template #default="{row}">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.status }}
          </el-tag>
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
          :disabled="selectedAudits.length === 0 || activeTab !== 'pending'"
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
import qs from 'qs'

const stockouts = ref([])
const selectedAudits = ref([])
const currentUser = ref(null)
const activeTab = ref('pending') // 默认显示待审核列表

const getStatusTagType = (status) => {
  if (!status) return ''
  if (status.startsWith('返还审核-')) {
    return 'warning'
  } else if (status === '已入库') {
    return 'success'
  } else {
    return ''
  }
}

const fetchData = async () => {
  try {
    const params = {}
    if (activeTab.value === 'pending') {
      params.status = [
        '返还审核-新好件',
        '返还审核-修好件',
        '返还审核-坏件'
      ]
    }

    const stockRes = await axios.get('/api/stockouts/all', {
      params,
      paramsSerializer: params => {
        return qs.stringify(params, { arrayFormat: 'repeat' })
      }
    })

    // 添加前端过滤逻辑
    stockouts.value = stockRes.data.filter(item => {
      // 全部标签页过滤已出库
      if (activeTab.value === 'all') {
        return item.status !== '已出库'
      }
      return true
    })

  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  }
}

const viewDetail = (id) => {
  router.push(`/stockout-detail/${id}`)
}
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确定通过${selectedAudits.value.length}项审核？`)

    const requests = selectedAudits.value.map(async item => {
      const [_, partStatus] = item.status.split('-')
      await axios.put(`/api/stockouts/${item.id}/status`, null, {
        params: { status: '已入库' }
      })

      const spareRes = await axios.get(`/spare_part/bySn/${item.sn}`)
      if (spareRes.data?.error) {
        throw new Error(spareRes.data.error)
      }

      const updateData = {
        ...spareRes.data,
        status: '在库',
        sparePartStatus: partStatus.trim()
      }

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
// 新增行点击处理
const handleRowClick = (row, column, event) => {
  // 排除点击复选框的情况（当点击复选框时不跳转）
  if (event.target.closest('.el-table__selection')) return

  router.push(`/view/stockout-detail/${row.id}`)
}
</script>