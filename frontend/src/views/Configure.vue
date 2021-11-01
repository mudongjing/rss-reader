<template>
	<div id="Configure">
    <p>设置</p>
    <input v-model.lazy.trim="rssUrl" placeholder="rss url"><br>
    <input @keyup.enter="submit" v-model.lazy.number="rssLevel" placeholder="rss level">
    <p>RssURL is: {{ rssUrl }}</p>
    <p>Level is: {{ rssLevel }}</p>
    <p>Success is: {{isSuccess}}</p>
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
        isSuccess: false
      }
    },
    methods:{
      submit(){
        if(this.rssUrl!==null && this.rssLevel!==null){
          let rss={'url':this.rssUrl,'level':this.rssLevel};
          submitNewRss(rss).then(res=>{
            console.log(res)
            this.isSuccess=res
          })
        }
      }
    }
	}
</script>

<style>
</style>
