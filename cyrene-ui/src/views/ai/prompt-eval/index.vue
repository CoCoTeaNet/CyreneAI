<template>
  <table-manage>
    <template #search>
      <el-form-item label="模板">
        <el-select v-model="pageParam.searchObject.templateId" placeholder="按模板筛选" clearable style="width: 200px">
          <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="分组">
        <el-select v-model="pageParam.searchObject.variant" placeholder="A/B" clearable style="width: 120px">
          <el-option label="A" value="A"/>
          <el-option label="B" value="B"/>
        </el-select>
      </el-form-item>
      <el-form-item label="评分">
        <el-select v-model="pageParam.searchObject.rating" placeholder="评分" clearable style="width: 120px">
          <el-option v-for="n in 5" :key="n" :label="n + '星'" :value="n"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="MagicStick" type="primary" @click="onRunDialog">执行评估</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%">
        <el-table-column prop="templateName" width="180" label="模板"/>
        <el-table-column prop="templateVersion" width="80" label="版本">
          <template #default="scope">v{{ scope.row.templateVersion || '-' }}</template>
        </el-table-column>
        <el-table-column prop="modelName" width="150" label="模型"/>
        <el-table-column prop="variant" width="70" label="分组"/>
        <el-table-column prop="renderedPrompt" min-width="220" label="Prompt" show-overflow-tooltip/>
        <el-table-column prop="output" min-width="220" label="输出" show-overflow-tooltip/>
        <el-table-column prop="latencyMs" width="100" label="耗时(ms)"/>
        <el-table-column prop="totalTokens" width="90" label="Tokens"/>
        <el-table-column prop="cost" width="100" label="花费(元)"/>
        <el-table-column prop="rating" width="120" label="评分">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" @change="(v: number) => onRate(scope.row, v)"/>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="140">
          <template #default="scope">
            <el-button size="small" @click="onView(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes="[5,10,15]"
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <!-- 执行评估 -->
      <el-dialog v-model="runDialogVisible" title="执行 Prompt 评估" width="720px">
        <el-form label-width="120px" :model="runForm">
          <el-form-item label="模板">
            <el-select v-model="runForm.templateId" placeholder="选择模板(可选)" clearable style="width: 100%">
              <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="模板版本">
            <el-input-number v-model="runForm.version" :min="1" placeholder="不填=当前版本"/>
          </el-form-item>
          <el-form-item label="直接输入内容" v-if="!runForm.templateId">
            <el-input v-model="runForm.promptContent" type="textarea" :rows="4"
                      placeholder="或直接输入 prompt 内容(与模板二选一)"/>
          </el-form-item>
          <el-form-item label="变量(JSON)">
            <el-input v-model="runForm.variablesText" type="textarea" :rows="3"
                      placeholder='{"name": "张三"}'/>
          </el-form-item>
          <el-form-item label="模型">
            <el-select v-model="runForm.modelId" placeholder="选择模型" style="width: 100%">
              <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="A/B 测试">
            <el-select v-model="runForm.abTestId" placeholder="可选" clearable style="width: 100%">
              <el-option v-for="t in abTests" :key="t.id" :label="t.name" :value="t.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="分组">
            <el-radio-group v-model="runForm.variant">
              <el-radio label="A">A</el-radio>
              <el-radio label="B">B</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="执行结果" v-if="runResult">
            <el-input :model-value="runResult" type="textarea" :rows="6" readonly/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button type="primary" :loading="running" @click="doRun">执行</el-button>
          <el-button @click="runDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 详情 -->
      <el-dialog v-model="detailDialogVisible" title="评估详情" width="720px">
        <el-descriptions :column="2" border v-if="detailRow">
          <el-descriptions-item label="模板">{{ detailRow.templateName }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ detailRow.templateVersion }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ detailRow.modelName }}</el-descriptions-item>
          <el-descriptions-item label="分组">{{ detailRow.variant || '-' }}</el-descriptions-item>
          <el-descriptions-item label="耗时(ms)">{{ detailRow.latencyMs }}</el-descriptions-item>
          <el-descriptions-item label="总Tokens">{{ detailRow.totalTokens }}</el-descriptions-item>
          <el-descriptions-item label="花费(元)">{{ detailRow.cost }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detailRow.rating || '-' }}</el-descriptions-item>
          <el-descriptions-item label="输入变量" :span="2">{{ detailRow.inputVariables }}</el-descriptions-item>
          <el-descriptions-item label="Rendered Prompt" :span="2">
            <div style="white-space: pre-wrap">{{ detailRow.renderedPrompt }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="输出" :span="2">
            <div style="white-space: pre-wrap">{{ detailRow.output }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="反馈" :span="2">{{ detailRow.feedback }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {listByPage, run, rate} from "@/api/ai/prompt-eval-api";
import {listEnabled as listTemplates} from "@/api/ai/prompt-template-api";
import {listEnabled as listModels} from "@/api/ai/model-api";
import {listByPage as listAbTestsPage} from "@/api/ai/prompt-ab-test-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessage} from "element-plus";
import {Search, RefreshRight, MagicStick} from "@element-plus/icons-vue";

const pageParam = ref<any>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);
const templates = ref<any[]>([]);
const models = ref<any[]>([]);
const abTests = ref<any[]>([]);

const runDialogVisible = ref<boolean>(false);
const runForm = ref<any>({variant: 'A', variablesText: ''});
const runResult = ref<string>('');
const running = ref<boolean>(false);

const detailDialogVisible = ref<boolean>(false);
const detailRow = ref<any>(null);

onMounted(() => {
  loadTableData();
  listTemplates().then((res: any) => templates.value = res.data || []);
  listModels().then((res: any) => models.value = (res.data || []).filter((m: any) => m.modelType === 'chat' || m.modelType === 'vision'));
  listAbTestsPage({pageNo: 1, pageSize: 100, aiPromptAbTest: {}}).then((res: any) => abTests.value = (res.data && res.data.records) || []);
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiPromptEval: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}

const onRunDialog = () => {
  runForm.value = {variant: 'A', variablesText: ''};
  runResult.value = '';
  runDialogVisible.value = true;
}
const doRun = () => {
  let variables: any = {};
  if (runForm.value.variablesText) {
    try { variables = JSON.parse(runForm.value.variablesText); }
    catch { return ElMessage.error('变量必须为合法JSON'); }
  }
  if (!runForm.value.modelId) return ElMessage.error('请选择模型');
  if (!runForm.value.templateId && !runForm.value.promptContent) return ElMessage.error('请提供模板或直接输入内容');
  running.value = true;
  run({
    templateId: runForm.value.templateId,
    version: runForm.value.version,
    promptContent: runForm.value.promptContent,
    variables,
    modelId: runForm.value.modelId,
    abTestId: runForm.value.abTestId,
    variant: runForm.value.variant
  }).then((res: any) => {
    runResult.value = res.data ? (res.data.output || '') : '';
    ElMessage.success('执行成功');
    loadTableData();
  }).finally(() => running.value = false);
}
const onRate = (row: any, rating: number) => {
  rate({id: row.id, rating}).then(() => ElMessage.success('评分已提交'));
}
const onView = (row: any) => {
  detailRow.value = row;
  detailDialogVisible.value = true;
}
</script>

<style scoped></style>
