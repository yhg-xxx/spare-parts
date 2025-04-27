<template>
  <div class="app-container">
    <!-- 操作按钮和搜索 -->
    <el-row :gutter="20" class="toolbar-container">
      <el-col :span="12">
        <el-button type="success" @click="handleCreate" :icon="Plus">添加备件</el-button>
      </el-col>
      <el-col :span="12" class="search-container">
        <el-input
            v-model="listQuery.partName"
            placeholder="请输入备件名称"
            clearable
            class="search-input"
        />
        <el-button type="primary" @click="handleFilter" :icon="Search" class="search-btn">查询</el-button>
        <el-button @click="clearSearch" class="reset-btn">清空</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        class="custom-table"
    >
      <el-table-column prop="partId" label="ID" width="80" align="center" />
      <el-table-column prop="partName" label="备件名称" min-width="120" />
      <el-table-column prop="partModel" label="型号规格" min-width="150" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{row}">
          <el-tag type="info">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sparePartStatus" label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.sparePartStatus)">{{ row.sparePartStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="locationName" label="仓库位置" min-width="150" />
      <el-table-column prop="manufacturer" label="生产厂家" min-width="150" />
      <el-table-column prop="unit" label="单位" width="100" />
      <!-- 在el-form中添加隐藏的status字段 -->
      <el-form-item label="状态" prop="status" v-show="false">
        <el-input v-model="formData.status" />
      </el-form-item>

      <!-- 操作列 -->
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button @click="handleUpdate(row)" type="warning" size="small">编辑</el-button>
          <el-button @click="handleDelete(row.partId)" type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model::page-size="listQuery.size"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        class="pagination-container"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTypeMap[dialogType]" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="备件名称" prop="partName">
          <el-input v-model="formData.partName" placeholder="请输入备件名称" />
        </el-form-item>

        <el-form-item label="型号规格" prop="partModel">
          <el-input v-model="formData.partModel" placeholder="请输入型号规格" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select
              v-model="formData.category"
              placeholder="请选择分类"
              style="width: 100%"
              clearable
          >
            <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="sparePartStatus">
          <el-select
              v-model="formData.sparePartStatus"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="类型" prop="sparePartType">
          <el-select
              v-model="formData.sparePartType"
              placeholder="请选择类型"
              style="width: 100%"
          >
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="仓库位置" prop="locationId">
          <el-select
              v-model="formData.locationId"
              placeholder="请选择仓库"
              style="width: 100%"
              clearable
          >
            <el-option
                v-for="wh in warehouseOptions"
                :key="wh.location_id"
                :label="wh.location_name"
                :value="wh.location_id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="序列号" prop="sn">
          <el-input v-model="formData.sn" placeholder="请输入序列号" />
        </el-form-item>

        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>

        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 枚举选项
const categoryOptions = [
  { value: '机械类', label: '机械类' },
  { value: '电气类', label: '电气类' },
  { value: '液压类', label: '液压类' },
  { value: '电子类', label: '电子类' },
  { value: '其他', label: '其他' }
]

const statusOptions = [
  { value: '新好件', label: '新好件' },
  { value: '修好件', label: '修好件' },
  { value: '坏件', label: '坏件' },
  { value: '二级修', label: '二级修' },
  { value: '返厂修', label: '返厂修' },
  { value: '待调拨', label: '待调拨' },
  { value: '已报废', label: '已报废' }
]

const typeOptions = [
  { value: '正常件', label: '正常件' },
  { value: '在保件', label: '在保件' },
  { value: '遗留件', label: '遗留件' }
]

// 数据管理
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('create')
const dialogTypeMap = { create: '新增备件', update: '编辑备件' }
const warehouseOptions = ref([]) // 仓库列表

// 修改查询参数对象 (src/views/SparePart.vue)
const listQuery = reactive({
  page: 1,
  size: 10, // 将limit改为size保持与后端一致
  partName: ''
})
// 表单数据
const formRef = ref(null)
const formData = reactive({
  partId: null,
  partName: '',
  partModel: '',
  category: '机械类',
  sparePartStatus: '新好件',
  sparePartType: '正常件',
  locationId: null,
  sn: '',
  manufacturer: '',
  unit: '',
  status: '在库' // 新增默认值
})

// 验证规则
const rules = {
  partName: [{ required: true, message: '必填项', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  sparePartStatus: [{ required: true, message: '请选择状态', trigger: 'change' }],
  sparePartType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  locationId: [{ required: true, message: '请选择仓库', trigger: 'change' }]
}

// 获取仓库列表
const fetchWarehouses = async () => {
  try {
    const res = await axios.get('/warehouse/x')
    warehouseOptions.value = res.data
  } catch (error) {
    ElMessage.error('仓库加载失败')
  }
}

// 在fetchData方法中添加详细错误处理
const fetchData = async () => {
  try {
    const params = {
      page: listQuery.page - 1,
      size: listQuery.size, // ✅ 统一使用size
      partName: listQuery.partName
    }
    const res = await axios.get('/spare_part', { params })
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error(`数据加载失败: ${error.response?.data?.message || error.message}`)
  }
}

// 状态标签样式
const statusTagType = (status) => {
  const map = {
    '新好件': 'success',
    '修好件': 'warning',
    '坏件': 'danger',
    '已报废': 'info'
  }
  return map[status] || ''
}

// 表单提交
const submitForm = async () => {
  try {
    await formRef.value.validate()
    const method = dialogType.value === 'create' ? 'post' : 'put'
    // 修改提交URL为 /spare_part
    const url = dialogType.value === 'create' ? '/spare_part' : `/spare_part/${formData.partId}`
// 提交时转换字段
    const submitData = {
      ...formData,
    }
    await axios[method](url, submitData)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}
// 新增每页数量变化处理
const handleSizeChange = (newSize) => {
  listQuery.size = newSize
  fetchData()
}
// 初始化加载
onMounted(() => {
  fetchData()
  fetchWarehouses()
})
// 打开新增对话框
const handleCreate = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}


// 修改编辑时的数据回填
const handleUpdate = (row) => {
  dialogType.value = 'update'
  Object.assign(formData, {
    ...row,
    locationId: row.warehouse?.locationId // 确保正确获取仓库ID
  })
  dialogVisible.value = true
}
// 修改后的删除方法
const handleDelete = async (partId) => {
  try {
    await ElMessageBox.confirm('确认删除该备件？', '警告', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await axios.delete(`/spare_part/${partId}`)

    if (response.data.success) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.warning(response.data.error || '删除失败')
    }
  } catch (error) {
    if (error.response) {
      // 处理HTTP错误状态码
      const errorMsg = error.response.data?.error || '操作失败'
      if (error.response.status === 404) {
        ElMessage.warning(errorMsg)
      } else {
        ElMessage.error(errorMsg)
      }
    } else if (!error.toString().includes('cancel')) {
      ElMessage.error('网络错误或请求被取消')
    }
  }
}
const resetForm = () => {
  formRef.value?.resetFields()
  // 重置所有字段初始值
  Object.assign(formData, {
    partId: null,
    partName: '',
    partModel: '',
    category: '机械类',
    sparePartStatus: '新好件',
    sparePartType: '正常件',
    locationId: null,
    sn: '',
    manufacturer: '',
    unit: ''
  })
}
// 分页处理
const handleCurrentChange = (newPage) => {
  listQuery.page = newPage;
  fetchData();
};

// 查询处理
const handleFilter = () => {
  listQuery.page = 1; // 重置到第一页
  fetchData();
};

// 清空搜索
const clearSearch = () => {
  listQuery.partName = '';
  handleFilter();
};
// 其他方法保持不变（handleCreate, handleUpdate, handleDelete等）
</script>
<style scoped>
.app-container {
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-container {
  margin-bottom: 16px;

  .el-col {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.search-container {
  justify-content: flex-end;

  .search-input {
    width: 240px;
  }

  .search-btn {
    margin-left: 8px;
  }
}

.data-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  :deep(.el-table__header) th {
    background-color: #f8fafc;
    font-weight: 600;
  }
}

.pagination-container {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toolbar-container {
    flex-direction: column;

    .el-col {
      width: 100%;
      justify-content: flex-start;

      &:last-child {
        flex-wrap: wrap;
        gap: 8px;
      }
    }

    .search-input {
      width: 100%;
    }

    .el-button {
      flex: 1;
    }
  }

  .data-table {
    :deep(.el-table__cell) {
      padding: 8px 0;
    }
  }
}
</style>