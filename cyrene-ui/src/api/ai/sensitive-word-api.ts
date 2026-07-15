import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/sensitiveWord/add', data, post);
}

export function update(data: any) {
    return request('ai/sensitiveWord/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/sensitiveWord/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/sensitiveWord/listByPage', data, post);
}
