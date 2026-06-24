import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/model/add', data, post);
}

export function update(data: any) {
    return request('ai/model/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/model/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/model/listByPage', data, post);
}
