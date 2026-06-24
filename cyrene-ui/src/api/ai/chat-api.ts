import {request, post} from '@/utils/axios-util';

export function listEnabled() {
    return request('ai/model/listEnabled', {}, 'GET');
}
