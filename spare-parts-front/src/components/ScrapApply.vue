<template>
  <div class="scrap-apply-container">
    <el-card class="apply-form">
      <el-form
          ref="form"
          :model="form"
          :rules="rules"
          label-width="120px"
      >
      <h2>备件报废申请</h2>
      <el-form-item label="备件SN号" prop="sn" required>
        <el-autocomplete
            id="scrap-sn-input"
            v-model="form.sn"
            :fetch-suggestions="querySearch"
            placeholder="请输入备件序列号"
            clearable
            @select="handleSelect"
            value-key="sn"
        >
          <template #default="{ item }">
            <div>{{ item.sn }}</div>
          </template>
        </el-autocomplete>
      </el-form-item>

        <!-- 报废原因输入 -->
        <el-form-item label="报废原因" prop="scrapReason" required>
          <el-input
              id="scrap-reason-input"
              type="textarea"
              v-model="form.scrapReason"
              :rows="3"
              placeholder="请详细描述报废原因"
          ></el-input>
        </el-form-item>

        <!-- 照片上传 -->
        <el-form-item label="现场照片" prop="damagePhoto">
          <el-upload
              id="scrap-photo-upload"
              action="#"
              :auto-upload="false"
              :on-change="handlePhotoChange"
              :show-file-list="false"
              :class="{ 'is-invalid': !photoFile }"
          >
            <el-button id="scrap-upload-btn" size="small" type="primary">点击上传</el-button>
            <span v-if="photoFile" class="photo-name">
              {{ photoFile.name }}
              <el-icon @click.stop="removePhoto"><Close /></el-icon>
            </span>
          </el-upload>

        </el-form-item>

        <el-form-item>
          <el-button
              id="scrap-submit-btn"
              type="primary"
              @click="submitForm"
              :loading="isSubmitting"
          >
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 申请记录表格 -->
    <el-card class="record-list mt-4" style="width: 100%">
      <h3>我的报废申请记录</h3>
      <el-table :data="records" style="width: 100%" ref="scrapTable">
        <el-table-column prop="orderId" label="报废单号" width="180" />
        <el-table-column prop="sn" label="SN号" width="120" />
        <el-table-column prop="scrapReason" label="报废原因" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.partStatus)">
              {{ row.partStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="照片" width="120">
          <template #default="{ row }">
            <el-image
                :src="getImageUrl(row.damagePhoto)"
                @error="handleImageError"
                :preview-src-list="[getImageUrl(row.damagePhoto)]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

  </div>

</template>

<script>
import axios from 'axios';
import { ElMessage, ElLoading } from 'element-plus';
import {Close, Picture} from '@element-plus/icons-vue';

export default {
  name: 'ScrapApply',
  components: {Close},
  data() {
    // 自定义照片验证规则
    const validatePhoto = (rule, value, callback) => {
      if (!this.photoFile) {
        callback(new Error('必须上传现场照片'));
      } else {
        callback();
      }
    };


    return {
      form: {
        sn: '',
        scrapReason: '',

      },
      rules: {
        sn: [
          { required: true, message: '请输入备件SN号', trigger: 'blur' }
        ],
        scrapReason: [
          { required: true, message: '请输入报废原因', trigger: 'blur' }
        ],
        damagePhoto: [
          { validator: validatePhoto, trigger: 'change' }
        ]
      },
      photoFile: null,
      isSubmitting: false,
      currentUser: null,
      records: [],
      loading: false,
      snOptions: [], // 存储所有SN号的缓存
      loadingSn: false


    };
  },
  async mounted() {
    // 获取当前登录用户
    await this.initializeUser();

    if (this.currentUser) {
      await this.fetchRecords();
    }
  },
  methods: {

    async querySearch(queryString, cb) {
      try {
        const response = await axios.get('/spare_part/sn-search', {
          params: { keyword: queryString }
        });
        const results = response.data.map(sn => ({ value: sn, sn: sn }));
        cb(results);
      } catch (error) {
        console.error('搜索SN失败:', error);
        cb([]);
      }
    },
    // 处理选中事件
    handleSelect(item) {
      this.form.sn = item.sn;
    },

    getImageUrl(url) {
      if (!url) return '';
      // 添加时间戳解决缓存问题
      const separator = url.includes('?') ? '&' : '?';
      return `${url}${separator}t=${new Date().getTime()}`;
    },

    // 新增方法：获取申请记录
    async fetchRecords() {
      if (!this.currentUser) return;

      this.loading = true;
      try {
        const response = await axios.get('/scrapRecord/scrap-records/my-records', {
          params: {
            applicantId: this.currentUser.user_id
          }

        });
        this.records = response.data;
        // 简化布局调整
        this.$nextTick(() => {
          this.adjustTableLayout();
          console.log('Records updated:', this.records);
        });
      } catch (error) {
        ElMessage.error('获取记录失败: ' + (error.response?.data?.message || error.message));
      } finally {
        this.loading = false;
      }
    },handleImageError(e) {
      console.error('Image load failed:', e);
      this.$message.warning('照片加载失败，请检查文件是否存在');
    },

    // 修改 adjustTableLayout 方法
    adjustTableLayout() {
      const table = this.$refs.scrapTable;
      if (!table) {
        console.warn('Table ref not found');
        return;
      }

      // 使用Element Plus内置方法
      this.$nextTick(() => {
        table.doLayout();  // 触发表格重新布局
        console.log('Table layout updated');
      });
    },
    // 状态标签样式
    statusTag(status) {
      return status === '已报废' ? 'success' : 'warning';
    },

    // 时间格式化
    formatTime(timeStr) {
      return new Date(timeStr).toLocaleString('zh-CN');
    },
    // 修改图片路径处理方法
    // 修改图片路径处理方法（添加调试）


    // 处理文件选择
    handlePhotoChange(file) {
      this.photoFile = file.raw;
    },// 新增独立用户初始化方法
    async initializeUser() {
      const user = JSON.parse(sessionStorage.getItem('user'));
      if (!user) {
        ElMessage.error('请先登录');
        await this.$router.push('/login'); // 保持路由跳转一致性
        return;
      }
      this.currentUser = user;
    },

    // 提交表单
    async submitForm() {
      if (!this.currentUser) return;

      try {
        // 先进行表单验证
        await this.$refs.form.validate();

        if (!this.photoFile) {
          this.$message.error('请上传现场照片');
          return;
        }
        this.isSubmitting = true;
        const formData = new FormData();
        formData.append('sn', this.form.sn);
        formData.append('scrapReason', this.form.scrapReason);
        formData.append('applicantId', this.currentUser.user_id);

        if (this.photoFile) {
          formData.append('damagePhoto', this.photoFile);
        }

        const response = await axios.post('/scrapRecord/scrap-records', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        this.$message.success('申请提交成功！');
        this.resetForm();
      } catch (error) {
        // 修复后的错误处理
        const errorMsg = error.response?.data?.message || error.message;
        this.$message.error(`提交失败: ${errorMsg}`);
      } finally {
        this.isSubmitting = false;
      }
    },

    removePhoto() {
      this.photoFile = null;
      this.$refs.form.validateField('damagePhoto');
    },

    // 重置表单
    resetForm() {
      this.form = {
        sn: '',
        scrapReason: ''
      };
      this.photoFile = null;
    }
  }
};

</script>

<style scoped>
.scrap-apply-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.apply-form {
  margin-bottom: 30px;
}

.photo-name {
  margin-left: 10px;
  color: #666;
}
.is-invalid {
  :deep(.el-upload) {
    border: 1px solid #f56c6c;
    border-radius: 6px;
  }
}

.photo-name {
  display: flex;
  align-items: center;
  margin-left: 10px;

  .el-icon {
    margin-left: 5px;
    color: #999;
    cursor: pointer;

    &:hover {
      color: #f56c6c;
    }
  }
}

.el-form-item__error {
  margin-top: 5px;
}
</style>