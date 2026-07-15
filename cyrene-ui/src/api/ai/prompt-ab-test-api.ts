import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/promptAbTest/add', data, post);
}

export function update(data: any) {
    return request('ai/promptAbTest/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/promptAbTest/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/promptAbTest/listByPage', data, post);
}

export function detail(id: string) {
    return request('ai/promptAbTest/detail/' + id, {}, get);
}

export function changeStatus(id: string, status: string) {
    return request('ai/promptAbTest/changeStatus/' + id + '/' + status, {}, post);
}
