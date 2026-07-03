import {request, post, get} from '@/utils/axios-util';

export function generate(data: any) {
    return request('ai/image/generate', data, post);
}

export function addModel(data: any) {
    return request('ai/image/model/add', data, post);
}

export function updateModel(data: any) {
    return request('ai/image/model/update', data, post);
}

export function deleteModel(data: any) {
    return request('ai/image/model/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/image/model/listByPage', data, post);
}

export function listEnabledModels() {
    return request('ai/image/model/listEnabled', {}, get);
}

export function listModels() {
    return request('ai/image/model/listModels', {}, get);
}

export function listRecordByPage(data: any) {
    return request('ai/image/record/listByPage', data, post);
}

export function deleteRecord(id: string) {
    return request('ai/image/record/delete/' + id, {}, post);
}
