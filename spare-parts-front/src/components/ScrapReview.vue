<template>
  <div class="app-container">
    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-select v-model="listQuery.status" placeholder="选择状态" clearable>
        <el-option
            v-for="status in statusOptions"
            :key="status"
            :label="status"
            :value="status"
        />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <!-- 审核列表 -->
    <el-table
        :data="list"
        border
        highlight-current-row
        @row-click="handleRowClick"
    >
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
          <el-select v-model="currentRecord.partStatus">
            <el-option
                v-for="status in auditStatusOptions"
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
              v-model="currentRecord.executor"
              placeholder="请输入执行人"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import {ElMessage} from "element-plus";

const apiUrl = 'http://localhost:8080/scrapRecord/scrap-records'

// 状态选项
const statusOptions = ['待审核', '已报废', '驳回']
const auditStatusOptions = ['已报废', '驳回']

// 数据列表相关
const list = ref([])
const listQuery = reactive({
  status: '待审核'
})

// 当前操作记录
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
const fetchData = async () => {
  try {
    const params = {}
    if (listQuery.status) params.partStatus = listQuery.status

    const response = await axios.get(apiUrl, { params })
    list.value = response.data
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  }
}

// 点击行显示审核对话框
const handleRowClick = (row) => {
  currentRecord.value = { ...row }
  dialogVisible.value = true
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
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(`操作失败: ${error.response?.data?.message || error.message}`)
  }
}

// 初始化加载数据
fetchData()
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.el-table {
  margin-top: 20px;
}

.el-image {
  cursor: pointer;
  border-radius: 4px;
}
</style>