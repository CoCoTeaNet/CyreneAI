import {request, post, get} from '@/utils/axios-util';

export function generate(data: any) {
    return request('ai/apiKey/generate', data, post);
}

export function update(data: any) {
    return request('ai/apiKey/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/apiKey/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/apiKey/listByPage', data, post);
}

export function statRecent(apiKeyId: string, days?: number) {
    const q = days ? '?days=' + days : '';
    return request('ai/apiKey/usage/' + apiKeyId + q, {}, get);
}
