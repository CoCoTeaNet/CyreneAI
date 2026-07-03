<template>
  <div class="stt-container">
    <el-card class="stt-card">
      <template #header>
        <span>语音转文字</span>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="上传文件" name="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept="audio/*"
            :on-change="handleFileChange"
          >
            <el-button type="primary" :icon="Upload">选择音频文件</el-button>
            <template #tip>
              <span style="font-size: 12px; color: #999; margin-left: 8px">支持 mp3, wav, m4a, ogg 等格式</span>
            </template>
          </el-upload>
          <div v-if="selectedFile" style="margin-top: 16px">
            <el-tag>{{ selectedFile.name }}</el-tag>
            <el-button type="success" :icon="Microphone" :loading="transcribing" style="margin-left: 12px" @click="doTranscribeFile">开始转写</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="音频URL" name="url">
          <el-input v-model="audioUrl" placeholder="输入音频文件的URL地址" style="width: 500px"/>
          <el-button type="success" :icon="Microphone" :loading="transcribing" style="margin-left: 12px; margin-top: 12px" @click="doTranscribeUrl">开始转写</el-button>
        </el-tab-pane>
      </el-tabs>

      <div v-if="transcript" style="margin-top: 20px">
        <el-divider/>
        <h4>转写结果:</h4>
        <el-input type="textarea" :rows="6" v-model="transcript" readonly/>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>转写记录</span>
      </template>
      <el-table v-loading="loading" :data="records" style="width: 100%">
        <el-table-column prop="modelName" width="120" label="模型"/>
        <el-table-column prop="transcript" label="转写文本" show-overflow-tooltip min-width="300"/>
        <el-table-column prop="fileSize" width="100" label="文件大小">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
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
import {onMounted, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {transcribe, transcribeUrl, listRecordByPage} from "@/api/ai/stt-api";
import {Upload, Microphone} from "@element-plus/icons-vue";

const activeTab = ref('file');
const selectedFile = ref<any>(null);
const audioUrl = ref('');
const transcript = ref('');
const transcribing = ref(false);
const loading = ref(false);
const records = ref<any[]>([]);
const total = ref(0);
const pageSize = ref(10);

onMounted(() => {
  loadRecords(1);
});

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw;
}

const doTranscribeFile = async () => {
  if (!selectedFile.value) return;
  transcribing.value = true;
  transcript.value = '';
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    const res = await transcribe(formData);
    if (res.data?.text) {
      transcript.value = res.data.text;
      reqSuccessFeedback(Promise.resolve(res), '转写成功', () => loadRecords(1));
    }
  } catch (e: any) {
    transcript.value = '转写失败: ' + (e.message || '未知错误');
  } finally {
    transcribing.value = false;
  }
}

const doTranscribeUrl = () => {
  if (!audioUrl.value) return;
  transcribing.value = true;
  transcript.value = '';
  reqCommonFeedback(transcribeUrl({audioUrl: audioUrl.value}), (res: any) => {
    transcript.value = res?.text || '';
    loadRecords(1);
    transcribing.value = false;
  }, () => { transcribing.value = false; });
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
.stt-container { max-width: 900px; margin: 0 auto; padding: 20px; }
</style>
