<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="数据集名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-input placeholder="分类" v-model="pageParam.searchObject.category"/>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">新增数据集</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" min-width="160" label="名称"/>
        <el-table-column prop="category" width="120" label="分类"/>
        <el-table-column prop="description" min-width="200" label="描述" show-overflow-tooltip/>
        <el-table-column prop="itemCount" width="100" label="条目数">
          <template #default="scope"><el-tag type="info">{{ scope.row.itemCount || 0 }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sort" width="80" label="排序"/>
        <el-table-column prop="enableStatus" width="90" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="scope">
            <el-button size="small" @click="onView(scope.row)">查看条目</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '新增'}数据集`" width="720px">
        <el-form ref="formRef" label-width="100px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="名称">
            <el-input v-model="editForm.name" placeholder="如: 客服问答评估集"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="editForm.category" placeholder="如: general、qa、summary"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item label="评估条目">
            <div style="width: 100%">
              <el-input v-model="editForm.itemsJson" type="textarea" :rows="10"
                        placeholder='JSON 数组, 例如: [{"prompt":"你好","expected":"问候语"}]'/>
              <div class="hint">
                <span :class="{err: jsonError}">{{ jsonError || ('已解析 ' + parsedCount + ' 条') }}</span>
                <el-button link type="primary" size="small" @click="formatJson">格式化</el-button>
              </div>
            </div>
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

  <el-dialog v-model="viewVisible" title="评估条目" width="720px">
    <el-table :data="viewItems" size="small" max-height="480">
      <el-table-column type="index" width="60" label="#"/>
      <el-table-column prop="prompt" label="提示词" show-overflow-tooltip/>
      <el-table-column prop="expected" label="期望输出" show-overflow-tooltip/>
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage, add, update, deleteBatch} from "@/api/ai/eval-dataset-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const viewVisible = ref<boolean>(false);
const viewItems = ref<any[]>([]);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<any>({pageNo: 1, pageSize: 20, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 20, total: 0, records: []});
const loading = ref<boolean>(true);

const editForm = ref<any>({enableStatus: 1, sort: 0, category: 'general', itemsJson: '[]'});

const rules = reactive({
  name: [{required: true, message: '请输入名称', trigger: 'blur'}]
});

const jsonError = ref<string>('');
const parsedCount = computed(() => {
  try {
    const arr = JSON.parse(editForm.value.itemsJson || '[]');
    jsonError.value = Array.isArray(arr) ? '' : '内容必须是 JSON 数组';
    return Array.isArray(arr) ? arr.length : 0;
  } catch (e: any) {
    jsonError.value = 'JSON 解析失败: ' + e.message;
    return 0;
  }
});

onMounted(() => loadTableData());

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {pageNo: pageParam.value.pageNo, pageSize: pageParam.value.pageSize, aiEvalDataset: pageParam.value.searchObject};
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}
const onPageChange = (p: number) => {pageParam.value.pageNo = p; nextTick(() => loadTableData());}
const onSizeChange = (s: number) => {pageParam.value.pageSize = s; nextTick(() => loadTableData());}
const onResetSearchForm = () => {pageParam.value.searchObject = {};}
const onCreate = () => {editForm.value = {enableStatus: 1, sort: 0, category: 'general', itemsJson: '[]'}; dialogFormVisible.value = true;}
const onEdit = (row: any) => {editForm.value = {...row, itemsJson: row.itemsJson || '[]'}; dialogFormVisible.value = true;}
const onView = (row: any) => {
  try {
    viewItems.value = JSON.parse(row.itemsJson || '[]');
  } catch (e) {
    viewItems.value = [];
  }
  viewVisible.value = true;
}
const formatJson = () => {
  try {
    editForm.value.itemsJson = JSON.stringify(JSON.parse(editForm.value.itemsJson || '[]'), null, 2);
  } catch (e) {
    ElMessage.error('JSON 格式错误, 无法格式化');
  }
}
const doUpdate = (formEl: any) => {
  formEl.validate((valid: any) => {
    if (!valid) return;
    if (jsonError.value) return ElMessage.error(jsonError.value);
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
.hint {display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px;}
.hint .err {color: var(--el-color-danger);}
</style>
