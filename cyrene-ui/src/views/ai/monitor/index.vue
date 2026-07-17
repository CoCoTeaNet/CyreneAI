<template>
  <div class="ai-monitor">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <span class="title">模型调用监控</span>
      <div class="right">
        <el-radio-group v-model="days" size="small" @change="loadAll">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
          <el-radio-button :label="90">近90天</el-radio-button>
        </el-radio-group>
        <el-button size="small" :icon="RefreshRight" @click="loadAll">刷新</el-button>
      </div>
    </div>

    <!-- 总览卡片 -->
    <el-row :gutter="16" class="cards">
      <el-col :span="5">
        <el-card shadow="hover"><div class="stat"><div class="label">调用次数</div><div class="value">{{ overview.requestCount || 0 }}</div></div></el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="hover"><div class="stat"><div class="label">Token 消耗</div><div class="value">{{ fmtNum(overview.totalTokens) }}</div></div></el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="hover"><div class="stat"><div class="label">总花费(元)</div><div class="value">{{ fmtCost(overview.totalCost) }}</div></div></el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover"><div class="stat"><div class="label">平均延迟</div><div class="value">{{ Math.round(overview.avgLatency || 0) }} ms</div></div></el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="hover"><div class="stat"><div class="label">成功率</div><div class="value">{{ overview.successRate || 0 }}%</div></div></el-card>
      </el-col>
    </el-row>

    <!-- 预算告警 -->
    <div class="budget-alerts" v-if="budgetAlerts.length">
      <el-alert v-for="b in budgetAlerts" :key="b.id" :closable="false"
                :type="b.exceeded ? 'error' : 'warning'" show-icon class="alert-item">
        <template #title>
          预算「{{ b.name }}」({{ scopeName(b) }} / {{ periodLabel(b.period) }}) 已用
          {{ fmtCost(b.usedCost) }} / {{ fmtCost(b.amount) }} 元，占比 {{ b.usagePercent }}%
          <span v-if="b.exceeded">— 已超支</span>
          <span v-else>— 达到告警阈值</span>
        </template>
      </el-alert>
    </div>

    <!-- Token / 花费趋势 -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-header">
          <span>Token 使用趋势</span>
          <el-radio-group v-model="groupType" size="small" @change="loadTrend">
            <el-radio-button label="day">日</el-radio-button>
            <el-radio-button label="week">周</el-radio-button>
            <el-radio-button label="month">月</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <mini-line-chart v-if="trend.length" :data="trendChartData" :height="200"/>
      <el-empty v-else description="暂无数据" :image-size="80"/>
    </el-card>

    <el-row :gutter="16">
      <!-- 模型调用排行 -->
      <el-col :span="12">
        <el-card shadow="never" class="block">
          <template #header><span>模型调用排行</span></template>
          <div v-if="modelRankList.length">
            <div v-for="m in modelRankList" :key="m.modelId" class="rank-row">
              <div class="rank-name">{{ m.modelName || '未知' }}<el-tag size="small" type="info" class="ml">{{ m.providerType }}</el-tag></div>
              <el-progress :percentage="rankPercent(m.requestCount, maxModelReq)" :stroke-width="14" :show-text="false"/>
              <div class="rank-meta">{{ m.requestCount }} 次 · {{ fmtNum(m.totalTokens) }} tokens · {{ Math.round(m.avgLatency || 0) }}ms</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
      <!-- 用户调用排行 -->
      <el-col :span="12">
        <el-card shadow="never" class="block">
          <template #header><span>用户调用排行</span></template>
          <div v-if="userRankList.length">
            <div v-for="u in userRankList" :key="u.userId" class="rank-row">
              <div class="rank-name">{{ u.userName || ('用户#' + u.userId) }}</div>
              <el-progress :percentage="rankPercent(u.requestCount, maxUserReq)" :stroke-width="14" :show-text="false" color="#67c23a"/>
              <div class="rank-meta">{{ u.requestCount }} 次 · {{ fmtNum(u.totalTokens) }} tokens · {{ fmtCost(u.cost) }} 元</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80"/>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 成本分析 -->
      <el-col :span="14">
        <el-card shadow="never" class="block">
          <template #header>
            <div class="block-header">
              <span>成本分析</span>
              <el-radio-group v-model="costDim" size="small" @change="loadCostStat">
                <el-radio-button label="model">按模型</el-radio-button>
                <el-radio-button label="user">按用户</el-radio-button>
                <el-radio-button label="time">按时间</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="costStatList" size="small" max-height="300">
            <el-table-column prop="dimName" label="维度"/>
            <el-table-column prop="requestCount" label="调用次数" width="100"/>
            <el-table-column label="Token" width="120"><template #default="s">{{ fmtNum(s.row.totalTokens) }}</template></el-table-column>
            <el-table-column label="花费(元)" width="120"><template #default="s">{{ fmtCost(s.row.cost) }}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <!-- 成本优化建议 -->
      <el-col :span="10">
        <el-card shadow="never" class="block">
          <template #header><span>成本优化建议</span></template>
          <el-timeline>
            <el-timeline-item v-for="(s, i) in suggestions" :key="i" :type="tlType(s.level)" :hollow="true">
              <div class="sug-title">{{ s.title }}</div>
              <div class="sug-detail">{{ s.detail }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import * as monitorApi from "@/api/ai/monitor-api";
import {listStatus as budgetStatusApi} from "@/api/ai/budget-api";
import MiniLineChart from "@/components/chart/MiniLineChart.vue";
import {RefreshRight} from "@element-plus/icons-vue";

const days = ref<number>(30);
const groupType = ref<string>('day');
const costDim = ref<string>('model');

const overview = ref<any>({});
const trend = ref<any[]>([]);
const modelRankList = ref<any[]>([]);
const userRankList = ref<any[]>([]);
const costStatList = ref<any[]>([]);
const suggestions = ref<any[]>([]);
const budgetAlerts = ref<any[]>([]);

const fmtNum = (n: any) => {
  const v = Number(n || 0);
  if (v >= 10000) return (v / 10000).toFixed(1) + 'w';
  return v.toString();
};
const fmtCost = (n: any) => Number(n || 0).toFixed(4);

const trendChartData = computed(() => trend.value.map(t => ({label: t.period, value: Number(t.totalTokens || 0)})));
const maxModelReq = computed(() => Math.max(1, ...modelRankList.value.map(m => Number(m.requestCount || 0))));
const maxUserReq = computed(() => Math.max(1, ...userRankList.value.map(u => Number(u.requestCount || 0))));
const rankPercent = (v: any, max: number) => Math.round((Number(v || 0) / max) * 100);

const tlType = (level: string) => ({info: 'primary', warning: 'warning', danger: 'danger'} as any)[level] || 'primary';
const periodLabel = (p: string) => ({day: '日', week: '周', month: '月'} as any)[p] || p;
const scopeName = (b: any) => b.scopeName || '全局';

const loadOverview = () => reqCommonFeedback(monitorApi.overview(days.value), (d: any) => overview.value = d || {});
const loadTrend = () => reqCommonFeedback(monitorApi.tokenTrend(groupType.value, days.value), (d: any) => trend.value = d || []);
const loadModelRank = () => reqCommonFeedback(monitorApi.modelRank(days.value), (d: any) => modelRankList.value = d || []);
const loadUserRank = () => reqCommonFeedback(monitorApi.userRank(days.value), (d: any) => userRankList.value = d || []);
const loadCostStat = () => reqCommonFeedback(monitorApi.costStat(costDim.value, days.value), (d: any) => costStatList.value = d || []);
const loadSuggestions = () => reqCommonFeedback(monitorApi.costSuggestions(days.value), (d: any) => suggestions.value = d || []);
const loadBudgetAlerts = () => reqCommonFeedback(budgetStatusApi(), (d: any) => {
  budgetAlerts.value = (d || []).filter((b: any) => b.exceeded || b.alerting);
});

const loadAll = () => {
  loadOverview();
  loadTrend();
  loadModelRank();
  loadUserRank();
  loadCostStat();
  loadSuggestions();
  loadBudgetAlerts();
};

onMounted(() => loadAll());
</script>

<style scoped>
.ai-monitor {padding: 4px;}
.toolbar {display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;}
.toolbar .title {font-size: 18px; font-weight: 600;}
.toolbar .right {display: flex; gap: 12px; align-items: center;}
.cards {margin-bottom: 16px;}
.stat {text-align: center; padding: 6px 0;}
.stat .label {font-size: 13px; color: var(--el-text-color-secondary);}
.stat .value {font-size: 24px; font-weight: 600; margin-top: 6px;}
.block {margin-bottom: 16px;}
.block-header {display: flex; justify-content: space-between; align-items: center;}
.budget-alerts {margin-bottom: 16px;}
.alert-item {margin-bottom: 8px;}
.rank-row {margin-bottom: 14px;}
.rank-name {font-size: 13px; margin-bottom: 4px;}
.rank-name .ml {margin-left: 8px;}
.rank-meta {font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px;}
.sug-title {font-weight: 600; font-size: 14px;}
.sug-detail {font-size: 12px; color: var(--el-text-color-secondary); margin-top: 2px;}
</style>
