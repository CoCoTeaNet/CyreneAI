<template>
  <div class="image-container">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card class="image-card">
          <template #header>
            <span>图片生成</span>
          </template>
          <el-form label-width="100px">
            <el-form-item label="提示词">
              <el-input v-model="prompt" type="textarea" :rows="4" placeholder="描述你想要生成的图片内容" maxlength="2000" show-word-limit/>
            </el-form-item>
            <el-form-item label="模型">
              <el-select v-model="modelId" placeholder="选择模型" style="width: 100%" filterable clearable>
                <el-option v-for="m in models" :key="m.id" :label="m.modelName + (m.providerType ? ' (' + m.providerType + ')' : '')" :value="m.id"/>
              </el-select>
            </el-form-item>
            <el-form-item label="尺寸">
              <el-select v-model="size" placeholder="选择图片尺寸" style="width: 100%">
                <el-option label="1024x1024 (方形)" value="1024x1024"/>
                <el-option label="1792x1024 (横版)" value="1792x1024"/>
                <el-option label="1024x1792 (竖版)" value="1024x1792"/>
                <el-option label="512x512" value="512x512"/>
              </el-select>
            </el-form-item>
            <el-form-item label="风格">
              <el-select v-model="style" placeholder="选择风格" style="width: 100%" clearable>
                <el-option label="生动 (Vivid)" value="vivid"/>
                <el-option label="自然 (Natural)" value="natural"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="PictureFilled" :loading="generating" @click="doGenerate" :disabled="!prompt">生成图片</el-button>
              <el-button :icon="Refresh" @click="resetForm" style="margin-left: 12px">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="image-card">
          <template #header>
            <span>生成结果</span>
          </template>
          <div v-if="generating" style="text-align: center; padding: 40px">
            <el-icon class="is-loading" :size="48"><Loading/></el-icon>
            <p style="margin-top: 16px; color: #999">正在生成中...</p>
          </div>
          <div v-else-if="imageUrl" class="image-result">
            <el-image :src="imageUrl" fit="contain" style="width: 100%; max-height: 400px" :preview-src-list="[imageUrl]"/>
            <div style="margin-top: 12px; text-align: center">
              <el-button :icon="Download" @click="downloadImage">下载</el-button>
              <el-button :icon="CopyDocument" @click="copyImageUrl">复制URL</el-button>
            </div>
          </div>
          <div v-else style="text-align: center; padding: 60px; color: #ccc">
            <el-icon :size="48"><PictureFilled/></el-icon>
            <p style="margin-top: 12px">输入提示词并点击生成</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>生成记录</span>
      </template>
      <el-table v-loading="loading" :data="records" style="width: 100%">
        <el-table-column prop="modelName" width="120" label="模型"/>
        <el-table-column prop="prompt" label="提示词" show-overflow-tooltip min-width="200"/>
        <el-table-column prop="imageSize" width="100" label="尺寸"/>
        <el-table-column prop="imageUrl" label="图片" width="100">
          <template #default="scope">
            <el-image :src="scope.row.imageUrl" style="width: 60px; height: 60px" fit="cover" :preview-src-list="[scope.row.imageUrl]" preview-teleported/>
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
import {generate, listEnabledModels, listRecordByPage} from "@/api/ai/image-api";
import {PictureFilled, Download, CopyDocument, Refresh, Loading} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";

const prompt = ref('');
const modelId = ref<any>(null);
const size = ref('1024x1024');
const style = ref('');
const generating = ref(false);
const imageUrl = ref('');
const models = ref<any[]>([]);
const loading = ref(false);
const records = ref<any[]>([]);
const total = ref(0);
const pageSize = ref(10);

onMounted(() => {
  loadModels();
  loadRecords(1);
});

const loadModels = () => {
  reqCommonFeedback(listEnabledModels(), (data: any) => {
    models.value = data || [];
  });
}

const doGenerate = () => {
  if (!prompt.value) return;
  generating.value = true;
  imageUrl.value = '';
  reqCommonFeedback(generate({
    prompt: prompt.value,
    modelId: modelId.value,
    size: size.value,
    style: style.value || undefined
  }), (url: string) => {
    imageUrl.value = url;
    loadRecords(1);
    generating.value = false;
  }, () => { generating.value = false; });
}

const resetForm = () => {
  prompt.value = '';
  modelId.value = null;
  size.value = '1024x1024';
  style.value = '';
  imageUrl.value = '';
}

const downloadImage = () => {
  const a = document.createElement('a');
  a.href = imageUrl.value;
  a.download = 'generated-image.png';
  a.click();
}

const copyImageUrl = () => {
  navigator.clipboard.writeText(imageUrl.value).then(() => {
    ElMessage.success('图片URL已复制');
  });
}

const loadRecords = (pageNo: number) => {
  loading.value = true;
  reqCommonFeedback(listRecordByPage({pageNo, pageSize: pageSize.value}), (data: any) => {
    records.value = data?.records || [];
    total.value = data?.total || 0;
    loading.value = false;
  });
}
</script>

<style scoped>
.image-container { padding: 20px; }
.image-card { height: 100%; }
.image-result { text-align: center; }
</style>
