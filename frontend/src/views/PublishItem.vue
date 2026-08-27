<template>
  <div class="publish-page-wrapper">
    <TopNavBar />

    <div class="publish-card">
      <h2 class="form-title">发布新商品</h2>

      <div class="form-group">
        <label>商品名称</label>
        <input type="text" v-model="item.title" placeholder="请输入商品名称..." />
      </div>

      <div class="form-group">
        <label>商品价格 (¥)</label>
        <input type="number" v-model="item.price" placeholder="0.00" />
      </div>

      <div class="form-group">
        <label>商品数量</label>
        <input type="number" v-model="item.stock" placeholder="0" />
      </div>

      <div class="form-group">
        <label>商品分类</label>
        <div class="select-wrapper">
          <select v-model="item.categoryId" @change="handleCategoryChange">
            <option value disabled selected>请选择大类</option>

            <option
              v-for="category in categoryList"
              :key="category.id"
              :value="category.id"
            >{{ category.name }}</option>
          </select>

          <select v-model="item.categoryItemId">
            <option value disabled selected>请选择子类</option>
            <option v-for="sub in categoryItems" :key="sub.id" :value="sub.id">{{ sub.itemName }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>商品描述</label>
        <textarea v-model="item.description" rows="4" placeholder="详细描述一下你的宝贝吧..."></textarea>
      </div>

      <div class="form-group">
        <label>商品图片</label>
        <div class="file-input-wrapper">
          <input type="file" ref="itemImage" @change="handleFileChange($event)" />
        </div>
      </div>

      <button class="submit-btn" @click="submitPublish">立即发布</button>
    </div>
  </div>
</template>

<script>
import TopNavBar from "@/components/TopNavBar.vue";
import request from "@/utils/request";
// import { f } from "vue-router/dist/router-CWoNjPRp.mjs";

export default {
  name: "publishItem",
  components: { TopNavBar: TopNavBar },
  data() {
    return {
      item: {
        title: "",
        price: "",
        stock: "",
        categoryId: "",
        categoryItemId: "",
        description: "",
        imageURL: ""
      },
      selectedFile: null,
      categoryList: [],
      categoryItems: [],
      categoryIdFlag: "",
      imageFile: ""
    };
  },
  methods: {
    getCategoryList() {
      request
        .get("/item/category")
        .then(res => {
          this.categoryList = res;
          console.log("数据加载成功:", this.categoryList);
          console.log(this.categoryList[0].categoryItems);
        })
        .catch(err => {
          console.log(err);
        });
    },
    handleCategoryChange() {
      this.categoryItems = this.categoryList.find(
        category => category.id === this.item.categoryId
      ).categoryItems;
    },
    handleFileChange(e) {
      // 使用 e 来获取选中的文件
      const file = e.target.files[0];
      if (file) {
        this.selectedFile = file;
        this.imageFile = file;
        console.log("选中的文件：", this.imageFile);
      }
    },

    submitPublish() {
        if (!this.item.title) {
        alert("请填写商品名称");
        return;
      }
      if (!this.item.price) {
        alert("请填写商品价格");
        return;
      }
      if (!this.item.stock) {
        alert("请填写商品数量");
        return;
      }
      if (!this.item.categoryId) {
        alert("请选择商品分类");
        return;
      }
      if (!this.item.categoryItemId) {
        alert("请选择商品子类");
        return;
      }
      if (!this.item.description) {
        alert("请填写商品描述");
        return;
      }
      if (!this.selectedFile) {
        alert("请选择图片");
        return;
      }

      const formData = new FormData();
      formData.append("file", this.selectedFile);

      console.log("1. 开始上传图片...");

      // --- 第一个请求：上传图片 ---
      request
        .post("/publish/image", formData)
        .then(res => {
          // 假设后端直接返回字符串 URL，或者是 res.data.url
          this.item.imageURL = res;
          console.log("2. 图片上传成功，地址为:", this.item.imageURL);

          // --- 第二个请求：发布商品（必须写在第一个的 then 里面） ---
          console.log("3. 开始提交商品数据...");
          return request.post("/publish", this.item);
        })
        .then(res => {
          // 这个 then 处理的是 /publish 的结果
          console.log("4. 商品发布成功:", res);
          alert("发布成功！");
        })
        .catch(err => {
          // 这里的 catch 会捕获上面任何一个请求的失败
          console.error("发布过程中出错:", err);
          alert("操作失败，请检查网络");
        });

      // 注意：不要在这里写 console.log(this.item.imageURL)，因为此时图片还没传完
    }
  },
  mounted() {
    this.getCategoryList();
  }
};
</script>

<style scoped>
/* 页面背景 */
.publish-page-wrapper {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 40px;
  text-align: left;
}

/* 卡片容器 */
.publish-card {
  max-width: 600px;
  margin: 30px auto;
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.form-title {
  margin-bottom: 25px;
  color: #333;
  font-size: 1.5rem;
  text-align: center;
}

/* 表单组布局 */
.form-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.select-wrapper {
  display: flex;
  flex-direction: row;
  justify-content: space-between; /* 均匀分布 */
  gap: 15px; /* 两个选择框之间的间距 */
}

.select-wrapper select {
  flex: 1; /* 关键：让两个 select 撑满剩余空间并平分宽度 */
  width: 0; /* 防止内容过长撑破布局 */
  min-width: 0;
}

/* 输入框通用样式 */
input[type="text"],
input[type="number"],
textarea,
select {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
  outline: none;
}

input:focus,
textarea:focus,
select:focus {
  border-color: #409eff;
}

/* 按钮样式 */
.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
  transition: background 0.3s ease;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:active {
  background-color: #3a8ee6;
}
</style>