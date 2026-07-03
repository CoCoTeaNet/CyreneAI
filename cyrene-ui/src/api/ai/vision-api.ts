import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/vision-model/add', data, post);
}

export function update(data: any) {
    return request('ai/vision-model/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/vision-model/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/vision-model/listByPage', data, post);
}

export function listEnabled() {
    return request('ai/vision-model/listEnabled', {}, get);
}
