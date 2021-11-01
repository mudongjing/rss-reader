<template>

<div id="RssDetails">
  <p>标题：---{{item.title}}</p><br>
  <p>日期与作者：---{{item.publishDate}}--------------------{{item.author}}</p><br>
  <p>分类：----{{item.category}}</p>
  <p>文章摘要：----</p>
  <p v-html="item.articleContent"></p>
  <p>文章介绍：---</p>
  <p v-html="item.articleDescription"></p>
  <a v-if="item.articleLink !==null && item.articleLink.trim()!==''" :href="item.articleLink" target="_blank">原文链接: {{item.articleLink}}</a><br>
  <a v-if="item.articleUri !==null && item.articleUri.trim()!==''" :href="item.articleUri" target="_blank">原文地址: {{item.articleUri}}</a><br>
  <a :href="item.articleUri.comments">评论</a>
</div>

</template>

<script>

export default {
  name: "RssDetails",
  data(){
    return{
      item: null
    }
  },
  created() {
    this.item=this.$route.params.item
  }
}
</script>

<style scoped>
  #RssDetails{
    margin-bottom: 60px;
    overflow: auto;
  }
</style>
