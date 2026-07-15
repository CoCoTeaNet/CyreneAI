import {request, post} from '@/utils/axios-util';

export function run(data: any) {
    return request('ai/promptEval/run', data, post);
}

export function rate(data: any) {
    return request('ai/promptEval/rate', data, post);
}

export function listByPage(data: any) {
    return request('ai/promptEval/listByPage', data, post);
}
