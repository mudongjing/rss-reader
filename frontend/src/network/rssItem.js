import { request } from "./request.js"
import qs from 'qs'
import 'url-search-params-polyfill';
import axios from "axios";
import DataBaseUrl from "../common/DataBaseUrl";
export function getAllRss() {
	let conf={
		url: 'url/showAllId',
		method: 'get'
	}
	return request(conf).then(res=>{
		let p=res.data;
		return p;
	})
}

export function getLogoAndTitle(rssList){
	let conf={
		headers:{
			post:{
				'Content-Type':'application/x-www-form-urlencoded;charset=utf-8'
			}
		},
		method:'post',
		url:'/url/getLogo-title',
		data: qs.stringify({'rssId':Object.values(rssList)},{ indices: false })
	}

	return request(conf).then(res=>{
		 res.data.forEach(v=>{
			 v.isClick=false
		 })
		console.log(res.data);
		return res.data
	});
}

export function getMessageFromId(idAndAll){
	let conf={url:"message/show/"+idAndAll.id+"/"+idAndAll.isAll, method: 'get'}
	return request(conf).then(res=>{
		let result=[]
		res.data.forEach(v=>{
			let json=JSON.parse(JSON.parse(v))
			let arjson=JSON.parse(json.messageContent)
			if(arjson.hasOwnProperty('publishDate')){
				let ndts=new Date(arjson.publishDate).toLocaleString()
				arjson.publishDate=ndts
			}
			arjson.messageId=json.messageId
			arjson.isRead=json.isRead
			result.push(arjson)
		})
		return result
	})
}

export function updateRead(tableAndMessage){
	let table=tableAndMessage.tableId
	let mess=tableAndMessage.messageId
	let conf={
		url: 'message/isread/'+table+'/'+mess,
		method: 'get'
	}
	return request(conf);
}

export function submitNewRss(rss){
	let conf={
		headers:{
			post:{
				'Content-Type':'application/x-www-form-urlencoded;charset=utf-8'
			}
		},
		method:'post',
		url:'/url/add',
		data: qs.stringify({'url':rss.url,'level':rss.level},{ indices: false })
	}
	return request(conf).then(res=>{return res.data})
}
export function deleteRss(rssId){
	let conf={
		url: 'url/delete/'+rssId,
		method: 'get'
	}
	return request(conf);
}
