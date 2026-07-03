<template>
  <div class="tts-container">
    <el-card class="tts-card">
      <template #header>
        <span>文本转语音</span>
      </template>
      <el-form label-width="100px">
        <el-form-item label="合成文本">
          <el-input v-model="text" type="textarea" :rows="6" placeholder="输入要合成语音的文本内容" maxlength="4096" show-word-limit/>
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="modelId" placeholder="选择TTS模型" style="width: 300px" filterable clearable>
            <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="音色">
          <el-select v-model="voice" placeholder="选择音色" style="width: 300px">
            <el-option v-for="v in currentVoices" :key="v" :label="v" :value="v"/>
          </el-select>
        </el-form-item>
        <el-form-item label="语速">
          <el-slider v-model="speed" :min="0.25" :max="4" :step="0.25" style="width: 300px"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Headset" :loading="synthesizing" @click="doSynthesize" :disabled="!text">合成语音</el-button>
        </el-form-item>
      </el-form>

      <div v-if="audioUrl" style="margin-top: 20px">
        <el-divider/>
        <h4>播放合成结果:</h4>
        <audio :src="audioUrl" controls style="width: 100%" @ended="playing = false" @play="playing = true"/>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>合成记录</span>
      </template>
      <el-table v-loading="loading" :data="records" style="width: 100%">
        <el-table-column prop="modelName" width="120" label="模型"/>
        <el-table-column prop="text" label="文本" show-overflow-tooltip min-width="250"/>
        <el-table-column prop="voice" width="80" label="音色"/>
        <el-table-column prop="fileSize" width="100" label="大小">
          <template #default="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" width="180" label="时间"/>
      </el-table>
      <el-pagination v-if="total > 0" background layout="total, prev, pager, next"
                     :total="total" :page-size="pageSize" style="margin-top: 16px; justify-content: center"
                     @current-change="loadRecords"/>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {synthesizeBlob as ttsSynthesize, listEnabledModels, listRecordByPage} from "@/api/ai/tts-api";
import {Headset} from "@element-plus/icons-vue";

const text = ref('');
const modelId = ref<any>(null);
const voice = ref('');
const speed = ref(1);
const synthesizing = ref(false);
const audioUrl = ref('');
const playing = ref(false);
const models = ref<any[]>([]);
const loading = ref(false);
const records = ref<any[]>([]);
const total = ref(0);
const pageSize = ref(10);

const currentVoices = computed(() => {
  const m = models.value.find(m => m.id === modelId.value);
  return m?.voices || [];
});

watch(modelId, (val) => {
  if (val) {
    const m = models.value.find(m => m.id === val);
    voice.value = m?.defaultVoice || (currentVoices.value.length > 0 ? currentVoices.value[0] : '');
  }
});

onMounted(() => {
  loadModels();
  loadRecords(1);
});

const loadModels = () => {
  reqCommonFeedback(listEnabledModels(), (data: any) => {
    models.value = data || [];
  });
}

const doSynthesize = async () => {
  if (!text.value) return;
  synthesizing.value = true;
  audioUrl.value = '';
  try {
    const resp = await ttsSynthesize({
      text: text.value,
      modelId: modelId.value,
      voice: voice.value,
      speed: speed.value
    });
    const blob = new Blob([resp.data], {type: 'audio/mpeg'});
    audioUrl.value = URL.createObjectURL(blob);
    loadRecords(1);
  } catch (e: any) {
    console.error('TTS合成失败', e);
  } finally {
    synthesizing.value = false;
  }
}

const loadRecords = (pageNo: number) => {
  loading.value = true;
  reqCommonFeedback(listRecordByPage({pageNo, pageSize: pageSize.value}), (data: any) => {
    records.value = data?.records || [];
    total.value = data?.total || 0;
    loading.value = false;
  });
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) { size /= 1024; i++; }
  return size.toFixed(1) + units[i];
}
</script>

<style scoped>
.tts-container { max-width: 900px; margin: 0 auto; padding: 20px; }
</style>
