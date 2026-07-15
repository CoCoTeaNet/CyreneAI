<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="告警名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="作用范围">
        <el-select style="width: 140px" v-model="pageParam.searchObject.scope" clearable>
          <el-option label="全局" value="global"/>
          <el-option label="单 Key" value="key"/>
        </el-select>
      </el-form-item>
      <el-form-item label="监控指标">
        <el-select style="width: 160px" v-model="pageParam.searchObject.metric" clearable>
          <el-option label="月度 Tokens" value="monthly_tokens"/>
          <el-option label="日费用" value="daily_cost"/>
          <el-option label="错误率" value="error_rate"/>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加告警</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="180" label="名称"/>
        <el-table-column prop="scope" width="100" label="范围">
          <template #default="scope">
            <el-tag :type="scope.row.scope === 'global' ? 'warning' : 'primary'">
              {{ scope.row.scope === 'global' ? '全局' : '单 Key' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metric" width="150" label="监控指标"/>
        <el-table-column label="阈值" width="150">
          <template #default="scope">
            <span v-if="scope.row.thresholdPercent">{{ scope.row.thresholdPercent }}%</span>
            <span v-else-if="scope.row.thresholdValue">{{ scope.row.thresholdValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="notifyChannel" width="120" label="通知渠道"/>
        <el-table-column prop="triggerCount" width="100" label="触发次数"/>
        <el-table-column prop="lastTriggeredTime" width="170" label="最近触发"/>
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
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes="[5,10,15]"
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}配额告警`" width="680px">
        <el-form ref="formRef" label-width="140px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="告警名称">
            <el-input v-model="editForm.name" placeholder="如: 月度 Token 80%"/>
          </el-form-item>
          <el-form-item prop="scope" label="作用范围">
            <el-radio-group v-model="editForm.scope">
              <el-radio label="global">全局</el-radio>
              <el-radio label="key">单 Key</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="editForm.scope === 'key'" label="API Key ID">
            <el-input v-model="editForm.apiKeyId" placeholder="目标 Key ID"/>
          </el-form-item>
          <el-form-item prop="metric" label="监控指标">
            <el-select v-model="editForm.metric" style="width: 100%">
              <el-option label="月度 Tokens" value="monthly_tokens"/>
              <el-option label="日费用" value="daily_cost"/>
              <el-option label="错误率" value="error_rate"/>
            </el-select>
          </el-form-item>
          <el-form-item label="阈值百分比 (%)">
            <el-input-number v-model="editForm.thresholdPercent" :min="0" :max="100"/>
          </el-form-item>
          <el-form-item label="阈值绝对值">
            <el-input-number v-model="editForm.thresholdValue" :min="0" :precision="2"/>
          </el-form-item>
          <el-form-item label="通知渠道">
            <el-select v-model="editForm.notifyChannel" clearable style="width: 100%">
              <el-option label="日志" value="log"/>
              <el-option label="邮件" value="email"/>
              <el-option label="Webhook" value="webhook"/>
            </el-select>
          </el-form-item>
          <el-form-item label="通知目标">
            <el-input v-model="editForm.notifyTarget" placeholder="邮箱地址或 Webhook URL"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="editForm.enableStatus">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
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
import {nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage, add, update, deleteBatch} from "@/api/ai/quota-alert-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<any>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);

const editForm = ref<any>({enableStatus: 1, scope: 'global', metric: 'monthly_tokens'});

const rules = reactive({
  name: [{required: true, message: '请输入告警名称', trigger: 'blur'}],
  scope: [{required: true, message: '请选择作用范围', trigger: 'change'}],
  metric: [{required: true, message: '请选择监控指标', trigger: 'change'}]
});

onMounted(() => loadTableData());

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiQuotaAlert: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {editForm.value = {enableStatus: 1, scope: 'global', metric: 'monthly_tokens'}; dialogFormVisible.value = true;}
const onEdit = (row: any) => {editForm.value = {...row}; dialogFormVisible.value = true;}
const doUpdate = (formEl: any) => {
  formEl.validate((valid: any) => {
    if (!valid) return;
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
    reqCommonFeedback(deleteBatch([id]), () => {
      ElMessage.success('删除成功');
      loadTableData();
    });
  });
}
const onDeleteBatch = () => {
  const ids: string[] = multipleSelection.value.map((it: any) => it.id);
  if (!ids.length) return ElMessage.warning('请选择记录');
  ElMessageBox.confirm('确认批量删除?', '提示', {type: 'warning'}).then(() => {
    reqCommonFeedback(deleteBatch(ids), () => {
      ElMessage.success('删除成功');
      loadTableData();
    });
  });
}
const handleSelectionChange = (arr: any) => {multipleSelection.value = arr;}
</script>

<style scoped></style>
