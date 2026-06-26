<template>
  <table-manage>
    <template #search>
      <el-form-item label="知识库名称">
        <el-input placeholder="名称" v-model="pageParam.searchObject.name"/>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">创建知识库</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%">
        <el-table-column prop="name" width="200" label="知识库名称"/>
        <el-table-column prop="description" min-width="250" label="描述" show-overflow-tooltip/>
        <el-table-column prop="modelName" width="150" label="关联模型"/>
        <el-table-column prop="embeddingModelName" width="150" label="嵌入模型"/>
        <el-table-column prop="documentCount" width="80" label="文档数"/>
        <el-table-column prop="topK" width="80" label="Top-K"/>
        <el-table-column prop="retrievalStrategy" width="100" label="检索策略"/>
        <el-table-column prop="enableStatus" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" width="180" label="创建时间"/>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain @click="onDelete(scope.row.id)">删除</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '创建'}知识库`" width="700px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="知识库名称">
            <el-input v-model="editForm.name"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item label="关联模型">
            <el-select v-model="editForm.modelId" placeholder="可选" filterable clearable style="width: 100%">
              <el-option v-for="m in modelList" :key="m.id" :label="m.modelName" :value="m.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="嵌入模型">
            <el-select v-model="editForm.embeddingModelId" placeholder="可选" filterable clearable style="width: 100%">
              <el-option v-for="em in embModelList" :key="em.id" :label="em.modelName" :value="em.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="分块策略">
            <el-select v-model="editForm.chunkStrategy" style="width: 100%">
              <el-option label="按段落" value="paragraph"/>
              <el-option label="按大小" value="size"/>
              <el-option label="递归分割" value="recursive"/>
            </el-select>
          </el-form-item>
          <el-form-item label="分块大小">
            <el-input-number v-model="editForm.chunkSize" :min="100" :max="2000" :step="100"/>
          </el-form-item>
          <el-form-item label="重叠字数">
            <el-input-number v-model="editForm.chunkOverlap" :min="0" :max="200" :step="10"/>
          </el-form-item>
          <el-form-item label="检索策略">
            <el-select v-model="editForm.retrievalStrategy" style="width: 100%">
              <el-option label="Top-K (相似度)" value="top_k"/>
              <el-option label="MMR (多样性)" value="mmr"/>
            </el-select>
          </el-form-item>
          <el-form-item label="检索数量">
            <el-input-number v-model="editForm.topK" :min="1" :max="50"/>
          </el-form-item>
          <el-form-item label="相似度阈值">
            <el-slider v-model="editForm.similarityThreshold" :min="0" :max="1" :step="0.05" show-input/>
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
import {listByPage, add, update, remove} from "@/api/ai/knowledge-base-api";
import {listEnabled as listModels} from "@/api/ai/model-api";
import {listEnabled as listEmbModels} from "@/api/ai/embedding-model-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);
const modelList = ref<any[]>([]);
const embModelList = ref<any[]>([]);

const editForm = ref<any>({
  enableStatus: 1,
  chunkSize: 500,
  chunkOverlap: 50,
  topK: 5,
  similarityThreshold: 0.7,
  chunkStrategy: 'paragraph',
  retrievalStrategy: 'top_k'
});

const rules = reactive({
  name: [{required: true, min: 1, max: 200, message: '请输入知识库名称', trigger: 'blur'}]
});

onMounted(() => {
  loadTableData();
  loadModelLists();
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    knowledgeBase: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const loadModelLists = () => {
  reqCommonFeedback(listModels(), (data: any) => {
    modelList.value = data || [];
  });
  reqCommonFeedback(listEmbModels(), (data: any) => {
    embModelList.value = data || [];
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
  editForm.value = {
    enableStatus: 1,
    chunkSize: 500,
    chunkOverlap: 50,
    topK: 5,
    similarityThreshold: 0.7,
    chunkStrategy: 'paragraph',
    retrievalStrategy: 'top_k'
  };
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
        reqSuccessFeedback(add(editForm.value), '创建成功', () => {
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
  ElMessageBox.confirm('确认删除该知识库?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    reqCommonFeedback(remove(id), () => {
      ElMessage.success('删除成功');
      loadTableData();
    });
  });
}
</script>

<style scoped></style>
