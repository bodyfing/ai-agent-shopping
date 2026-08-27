<template>
  <div class="ai-assistant" :class="{ 'is-open': isOpen }">
    <transition name="assistant-panel">
      <section v-if="isOpen" class="assistant-panel">
        <header class="panel-header">
          <div>
            <p class="eyebrow">GooShare Assistant</p>
            <h3>AI shopping guide</h3>
          </div>
          <div class="header-actions">
            <button class="ghost-btn" type="button" @click="fetchRecommend" :disabled="loading">
              推荐
            </button>
            <button class="ghost-btn" type="button" @click="clearHistory" :disabled="loading">
              清空
            </button>
            <button class="icon-btn" type="button" @click="togglePanel(false)">×</button>
          </div>
        </header>

        <div v-if="itemContext.currentItem" class="context-banner">
          <p class="context-label">当前商品</p>
          <div class="context-main">
            <strong>{{ itemContext.currentItem.title || "当前商品" }}</strong>
            <span v-if="itemContext.currentItem.price">￥{{ itemContext.currentItem.price }}</span>
          </div>
          <p class="context-summary">{{ itemContext.contextSummary }}</p>
        </div>

        <div ref="messageList" class="message-list">
          <div class="intro-card">
            <p>可以直接问我商品推荐、预算建议、分类选择，或者让助手帮你梳理购买前疑问。</p>
          </div>

          <article
            v-for="message in messages"
            :key="message.id"
            class="message"
            :class="`role-${message.role}`"
          >
            <div class="message-bubble">
              <span v-if="message.route" class="route-badge" :class="`route-${message.route}`">
                {{ routeLabel(message.route) }}
              </span>
              {{ message.content }}
              <div v-if="message.references && message.references.length" class="references">
                <p class="references-title">参考资料</p>
                <ul>
                  <li v-for="(ref, index) in message.references" :key="index">
                    <span class="ref-source">{{ ref.source }}</span>
                    <span class="ref-snippet">{{ ref.snippet }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </article>

          <section v-if="recommendation.items.length" class="recommend-block">
            <div class="recommend-header">
              <span class="recommend-kicker">AI 推荐</span>
              <p>{{ recommendation.summary }}</p>
            </div>
            <div class="recommend-list">
              <button
                v-for="item in recommendation.items"
                :key="item.id"
                type="button"
                class="recommend-card"
                @click="goToItem(item.id)"
              >
                <strong>{{ item.title || "未命名商品" }}</strong>
                <span v-if="item.price">￥{{ item.price }}</span>
                <small>{{ formatCategory(item) }}</small>
              </button>
            </div>
          </section>

          <div v-if="loading" class="message role-assistant">
            <div class="message-bubble typing">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <div v-if="suggestedQuestions.length" class="quick-actions">
          <button
            v-for="prompt in suggestedQuestions"
            :key="prompt"
            type="button"
            class="quick-btn"
            @click="fillPrompt(prompt)"
          >
            {{ prompt }}
          </button>
        </div>

        <form class="composer" @submit.prevent="sendMessage">
          <textarea
            v-model="input"
            rows="3"
            placeholder="比如：我想买一个200元以内的宿舍台灯，有推荐吗？"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <div class="composer-footer">
            <span class="hint">Enter 发送，Shift + Enter 换行</span>
            <button type="submit" class="send-btn" :disabled="loading || !trimmedInput">
              {{ loading ? "思考中..." : "发送" }}
            </button>
          </div>
        </form>
      </section>
    </transition>

    <button class="launcher" type="button" @click="togglePanel()">
      <span class="launcher-mark">AI</span>
      <span class="launcher-text">导购助手</span>
    </button>
  </div>
</template>

<script>
const DEFAULT_QUICK_PROMPTS = [
  "帮我推荐适合宿舍用的商品",
  "预算100元以内有什么值得买",
  "校园二手交易要注意什么？"
];

const ROUTE_LABELS = {
  PRODUCT: "商品咨询",
  POLICY: "平台规则",
  CHITCHAT: "闲聊"
};

export default {
  name: "AiAssistant",
  props: {
    currentItemId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      isOpen: false,
      loading: false,
      input: "",
      messages: [
        {
          id: 1,
          role: "assistant",
          content: "你好，我是 GooShare AI 导购助手。你可以问我商品推荐、预算建议，或者购买前的疑问。"
        }
      ],
      recommendation: {
        summary: "",
        items: []
      },
      itemContext: {
        currentItem: null,
        contextSummary: ""
      },
      quickPrompts: DEFAULT_QUICK_PROMPTS,
      conversationId: "",
      limit: 5  // 推荐数量，可随需求调整
    };
  },
  computed: {
    trimmedInput() {
      return this.input.trim();
    },
    apiBaseUrl() {
      return process.env.VUE_APP_API_BASE_URL || "http://localhost:8084";
    },
    suggestedQuestions() {
      const dynamicQuestions = this.itemContext && this.itemContext.suggestedQuestions
        ? this.itemContext.suggestedQuestions
        : [];
      return dynamicQuestions.length ? dynamicQuestions : this.quickPrompts;
    }
  },
  watch: {
    currentItemId: {
      immediate: true,
      handler() {
        this.fetchItemContext();
      }
    },
    isOpen(opened) {
      if (opened) {
        this.$nextTick(this.scrollToBottom);
      }
    }
  },
  created() {
    this.ensureConversationId();
  },
  methods: {
    createConversationId() {
      return `conv-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    },
    ensureConversationId() {
      const stored = localStorage.getItem("aiConversationId");
      if (stored) {
        this.conversationId = stored;
        return;
      }
      this.conversationId = this.createConversationId();
      localStorage.setItem("aiConversationId", this.conversationId);
    },
    buildHeaders() {
      const token = localStorage.getItem("token");
      return {
        "Content-Type": "application/json",
        ...(token ? { Authorization: token } : {})
      };
    },
    togglePanel(forceState) {
      this.isOpen = typeof forceState === "boolean" ? forceState : !this.isOpen;
      if (this.isOpen) {
        this.$nextTick(this.scrollToBottom);
      }
    },
    fillPrompt(prompt) {
      this.input = prompt;
      this.sendMessage();
    },
    appendMessage(role, content, extra = {}) {
      this.messages.push({
        id: Date.now() + Math.random(),
        role,
        content,
        route: extra.route || "",
        references: extra.references || []
      });
    },
    routeLabel(route) {
      return ROUTE_LABELS[route] || route;
    },
    fetchItemContext() {
      if (!this.currentItemId) {
        this.itemContext = {
          currentItem: null,
          contextSummary: ""
        };
        return;
      }
      this.itemContext = {
        currentItem: {
          id: this.currentItemId,
          title: `商品 #${this.currentItemId}`
        },
        contextSummary: "可以询问当前商品的价格、库存和详细信息。"
      };
    },
    clearHistory() {
      this.conversationId = this.createConversationId();
      localStorage.setItem("aiConversationId", this.conversationId);
      this.messages = [
        {
          id: Date.now(),
          role: "assistant",
          content: "聊天记录已经清空，我们可以重新开始。"
        }
      ];
      this.recommendation = { summary: "", items: [] };
    },
    async sendMessage() {
      const message = this.trimmedInput;
      if (!message || this.loading) {
        return;
      }

      this.appendMessage("user", message);
      this.input = "";
      this.loading = true;
      this.$nextTick(this.scrollToBottom);

      try {
        const referencesCurrentItem = this.currentItemId
          && /(这个|当前|该商品|它)/.test(message);
        const agentMessage = referencesCurrentItem
          ? `当前页面商品ID为${this.currentItemId}。用户问题：${message}`
          : message;

        const response = await fetch(`${this.apiBaseUrl}/ai/chat-v2`, {
          method: "POST",
          headers: this.buildHeaders(),
          body: JSON.stringify({
            conversationId: this.conversationId,
            message: agentMessage
          })
        });

        const payload = await response.json().catch(() => null);
        if (!response.ok) {
          throw new Error(
            payload && payload.msg
              ? payload.msg
              : `HTTP ${response.status}`
          );
        }

        if (!payload || payload.code !== 200) {
          throw new Error(
            payload && payload.msg
              ? payload.msg
              : "AI 助手执行失败"
          );
        }

        const data = payload.data || {};
        if (data.conversationId) {
          this.conversationId = data.conversationId;
          localStorage.setItem("aiConversationId", data.conversationId);
        }

        const reply = typeof data === "string"
          ? data
          : data.answer;

        this.appendMessage(
          "assistant",
          reply || "当前助手暂时没有返回内容，你可以换个问法试试。"
        );
      } catch (error) {
        console.error("AI assistant request failed", error);
        const errorMessage = error && error.message === "请先登录"
          ? "请先登录后再使用 AI 导购助手。"
          : "现在暂时没连上 AI 服务。你可以稍后再试。";
        this.appendMessage(
          "assistant",
          errorMessage
        );
      } finally {
        this.loading = false;
        this.$nextTick(this.scrollToBottom);
      }
    },
    async fetchRecommend() {
      if (this.loading) {
        return;
      }
      this.input = this.trimmedInput
        || `请推荐${this.limit}件适合校园生活的二手商品`;
      await this.sendMessage();
    },
    goToItem(itemId) {
      if (!itemId) {
        return;
      }
      if (this.$route.path !== `/itemInfo/${itemId}`) {
        this.$router.push(`/itemInfo/${itemId}`);
      }
    },
    formatCategory(item) {
      if (!item) {
        return "";
      }
      const category = item.category || "";
      const sub = item.categoryItem || "";
      return [category, sub].filter(Boolean).join(" / ");
    },
    scrollToBottom() {
      const container = this.$refs.messageList;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }
  }
};
</script>

