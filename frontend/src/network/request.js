import axios from 'axios'
import DataBaseUrl from "../common/DataBaseUrl";
export function request(config){
	const instance = axios.create({
		baseURL: 'http://'+DataBaseUrl.Url+':8110',
		timeout: 600000
	})
	return instance(config)
}
