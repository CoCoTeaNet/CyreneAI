<template>
  <table-manage>
    <template #search>
      <el-form-item label="敏感词">
        <el-input placeholder="包含关键字" v-model="pageParam.searchObject.word"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-input placeholder="分类" v-model="pageParam.searchObject.category"/>
      </el-form-item>
      <el-form-item label="策略">
        <el-select style="width: 140px" v-model="pageParam.searchObject.strategy" clearable>
          <el-option label="拦截" value="block"/>
          <el-option label="替换" value="replace"/>
          <el-option label="警告" value="warn"/>
        </el-select>
      </el-form-item>
      <el-form-item label="作用位置">
        <el-select style="width: 140px" v-model="pageParam.searchObject.target" clearable>
          <el-option label="输入" value="input"/>
          <el-option label="输出" value="output"/>
          <el-option label="双向" value="both"/>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加敏感词</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="word" min-width="180" label="敏感词"/>
        <el-table-column prop="category" width="120" label="分类"/>
        <el-table-column prop="strategy" width="100" label="策略">
          <template #default="scope">
            <el-tag :type="strategyTag(scope.row.strategy)">{{ strategyLabel(scope.row.strategy) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replacement" width="160" label="替换文本"/>
        <el-table-column prop="target" width="100" label="作用位置">
          <template #default="scope">
            <el-tag type="info">{{ targetLabel(scope.row.target) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" width="80" label="排序"/>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}敏感词`" width="620px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="word" label="敏感词">
            <el-input v-model="editForm.word" placeholder="待过滤词汇"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="editForm.category" placeholder="如: 政治、暴力"/>
          </el-form-item>
          <el-form-item prop="strategy" label="策略">
            <el-radio-group v-model="editForm.strategy">
              <el-radio label="block">拦截</el-radio>
              <el-radio label="replace">替换</el-radio>
              <el-radio label="warn">警告</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="editForm.strategy === 'replace'" label="替换文本">
            <el-input v-model="editForm.replacement" placeholder="如: ***"/>
          </el-form-item>
          <el-form-item label="作用位置">
            <el-radio-group v-model="editForm.target">
              <el-radio label="input">输入</el-radio>
              <el-radio label="output">输出</el-radio>
              <el-radio label="both">双向</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="editForm.sort" :min="0"/>
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
import {listByPage, add, update, deleteBatch} from "@/api/ai/sensitive-word-api";
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

const editForm = ref<any>({enableStatus: 1, sort: 0, strategy: 'block', target: 'both'});

const rules = reactive({
  word: [{required: true, message: '请输入敏感词', trigger: 'blur'}],
  strategy: [{required: true, message: '请选择策略', trigger: 'change'}]
});

onMounted(() => loadTableData());

const strategyLabel = (s: string) => ({block: '拦截', replace: '替换', warn: '警告'} as any)[s] || s;
const strategyTag = (s: string) => ({block: 'danger', replace: 'warning', warn: 'info'} as any)[s] || 'info';
const targetLabel = (t: string) => ({input: '输入', output: '输出', both: '双向'} as any)[t] || t;

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiSensitiveWord: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {editForm.value = {enableStatus: 1, sort: 0, strategy: 'block', target: 'both'}; dialogFormVisible.value = true;}
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