<style scoped>
/* 样式保持不变，省略（与原样式完全一致） */
.ai-assistant {
  position: fixed;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3000;
  display: flex;
  align-items: center;
  gap: 14px;
  pointer-events: none;
}

.ai-assistant > * {
  pointer-events: auto;
}

.launcher {
  border: none;
  border-radius: 18px;
  background: linear-gradient(180deg, #4eb07d 0%, #2f8d63 100%);
  color: #f7fff9;
  width: 64px;
  min-height: 152px;
  padding: 14px 9px;
  box-shadow: 0 14px 30px rgba(47, 141, 99, 0.22);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.launcher:hover {
  transform: translateX(-3px);
  box-shadow: 0 18px 34px rgba(47, 141, 99, 0.28);
  filter: saturate(1.05);
}

.launcher-mark {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.16);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.launcher-text {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 0.18em;
  font-size: 13px;
  font-weight: 600;
}

.assistant-panel {
  width: min(390px, calc(100vw - 120px));
  height: min(80vh, 720px);
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.98), rgba(241, 250, 245, 0.96)),
    linear-gradient(180deg, #ffffff 0%, #eef8f1 100%);
  border: 1px solid rgba(69, 150, 106, 0.14);
  border-radius: 20px;
  box-shadow: 0 22px 48px rgba(31, 75, 52, 0.16);
  display: grid;
  grid-template-rows: auto auto 1fr auto auto;
  overflow: hidden;
  backdrop-filter: blur(14px);
}

.panel-header {
  padding: 16px 18px 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(180deg, rgba(45, 125, 86, 0.98), rgba(76, 170, 118, 0.92));
  color: #f8fffa;
}

.panel-header h3 {
  margin: 4px 0 0;
  font-size: 18px;
}

.eyebrow {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.78;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ghost-btn {
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.12);
  color: #f8fffa;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  cursor: pointer;
}

.icon-btn {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.16);
  color: #f7fff9;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.context-banner {
  padding: 12px 16px;
  background: rgba(237, 248, 240, 0.95);
  border-bottom: 1px solid rgba(69, 150, 106, 0.12);
}

