import {request, post} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/model-provider/add', data, post);
}

export function update(data: any) {
    return request('ai/model-provider/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/model-provider/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/model-provider/listByPage', data, post);
}

// 密钥轮换：用旧主密钥解密、新主密钥重新加密所有提供商密钥
export function rotateSecret(oldSecret: string, newSecret: string) {
    return request(`ai/model-provider/rotate-secret/${encodeURIComponent(oldSecret)}/${encodeURIComponent(newSecret)}`, {}, post);
}
