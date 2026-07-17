<template>
  <div class="mini-line-chart">
    <svg :viewBox="`0 0 ${width} ${height}`" preserveAspectRatio="none" width="100%" :height="height">
      <!-- 横向网格线 -->
      <line v-for="(gy, i) in gridLines" :key="'g' + i"
            :x1="pad" :y1="gy" :x2="width - pad" :y2="gy"
            stroke="var(--el-border-color-lighter)" stroke-width="1"/>
      <!-- 面积 -->
      <polygon v-if="points.length > 1" :points="areaPoints" :fill="color" fill-opacity="0.12"/>
      <!-- 折线 -->
      <polyline v-if="points.length > 1" :points="linePoints" fill="none" :stroke="color" stroke-width="2"/>
      <!-- 数据点 -->
      <circle v-for="(p, i) in points" :key="'c' + i" :cx="p.x" :cy="p.y" r="2.5" :fill="color">
        <title>{{ p.label }}: {{ p.raw }}</title>
      </circle>
    </svg>
    <div class="x-labels" v-if="showLabels">
      <span v-for="(p, i) in points" :key="'l' + i" :style="{left: labelLeft(p.x)}">{{ p.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from "vue";

const props = defineProps<{
  data: Array<{ label: string; value: number }>;
  height?: number;
  color?: string;
  showLabels?: boolean;
}>();

const width = 600;
const height = computed(() => props.height || 180) as any;
const pad = 30;
const color = computed(() => props.color || 'var(--el-color-primary)');
const showLabels = computed(() => props.showLabels !== false);

const maxVal = computed(() => Math.max(1, ...props.data.map(d => d.value || 0)));

const points = computed(() => {
  const n = props.data.length;
  const h = props.height || 180;
  if (n === 0) return [] as any[];
  const usableW = width - pad * 2;
  const usableH = h - pad * 2;
  return props.data.map((d, i) => {
    const x = n === 1 ? width / 2 : pad + (usableW * i) / (n - 1);
    const y = pad + usableH - (usableH * (d.value || 0)) / maxVal.value;
    return {x, y, label: d.label, raw: d.value};
  });
});

const linePoints = computed(() => points.value.map(p => `${p.x},${p.y}`).join(' '));
const areaPoints = computed(() => {
  const h = props.height || 180;
  if (points.value.length < 2) return '';
  const first = points.value[0];
  const last = points.value[points.value.length - 1];
  return `${first.x},${h - pad} ` + points.value.map(p => `${p.x},${p.y}`).join(' ') + ` ${last.x},${h - pad}`;
});

const gridLines = computed(() => {
  const h = props.height || 180;
  const usableH = h - pad * 2;
  return [0, 0.25, 0.5, 0.75, 1].map(r => pad + usableH * r);
});

const labelLeft = (x: number) => `${(x / width) * 100}%`;
</script>

<style scoped>
.mini-line-chart {
  width: 100%;
}
.x-labels {
  position: relative;
  height: 18px;
  margin-top: 4px;
}
.x-labels span {
  position: absolute;
  transform: translateX(-50%);
  font-size: 11px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
</style>
