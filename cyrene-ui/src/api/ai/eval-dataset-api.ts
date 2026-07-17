import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/evalDataset/add', data, post);
}

export function update(data: any) {
    return request('ai/evalDataset/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/evalDataset/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/evalDataset/listByPage', data, post);
}
