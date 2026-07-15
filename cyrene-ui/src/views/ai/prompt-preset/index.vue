<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="预设名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-input placeholder="分类" v-model="pageParam.searchObject.category"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select placeholder="状态" style="width: 140px" v-model="pageParam.searchObject.enableStatus" clearable>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加预设</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="200" label="名称"/>
        <el-table-column prop="description" min-width="220" label="描述" show-overflow-tooltip/>
        <el-table-column prop="category" width="120" label="分类"/>
        <el-table-column prop="isBuiltin" width="90" label="内置">
          <template #default="scope">
            <el-tag v-if="scope.row.isBuiltin === 1" type="primary">内置</el-tag>
            <el-tag v-else type="info">自建</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enableStatus" width="90" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain
                       :disabled="scope.row.isBuiltin === 1"
                       @click="onDelete(scope.row.id)">删除
            </el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}系统提示词预设`" width="680px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="名称">
            <el-input v-model="editForm.name" placeholder="预设名称"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="editForm.category" placeholder="如: 助手、翻译、编程"/>
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="editForm.icon" placeholder="Element Plus 图标名"/>
          </el-form-item>
          <el-form-item prop="content" label="提示词内容">
            <el-input v-model="editForm.content" type="textarea" :rows="8"
                      placeholder="You are a helpful assistant..."/>
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
import {listByPage, add, update, deleteBatch} from "@/api/ai/prompt-preset-api";
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

const editForm = ref<any>({enableStatus: 1, sort: 0});

const rules = reactive({
  name: [{required: true, message: '请输入名称', trigger: 'blur'}],
  content: [{required: true, message: '请输入提示词内容', trigger: 'blur'}]
});

onMounted(() => loadTableData());

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiPromptPreset: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {editForm.value = {enableStatus: 1, sort: 0}; dialogFormVisible.value = true;}
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
