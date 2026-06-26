import {request, post} from '@/utils/axios-util';

export function scrape(data: any) {
    return request('ai/webScraper/scrape', data, post);
}
