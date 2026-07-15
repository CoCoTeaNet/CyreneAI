import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/promptPreset/add', data, post);
}

export function update(data: any) {
    return request('ai/promptPreset/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/promptPreset/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/promptPreset/listByPage', data, post);
}

export function listEnabled(category?: string) {
    const q = category ? '?category=' + encodeURIComponent(category) : '';
    return request('ai/promptPreset/listEnabled' + q, {}, get);
}
