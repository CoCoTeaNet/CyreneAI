import {ref, type Ref} from 'vue';

export type ChatRole = 'user' | 'assistant' | 'system';

export interface ChatMessage {
    role: ChatRole;
    content: string;
}

export function useChatStream() {
    const streaming = ref(false);
    const abortController = ref<AbortController | null>(null);

    async function sendMessage(
        messages: Ref<ChatMessage[]>,
        modelId: string | null,
        userContent: string
    ): Promise<void> {
        if (streaming.value || !userContent.trim()) return;

        messages.value.push({role: 'user', content: userContent});

        const assistantMsg: ChatMessage = {role: 'assistant', content: ''};
        messages.value.push(assistantMsg);

        streaming.value = true;
        abortController.value = new AbortController();

        const messagePayload = messages.value.slice(0, -1).map(m => ({
            role: m.role,
            content: m.content
        }));

        try {
            const response = await fetch('/api/ai/chat/stream', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    modelId: modelId || null,
                    messages: messagePayload
                }),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const reader = response.body!.getReader();
            const decoder = new TextDecoder();
            let buffer = '';

            while (true) {
                const {done, value} = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, {stream: true});
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (!line.startsWith('data: ')) continue;
                    const data = line.slice(6);

                    if (data === '[DONE]') {
                        streaming.value = false;
                        continue;
                    }

                    try {
                        const parsed = JSON.parse(data);
                        if (parsed.content) {
                            assistantMsg.content += parsed.content;
                            messages.value = [...messages.value];
                        }
                        if (parsed.error) {
                            assistantMsg.content = `Error: ${parsed.error}`;
                            messages.value = [...messages.value];
                            streaming.value = false;
                        }
                    } catch {
                        // ignore parse errors on partial chunks
                    }
                }
            }

            if (assistantMsg.content === '') {
                assistantMsg.content = '(no response)';
                messages.value = [...messages.value];
            }
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

    return {streaming, sendMessage, stopStream};
}
