import {request, post, get} from '@/utils/axios-util';

export function add(data: any) {
    return request('ai/tool/add', data, post);
}

export function update(data: any) {
    return request('ai/tool/update', data, post);
}

export function deleteBatch(data: any) {
    return request('ai/tool/deleteBatch', data, post);
}

export function listByPage(data: any) {
    return request('ai/tool/listByPage', data, post);
}

export function listEnabled() {
    return request('ai/tool/listEnabled', {}, get);
}

export function listByType(type: string) {
    return request('ai/tool/listByType/' + type, {}, get);
}

export function execute(data: any) {
    return request('ai/tool/execute', data, post);
}

export function getSpecifications() {
    return request('ai/tool/specifications', {}, get);
}
