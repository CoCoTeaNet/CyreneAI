import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/quotaAlert/add', data, post);
}

export function update(data: any) {
    return request('ai/quotaAlert/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/quotaAlert/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/quotaAlert/listByPage', data, post);
}
