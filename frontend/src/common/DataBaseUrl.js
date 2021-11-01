let DataBaseUrl= ''
export default {
    Url: DataBaseUrl,
    methods:{
        editDataUrl(url){
            DataBaseUrl=url;
        },
        show(){
            console.log(DataBaseUrl)
        }
    }
}
