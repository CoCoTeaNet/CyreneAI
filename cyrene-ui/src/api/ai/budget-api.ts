import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/budget/add', data, post);
}

export function update(data: any) {
    return request('ai/budget/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/budget/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/budget/listByPage', data, post);
}

export function listStatus() {
    return request('ai/budget/status', {}, get);
}
