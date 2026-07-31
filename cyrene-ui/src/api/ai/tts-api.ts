import {request, post, get} from '@/utils/axios-util';
import axios from 'axios';
import {useUserStore} from "@/stores/user.ts";

export function synthesizeBlob(data: any) {
    const userStore = useUserStore();
    return axios.request({
        url: '/api/ai/tts/synthesize',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8',
            "Authorization": userStore.userinfo.token || ''
        },
        data: JSON.stringify(data),
        responseType: 'blob',
        timeout: 120000
    });
}

export function synthesizeUrl(data: any) {
    return request('ai/tts/synthesize-url', data, post);
}

export function addModel(data: any) {
    return request('ai/tts/model/add', data, post);
}

export function updateModel(data: any) {
    return request('ai/tts/model/update', data, post);
}

export function deleteModel(data: any) {
    return request('ai/tts/model/deleteBatch', data, post);
}

export function listModelByPage(data: any) {
    return request('ai/tts/model/listByPage', data, post);
}

export function listEnabledModels() {
    return request('ai/tts/model/listEnabled', {}, get);
}

export function listRecordByPage(data: any) {
    return request('ai/tts/record/listByPage', data, post);
}
