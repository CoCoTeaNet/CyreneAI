import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/model-provider/add', data, post);
}

export function update(data: any) {
    return request('ai/model-provider/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/model-provider/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/model-provider/listByPage', data, post);
}
