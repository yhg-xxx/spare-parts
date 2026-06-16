<template>
  <div class="login-container">
    <div class="login-header">
      <h1 class="system-title">备件管理系统</h1>
      <div class="system-subtitle">智能化备件仓储解决方案</div>
    </div>

    <div class="login-card">
      <div class="form-title">用户登录</div>

      <el-form class="login-form">
        <el-form-item>
          <el-input
              id="login-username"
              v-model="form.username"
              placeholder="工号/用户名"
              class="custom-input"
              :prefix-icon="User"
              size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-input
              id="login-password"
              v-model="form.password"
              type="password"
              placeholder="登录密码"
              class="custom-input"
              :prefix-icon="Lock"
              size="large"
              show-password
          />
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="form.remember" label="保持登录状态" />
          <el-link type="primary" :underline="false" @click="router.push('/forget')">
            忘记密码?
          </el-link>
        </div>

        <el-button
            id="login-submit-btn"
            class="login-btn"
            type="primary"
            size="large"
            @click="login"
        >
          立即登录
        </el-button>
      </el-form>

      <div class="register-guide">
        <span class="divider-text">新员工接入</span>
        <el-button
            class="register-btn"
            type="info"
            link
            @click="router.push('/register')"
        >
          申请系统权限
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 脚本部分保持原有逻辑不变，仅修改注释说明
import { reactive } from 'vue';
import axios from 'axios';
import router from "@/router.js";
import { ElMessage } from "element-plus";
import { Lock, User } from "@element-plus/icons-vue";

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const login = async () => {
  try {
    const response = await axios.post('http://localhost:8080/login', {
      name: form.username,
      password: form.password
    });

    if (response.data.name === form.username && response.data.password === form.password) {
      ElMessage.success("登录成功");
      sessionStorage.setItem('user', JSON.stringify(response.data));
      await router.push('/view');
    } else {
      ElMessage.error(response.data.message || "验证失败");
    }
  } catch (error) {
    ElMessage.error("登录请求失败，请检查网络连接");
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-header {
  text-align: center;
  margin-top: 8vh;
  margin-bottom: 5vh;

  .system-title {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    letter-spacing: 2px;
    margin-bottom: 12px;
  }

  .system-subtitle {
    font-size: 16px;
    color: #7f8c8d;
    font-weight: 300;
  }
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);

  .form-title {
    font-size: 24px;
    color: #34495e;
    margin-bottom: 32px;
    text-align: center;
    font-weight: 500;
  }
}

.custom-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    padding: 12px 16px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);

    &.is-focus {
      box-shadow: 0 0 0 2px var(--el-color-primary) inset;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 30px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 2px;
  background: linear-gradient(45deg, #409eff, #3375e0);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
  }
}

.register-guide {
  margin-top: 28px;
  text-align: center;

  .divider-text {
    color: #95a5a6;
    font-size: 14px;
    position: relative;
    padding: 0 12px;

    &::before,
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      width: 80px;
      height: 1px;
      background: #eee;
    }

    &::before { left: -90px; }
    &::after { right: -90px; }
  }

  .register-btn {
    margin-top: 18px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .login-card {
    width: 85%;
    padding: 24px;
  }

  .login-header {
    margin-top: 5vh;

    .system-title {
      font-size: 28px;
    }
  }
}
</style>