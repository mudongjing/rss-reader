<template>
<div id="RssNavBar">
  <div class="rss-left" slot="left"><img src="~assets/img/comback.svg"></div>
  <div class="rss-middle" slot="middle"><div> Rss源 </div></div>
  <div class="rss-right" slot="right"><img src="~assets/img/filter.svg"></div>
</div>
</template>

<script>
export default {
  name: "RssNavBar"
}
</script>

<style scoped>
#RssNavBar{
  display: flex;
  box-shadow: 0 1px 1px rgba(100,100,100,0.29);
  position: sticky;
  height: 20px;
}
.rss-left, .rss-right{
  width: 8%;
  background-color: #ecf0ef;
}
.rss-middle{
  flex: 1;
  background-color: #f9ecf9;
  text-align: center;
  color: #ff5777;
}
.rss-left img, .rss-right img{
  width: 100%;
  height: 15px;
}
</style>
