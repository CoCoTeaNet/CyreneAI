<template>
  <table-manage>
    <template #search>
      <el-form-item label="智能体名称">
        <el-input placeholder="名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select placeholder="状态" style="width: 200px" v-model="pageParam.searchObject.enableStatus" clearable>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">创建智能体</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="150" label="名称"/>
        <el-table-column prop="description" min-width="200" label="描述" show-overflow-tooltip/>
        <el-table-column prop="modelName" width="150" label="关联模型"/>
        <el-table-column label="工具" width="200">
          <template #default="scope">
            <el-tag v-for="t in (scope.row.tools || [])" :key="t.id" size="small" style="margin: 2px">
              {{ t.name }}
            </el-tag>
            <span v-if="!scope.row.tools || scope.row.tools.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxIterations" width="100" label="最大迭代"/>
        <el-table-column prop="enableStatus" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="Message" size="small" @click="onChat(scope.row)">对话</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '创建'}智能体`" width="700px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="名称">
            <el-input v-model="editForm.name" placeholder="智能体名称"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="描述智能体功能"/>
          </el-form-item>
          <el-form-item prop="modelId" label="关联模型">
            <el-select v-model="editForm.modelId" placeholder="选择模型" filterable style="width: 100%">
              <el-option v-for="m in modelList" :key="m.id" :label="m.modelName" :value="m.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="系统提示词">
            <el-input v-model="editForm.systemPrompt" type="textarea" :rows="4"
                      placeholder="你是一个智能助手，可以回答问题并使用各种工具来帮助用户。"/>
          </el-form-item>
          <el-form-item label="关联工具">
            <el-select v-model="editForm.toolIds" multiple placeholder="选择工具" filterable style="width: 100%">
              <el-option v-for="t in toolList" :key="t.id" :label="t.name" :value="t.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="最大迭代">
            <el-input-number v-model="editForm.maxIterations" :min="1" :max="50"/>
          </el-form-item>
          <el-form-item label="温度">
            <el-slider v-model="editForm.temperature" :min="0" :max="2" :step="0.1" style="width: 300px"/>
          </el-form-item>
          <el-form-item label="Top-P">
            <el-slider v-model="editForm.topP" :min="0" :max="1" :step="0.05" style="width: 300px"/>
          </el-form-item>
          <el-form-item label="最大Token">
            <el-input-number v-model="editForm.maxTokens" :min="1" :max="32768" :step="256"/>
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
import {listByPage as listAgentPage, add, update, deleteBatch} from "@/api/ai/agent-api";
import {listByPage as listToolPage} from "@/api/ai/tool-api";
import {listByPage as listModelPage} from "@/api/ai/model-api";
import TableManage from "@/components/container/TableManage.vue";
import {useRouter} from "vue-router";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight, Message} from "@element-plus/icons-vue";

const router = useRouter();
type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);
const modelList = ref<any[]>([]);
const toolList = ref<any[]>([]);

const editForm = ref<any>({enableStatus: 1, sort: 0, maxIterations: 10, toolIds: [], temperature: 0.7, topP: 0.9, maxTokens: 2048});

const rules = reactive({
  name: [{required: true, min: 1, max: 100, message: '请输入智能体名称', trigger: 'blur'}],
  modelId: [{required: true, message: '请选择关联模型', trigger: 'change'}]
});

onMounted(() => {
  loadModelList();
  loadToolList();
  loadTableData();
});

const loadModelList = () => {
  listModelPage({pageNo: 1, pageSize: 999, aiModel: {enableStatus: 1}}).then((res: any) => {
    modelList.value = res.data?.records || [];
  });
}

const loadToolList = () => {
  listToolPage({pageNo: 1, pageSize: 999, aiTool: {enableStatus: 1}}).then((res: any) => {
    toolList.value = res.data?.records || [];
  });
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiAgent: pageParam.value.searchObject
  };
  reqCommonFeedback(listAgentPage(param), (data: any) => {
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
  editForm.value = {enableStatus: 1, sort: 0, maxIterations: 10, toolIds: [], temperature: 0.7, topP: 0.9, maxTokens: 2048};
  dialogFormVisible.value = true;
}

const onEdit = (row: any) => {
  editForm.value = {
    ...row,
    toolIds: row.toolIds || []
  };
  dialogFormVisible.value = true;
}

const onChat = (row: any) => {
  router.push({name: 'AgentChatView', query: {agentId: row.id, agentName: row.name}});
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
  ElMessageBox.confirm('确认删除该智能体?', '提示', {
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
  ElMessageBox.confirm('确认删除所选智能体?', '提示', {
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
