import {request} from '@/utils/axios-util';

export function listConversations() {
    return request('ai/conversation/list', {}, 'GET');
}

export function createConversation(data: any) {
    return request('ai/conversation/create', data, 'POST');
}

export function deleteConversation(id: string) {
    return request('ai/conversation/delete', {id}, 'POST');
}

export function getConversationMessages(conversationId: string) {
    return request('ai/conversation/messages', {conversationId}, 'GET');
}

export function deleteMessage(id: string) {
    return request('ai/conversation/deleteMessage', {id}, 'POST');
}

export function clearMessages(conversationId: string) {
    return request('ai/conversation/clearMessages', {conversationId}, 'POST');
}

export function exportConversation(conversationId: string) {
    return request('ai/conversation/export', {conversationId}, 'GET');
}

export function importConversation(data: any) {
    return request('ai/conversation/import', data, 'POST');
}
