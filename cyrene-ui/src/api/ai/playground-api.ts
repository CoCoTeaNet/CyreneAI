import {request, post} from '@/utils/axios-util';

export function run(data: any) {
    return request('ai/playground/run', data, post);
}
