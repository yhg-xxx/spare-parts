<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: grey">在进入系统之前请先输入用户名和密码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <el-input v-model="form.username" type="text" placeholder="用户名/邮箱">
        <template #prefix>
          <el-icon><User/></el-icon>
        </template>
      </el-input>
      <el-input v-model="form.password" type="password" style="margin-top: 10px" placeholder="密码">
        <template #prefix>
          <el-icon><Lock/></el-icon>
        </template>
      </el-input>
    </div>
    <el-row style="margin-top: 5px">
      <el-col :span="12" style="text-align: left">
        <el-checkbox v-model="form.remember" label="记住我"/>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-link @click="router.push('/forget')">忘记密码？</el-link>
      </el-col>
    </el-row>
    <div style="margin-top: 40px">
      <el-button @click="login()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>


    <el-divider>
      <span style="color: grey;font-size: 13px">没有账号</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/register')" type="warning" plain>注册账号</el-button>
    </div>
  </div>
</template>

<script setup>

import { reactive} from 'vue';
import axios from 'axios';
import router from "@/router.js";
import {ElMessage} from "element-plus";
import {Lock, User} from "@element-plus/icons-vue";

const form =reactive({
  username:'',
  password:''
})
const login = async ()=> {
  const response = await axios.post('http://localhost:8080/login', {
    username: form.username,
    password: form.password
  })
  console.log(response.data)
  if (response.data.username === form.username && response.data.password === form.password) {
    console.log(response.data)
    ElMessage.success("登录成功");

    await router.push('/view')
  } else {
    ElMessage.error("用户名或密码错误");

  }

  console.log("成功")
  //储存user
  sessionStorage.setItem('user', JSON.stringify(response.data));


}
</script>