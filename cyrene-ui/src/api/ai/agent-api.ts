import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/agent/add', data, post);
}

export function update(data: any) {
    return request('ai/agent/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/agent/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/agent/listByPage', data, post);
}

export function listEnabled() {
    return request('ai/agent/listEnabled', {}, get);
}
