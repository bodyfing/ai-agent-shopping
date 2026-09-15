<template>
  <div class="login-container">
    <div v-if="!isCodeLogin && isChange" class="login-box">
      <img src="../assets/GooShare.png" alt="logo" class="logo-img" />
      <h2>欢迎登录校园二手交易平台</h2>
      <input type="text" v-model="user.username" placeholder="请输入用户名" />
      <input type="password" v-model="user.password" placeholder="请输入密码" />
      <div class="login-type">
        <a @click="isCodeLogin = false" class="type-password">密码登录</a>
        <a @click="isCodeLogin = true" class="type-code">验证码登录</a>
      </div>
      <button @click="handleLoginByUserName">登 录</button>
    </div>
    <div v-else class="login-box">
      <img src="../assets/GooShare.png" alt="logo" class="logo-img" />
      <h2>欢迎登录校园二手交易平台</h2>
      <input type="text" v-model="phone" placeholder="请输入手机号" />
      <div class="code-login">
        <input type="text" v-model="code" placeholder="请输入验证码" />
        <button
          :class="{'disabled-btn':isCounting && phone != null}"
          :disabled="isCounting && phone != null"
          @click="getCode"
        >{{ isCounting ? countdown + 's' : '获取验证码' }}</button>
      </div>
      <div class="login-type">
        <a @click="isCodeLogin = false; isChange = true" class="type-password">密码登录</a>
        <a @click="isCodeLogin = true" class="type-code">验证码登录</a>
      </div>
      <button @click="handleLoginByCode">登 录</button>
    </div>
  </div>
</template>
<script>
import request from "@/utils/request";

export default {
  name: "LoginPage",
  data() {
    return {
      user: {
        username: "",
        password: ""
      },
      phone: "",
      code: "",
      isCodeLogin: false,
      isCounting: false, //是否正在倒计时
      isChange: false,
      countdown: 60
    };
  },
  methods: {
    handleLoginByUserName() {
      if (this.user.username === "" || this.user.password === "") {
        alert("请输入用户名和密码");
        return;
      }

      request
        .post("/user/loginByPassword", this.user)
        .then(res => {
          // --- 修改点 1：兼容处理数据解析 ---
          // 如果 res 已经是对象，就不执行 JSON.parse，否则会报错进入 catch
          const userData = typeof res === "string" ? JSON.parse(res) : res;

          if (!userData || !userData.token) {
            alert("登录返回数据异常");
            return;
          }

          // --- 修改点 2：存储逻辑 ---
          localStorage.setItem("token", userData.token);

          // 注意：存入 localStorage 没问题，报错是因为你在拦截器里把这个读出来塞进 Header 了
          localStorage.setItem(
            "userInfo",
            JSON.stringify({
              username: userData.username,
              userId: userData.id
            })
          );

          console.log("登录成功", userData);
          alert("登录成功！");
          this.$router.push("/");
        })
        .catch(err => {
          // 这里的 err 就是你看到的那个 setRequestHeader 错误
          console.error("登录逻辑异常:", err);
          alert("登录失败，请检查账号密码或联系管理员");
        });
    },
    getCode() {
      if (this.isCounting) {
        return;
      }
      if (this.phone == "") {
        alert("请输入手机号");
        this.isCodeLogin = false;
        this.isChange = false;
        return;
      }
      this.isCounting = true;
      this.countdown = 60;
      let timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(timer);
          this.isCounting = false;
          this.countdown = 60;
        }
      }, 1000);
      request
        .post("/user/getCode?phone=" + this.phone) // 发送手机号给后端
        .then(res => {
          console.log("获取验证码成功", res);
          alert("验证码已发送至手机，有效期5分钟");
        })
        .catch(err => {
          console.error("获取验证码失败:", err);
          console.log(this.phone);
          alert(err);
        });
    },
    handleLoginByCode() {
      if (this.phone == "" || this.code == "") {
        alert("请输入手机号和验证码");
        this.isCodeLogin = true;
        return;
      }
      request
        .post("/user/loginByCode?phone=" + this.phone + "&code=" + this.code) // 获取手机号和验证码
        .then(res => {
          console.log("登录成功", res);
          const userData = JSON.parse(res); // 解析字符串为对象
          localStorage.setItem("token", userData.token);
          console.log("登录成功", userData);
          alert("登录成功！\n欢迎 " + userData.username + "！");
          localStorage.setItem(
            "userInfo",
            JSON.stringify({
              username: userData.username
            })
          );
          this.$router.push("/");
        })
        .catch(err => {
          console.error("登录请求失败:", err);
          alert("登录请求失败，请稍后重试");
        });
    },
    
  }
};
</script>

<style scoped>
/* 简单的居中样式 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.logo-img {
  width: 100px;
  height: auto;
  align-self: center;
}
.login-box {
  width: 350px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 15px;
}
input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.login-type {
  display: flex;
  justify-content: center;
  gap: 30px;
  text-decoration: underline;
  cursor: pointer;
  font-size: 14px;
}
.code-login {
  width: 100%;
  display: flex;
  gap: 15px;
}
.code-login input {
  flex: 1; /* 核心：让输入框自动伸缩，填满剩余空间 */
  min-width: 0; /* 防止 input 内容过长撑破布局 */
}
.code-login button {
  width: 100px; /* 给获取验证码按钮一个固定宽度 */
  padding: 0 5px; /* 减小内边距，防止文字溢出 */
  white-space: nowrap; /* 确保按钮文字不换行 */
}
.type-password:hover {
  color: #409eff;
}
.type-code:hover {
  color: #409eff;
}
button {
  padding: 10px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.disabled-btn {
  background-color: #a0cfff !important; /* 浅蓝色或灰色 */
  cursor: not-allowed;
}
</style>
