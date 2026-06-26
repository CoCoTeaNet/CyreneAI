import { ref, type Ref } from 'vue';
import { useUserStore } from '@/stores/user';

export type ChatRole = 'user' | 'assistant' | 'system';

export interface ChatMessage {
    id?: string;
    role: ChatRole;
    content: string;
    tokenUsage?: {
        promptTokens: number;
        completionTokens: number;
        totalTokens: number;
        cost?: string;
    };
}

export interface ChatParams {
    temperature?: number;
    topP?: number;
    maxTokens?: number;
    systemPrompt?: string;
    contextStrategy?: string;
}

export function useChatStream() {
    const streaming = ref(false);
    const abortController = ref<AbortController | null>(null);
    const userStore = useUserStore();

    async function sendMessage(
        messages: Ref<ChatMessage[]>,
        modelId: string | null,
        userContent: string,
        params?: ChatParams,
        conversationId?: string | null,
        editMessageId?: string | null
    ): Promise<void> {
        // 1. 修复逻辑：如果正在流式传输，先中断之前的请求，防止并发冲突
        if (streaming.value) {
            stopStream();
        }

        if (!userContent.trim()) return;

        messages.value.push({ role: 'user', content: userContent });
        const assistantMsg: ChatMessage = { role: 'assistant', content: '' };
        messages.value.push(assistantMsg);

        streaming.value = true;

        // 2. 修复逻辑：在创建新的控制器前，确保清理旧的
        if (abortController.value) {
            abortController.value.abort();
        }
        abortController.value = new AbortController();

        const messagePayload = messages.value.slice(0, -1).map(m => ({
            role: m.role,
            content: m.content
        }));

        const headers: Record<string, string> = { 'Content-Type': 'application/json' };
        if (userStore.userinfo.token) {
            headers['sa-token'] = userStore.userinfo.token;
        }

        const body: Record<string, any> = {
            modelId: modelId || null,
            messages: messagePayload
        };

        if (conversationId) {
            body.conversationId = conversationId;
        }

        if (editMessageId) {
            body.editMessageId = editMessageId;
        }

        if (params) {
            if (params.temperature !== undefined) body.temperature = params.temperature;
            if (params.topP !== undefined) body.topP = params.topP;
            if (params.maxTokens !== undefined) body.maxTokens = params.maxTokens;
            if (params.systemPrompt) body.systemPrompt = params.systemPrompt;
            if (params.contextStrategy) body.contextStrategy = params.contextStrategy;
        }

        try {
            const response = await fetch('/api/ai/chat/stream', {
                method: 'POST',
                headers,
                body: JSON.stringify(body),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const reader = response.body!.getReader();
            const decoder = new TextDecoder();
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (!line.startsWith('data: ')) continue;
                    const data = line.slice(6);

                    if (data === '[DONE]') {
                        // 注意：这里原代码逻辑可能有误，[DONE] 通常意味着结束，不应设置 streaming=false 后继续循环
                        // 但为了保持原结构，暂时保留，建议在外部处理结束逻辑
                        continue;
                    }

                    try {
                        const parsed = JSON.parse(data);
                        if (parsed.content) {
                            assistantMsg.content += parsed.content;
                            messages.value = [...messages.value];
                        }
                        if (parsed.type === 'token_usage') {
                            assistantMsg.tokenUsage = {
                                promptTokens: parsed.promptTokens,
                                completionTokens: parsed.completionTokens,
                                totalTokens: parsed.totalTokens,
                                cost: parsed.cost
                            };
                            messages.value = [...messages.value];
                        }
                        if (parsed.error) {
                            assistantMsg.content = `Error: ${parsed.error}`;
                            messages.value = [...messages.value];
                            streaming.value = false;
                        }
                    } catch (parseError) {
                        // 忽略解析错误
                        console.warn('Failed to parse stream data:', parseError);
                    }
                }
            }

            // 流结束，重置状态
            streaming.value = false;

        } catch (err: any) {
            if (err.name === 'AbortError') {
                assistantMsg.content += '\n\n[stopped]';
            } else {
                assistantMsg.content = `Error: ${err.message}`;
            }
            messages.value = [...messages.value];
        } finally {
            streaming.value = false;
            abortController.value = null;
        }
    }

    function stopStream() {
        if (abortController.value) {
            abortController.value.abort();
        }
    }

    return { streaming, sendMessage, stopStream };
}