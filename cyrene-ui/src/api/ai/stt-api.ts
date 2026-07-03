import {request, post, get} from '@/utils/axios-util';

export function transcribe(data: any) {
    return request('ai/stt/transcribe', data, post);
}

export function transcribeUrl(data: any) {
    return request('ai/stt/transcribe-url', data, post);
}

export function addModel(data: any) {
    return request('ai/stt/model/add', data, post);
}

export function updateModel(data: any) {
    return request('ai/stt/model/update', data, post);
}

export function deleteModel(data: any) {
    return request('ai/stt/model/deleteBatch', data, post);
}

export function listModelByPage(data: any) {
    return request('ai/stt/model/listByPage', data, post);
}

export function listEnabledModels() {
    return request('ai/stt/model/listEnabled', {}, get);
}

export function listRecordByPage(data: any) {
    return request('ai/stt/record/listByPage', data, post);
}
