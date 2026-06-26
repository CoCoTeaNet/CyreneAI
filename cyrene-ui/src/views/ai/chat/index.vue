<template>
  <div class="chat-layout">
    <!-- Sidebar -->
    <div class="chat-sidebar">
      <div class="chat-sidebar-header">
        <el-button type="primary" :icon="Plus" @click="newConversation">New Chat</el-button>
        <el-button :icon="Upload" circle @click="importChat" style="margin-left: 8px" />
      </div>
      <div class="chat-sidebar-list">
        <div
          v-for="conv in conversationList"
          :key="conv.id"
          :class="['chat-sidebar-item', { active: currentConversationId === conv.id }]"
          @click="loadConversation(conv)"
        >
          <span class="chat-sidebar-title">{{ conv.title || 'New Chat' }}</span>
          <div class="chat-sidebar-actions">
            <el-button
              :icon="Download"
              size="small"
              circle
              class="chat-sidebar-action"
              @click.stop="exportChat(conv.id)"
            />
            <el-button
              :icon="Delete"
              size="small"
              type="danger"
              circle
              class="chat-sidebar-action"
              @click.stop="deleteConversation(conv.id)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Main Chat -->
    <div class="chat-container">
      <!-- Top Bar -->
      <div class="chat-topbar">
        <div class="chat-topbar-left">
          <span class="chat-title">AI Chat</span>
          <el-select
            v-model="selectedModelId"
            placeholder="Select Model"
            style="width: 260px; margin-left: 16px"
            filterable
            clearable
            @change="loadModels"
          >
            <el-option
              v-for="m in modelList"
              :key="m.id"
              :label="`${m.modelName} (${m.providerName})`"
              :value="m.id"
            />
          </el-select>
        </div>
        <div class="chat-topbar-right">
          <el-button :icon="Setting" circle @click="showParams = !showParams" />
          <el-button :icon="Refresh" circle @click="loadModels" />
        </div>
      </div>

      <!-- Params Panel -->
      <el-collapse-transition>
        <div v-show="showParams" class="chat-params-panel">
          <el-form :inline="true" :model="chatParams" size="small">
            <el-form-item label="Temperature">
              <el-slider v-model="chatParams.temperature" :min="0" :max="2" :step="0.1" style="width: 120px" />
            </el-form-item>
            <el-form-item label="Top P">
              <el-slider v-model="chatParams.topP" :min="0" :max="1" :step="0.05" style="width: 120px" />
            </el-form-item>
            <el-form-item label="Max Tokens">
              <el-input-number v-model="chatParams.maxTokens" :min="1" :max="4096" :step="64" />
            </el-form-item>
            <el-form-item label="System Prompt">
              <el-input v-model="chatParams.systemPrompt" placeholder="Optional system prompt" style="width: 300px" />
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <!-- Messages -->
      <div ref="messageContainer" class="chat-messages">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-empty description="Start a conversation" />
        </div>
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="['chat-msg', msg.role === 'user' ? 'chat-msg-user' : 'chat-msg-assistant']"
        >
          <div class="chat-msg-avatar">
            <el-avatar :icon="msg.role === 'user' ? UserFilled : Promotion" :size="36" />
          </div>
          <div class="chat-msg-content">
            <div class="chat-msg-bubble" v-html="renderContent(msg)" />
            <div v-if="msg.tokenUsage" class="chat-msg-tokens">
              Tokens: {{ msg.tokenUsage.promptTokens }} (prompt) + {{ msg.tokenUsage.completionTokens }} (completion) = {{ msg.tokenUsage.totalTokens }} (total)
            </div>
          </div>
        </div>
        <div v-if="streaming" class="chat-streaming-indicator">
          <span class="dot-pulse" />
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="3"
          placeholder="Type your message... (Enter to send, Shift+Enter for newline)"
          :disabled="streaming"
          @keydown.enter.exact="onSend"
        />
        <div class="chat-input-actions">
          <el-button type="primary" :disabled="!userInput.trim() || streaming" @click="onSend">
            Send
          </el-button>
          <el-button v-if="streaming" type="danger" @click="stopStream">
            Stop
          </el-button>
          <el-button :disabled="messages.length === 0" @click="clearChat">
            Clear
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, nextTick} from 'vue';
import {marked} from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css';
import {Refresh, Setting, Plus, Delete, Download, Upload, UserFilled, Promotion} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {listEnabled} from '@/api/ai/chat-api';
import {listConversations, createConversation, deleteConversation as apiDeleteConversation, getConversationMessages, exportConversation, importConversation} from '@/api/ai/conversation-api';
import {useChatStream, type ChatMessage, type ChatParams} from '@/composables/useChatStream';

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

const modelList = ref<any[]>([]);
const selectedModelId = ref<string | null>(null);
const userInput = ref('');
const messages = ref<ChatMessage[]>([]);
const messageContainer = ref<HTMLElement | null>(null);
const showParams = ref(false);
const chatParams = ref<ChatParams>({
  temperature: 0.7,
  topP: 0.9,
  maxTokens: 2048,
  systemPrompt: ''
});
const {streaming, sendMessage, stopStream} = useChatStream();

