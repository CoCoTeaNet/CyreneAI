<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="预算名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="范围">
        <el-select style="width: 140px" v-model="pageParam.searchObject.scopeType" clearable>
          <el-option label="全局" value="global"/>
          <el-option label="按模型" value="model"/>
          <el-option label="按用户" value="user"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select style="width: 120px" v-model="pageParam.searchObject.enableStatus" clearable>
          <el-option label="启用" :value="1"/>
          <el-option label="关闭" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onCreate">新增预算</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" min-width="160" label="预算名称"/>
        <el-table-column prop="scopeType" width="100" label="范围">
          <template #default="scope"><el-tag>{{ scopeLabel(scope.row.scopeType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="period" width="90" label="周期">
          <template #default="scope">{{ periodLabel(scope.row.period) }}</template>
        </el-table-column>
        <el-table-column prop="amount" width="120" label="预算(元)">
          <template #default="scope">{{ Number(scope.row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="执行情况" min-width="220">
          <template #default="scope">
            <div v-if="statusMap[scope.row.id]">
              <el-progress :percentage="Math.min(100, Number(statusMap[scope.row.id].usagePercent || 0))"
                           :status="progressStatus(statusMap[scope.row.id])" :stroke-width="14"/>
              <span class="used">已用 {{ Number(statusMap[scope.row.id].usedCost || 0).toFixed(4) }} 元</span>
            </div>
            <span v-else class="used">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="enableStatus" width="90" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain @click="onDelete(scope.row.id)">删除</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '新增'}预算`" width="620px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="预算名称">
            <el-input v-model="editForm.name" placeholder="如: 全局月度预算"/>
          </el-form-item>
          <el-form-item prop="scopeType" label="范围类型">
            <el-radio-group v-model="editForm.scopeType">
              <el-radio label="global">全局</el-radio>
              <el-radio label="model">按模型</el-radio>
              <el-radio label="user">按用户</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="editForm.scopeType === 'model'" label="选择模型">
            <el-select v-model="editForm.scopeId" filterable placeholder="选择模型" style="width: 100%">
              <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-if="editForm.scopeType === 'user'" label="用户ID">
            <el-input v-model="editForm.scopeId" placeholder="输入用户ID"/>
          </el-form-item>
          <el-form-item label="统计周期">
            <el-radio-group v-model="editForm.period">
              <el-radio label="day">日</el-radio>
              <el-radio label="week">周</el-radio>
              <el-radio label="month">月</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="amount" label="预算金额(元)">
            <el-input-number v-model="editForm.amount" :min="0" :precision="2" :step="10"/>
          </el-form-item>
          <el-form-item label="告警阈值">
            <el-slider v-model="alertPercent" :min="10" :max="100" :step="5" :format-tooltip="(v:number)=>v+'%'" style="width: 300px"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="editForm.enableStatus">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="editForm.remark" type="textarea" :rows="2"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="doUpdate(formRef)">确认</el-button>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage, add, update, deleteBatch, listStatus} from "@/api/ai/budget-api";
import {listEnabled} from "@/api/ai/model-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<any>({pageNo: 1, pageSize: 20, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 20, total: 0, records: []});
const loading = ref<boolean>(true);
const models = ref<any[]>([]);
const statusMap = ref<any>({});

const editForm = ref<any>({scopeType: 'global', period: 'month', enableStatus: 1, amount: 100, alertThreshold: 0.8});
const alertPercent = ref<number>(80);

const rules = reactive({
  name: [{required: true, message: '请输入预算名称', trigger: 'blur'}],
  scopeType: [{required: true, message: '请选择范围', trigger: 'change'}],
  amount: [{required: true, message: '请输入预算金额', trigger: 'blur'}]
});

const scopeLabel = (s: string) => ({global: '全局', model: '按模型', user: '按用户'} as any)[s] || s;
const periodLabel = (p: string) => ({day: '日', week: '周', month: '月'} as any)[p] || p;
const progressStatus = (st: any) => st.exceeded ? 'exception' : (st.alerting ? 'warning' : 'success');

onMounted(() => {
  loadTableData();
  reqCommonFeedback(listEnabled(), (d: any) => models.value = (d || []).filter((m: any) => m.modelType === 'chat'));
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {pageNo: pageParam.value.pageNo, pageSize: pageParam.value.pageSize, aiBudget: pageParam.value.searchObject};
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
    loadStatus();
  });
}
const loadStatus = () => {
  reqCommonFeedback(listStatus(), (d: any) => {
    const map: any = {};
    (d || []).forEach((s: any) => map[s.id] = s);
    statusMap.value = map;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {
  editForm.value = {scopeType: 'global', period: 'month', enableStatus: 1, amount: 100};
  alertPercent.value = 80;
  dialogFormVisible.value = true;
}
const onEdit = (row: any) => {
  editForm.value = {...row};
  alertPercent.value = Math.round(Number(row.alertThreshold || 0.8) * 100);
  dialogFormVisible.value = true;
}
const doUpdate = (formEl: any) => {
  formEl.validate((valid: any) => {
    if (!valid) return;
    editForm.value.alertThreshold = alertPercent.value / 100;
    if (editForm.value.scopeType === 'global') editForm.value.scopeId = null;
    const fn = editForm.value.id ? update : add;
    const msg = editForm.value.id ? '修改成功' : '新增成功';
    reqSuccessFeedback(fn(editForm.value), msg, () => {
      loadTableData();
      dialogFormVisible.value = false;
    });
  });
}
const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除?', '提示', {type: 'warning'}).then(() => {
    reqCommonFeedback(deleteBatch([id]), () => {ElMessage.success('删除成功'); loadTableData();});
  });
}
const onDeleteBatch = () => {
  const ids: string[] = multipleSelection.value.map((it: any) => it.id);
  if (!ids.length) return ElMessage.warning('请选择记录');
  ElMessageBox.confirm('确认批量删除?', '提示', {type: 'warning'}).then(() => {
    reqCommonFeedback(deleteBatch(ids), () => {ElMessage.success('删除成功'); loadTableData();});
  });
}
const handleSelectionChange = (arr: any) => {multipleSelection.value = arr;}
</script>

<style scoped>
.used {font-size: 12px; color: var(--el-text-color-secondary);}
</style>
