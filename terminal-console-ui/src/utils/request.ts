import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL, // api base_url
    timeout: 60 * 1000 // 请求超时时间
});
// 请求拦截
service.interceptors.request.use((config: any) => {
    // 这里可设置请求头等信息
    return config;
});
// 返回拦截
service.interceptors.response.use(res => {
    if (res.status === 200) {
        return Promise.resolve(res.data);
    }
    return Promise.reject(res)
}, err => {
    console.error('响应异常', err)
    if (err.response && err.response.data && err.response.data.errorCode === 500) {
        return Promise.reject(err.response.data.errorInfo || '服务端异常')
    }
    return Promise.reject(err)
});

export default service;
