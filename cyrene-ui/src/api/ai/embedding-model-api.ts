import {request, post, get} from '@/utils/axios-util';

export function listByPage(data: any) {
    return request('ai/embedding/model/listByPage', data, post);
}

export function add(data: any) {
    return request('ai/embedding/model/add', data, post);
}

export function update(data: any) {
    return request('ai/embedding/model/update', data, post);
}

export function remove(id: string) {
    return request('ai/embedding/model/delete/' + id, {}, post);
}

export function listEnabled() {
    return request('ai/embedding/model/listEnabled', {}, get);
}

/**
 * 文本嵌入：将输入文本转换为向量
 * @param data { modelId?: string; input: string[] }
 */
export function embeddings(data: any) {
    return request('ai/embeddings', data, post);
}
