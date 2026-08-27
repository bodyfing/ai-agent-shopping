<template>
  <div id="app">
    <TopNavBar @return-change="handleReturn"></TopNavBar>
    <BrandHeader></BrandHeader>
    <SortNav ref="mySort" @filter-change="handleFilter"></SortNav>
    <ItemRanking></ItemRanking>
    <ItemCard ref="myList"></ItemCard>
  </div>
</template>

<script>
import BrandHeader from "./components/BrandHeader.vue";
import ItemCard from "./components/ItemCard.vue";
import SortNav from "./components/SortNav.vue";
import TopNavBar from "./components/TopNavBar.vue";

export default {
  name: "HomePage",
  components: {
    TopNavBar: TopNavBar,
    BrandHeader: BrandHeader,
    ItemCard: ItemCard,
    SortNav: SortNav
  },
  methods: {
    handleFilter(filter) {
      this.$refs.myList.getItem(filter);
    },
    handleReturn() {
      this.$refs.myList.getItem("");
      if (this.$refs.mySort && this.$refs.mySort.reset) {
        this.$refs.mySort.reset();
      }
    }
  },
  watch: {
    "$route.query.keyword": {
      handler(newKeyword) {
        // 使用 $nextTick 确保子组件已经挂载完毕
        this.$nextTick(() => {
          if (this.$refs.myList) {
            // 将新拿到的关键字传给 ItemCard 的 searchItem 方法
            this.$refs.myList.searchItem(newKeyword || "");
          }
        });
      },
      immediate: true // 页面一加载就执行一次，处理直接携带参数进入页面的情况
    }
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 0px;
}
</style>
