<template>
  <div class="inbox-container">
    <TopNavBar />
    
    <div class="inbox-content">
      <div class="inbox-header">
        <h2>消息推送</h2>
        <span class="count">{{ msgList.length? "最近更新 " + msgList.length + " 条" : "暂无消息推送" }}</span>
      </div>

      <div v-if="msgList.length > 0" class="list-wrapper">
        <div class="msg-card"
          v-for="msg in msgList" 
          :key="msg.id"
          @click="goToDetail(msg.id)"
        >
          <div class="card-left">
            <img :src="msg.avatar" class="user-avatar" />
          </div>
          
          <div class="card-body">
            <div class="card-title">
              <span class="username">{{ msg.seller }}</span>
              <span class="action-text">发布了新宝贝</span>
              <span class="time">{{ msg.createTime }}</span>
            </div>
            
            <div class="item-preview">
              <img :src="msg.imageURL" class="item-img" />
              <div class="item-info">
                <p class="item-name">{{ msg.title }}</p>
                <p class="item-price">￥{{ msg.price }}</p>
              </div>
            </div>
          </div>
          
          <!-- <div v-if="!msg.isRead" class="unread-status"></div> -->
        </div>
      </div>

      <div v-else class="empty-state">
        <p>你关注的人还没发新宝贝，去广场逛逛吧</p>
      </div>
    </div>
  </div>
</template>

<script>
import TopNavBar from "@/components/TopNavBar.vue";
import request from "@/utils/request";
// import { ref, toRaw } from "vue";

export default {
  name: "InboxPrompt",
  components: { TopNavBar },
  data() {
    return {
      // 1. 统统去掉 ref 和 toRaw，回归最纯粹的初始值
      maxTime: Date.now(), 
      offset: 0,
      msgList: [] // 2. 明确声明这是一个数组！
    };
  },
  methods: {
    goToDetail(itemId) {
      this.$router.push({
        name: "ItemInfo",
        params: { id: itemId }
      });
    },
    
    getMsgsById() {
      console.log("正在请求数据，游标 maxTime:", this.maxTime, "offset:", this.offset);
      
      request.post("/inbox", null, {
        params: {
          max: this.maxTime,
          offset: this.offset
        }
      }).then(res => {
        // 假设拦截器已经帮你解构了外层的 Result，这里的 res 直接就是 ScrollResult 对象
        const dataList = res.list || [];
        
        if (dataList.length === 0) {
          console.log("没有更多数据啦");
          return;
        }

        // 3. 【核心修复】直接使用扩展运算符，将新数据追加到旧数组后面
        this.msgList.push(...dataList);

        // 4. 更新游标，为请求下一页做准备
        this.maxTime = res.minTime;
        this.offset = res.offset;
        
        console.log("当前列表总数：", this.msgList.length);
      }).catch(err => {
        console.error("获取推送失败:", err);
      });
    }
  },
  mounted() {
    // 页面刚加载时，自动拉取第一页数据
    this.getMsgsById();
  }
};
</script>

<style scoped>

.inbox-content {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 15px;
  text-align: left;
}

.inbox-header {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 20px;
}

.inbox-header h2 { font-size: 24px; color: #333; }
.count { color: #999; font-size: 14px; }

.list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 消息卡片样式 */
.msg-card {
  display: flex;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.msg-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-left { margin-right: 15px; }
.user-avatar { width: 45px; height: 45px; border-radius: 50%; border: 1px solid #eee; }

.card-body { flex: 1; }

.card-title { margin-bottom: 12px; font-size: 14px; }
.username { font-weight: 600; color: #333; margin-right: 8px; }
.action-text { color: #666; }
.time { float: right; color: #bbb; font-size: 12px; }

/* 内部商品预览区 */
.item-preview {
  display: flex;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  align-items: center;
}

.item-img { width: 60px; height: 60px; border-radius: 4px; object-fit: cover; margin-right: 12px; }
.item-name { font-size: 14px; color: #333; margin-bottom: 5px; font-weight: 500; }
.item-price { color: #ff4d4f; font-weight: bold; }

.unread-status {
  width: 10px;
  height: 10px;
  background: #409eff;
  border-radius: 50%;
  position: absolute;
  left: 8px;
  top: 8px;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: #999;
}
</style>