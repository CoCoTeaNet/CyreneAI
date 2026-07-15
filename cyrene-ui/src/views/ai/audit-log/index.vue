<template>
  <table-manage>
    <template #search>
      <el-form-item label="用户 ID">
        <el-input placeholder="用户 ID" v-model="pageParam.searchObject.userId"/>
      </el-form-item>
      <el-form-item label="Key ID">
        <el-input placeholder="API Key ID" v-model="pageParam.searchObject.apiKeyId"/>
      </el-form-item>
      <el-form-item label="模型 ID">
        <el-input placeholder="模型 ID" v-model="pageParam.searchObject.modelId"/>
      </el-form-item>
      <el-form-item label="端点">
        <el-input placeholder="/ai/chat/stream" v-model="pageParam.searchObject.endpoint"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select style="width: 130px" v-model="pageParam.searchObject.status" clearable>
          <el-option label="success" value="success"/>
          <el-option label="error" value="error"/>
          <el-option label="blocked" value="blocked"/>
        </el-select>
      </el-form-item>
      <el-form-item label="时间段">
        <el-date-picker v-model="dateRange" type="datetimerange" range-separator="~"
                        start-placeholder="开始" end-placeholder="结束"
                        value-format="YYYY-MM-DDTHH:mm:ss" style="width: 340px"/>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="onSearch">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%">
        <el-table-column prop="createTime" width="170" label="时间"/>
        <el-table-column prop="userName" width="120" label="用户"/>
        <el-table-column prop="apiKeyName" width="140" label="API Key"/>
        <el-table-column prop="endpoint" width="180" label="端点"/>
        <el-table-column prop="modelName" width="140" label="模型"/>
        <el-table-column prop="providerType" width="120" label="提供者"/>
        <el-table-column label="Token" width="180">
          <template #default="scope">
            <span>提示: {{ scope.row.promptTokens || 0 }}</span><br/>
            <span>输出: {{ scope.row.completionTokens || 0 }}</span><br/>
            <span>总计: {{ scope.row.totalTokens || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cost" width="100" label="费用"/>
        <el-table-column prop="latencyMs" width="100" label="耗时(ms)"/>
        <el-table-column prop="status" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" width="140" label="IP"/>
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="scope">
            <el-button size="small" @click="onShowDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes="[10,20,50]"
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <el-dialog v-model="detailVisible" title="审计日志详情" width="720px">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ detailRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="请求 ID">{{ detailRow.requestId }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ detailRow.userName }}</el-descriptions-item>
          <el-descriptions-item label="API Key">{{ detailRow.apiKeyName }}</el-descriptions-item>
          <el-descriptions-item label="端点">{{ detailRow.endpoint }}</el-descriptions-item>
          <el-descriptions-item label="HTTP 方法">{{ detailRow.httpMethod }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ detailRow.modelName }}</el-descriptions-item>
          <el-descriptions-item label="提供者">{{ detailRow.providerType }}</el-descriptions-item>
          <el-descriptions-item label="Token 使用">
            {{ detailRow.promptTokens || 0 }} / {{ detailRow.completionTokens || 0 }} / {{ detailRow.totalTokens || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="费用">{{ detailRow.cost }}</el-descriptions-item>
          <el-descriptions-item label="耗时(ms)">{{ detailRow.latencyMs }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detailRow.status)">{{ detailRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP">{{ detailRow.ip }}</el-descriptions-item>
          <el-descriptions-item label="User-Agent" :span="2">{{ detailRow.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="Prompt 片段" :span="2">
            <div style="white-space: pre-wrap; word-break: break-all;">{{ detailRow.promptSnippet }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="输出片段" :span="2">
            <div style="white-space: pre-wrap; word-break: break-all;">{{ detailRow.outputSnippet }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">{{ detailRow.errorMsg }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {listByPage} from "@/api/ai/audit-log-api";
import TableManage from "@/components/container/TableManage.vue";
import {Search, RefreshRight} from "@element-plus/icons-vue";

const detailVisible = ref<boolean>(false);
const detailRow = ref<any>({});
const dateRange = ref<any>(null);
const pageParam = ref<any>({pageNo: 1, pageSize: 20, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 20, total: 0, records: []});
const loading = ref<boolean>(true);

onMounted(() => loadTableData());

const statusTag = (s: string) => {
  if (s === 'success') return 'success';
  if (s === 'blocked') return 'warning';
  if (s === 'error') return 'danger';
  return 'info';
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const search = {...pageParam.value.searchObject};
  if (dateRange.value && dateRange.value.length === 2) {
    search.startTime = dateRange.value[0];
    search.endTime = dateRange.value[1];
  }
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiAuditLog: search
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onSearch = () => {pageParam.value.pageNo = 1; loadTableData();}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {}; dateRange.value = null;}
const onShowDetail = (row: any) => {detailRow.value = row; detailVisible.value = true;}
</script>

<style scoped></style>
