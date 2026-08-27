<template>
  <div class="inbox-container">
    <TopNavBar />
    <div class="inbox-layout">
      <div class="message-list">
        <div class="list-header">
          <h3>全部消息</h3>
        </div>
        <div 
          v-for="chat in chatList" 
          :key="chat.id" 
          :class="['chat-item', { active: currentChatId === chat.id }]"
          @click="selectChat(chat)"
        >
          <img :avatar="chat.avatar" src="https://via.placeholder.com/40" class="avatar" />
          <div class="chat-info">
            <div class="info-top">
              <span class="username">{{ chat.username }}</span>
              <span class="time">{{ chat.lastTime }}</span>
            </div>
            <p class="last-msg">{{ chat.lastMsg }}</p>
          </div>
          <div v-if="chat.unread" class="unread-dot"></div>
        </div>
      </div>

      <div class="chat-window" v-if="currentChat">
        <div class="chat-header">
          <span class="current-user">{{ currentChat.username }}</span>
          <span class="item-tag">关于商品：{{ currentChat.itemName }}</span>
        </div>
        
        <div class="chat-content" ref="chatBox">
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            :class="['msg-bubble', msg.self ? 'self' : 'other']"
          >
            <div class="text">{{ msg.content }}</div>
          </div>
        </div>

        <div class="chat-input-area">
          <textarea v-model="inputMsg" placeholder="询问商品细节，或约定校内面交地点..." @keyup.enter="sendMsg"></textarea>
          <button class="send-btn" @click="sendMsg">发送</button>
        </div>
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">✉️</div>
        <p>点击左侧联系人开始沟通吧</p>
      </div>
    </div>
  </div>
</template>

<script>
import TopNavBar from "@/components/TopNavBar.vue";

export default {
  name: "MyInbx",
  components: { TopNavBar: TopNavBar },
  data() {
    return {
      currentChatId: null,
      inputMsg: "",
      // 模拟列表数据
      chatList: [
        { id: 1, username: "张三学长", lastMsg: "同学，书还在吗？", lastTime: "10:20", unread: true, itemName: "考研数学指南" },
        { id: 2, username: "李四", lastMsg: "下午两点在食堂门口面交可以吗？", lastTime: "昨天", unread: false, itemName: "二手自行车" }
      ],
      // 模拟对话内容
      messages: [
        { content: "你好，请问这个自行车刹车灵敏吗？", self: false },
        { content: "挺灵敏的，上周刚调过。", self: true },
        { content: "下午两点在食堂门口面交可以吗？", self: false }
      ]
    };
  },
  computed: {
    currentChat() {
      return this.chatList.find(c => c.id === this.currentChatId);
    }
  },
  methods: {
    selectChat(chat) {
      this.currentChatId = chat.id;
      chat.unread = false; // 点击后消除红点
    },
    sendMsg() {
      if (!this.inputMsg.trim()) return;
      this.messages.push({
        content: this.inputMsg,
        self: true
      });
      this.inputMsg = "";
      // 发送后自动滚动到底部逻辑可加在此处
    }
  }
};
</script>

<style scoped>
.inbox-layout {
  display: flex;
  max-width: 1000px;
  margin: 20px auto;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  overflow: hidden;
}

/* 消息列表样式 */
.message-list {
  width: 300px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}
.list-header {
  padding: 20px;
  border-bottom: 1px solid #f5f5f5;
}
.chat-item {
  display: flex;
  padding: 15px;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}
.chat-item:hover { background: #f9f9f9; }
.chat-item.active { background: #eef5fe; }
.avatar { width: 45px; height: 45px; border-radius: 50%; margin-right: 12px; }
.chat-info { flex: 1; min-width: 0; }
.info-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.username { font-weight: bold; font-size: 14px; }
.time { color: #999; font-size: 12px; }
.last-msg { color: #666; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.unread-dot { width: 8px; height: 8px; background: #ff4d4f; border-radius: 50%; position: absolute; right: 15px; bottom: 20px; }

/* 聊天窗口样式 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 15px 25px;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-direction: column;
}
.item-tag { font-size: 12px; color: #409eff; margin-top: 4px; }

.chat-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fdfdfd;
}
.msg-bubble { margin-bottom: 15px; display: flex; }
.msg-bubble.self { justify-content: flex-end; }
.text { 
  max-width: 70%; 
  padding: 10px 14px; 
  border-radius: 8px; 
  font-size: 14px; 
  line-height: 1.5;
}
.other .text { background: #eee; color: #333; }
.self .text { background: #409eff; color: #fff; }

.chat-input-area {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}
textarea {
  flex: 1;
  height: 60px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px;
  resize: none;
  outline: none;
}
.send-btn {
  padding: 0 20px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}
.empty-icon { font-size: 50px; margin-bottom: 10px; }
</style>