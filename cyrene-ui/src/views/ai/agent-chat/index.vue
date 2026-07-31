<template>
  <div class="agent-chat-container">
    <div class="chat-header">
      <h2>{{ agentName || '智能体对话' }}</h2>
      <div class="header-info">
        <el-tag v-if="agentName" type="success" effect="dark">智能体模式</el-tag>
        <span v-if="iterationCount > 0" class="iteration-info">迭代次数: {{ iterationCount }}</span>
      </div>
    </div>

    <div class="chat-messages" ref="messagesRef">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
        <div class="message-avatar">
          <el-avatar :icon="msg.role === 'user' ? UserFilled : Promotion" :style="msg.role === 'user' ? 'background: #409eff' : 'background: #67c23a'"/>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <div v-if="msg.isToolCall" class="tool-call">
              <el-tag type="warning" size="small">🔧 调用工具: {{ msg.toolName }}</el-tag>
              <div class="tool-args"><pre>{{ msg.toolArgs }}</pre></div>
            </div>
            <div v-else-if="msg.isToolResult" class="tool-result">
              <el-tag type="success" size="small">✅ 工具返回: {{ msg.toolName }}</el-tag>
              <div class="tool-result-content"><pre>{{ msg.toolResult }}</pre></div>
            </div>
            <div v-else-if="msg.isThinking" class="thinking">
              <el-tag type="info" size="small">🤔 思考中 (迭代 {{ msg.iteration }})...</el-tag>
            </div>
            <div v-else class="markdown-content" v-html="renderMarkdown(msg.content)"></div>
          </div>
          <div v-if="msg.tokenUsage" class="token-usage">
            Prompt: {{ msg.tokenUsage.promptTokens }} | Completion: {{ msg.tokenUsage.completionTokens }} | Total: {{ msg.tokenUsage.totalTokens }}
            <span v-if="msg.tokenUsage.cost"> | 花费: ¥{{ msg.tokenUsage.cost }}</span>
          </div>
        </div>
      </div>
      <div v-if="loading" class="message assistant">
        <div class="message-avatar">
          <el-avatar :icon="Promotion" style="background: #67c23a"/>
        </div>
        <div class="message-content">
          <div class="message-bubble">
            <span class="typing-indicator">
              <span class="dot">.</span><span class="dot">.</span><span class="dot">.</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题..."
          @keydown.enter.prevent="sendMessage"
          :disabled="loading"
      />
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage" :disabled="loading || !inputMessage.trim()">
          {{ loading ? '处理中...' : '发送' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ElMessage} from "element-plus";
import {nextTick, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {UserFilled, Promotion} from "@element-plus/icons-vue";
import {marked} from "marked";
import hljs from "highlight.js";
import {useUserStore} from "@/stores/user";
import 'highlight.js/styles/github-dark.css';

marked.setOptions({
  breaks: true,
  gfm: true,
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, {language: lang}).value;
    }
    return hljs.highlightAuto(code).value;
  }
});

const route = useRoute();
const router = useRouter();
const messagesRef = ref<HTMLElement>();
const inputMessage = ref('');
const messages = ref<any[]>([]);
const loading = ref(false);
const agentId = ref<string>('');
const agentName = ref<string>('');
const iterationCount = ref(0);

onMounted(() => {
  const qAgentId = route.query.agentId as string;
  const qAgentName = route.query.agentName as string;
  if (qAgentId) {
    agentId.value = qAgentId;
    agentName.value = qAgentName || '智能体';
    messages.value.push({
      role: 'assistant',
      content: `你好！我是 **${agentName.value}**。请告诉我你需要什么帮助，我会使用可用工具来为你解决问题。`
    });
  } else {
    ElMessage.error('未指定智能体，请返回智能体列表选择');
    router.push({name: 'AiAgentView'});
  }
});

const renderMarkdown = (content: string) => {
  if (!content) return '';
  try {
    return marked.parse(content) as string;
  } catch {
    return content;
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return;

  const userMsg = inputMessage.value;
  messages.value.push({role: 'user', content: userMsg});
  inputMessage.value = '';
  loading.value = true;

  const history = messages.value
      .filter(m => !m.isToolCall && !m.isToolResult && !m.isThinking && m.role !== 'system')
      .slice(-20)
      .map(m => ({role: m.role, content: m.content}));

  try {
    // 与后端 sa-token.token-name: Authorization 保持一致，从用户仓库取 Token
    const token = useUserStore().userinfo.token || '';
    const response = await fetch('/api/ai/agent/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify({
        agentId: agentId.value,
        message: userMsg,
        history: history.slice(-10)
      })
    });

    const reader = response.body?.getReader();
    if (!reader) return;

    const decoder = new TextDecoder();
    let buffer = '';
    let assistantMsg = '';

    while (true) {
      const {done, value} = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, {stream: true});
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (!line.startsWith('data: ')) continue;
        const data = line.slice(6);
        if (data === '[DONE]') continue;

        try {
          const json = JSON.parse(data);
          handleAgentEvent(json, assistantMsg);
        } catch {
          // skip
        }
      }
    }
  } catch (err: any) {
    messages.value.push({role: 'assistant', content: '错误: ' + (err.message || '连接失败')});
  } finally {
    loading.value = false;
    scrollToBottom();
  }
}

const handleAgentEvent = (json: any, assistantMsg: string) => {
  switch (json.type) {
    case 'agent_start':
      messages.value.push({role: 'system', isThinking: true, content: `启动智能体: ${json.agentName}`});
      break;
    case 'thinking':
      messages.value.push({role: 'system', isThinking: true, content: `思考中 (迭代 ${json.iteration})...`});
      break;
    case 'tool_call':
      messages.value.push({
        role: 'system',
        isToolCall: true,
        toolName: json.tool,
        toolArgs: JSON.stringify(json.arguments, null, 2)
      });
      break;
    case 'tool_result':
      messages.value.push({
        role: 'system',
        isToolResult: true,
        toolName: json.tool,
        toolResult: json.result
      });
      break;
    case 'content':
      messages.value.push({role: 'assistant', content: json.content});
      break;
    case 'agent_complete':
      iterationCount.value = json.iterationCount || 0;
      messages.value.push({
        role: 'assistant',
        content: '',
        tokenUsage: {
          promptTokens: json.totalPromptTokens,
          completionTokens: json.totalCompletionTokens,
          totalTokens: json.totalTokens,
          cost: json.cost
        }
      });
      break;
  }
  scrollToBottom();
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  });
}
</script>

<style scoped>
.agent-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.iteration-info {
  font-size: 13px;
  color: #909399;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  line-height: 1.6;
  font-size: 14px;
}

.message.user .message-bubble {
  background: #409eff;
  color: #fff;
}

.tool-call, .tool-result, .thinking {
  padding: 8px;
}

.tool-args, .tool-result-content {
  margin-top: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px;
  max-height: 200px;
  overflow: auto;
}

.tool-args pre, .tool-result-content pre {
  margin: 0;
  white-space: pre-wrap;
  font-size: 12px;
  color: #606266;
}

.token-usage {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.typing-indicator {
  display: inline-flex;
  gap: 2px;
}

.dot {
  animation: typing 1.4s infinite;
  font-size: 24px;
  line-height: 1;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}

.chat-input {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