.context-label {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #69927a;
}

.context-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #254132;
}

.context-main strong {
  font-size: 14px;
}

.context-main span {
  color: #2f8d63;
  font-size: 13px;
  font-weight: 600;
}

.context-summary {
  margin: 6px 0 0;
  color: #4c6b58;
  font-size: 12px;
  line-height: 1.5;
}

.message-list {
  padding: 16px 18px;
  overflow-y: auto;
  background:
    radial-gradient(circle at top right, rgba(92, 190, 136, 0.09), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.72), rgba(244, 250, 245, 0.94));
}

.intro-card {
  margin-bottom: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #edf8f0;
  color: #32624a;
  font-size: 13px;
  line-height: 1.6;
}

.intro-card p {
  margin: 0;
}

.message {
  display: flex;
  margin-bottom: 12px;
}

.role-user {
  justify-content: flex-end;
}

.role-assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 88%;
  border-radius: 14px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.65;
  text-align: left;
  white-space: pre-wrap;
  word-break: break-word;
}

.role-user .message-bubble {
  background: linear-gradient(180deg, #4aa874 0%, #2f875c 100%);
  color: #fff;
  border-bottom-right-radius: 6px;
}

.role-assistant .message-bubble {
  background: #fff;
  color: #254132;
  border: 1px solid rgba(72, 143, 103, 0.12);
  border-bottom-left-radius: 6px;
  box-shadow: 0 8px 18px rgba(43, 87, 61, 0.06);
}

.route-badge {
  display: inline-block;
  margin-right: 6px;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  vertical-align: middle;
}

.route-PRODUCT {
  background: #e7f6ec;
  color: #2c6f4d;
}

.route-POLICY {
  background: #eaf1fb;
  color: #2f5fa8;
}

.route-CHITCHAT {
  background: #f2eefb;
  color: #6a4fa3;
}

.references {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(72, 143, 103, 0.22);
}

.references-title {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: #6b9277;
}

.references ul {
  margin: 0;
  padding-left: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.references li {
  font-size: 12px;
  line-height: 1.5;
  color: #4c6b58;
}

.ref-source {
  display: inline-block;
  margin-right: 6px;
  padding: 0 6px;
  border-radius: 6px;
  background: #f0f6f2;
  color: #3a7a56;
  font-weight: 600;
}

.recommend-block {
  margin: 18px 0 10px;
  padding: 14px;
  border-radius: 14px;
  background: #f6fbf7;
  border: 1px solid rgba(72, 143, 103, 0.1);
}

.recommend-header {
  margin-bottom: 12px;
}

.recommend-header p {
  margin: 6px 0 0;
  color: #365345;
  font-size: 13px;
  line-height: 1.6;
}

.recommend-kicker {
  font-size: 11px;
  color: #6b9277;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.recommend-list {
  display: grid;
  gap: 10px;
}

.recommend-card {
  width: 100%;
  text-align: left;
  border: 1px solid rgba(72, 143, 103, 0.12);
  border-radius: 12px;
  background: #fff;
  padding: 12px;
  cursor: pointer;
  display: grid;
  gap: 4px;
}

.recommend-card strong {
  color: #1f382a;
  font-size: 14px;
}

.recommend-card span {
  color: #2f8d63;
  font-size: 13px;
  font-weight: 600;
}

.recommend-card small {
  color: #71897a;
  font-size: 12px;
}

.typing {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6bb58b;
  animation: typingPulse 1s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.18s;
}

.typing span:nth-child(3) {
  animation-delay: 0.36s;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 18px 14px;
}

.quick-btn {
  border: none;
  border-radius: 999px;
  background: #e7f6ec;
  color: #2c6f4d;
  padding: 9px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.quick-btn:hover {
  background: #d7efdf;
  transform: translateY(-1px);
}

.composer {
  border-top: 1px solid rgba(69, 150, 106, 0.12);
  padding: 16px 18px 18px;
  background: rgba(255, 255, 255, 0.82);
}

.composer textarea {
  width: 100%;
  resize: none;
  border: 1px solid rgba(69, 150, 106, 0.18);
  border-radius: 14px;
  padding: 14px 15px;
  font: inherit;
  color: #22382b;
  background: #fff;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.composer textarea:focus {
  border-color: #3e9d69;
  box-shadow: 0 0 0 4px rgba(62, 157, 105, 0.12);
}

.composer-footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hint {
  font-size: 12px;
  color: #6f8b78;
}

.send-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(180deg, #50b57f 0%, #2f8d63 100%);
  color: #fff;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(47, 141, 99, 0.2);
}

.send-btn:disabled,
.ghost-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

.assistant-panel-enter-active,
.assistant-panel-leave-active {
  transition: all 0.24s ease;
}

.assistant-panel-enter-from,
.assistant-panel-leave-to {
  opacity: 0;
  transform: translateX(16px) scale(0.98);
}

@keyframes typingPulse {
  0%,
  80%,
  100% {
    transform: scale(0.75);
    opacity: 0.35;
  }

  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .ai-assistant {
    right: 14px;
    top: auto;
    bottom: 16px;
    transform: none;
    flex-direction: column-reverse;
    align-items: flex-end;
  }

  .launcher {
    width: 58px;
    min-height: 58px;
    border-radius: 16px;
    padding: 10px;
  }

  .launcher-text {
    writing-mode: horizontal-tb;
    text-orientation: initial;
    letter-spacing: 0.08em;
    font-size: 12px;
  }

  .launcher-mark {
    width: 30px;
    height: 30px;
    font-size: 14px;
  }

  .assistant-panel {
    width: min(92vw, 390px);
    height: min(78vh, 680px);
  }

  .composer-footer,
  .header-actions {
    align-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
