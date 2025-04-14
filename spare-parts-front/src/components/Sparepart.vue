<template>
  <div class="app-container">
    <!-- 搜索筛选 -->
    <div class="filter-container">
      <el-button type="success" @click="handleCreate" :icon="Plus">添加备件</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
        :data="tableData"
        border
        fit
        highlight-current-row
        style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="part_id" label="序号" width="80" align="center" />
      <el-table-column prop="part_name" label="备件名称" min-width="120" />
      <el-table-column prop="part_model" label="型号规格" min-width="150" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{row}">
          <el-tag type="info">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="备件种类" width="120">
        <template #default="{row}">
          <el-tag :type="typeTagFilter(row.type)">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="statusTagFilter(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warranty_until" label="保修截止" width="150">
        <template #default="{row}">
          {{ formatDate(row.warranty_until) }}
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="创建时间" width="160" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
              @click="handleUpdate(row)"
              type="warning"
              size="small"
          >编辑</el-button>
          <el-button
              @click="handleDelete(row.part_id)"
              type="danger"
              size="small"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        @current-change="handleCurrentChange"
        style="margin-top: 20px"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTypeMap[dialogType]" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="备件名称" prop="part_name">
          <el-input v-model="formData.part_name" placeholder="请输入备件名称" />
        </el-form-item>

        <el-form-item label="型号规格" prop="part_model">
          <el-input v-model="formData.part_model" placeholder="请输入型号规格" />
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


        <el-form-item label="备件种类" prop="type">
          <el-select v-model="formData.type" placeholder="" style="width: 100%">
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备件状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择" style="width: 100%">
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="保修截止" prop="warranty_until">
          <el-date-picker
              v-model="formData.warranty_until"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
          />
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

const categoryOptions = [
  { value: '机械', label: '机械' },
  { value: '电子', label: '电子' },
  { value: '液压', label: '液压' },
  { value: '电气', label: '电气' }
]
// 枚举配置
const typeOptions = [
  { value: '正常件', label: '正常件' },
  { value: '在保件', label: '在保件' },
  { value: '遗留件', label: '遗留件' }
]

const statusOptions =[{ value: '新好件', label: '新好件' },
  { value: '修好件', label: '修好件' },
  { value: '坏件', label: '坏件' },
  { value: '二级修', label: '二级修' },
  { value: '返厂修', label: '返厂修' },
  { value: '待调拨', label: '待调拨' },
  { value: '待报废', label: '待报废' },
  { value: '已报废', label: '已报废' }
]

// 标签样式映射
const typeTagFilter = (type) => {
  const map = { 正常件: '', 在保件: 'warning', 遗留件: 'info' }
  return map[type] || 'info'
}

const statusTagFilter = (status) => {
  const map = {
    新好件: 'success',
    修: 'warning',
    坏件: 'danger',
    二级修: '',
    返厂修: 'info',
    待调拨: '',
    待报废: 'danger',
    已报废: 'info'
  }
  return map[status] || 'info'
}

// 数据管理
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('create')
const dialogTypeMap = { create: '新增备件', update: '编辑备件' }

// 查询参数
const listQuery = reactive({
  page: 1,
  limit: 10,
  part_name: ''
})

// 表单数据
const formRef = ref(null)
const formData = reactive({
  part_id: null,
  part_name: '',
  part_model: '',
  category: '',
  type: '',
  status: '',
  warranty_until: ''
})

// 验证规则
// ✅ 正确写法（使用大括号包裹键值对）
const rules = {
  part_name: [{ required: true, message: '必填项', trigger: 'blur' }],
  part_model: [{ required: true, message: '必填项', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  type: [{ required: true, message: '必填项', trigger: 'change' }],
  status: [{ required: true, message: '必填项', trigger: 'change' }]
};

// 初始化加载数据
const fetchData = async () => {
  try {
    loading.value = true
    const res = await axios.get('/spare_part')
    tableData.value = res.data
    total.value = res.data.length
  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleFilter = () => {
  listQuery.page = 1
  fetchData()
}

// 分页变化
const handleCurrentChange = (val) => {
  listQuery.page = val
  fetchData()
}

// 打开新增对话框
const handleCreate = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const handleUpdate = (row) => {
  dialogType.value = 'update'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()

    const isCreate = dialogType.value === 'create'
    const method = isCreate ? 'post' : 'put'
    const url = isCreate ? '/spare_part' : `/spare_part/${formData.part_id}`

    await axios[method](url, formData)
    ElMessage.success(`${dialogTypeMap[dialogType.value]}成功`)
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('表单提交失败:', error)
  }
}

// 删除操作
const handleDelete = async (partId) => {
  try {
    await ElMessageBox.confirm('确认删除该备件？', '警告', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await axios.delete(`/spare_part/${partId}`, {
      withCredentials: true,
      validateStatus: (status) => status === 200
    })

    if (res.data === 1) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.warning('删除失败：备件不存在')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(`操作失败: ${error.response.data?.message || '服务器错误'}`)
    } else if (!error.response) {
      console.log('操作已取消')
    }
  }
}


// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.part_id = null
}

// 日期格式化
const formatDate = (dateStr) => {
  return dateStr || '--'
}

// 初始化加载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.filter-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.el-pagination {
  justify-content: flex-end;
  margin-top: 20px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}
</style>
