<template>
  <div class="container">
    <ItemRanking />
    <div class="input-container">
      <input type="text" placeholder="请输入关键字" class="input" v-model="keyword" @keyup.enter="handleSearch" />
      <button class="button" @click="handleSearch" @keyup.enter="handleSearch">搜 索</button>
    </div>
  </div>
</template>

<script>
// 这里不需要引入 request.js 了，因为请求交给了 ItemCard 去做
import ItemRanking from "./ItemRanking.vue";

export default {
  name: "BrandHeader",
  components: {
    ItemRanking: ItemRanking
  },
  data() {
    return {
      keyword: ""
    };
  },
  methods: {
    handleSearch() {
      // 1. 如果搜索框为空，给个提示并拦截
      if (!this.keyword.trim()) {
        alert("请输入关键字");
        return;
      }
      
      // 2. 只做路由跳转，把 keyword 作为参数传递过去
      this.$router.push({
        path: "/",
        query: {
          keyword: this.keyword
        }
      }).catch(err => {
        // 防止重复点击搜索相同的词导致 Vue Router 报错
        if (err.name !== 'NavigationDuplicated') {
          console.error(err);
        }
      });
    }
  }
};
</script>

<style>
.container {
  width: 100%;
  height: 500px;
  background-color: rgb(237, 237, 237);
  background-image: url("../assets/image.png");
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
  margin-top: 5px;
  display: flex;
  justify-content: center;
  flex-direction: row;
  align-items: center;
  gap: 150px;
}

.input-container {
  margin-top: 60px;
  position: relative;
  width: 40%;
  max-width: 600px;
  display: flex;
  align-items: center;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  border-radius: 50px;
  overflow: hidden;
}

.input {
  width: 100%;
  height: 55px;
  padding: 0 100px 0 25px;
  border: none;
  outline: none;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.95);
}

.button {
  position: absolute;
  right: 5px;
  top: 5px;
  bottom: 5px;
  width: 90px;
  border-radius: 40px;
  border: none;
  background: linear-gradient(90deg, #3a8eff, #2860d8);
  color: white;
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
  transition: all 0.3s;
}

.button:hover {
  background-color: rgb(58, 160, 255);
  color: white;
  transform: scale(1.03);
}
</style>