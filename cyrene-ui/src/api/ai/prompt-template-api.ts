import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/promptTemplate/add', data, post);
}

export function update(data: any) {
    return request('ai/promptTemplate/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/promptTemplate/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/promptTemplate/listByPage', data, post);
}

export function listEnabled(params?: { category?: string; scene?: string }) {
    const q = params ? Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`).join('&') : '';
    return request('ai/promptTemplate/listEnabled' + (q ? '?' + q : ''), {}, get);
}

export function render(data: any) {
    return request('ai/promptTemplate/render', data, post);
}

export function listVersions(templateId: string) {
    return request('ai/promptTemplate/listVersions/' + templateId, {}, get);
}

export function getVersion(templateId: string, version: number) {
    return request('ai/promptTemplate/getVersion/' + templateId + '/' + version, {}, get);
}

export function rollback(templateId: string, version: number, changeNote?: string) {
    const q = changeNote ? '?changeNote=' + encodeURIComponent(changeNote) : '';
    return request('ai/promptTemplate/rollback/' + templateId + '/' + version + q, {}, post);
}
