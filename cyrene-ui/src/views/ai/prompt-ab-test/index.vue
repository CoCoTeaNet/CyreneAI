<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="测试名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select placeholder="状态" style="width: 160px" v-model="pageParam.searchObject.status" clearable>
          <el-option label="草稿" value="draft"/>
          <el-option label="运行中" value="running"/>
          <el-option label="已结束" value="finished"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onCreate">新增测试</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" min-width="180" label="名称"/>
        <el-table-column label="A/B 模板" min-width="260">
          <template #default="scope">
            <div>A: {{ scope.row.templateAName }} <el-tag size="small">v{{ scope.row.templateAVersion || '-' }}</el-tag></div>
            <div>B: {{ scope.row.templateBName }} <el-tag size="small">v{{ scope.row.templateBVersion || '-' }}</el-tag></div>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" width="150" label="模型"/>
        <el-table-column prop="trafficSplit" width="120" label="A侧流量">
          <template #default="scope">{{ scope.row.trafficSplit }}%</template>
        </el-table-column>
        <el-table-column label="统计" min-width="220">
          <template #default="scope">
            <div>A: 样本 {{ scope.row.sampleCountA || 0 }} · 均分 {{ (scope.row.avgRatingA || 0).toFixed(2) }}</div>
            <div>B: 样本 {{ scope.row.sampleCountB || 0 }} · 均分 {{ (scope.row.avgRatingB || 0).toFixed(2) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" width="110" label="状态">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button size="small" v-if="scope.row.status !== 'running'" @click="onChangeStatus(scope.row, 'running')">启动</el-button>
            <el-button size="small" v-if="scope.row.status === 'running'" @click="onChangeStatus(scope.row, 'finished')">结束</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}A/B测试`" width="700px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="名称">
            <el-input v-model="editForm.name" placeholder="测试名称"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item prop="templateAId" label="模板 A">
            <el-select v-model="editForm.templateAId" placeholder="选择模板 A" style="width: 100%">
              <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="版本 A">
            <el-input-number v-model="editForm.templateAVersion" :min="1" placeholder="不填=当前版本"/>
          </el-form-item>
          <el-form-item prop="templateBId" label="模板 B">
            <el-select v-model="editForm.templateBId" placeholder="选择模板 B" style="width: 100%">
              <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="版本 B">
            <el-input-number v-model="editForm.templateBVersion" :min="1" placeholder="不填=当前版本"/>
          </el-form-item>
          <el-form-item label="使用模型">
            <el-select v-model="editForm.modelId" placeholder="选择模型" style="width: 100%" clearable>
              <el-option v-for="m in models" :key="m.id" :label="m.modelName" :value="m.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="A侧流量">
            <el-input-number v-model="editForm.trafficSplit" :min="0" :max="100"/> %
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="editForm.status" style="width: 200px">
              <el-option label="草稿" value="draft"/>
              <el-option label="运行中" value="running"/>
              <el-option label="已结束" value="finished"/>
            </el-select>
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
import {listByPage, add, update, deleteBatch, changeStatus} from "@/api/ai/prompt-ab-test-api";
import {listEnabled as listTemplates} from "@/api/ai/prompt-template-api";
import {listEnabled as listModels} from "@/api/ai/model-api";
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
const templates = ref<any[]>([]);
const models = ref<any[]>([]);

const editForm = ref<any>({trafficSplit: 50, status: 'running'});

const rules = reactive({
  name: [{required: true, message: '请输入测试名称', trigger: 'blur'}],
  templateAId: [{required: true, message: '请选择模板 A', trigger: 'change'}],
  templateBId: [{required: true, message: '请选择模板 B', trigger: 'change'}]
});

onMounted(() => {
  loadTableData();
  listTemplates().then((res: any) => templates.value = res.data || []);
  listModels().then((res: any) => models.value = (res.data || []).filter((m: any) => m.modelType === 'chat' || m.modelType === 'vision'));
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiPromptAbTest: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {editForm.value = {trafficSplit: 50, status: 'running'}; dialogFormVisible.value = true;}
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
const onChangeStatus = (row: any, status: string) => {
  reqSuccessFeedback(changeStatus(row.id, status), '状态已更新', () => loadTableData());
}
const handleSelectionChange = (arr: any) => {multipleSelection.value = arr;}

const statusLabel = (s: string) => ({draft: '草稿', running: '运行中', finished: '已结束'} as any)[s] || s;
const statusTagType = (s: string) => ({draft: 'info', running: 'success', finished: 'warning'} as any)[s] || 'info';
</script>

<style scoped></style>
