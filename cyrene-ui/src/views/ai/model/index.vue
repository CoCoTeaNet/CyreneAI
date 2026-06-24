<template>
  <table-manage>
    <template #search>
      <el-form-item label="模型名称">
        <el-input placeholder="名称" v-model="pageParam.searchObject.modelName"/>
      </el-form-item>
      <el-form-item label="提供商">
        <el-select placeholder="选择提供商" style="width: 200px" v-model="pageParam.searchObject.providerId" filterable clearable>
          <el-option v-for="p in providerList" :key="p.id" :label="p.providerName" :value="p.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select placeholder="选择状态" style="width: 200px" v-model="pageParam.searchObject.enableStatus">
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加模型</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="providerName" width="150" label="所属提供商"/>
        <el-table-column prop="modelName" width="200" label="模型名称"/>
        <el-table-column prop="contextWindow" width="120" label="上下文窗口"/>
        <el-table-column prop="inputPrice" width="120" label="输入价格"/>
        <el-table-column prop="outputPrice" width="120" label="输出价格"/>
        <el-table-column prop="isDefault" width="100" label="默认">
          <template #default="scope">
            <el-tag :type="scope.row.isDefault === 1 ? 'success' : 'info'">
              {{ scope.row.isDefault === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" width="80" label="排序"/>
        <el-table-column prop="enableStatus" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain @click="onDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes=[5,10,15]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}模型`" width="600px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="providerId" label="所属提供商">
            <el-select v-model="editForm.providerId" placeholder="选择提供商" filterable style="width: 100%">
              <el-option v-for="p in providerList" :key="p.id" :label="p.providerName" :value="p.id"/>
            </el-select>
          </el-form-item>
          <el-form-item prop="modelName" label="模型名称">
            <el-input v-model="editForm.modelName"/>
          </el-form-item>
          <el-form-item label="上下文窗口">
            <el-input-number v-model="editForm.contextWindow" :min="0" :step="1024" style="width: 100%"/>
          </el-form-item>
          <el-form-item label="输入价格">
            <el-input-number v-model="editForm.inputPrice" :min="0" :precision="4" :step="0.0001" style="width: 100%"/>
          </el-form-item>
          <el-form-item label="输出价格">
            <el-input-number v-model="editForm.outputPrice" :min="0" :precision="4" :step="0.0001" style="width: 100%"/>
          </el-form-item>
          <el-form-item label="默认模型">
            <el-radio-group v-model="editForm.isDefault">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
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
import {nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage as listProviderPage} from "@/api/ai/model-provider-api";
import {listByPage, add, update, deleteBatch} from "@/api/ai/model-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);
const providerList = ref<any[]>([]);

const editForm = ref<any>({enableStatus: 1, isDefault: 0, sort: 0});

const rules = reactive({
  providerId: [{required: true, message: '请选择提供商', trigger: 'change'}],
  modelName: [{required: true, min: 1, max: 100, message: '请输入模型名称', trigger: 'blur'}]
});

onMounted(() => {
  loadProviderList();
  loadTableData();
});

const loadProviderList = () => {
  listProviderPage({pageNo: 1, pageSize: 999, aiModelProvider: {enableStatus: 1}}).then((res: any) => {
    providerList.value = res.data?.records || [];
  });
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiModel: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  nextTick(() => loadTableData());
}

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  nextTick(() => loadTableData());
}

const onResetSearchForm = () => {
  pageParam.value.searchObject = {};
}

const onCreate = () => {
  editForm.value = {enableStatus: 1, isDefault: 0, sort: 0};
  dialogFormVisible.value = true;
}

const onEdit = (row: any) => {
  editForm.value = {...row};
  dialogFormVisible.value = true;
}

const doUpdate = (formEl: any) => {
  formEl.validate((valid: any) => {
    if (valid) {
      if (!editForm.value.id) {
        reqSuccessFeedback(add(editForm.value), '新增成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      } else {
        reqSuccessFeedback(update(editForm.value), '修改成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      }
    }
  });
}

const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除该模型?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch([id]), () => {
      ElMessage({type: 'success', message: '删除成功'});
      loadTableData();
    });
  });
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item: any) => ids.push(item.id));
  ElMessageBox.confirm('确认删除所选模型?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch(ids), () => {
      ElMessage({type: 'success', message: '删除成功'});
      loadTableData();
    });
  });
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>
