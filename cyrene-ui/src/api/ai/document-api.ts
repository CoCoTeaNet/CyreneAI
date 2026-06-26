import {request, post, get} from '@/utils/axios-util';

export function listByPage(data: any) {
    return request('ai/document/listByPage', data, post);
}

export function uploadFile(formData: FormData) {
    return request('ai/document/upload', formData, post);
}

export function reIndex(id: string) {
    return request('ai/document/reIndex/' + id, {}, post);
}

export function remove(id: string) {
    return request('ai/document/delete/' + id, {}, post);
}

export function get(id: string) {
    return request('ai/document/get/' + id, {}, get);
}
