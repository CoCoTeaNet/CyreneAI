import {request, get} from '@/utils/axios-util';

export function overview(days?: number) {
    return request('ai/monitor/overview', days ? {days} : {}, get);
}

export function tokenTrend(groupType?: string, days?: number) {
    return request('ai/monitor/tokenTrend', {groupType, days}, get);
}

export function modelRank(days?: number) {
    return request('ai/monitor/modelRank', days ? {days} : {}, get);
}

export function userRank(days?: number) {
    return request('ai/monitor/userRank', days ? {days} : {}, get);
}

export function costStat(dimension?: string, days?: number) {
    return request('ai/monitor/costStat', {dimension, days}, get);
}

export function costSuggestions(days?: number) {
    return request('ai/monitor/costSuggestions', days ? {days} : {}, get);
}
