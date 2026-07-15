import {request, post} from '@/utils/axios-util';

export function listByPage(data: any) {
    return request('ai/auditLog/listByPage', data, post);
}