const conversationList = ref<any[]>([]);
const currentConversationId = ref<string | null>(null);

onMounted(() => {
  loadModels();
  loadConversations();
});

function loadModels() {
  listEnabled().then((res: any) => {
    const data = res?.data || [];
    modelList.value = data;
    if (data.length > 0 && !selectedModelId.value) {
      const def = data.find((m: any) => m.isDefault === 1);
      selectedModelId.value = def ? def.id : data[0].id;
    }
  }).catch(() => {
    ElMessage.error('Failed to load models');
  });
}

function loadConversations() {
  listConversations().then((res: any) => {
    conversationList.value = res?.data || [];
  }).catch(() => {
    ElMessage.error('Failed to load conversations');
  });
}

async function newConversation() {
  try {
    const res = await createConversation({title: 'New Chat'});
    const newConv = res?.data;
    if (newConv) {
      conversationList.value.unshift(newConv);
      currentConversationId.value = newConv.id;
      messages.value = [];
    }
  } catch {
    ElMessage.error('Failed to create conversation');
  }
}

async function loadConversation(conv: any) {
  currentConversationId.value = conv.id;
  try {
    const res = await getConversationMessages(conv.id);
    const data = res?.data || [];
    messages.value = data.map((msg: any) => ({
      role: msg.role,
      content: msg.content,
      tokenUsage: msg.totalTokens > 0 ? {
        promptTokens: msg.promptTokens,
        completionTokens: msg.completionTokens,
        totalTokens: msg.totalTokens
      } : undefined
    }));
  } catch {
    ElMessage.error('Failed to load messages');
  }
}

async function deleteConversation(id: string) {
  try {
    await ElMessageBox.confirm('Are you sure to delete this conversation?', 'Warning', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning'
    });
    await apiDeleteConversation(id);
    conversationList.value = conversationList.value.filter(c => c.id !== id);
    if (currentConversationId.value === id) {
      currentConversationId.value = null;
      messages.value = [];
    }
    ElMessage.success('Deleted');
  } catch {
    // User cancelled
  }
}

async function exportChat(id: string) {
  try {
    const res = await exportConversation(id);
    const data = res?.data;
    if (data) {
      const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `chat-${id}.json`;
      a.click();
      URL.revokeObjectURL(url);
      ElMessage.success('Exported');
    }
  } catch {
    ElMessage.error('Export failed');
  }
}

function importChat() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  input.onchange = async (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const data = JSON.parse(text);
      await importConversation({
        title: data.conversation?.title || 'Imported Chat',
        messages: data.messages || []
      });
      await loadConversations();
      ElMessage.success('Imported');
    } catch {
      ElMessage.error('Import failed');
    }
  };
  input.click();
}

async function onSend() {
  const content = userInput.value.trim();
  if (!content || streaming.value) return;
  userInput.value = '';
  await sendMessage(messages, selectedModelId.value, content, chatParams.value, currentConversationId.value);
  await scrollToBottom();
}

function clearChat() {
  messages.value = [];
}

function renderContent(msg: ChatMessage): string {
  if (!msg.content) return '';
  try {
    return marked.parse(msg.content) as string;
  } catch {
    return msg.content;
  }
}

async function scrollToBottom() {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: calc(100vh - 120px);
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
}

.chat-sidebar {
  width: 260px;
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-overlay);
}

.chat-sidebar-header {
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.chat-sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
}

.chat-sidebar-item:hover {
  background: var(--el-fill-color-light);
}

.chat-sidebar-item.active {
  background: var(--el-color-primary-light-3);
  color: #fff;
}

.chat-sidebar-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.chat-sidebar-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-sidebar-item:hover .chat-sidebar-actions {
  opacity: 1;
}

.chat-sidebar-action {
  padding: 4px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-overlay);
  flex-shrink: 0;
}

.chat-topbar-left {
  display: flex;
  align-items: center;
}

.chat-title {
  font-size: 18px;
  font-weight: 600;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.chat-msg {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.chat-msg-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-msg-assistant {
  align-self: flex-start;
}

.chat-msg-avatar {
  flex-shrink: 0;
}

.chat-msg-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
  font-size: 14px;
}

.chat-msg-tokens {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  padding: 0 4px;
}

.chat-msg-user .chat-msg-bubble {
  background: var(--el-color-primary-light-3);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-msg-assistant .chat-msg-bubble {
  background: var(--el-fill-color-light);
  border-bottom-left-radius: 4px;
}

.chat-msg-bubble :deep(pre) {
  background: #1e1e1e;
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
  margin: 8px 0;
}

.chat-msg-bubble :deep(code) {
  font-size: 13px;
}

.chat-msg-bubble :deep(p) {
  margin: 4px 0;
}

.chat-streaming-indicator {
  align-self: flex-start;
  padding-left: 48px;
}

.dot-pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: pulse 1.2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-overlay);
  flex-shrink: 0;
}

.chat-input-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.chat-params-panel {
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-overlay);
}

.chat-params-panel :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.chat-params-panel :deep(.el-form-item__label) {
  font-size: 13px;
}
</style>
