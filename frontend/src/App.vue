<template>
  <div id="app">
    <router-view />
    <AiAssistant v-if="!['/login','/register'].includes($route.path)" :current-item-id="currentItemId" />
  </div>
</template>

<script>
import AiAssistant from "./components/AiAssistant.vue";

export default {
  name: "App",
  components: {
    AiAssistant
  },
  computed: {
    currentItemId() {
      const routeId = this.$route && this.$route.params ? this.$route.params.id : null;
      if (!routeId) {
        return null;
      }
      const parsed = Number(routeId);
      return Number.isNaN(parsed) ? null : parsed;
    }
  }
};
</script>

<style>
html,
body,
#app {
  margin: 0;
  min-height: 100%;
}

body {
  font-family: "Segoe UI", "PingFang SC", "Hiragino Sans GB", sans-serif;
  background: #f5f7fb;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}
</style>
