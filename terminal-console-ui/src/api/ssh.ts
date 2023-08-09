import request from "../utils/request";

export function auth(data: any) {
    return request({
        url: `/ssh/auth`,
        method: 'post',
        data: data
    })
}
