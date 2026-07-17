<template>
  <div class="playground">
    <el-row :gutter="16">
      <!-- 左侧配置 -->
      <el-col :span="7">
        <el-card shadow="never" class="config">
          <template #header><span>运行配置</span></template>
          <el-form label-position="top">
            <el-form-item label="对比模型(可多选)">
              <el-select v-model="form.modelIds" multiple filterable placeholder="选择要对比的模型" style="width: 100%">
                <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id"/>
              </el-select>
            </el-form-item>
            <el-form-item label="System 提示词">
              <el-input v-model="form.systemPrompt" type="textarea" :rows="3" placeholder="可选, 设定模型角色"/>
            </el-form-item>
            <el-form-item label="用户提示词">
              <el-input v-model="form.prompt" type="textarea" :rows="5" placeholder="输入要测试的提示词"/>
            </el-form-item>
            <el-form-item :label="'Temperature: ' + form.temperature">
              <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width: 100%"/>
            </el-form-item>
            <el-form-item label="最大输出 Token">
              <el-input-number v-model="form.maxTokens" :min="0" :step="128" placeholder="不限"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="VideoPlay" :loading="running" style="width: 100%" @click="onRun">
                运行对比
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧结果 -->
      <el-col :span="17">
        <el-empty v-if="!results.length && !running" description="配置并运行以查看模型输出对比" style="margin-top: 80px"/>
        <div v-else class="results" :class="'cols-' + Math.min(results.length || 1, 3)">
          <el-card v-for="(r, i) in results" :key="i" shadow="never" class="result-card">
            <template #header>
              <div class="result-header">
                <span class="model-name">{{ r.modelName || ('模型#' + r.modelId) }}</span>
                <el-tag :type="r.status === 'success' ? 'success' : 'danger'" size="small">
                  {{ r.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </div>
            </template>
            <div v-if="r.status === 'success'">
              <div class="content">{{ r.content }}</div>
              <el-divider/>
              <div class="metrics">
                <el-tag size="small" type="info">输入 {{ r.promptTokens }}</el-tag>
                <el-tag size="small" type="info">输出 {{ r.completionTokens }}</el-tag>
                <el-tag size="small" type="info">共 {{ r.totalTokens }} tokens</el-tag>
                <el-tag size="small">￥{{ Number(r.cost || 0).toFixed(6) }}</el-tag>
                <el-tag size="small" type="warning">{{ r.latencyMs }} ms</el-tag>
              </div>
            </div>
            <el-alert v-else :title="r.errorMsg || '调用失败'" type="error" :closable="false" show-icon/>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {run} from "@/api/ai/playground-api";
import {listEnabled} from "@/api/ai/model-api";
import {ElMessage} from "element-plus";
import {VideoPlay} from "@element-plus/icons-vue";

const models = ref<any[]>([]);
const results = ref<any[]>([]);
const running = ref<boolean>(false);
const form = ref<any>({modelIds: [], systemPrompt: '', prompt: '', temperature: 0.7, maxTokens: undefined});

onMounted(() => {
  reqCommonFeedback(listEnabled(), (d: any) => models.value = (d || []).filter((m: any) => m.modelType === 'chat'));
});

const onRun = () => {
  if (!form.value.modelIds.length) return ElMessage.warning('请至少选择一个模型');
  if (!form.value.prompt || !form.value.prompt.trim()) return ElMessage.warning('请输入提示词');
  running.value = true;
  results.value = [];
  run(form.value).then((res: any) => {
    running.value = false;
    if (res.code === 200) {
      results.value = res.data || [];
    } else {
      ElMessage.error(res.message || '运行失败');
    }
  }).catch(() => {
    running.value = false;
    ElMessage.error('运行失败');
  });
}
</script>

<style scoped>
.playground {padding: 4px;}
.config {position: sticky; top: 8px;}
.results {display: grid; gap: 16px;}
.results.cols-1 {grid-template-columns: 1fr;}
.results.cols-2 {grid-template-columns: 1fr 1fr;}
.results.cols-3 {grid-template-columns: 1fr 1fr 1fr;}
.result-header {display: flex; justify-content: space-between; align-items: center;}
.model-name {font-weight: 600;}
.content {white-space: pre-wrap; word-break: break-word; font-size: 13px; line-height: 1.6; max-height: 420px; overflow-y: auto;}
.metrics {display: flex; flex-wrap: wrap; gap: 6px;}
</style>
