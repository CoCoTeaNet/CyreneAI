import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/moderationRule/add', data, post);
}

export function update(data: any) {
    return request('ai/moderationRule/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/moderationRule/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/moderationRule/listByPage', data, post);
}
