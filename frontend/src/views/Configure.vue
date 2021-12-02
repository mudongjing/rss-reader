<template>
	<div id="Configure">
    <p>设置</p>
    <input v-model.lazy.trim="rssUrl" placeholder="rss url"><br>
    <input v-model.lazy.trim="rssTitle" placeholder="rss title"><br>
    <input @keyup.enter="submit" v-model.lazy.number="rssLevel" placeholder="rss level">
    <p>RssURL is: {{ rssUrl }}</p>
    <p>RssTitle is: {{rssTitle}}</p>
    <p>Level is: {{ rssLevel }}</p>
    <p>Success is: {{isSuccess}}</p>
    <p>状况为： {{stat}}  </p>
  </div>
</template>

<script>
import {submitNewRss} from "../network/rssItem";
	export default{
		name: 'Configure',
    data(){
      return {
        rssUrl: null,
        rssLevel: null,
        rssTitle:null,
        isSuccess: null,
        stat: null
      }
    },
    methods:{
      submit(){
        if(this.rssUrl!==null && this.rssLevel!==null){
          let rss={'url':this.rssUrl,'level':this.rssLevel,'title':this.rssTitle};
          submitNewRss(rss).then(res=>{
            console.log(res)
            this.isSuccess=res.succss
            this.stat=res.mess
          })
        }
      }
    }
	}
</script>

<style>
</style>
