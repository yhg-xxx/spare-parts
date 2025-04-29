<template>
  <div class="return-factory-management">
    <!-- 新增返厂单按钮 -->
    <el-button @click="openAddReturnDialog" type="primary" :icon="Plus">新增返厂单</el-button>

    <!-- 返厂记录列表 -->
    <!-- 返厂记录列表 -->
    <el-table
        :data="returnList"
        stripe
        style="width: 100%"
        v-loading="loading"
        @row-click="showDetail">  <!-- 添加行点击事件 -->
      <el-table-column prop="returnId" label="返厂单号" width="120"></el-table-column>
      <el-table-column prop="sn" label="SN号" width="180"></el-table-column>
      <el-table-column prop="partName" label="备件名称"></el-table-column>
      <el-table-column prop="partModel" label="备件型号"></el-table-column>
      <el-table-column prop="manufacturerName" label="生产厂家"></el-table-column>
      <el-table-column prop="returnReason" label="返厂原因"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.status || '待返厂' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sentTime" label="发出时间" width="160">
        <template #default="{row}">
          {{ formatDate(row.sentTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="expectedReturnTime" label="预计返回" width="160">
        <template #default="{row}">
          {{ formatDate(row.expectedReturnTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <!-- 移除详情按钮 -->
          <el-button
              size="small"
              type="primary"
              @click.stop="showLogisticsDialog(row)"
              v-if="row.status === '待返厂'">
            填写物流
          </el-button>

          <el-button
              size="small"
              type="success"
              @click.stop="showResultDialog(row)"
              v-if="(row.status === '已返厂' || row.status === '维修中')">
            处理结果
          </el-button>

          <el-button
              size="small"
              type="warning"
              @click.stop="confirmRepair(row.returnId)"
              v-if="row.status === '已返回' && row.repairResult === '修复成功'">
            验收
          </el-button>

          <el-button
              size="small"
              type="danger"
              @click.stop="scrapPart(row.returnId)"
              v-if="row.status === '已返回' && row.repairResult === '修复失败'">
            报废
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
          layout="total, prev, pager, next, jumper"
      />
    </div>

    <!-- 新增返厂单对话框 -->
    <el-dialog v-model="addReturnDialogVisible" title="新增返厂单" width="50%">
      <el-form :model="addReturnForm" label-width="120px">
        <el-form-item label="故障工单" required>
          <el-select
              v-model="addReturnForm.faultId"
              placeholder="请选择故障工单"
              filterable
              remote
              :remote-method="searchFaultOrders"
              :loading="faultOrderLoading">
            <el-option
                v-for="item in faultOrderOptions"
                :key="item.faultId"
                :label="`${item.faultId} - ${item.sn} - ${item.faultDescription}`"
                :value="item.faultId">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="备件信息" v-if="selectedFaultOrder">
          <div class="part-info">
            <p>SN号: {{ selectedFaultOrder.sn || '--' }}</p>
            <p>备件名称: {{ selectedPart?.partName || '--' }}</p>
            <p>备件型号: {{ selectedPart?.partModel || '--' }}</p>
            <p>生产厂家: {{ selectedPart?.manufacturerName || '--' }}</p>
          </div>
        </el-form-item>

        <el-form-item label="返厂原因" required>
          <el-input
              v-model="addReturnForm.returnReason"
              type="textarea"
              :rows="3"
              placeholder="请输入返厂原因"></el-input>
        </el-form-item>

        <el-form-item label="预计维修天数" required>
          <el-input-number
              v-model="addReturnForm.expectedRepairDays"
              :min="1"
              :max="90"></el-input-number>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addReturnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddReturnForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 填写物流信息对话框 -->
    <el-dialog v-model="logisticsDialogVisible" title="填写物流信息" width="50%">
      <el-form :model="logisticsForm" label-width="120px">
        <el-form-item label="物流公司" required>
          <el-input v-model="logisticsForm.logisticsCompany" placeholder="请输入物流公司"></el-input>
        </el-form-item>

        <el-form-item label="物流单号" required>
          <el-input v-model="logisticsForm.logisticsNumber" placeholder="请输入物流单号"></el-input>
        </el-form-item>

        <el-form-item label="发出时间" required>
          <el-date-picker
              v-model="logisticsForm.sentTime"
              type="datetime"
              placeholder="选择发出时间">
          </el-date-picker>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="logisticsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLogisticsForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 处理返厂结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="处理返厂结果" width="50%">
      <el-form :model="resultForm" label-width="120px">
        <el-form-item label="维修结果" required>
          <el-radio-group v-model="resultForm.repairResult">
            <el-radio label="修复成功">修复成功</el-radio>
            <el-radio label="修复失败">修复失败</el-radio>
            <el-radio label="未修复">未修复</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="维修描述">
          <el-input
              v-model="resultForm.repairDescription"
              type="textarea"
              :rows="3"
              placeholder="请输入维修描述"></el-input>
        </el-form-item>

        <el-form-item label="实际返回时间" required>
          <el-date-picker
              v-model="resultForm.actualReturnTime"
              type="datetime"
              placeholder="选择返回时间">
          </el-date-picker>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResultForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 返厂单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="返厂单详情" width="60%">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="返厂单号">{{ currentDetail.returnId || '--' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDetail.status)">
            {{ currentDetail.status || '待返厂' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备件SN">{{ currentDetail.sn || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备件名称">{{ currentDetail.partName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备件型号">{{ currentDetail.partModel || '--' }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ currentDetail.manufacturerName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="返厂原因" :span="2">{{ currentDetail.returnReason || '--' }}</el-descriptions-item>
        <el-descriptions-item label="预计维修天数">{{ currentDetail.expectedRepairDays || '0' }}天</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ currentDetail.createdBy || '--' }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ currentDetail.logisticsCompany || '--' }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentDetail.logisticsNumber || '--' }}</el-descriptions-item>
        <el-descriptions-item label="发出时间">{{ formatDate(currentDetail.sentTime) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="预计返回时间">{{ formatDate(currentDetail.expectedReturnTime) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="实际返回时间">{{ formatDate(currentDetail.actualReturnTime) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="维修结果">{{ currentDetail.repairResult || '--' }}</el-descriptions-item>
        <el-descriptions-item label="维修描述" :span="2">{{ currentDetail.repairDescription || '--' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentDetail.createdAt) || '--' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentDetail.updatedAt) || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios' // 添加axios导入

// 搜索表单
const searchForm = reactive({
  sn: '',
  partName: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const returnList = ref([])
const loading = ref(false)

// 对话框控制
const addReturnDialogVisible = ref(false)
const logisticsDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const detailDialogVisible = ref(false)

// 表单数据
const addReturnForm = reactive({
  faultId: null,
  returnReason: '',
  expectedRepairDays: 15
})

const logisticsForm = reactive({
  logisticsCompany: '',
  logisticsNumber: '',
  sentTime: new Date()
})

const resultForm = reactive({
  repairResult: '修复成功',
  repairDescription: '',
  actualReturnTime: new Date()
})

// 当前操作的行数据
const currentRow = ref(null)
const currentDetail = ref(null)

// 故障工单选项
const faultOrderOptions = ref([])
const faultOrderLoading = ref(false)
const selectedFaultOrder = ref(null)
const selectedPart = ref(null)

// 假设当前用户信息
const currentUser = ref({
  userId: 'admin',
  userName: '管理员'
})

// 初始化加载数据
onMounted(() => {
  fetchData()
})

// 获取表格数据 - 修改后的版本
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      page: pagination.current - 1,
      size: pagination.size
    }

    const res = await axios.get('/api/return-factory', {params})
    console.log('API response:', res)

    // 检查响应是否有效
    if (!res || !res.data) {
      throw new Error('API返回数据为空')
    }

    // 处理不同API返回结构
    let data = []
    let total = 0

    // 如果返回的是数组
    if (Array.isArray(res.data)) {
      data = res.data
      total = res.data.length
    }
    // 如果返回的是分页对象
    else if (res.data.content) {
      data = res.data.content
      total = res.data.totalElements || res.data.total || 0
    }
    // 其他情况
    else {
      data = [res.data]
      total = 1
    }

    // 映射数据到表格
    returnList.value = data.map(item => ({
      returnId: item.returnId || item.id || '--',
      sn: item.sn || '--',
      partName: item.partName || item.part?.name || '--',
      partModel: item.partModel || item.part?.model || '--',
      manufacturerName: item.manufacturerName || item.part?.manufacturerName || '--',
      returnReason: item.returnReason || '--',
      status: item.status || '待返厂',
      sentTime: item.sentTime || null,
      expectedReturnTime: item.expectedReturnTime || null,
      actualReturnTime: item.actualReturnTime || null,
      repairResult: item.repairResult || '--',
      repairDescription: item.repairDescription || '--',
      logisticsCompany: item.logisticsCompany || '--',
      logisticsNumber: item.logisticsNumber || '--',
      createdBy: item.createdBy || item.creator?.name || '--',
      createdAt: item.createdAt || null,
      updatedAt: item.updatedAt || null
    }))

    pagination.total = total

  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error(`获取数据失败: ${error.message || '未知错误'}`)
    returnList.value = [] // 确保表格有默认空数组
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索故障工单
const searchFaultOrders = async (query) => {
  if (!query) {
    faultOrderOptions.value = []
    return
  }

  try {
    faultOrderLoading.value = true
    const res = await axios.get('/api/fault-orders', {
      params: {query, status: '待处理'}
    })

    // 确保返回的是数组
    faultOrderOptions.value = Array.isArray(res?.data) ? res.data : []

  } catch (error) {
    console.error('搜索故障工单失败:', error)
    ElMessage.error('搜索故障工单失败')
    faultOrderOptions.value = []
  } finally {
    faultOrderLoading.value = false
  }
}

// 选择故障工单
const handleFaultOrderSelect = async (faultId) => {
  if (!faultId) {
    selectedFaultOrder.value = null
    selectedPart.value = null
    return
  }

  try {
    const res = await axios.get(`/api/fault-orders/${faultId}`)

    // 确保有返回数据
    if (!res?.data) {
      throw new Error('未获取到工单详情')
    }

    selectedFaultOrder.value = res.data

    // 获取备件信息
    const partId = res.data.partId || res.data.part?.id
    if (partId) {
      const partRes = await axios.get(`/api/spare-parts/${partId}`)
      selectedPart.value = partRes?.data || null
    } else {
      selectedPart.value = null
    }

  } catch (error) {
    console.error('获取故障工单详情失败:', error)
    ElMessage.error('获取故障工单详情失败')
    selectedFaultOrder.value = null
    selectedPart.value = null
  }
}

// 打开新增返厂单对话框
const openAddReturnDialog = () => {
  addReturnDialogVisible.value = true
  addReturnForm.faultId = null
  addReturnForm.returnReason = ''
  addReturnForm.expectedRepairDays = 15
  selectedFaultOrder.value = null
  selectedPart.value = null
}

// 提交新增返厂单
const submitAddReturnForm = async () => {
  if (!addReturnForm.faultId) {
    ElMessage.warning('请选择故障工单')
    return
  }

  if (!addReturnForm.returnReason) {
    ElMessage.warning('请输入返厂原因')
    return
  }

  try {
    const payload = {
      faultId: addReturnForm.faultId,
      returnReason: addReturnForm.returnReason,
      expectedRepairDays: addReturnForm.expectedRepairDays,
      createdBy: currentUser.value.userId
    }

    const res = await axios.post('/api/return-factory', payload)

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    ElMessage.success('创建返厂单成功')
    addReturnDialogVisible.value = false
    fetchData()

  } catch (error) {
    console.error('创建返厂单失败:', error)
    ElMessage.error(`创建返厂单失败: ${error.message || '未知错误'}`)
  }
}

// 显示物流对话框
const showLogisticsDialog = (row) => {
  currentRow.value = row
  logisticsForm.logisticsCompany = ''
  logisticsForm.logisticsNumber = ''
  logisticsForm.sentTime = new Date()
  logisticsDialogVisible.value = true
}

// 提交物流表单
const submitLogisticsForm = async () => {
  if (!logisticsForm.logisticsCompany) {
    ElMessage.warning('请输入物流公司')
    return
  }

  if (!logisticsForm.logisticsNumber) {
    ElMessage.warning('请输入物流单号')
    return
  }

  try {
    const params = new URLSearchParams()
    params.append('logisticsCompany', logisticsForm.logisticsCompany)
    params.append('logisticsNumber', logisticsForm.logisticsNumber)
    params.append('sentTime', logisticsForm.sentTime.toISOString())

    const res = await axios.put(
        `/api/return-factory/${currentRow.value.returnId}/logistics`,
        params,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
    )

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    ElMessage.success('更新物流信息成功')
    logisticsDialogVisible.value = false
    fetchData()

  } catch (error) {
    console.error('更新物流信息失败:', error)
    ElMessage.error(`更新物流信息失败: ${error.message || '未知错误'}`)
  }
}

// 显示结果对话框
const showResultDialog = (row) => {
  currentRow.value = row
  resultForm.repairResult = '修复成功'
  resultForm.repairDescription = ''
  resultForm.actualReturnTime = new Date()
  resultDialogVisible.value = true
}

// 提交结果表单
const submitResultForm = async () => {
  if (!resultForm.repairResult) {
    ElMessage.warning('请选择维修结果')
    return
  }

  try {
    const params = new URLSearchParams()
    params.append('repairResult', resultForm.repairResult)
    params.append('repairDescription', resultForm.repairDescription)
    params.append('actualReturnTime', resultForm.actualReturnTime.toISOString())

    const res = await axios.put(
        `/api/return-factory/${currentRow.value.returnId}/result`,
        params,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
    )

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    ElMessage.success('处理返厂结果成功')
    resultDialogVisible.value = false
    fetchData()

  } catch (error) {
    console.error('处理返厂结果失败:', error)
    ElMessage.error(`处理返厂结果失败: ${error.response?.data?.message || error.message || '未知错误'}`)
  }
}

// 验收
// 在 setup 中



const confirmRepair = async (returnId) => {
  try {
    await ElMessageBox.confirm('确认验收该备件?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 确保userId是数字类型
    const userId = parseInt(currentUser.value.userId) || 0

    // 使用URLSearchParams并确保值为数字
    const params = new URLSearchParams()
    params.append('userId', userId.toString()) // 明确转换为字符串

    const res = await axios.put(
        `/api/return-factory/${returnId}/confirm`,
        params,
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
    )

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    ElMessage.success('验收成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完整错误:', error.response)
      const errorMsg = error.response?.data?.message ||
          error.message ||
          '未知错误'
      ElMessage.error(`验收失败: ${errorMsg}`)
    }
  }
}

// 报废
const scrapPart = async (returnId) => {
  try {
    await ElMessageBox.confirm('确认报废该备件?', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    const res = await axios.put(`/api/return-factory/${returnId}/scrap`, {
      userId: currentUser.value.userId
    })

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    ElMessage.success('报废成功')
    fetchData()

  } catch (error) {
    if (error !== 'cancel') {
      console.error('报废失败:', error)
      ElMessage.error(`报废失败: ${error.message || '未知错误'}`)
    }
  }
}

// 显示详情
const showDetail = async (row) => {
  try {
    const res = await axios.get(`/api/return-factory/${row.returnId}`)

    if (!res?.data) {
      throw new Error('API返回数据为空')
    }

    currentDetail.value = {
      returnId: res.data.returnId || res.data.id || '--',
      sn: res.data.sn || '--',
      partName: res.data.partName || res.data.part?.name || '--',
      partModel: res.data.partModel || res.data.part?.model || '--',
      manufacturerName: res.data.manufacturerName || res.data.part?.manufacturerName || '--',
      returnReason: res.data.returnReason || '--',
      status: res.data.status || '待返厂',
      sentTime: res.data.sentTime || null,
      expectedReturnTime: res.data.expectedReturnTime || null,
      actualReturnTime: res.data.actualReturnTime || null,
      repairResult: res.data.repairResult || '--',
      repairDescription: res.data.repairDescription || '--',
      logisticsCompany: res.data.logisticsCompany || '--',
      logisticsNumber: res.data.logisticsNumber || '--',
      createdBy: res.data.createdBy || res.data.creator?.name || '--',
      createdAt: res.data.createdAt || null,
      updatedAt: res.data.updatedAt || null,
      expectedRepairDays: res.data.expectedRepairDays || 0
    }

    detailDialogVisible.value = true

  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error(`获取详情失败: ${error.message || '未知错误'}`)
    detailDialogVisible.value = false
  }
}

// 状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待返厂':
      return 'warning'
    case '已返厂':
      return 'primary'
    case '维修中':
      return ''
    case '已返回':
      return 'success'
    case '已验收':
      return 'success'
    case '已报废':
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '--'
  try {
    const date = new Date(dateStr)
    return isNaN(date.getTime()) ? '--' : date.toLocaleDateString()
  } catch (e) {
    return '--'
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '--'
  try {
    const date = new Date(dateStr)
    return isNaN(date.getTime()) ? '--' : date.toLocaleString()
  } catch (e) {
    return '--'
  }
}

// 分页变化
const handlePageChange = () => {
  fetchData()
}
</script>

<style scoped>
.return-factory-management {
  padding: 20px;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.part-info {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>