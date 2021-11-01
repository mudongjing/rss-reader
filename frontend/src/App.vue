<template>
  <div id="app">
    <keep-alive>
      <router-view></router-view>
    </keep-alive>

		<tab-bar>
			<tab-bar-item path="/rss">
				<img slot="item-icon" src="./assets/img/tab-bar-unlive/rss.svg" alt="">
				<img slot="item-icon-active"  src="./assets/img/tab-icon-alive/rss.svg"/>
				<div slot="item-title">RSS源</div>
			</tab-bar-item>
			<tab-bar-item  path="/visit">
				<img slot="item-icon" src="./assets/img/tab-bar-unlive/visit.svg" alt="">
				<img slot="item-icon-active"  src="./assets/img/tab-icon-alive/visit.svg"/>
				<div slot="item-title">访问</div>
			</tab-bar-item>
			<tab-bar-item path="/search">
				<img slot="item-icon" src="./assets/img/tab-bar-unlive/search.svg" alt="">
				<img slot="item-icon-active"  src="./assets/img/tab-icon-alive/search.svg"/>
				<div slot="item-title">搜索</div>
			</tab-bar-item>
			<tab-bar-item path="/configure">
				<img slot="item-icon" src="./assets/img/tab-bar-unlive/configure.svg" alt="">
				<img slot="item-icon-active"  src="./assets/img/tab-icon-alive/confiure.svg"/>
				<div slot="item-title">设置</div>
			</tab-bar-item>
		</tab-bar>
  </div>
</template>
<script>
	import TabBar from './components/common/TabBar.vue'
	import TabBarItem from './components/content/TabBarItem.vue'

	export default{
		name:'App',
		components:{
			TabBar,
			TabBarItem
		},
    beforeRouteEnter(to, from, next) {
      to.meta.isBack=true;
      next();
    },
    activated() {
      if(!this.$route.meta.isBack || this.isFirstEnter){
        this.getData();
      }
      this.$route.meta.isBack=false
      this.isFirstEnter=false;

    },
    created() {
      this.isFirstEnter = true;
    }
  }
</script>
<style>
	@import "./assets/css/base.css";
</style>
