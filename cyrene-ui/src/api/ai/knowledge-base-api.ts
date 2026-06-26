import {request, post, get as httpGet} from '@/utils/axios-util';

export function listByPage(data: any) {
    return request('ai/knowledgeBase/listByPage', data, post);
}

export function add(data: any) {
    return request('ai/knowledgeBase/add', data, post);
}

export function update(data: any) {
    return request('ai/knowledgeBase/update', data, post);
}

export function remove(id: string) {
    return request('ai/knowledgeBase/delete/' + id, {}, post);
}

export function get(id: string) {
    return request('ai/knowledgeBase/get/' + id, {}, httpGet);
}

export function listEnabled() {
    return request('ai/knowledgeBase/listEnabled', {}, httpGet);
}

export function retrieve(data: any) {
    return request('ai/knowledgeBase/retrieve', data, post);
}

export function addDocument(kbId: string, documentId: string) {
    return request('ai/knowledgeBase/addDocument/' + kbId + '/' + documentId, {}, post);
}

export function removeDocument(kbId: string, documentId: string) {
    return request('ai/knowledgeBase/removeDocument/' + kbId + '/' + documentId, {}, post);
}
