<template>
  <table-manage>
    <template #search>
      <el-form-item label="工具名称">
        <el-input placeholder="名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="类型">
        <el-select placeholder="类型" style="width: 200px" v-model="pageParam.searchObject.type" clearable>
          <el-option label="内置工具" value="builtin"/>
          <el-option label="自定义工具" value="custom"/>
        </el-select>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加工具</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="150" label="工具名称"/>
        <el-table-column prop="description" min-width="250" label="描述" show-overflow-tooltip/>
        <el-table-column prop="type" width="120" label="类型">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'builtin' ? 'primary' : 'warning'">
              {{ scope.row.type === 'builtin' ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="builtinHandler" width="130" label="处理器"/>
        <el-table-column prop="enableStatus" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" width="80" label="排序"/>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="Tools" size="small" @click="onTestTool(scope.row)" v-if="scope.row.type === 'builtin'">测试</el-button>
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
      <el-dialog v-model="testDialogVisible" title="工具测试沙盒" width="600px">
        <el-form label-width="100px">
          <el-form-item label="工具">
            <el-tag>{{ testToolName }}</el-tag>
          </el-form-item>
          <el-form-item label="参数(JSON)">
            <el-input v-model="testArgs" type="textarea" :rows="6"
                      placeholder='{"expression": "2 + 3 * 4"}'/>
          </el-form-item>
          <el-form-item label="执行结果">
            <el-input v-model="testResult" type="textarea" :rows="6" readonly/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button type="primary" @click="doTest">执行</el-button>
          <el-button @click="testDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}工具`" width="650px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="工具名称">
            <el-input v-model="editForm.name" placeholder="工具名称"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="描述工具功能"/>
          </el-form-item>
          <el-form-item prop="type" label="工具类型">
            <el-radio-group v-model="editForm.type">
              <el-radio label="builtin">内置工具</el-radio>
              <el-radio label="custom">自定义工具</el-radio>
            </el-radio-group>
          </el-form-item>

          <template v-if="editForm.type === 'builtin'">
            <el-form-item label="处理器标识">
              <el-select v-model="editForm.builtinHandler" placeholder="选择处理器" style="width: 100%">
                <el-option label="计算器 (calculator)" value="calculator"/>
                <el-option label="日期时间 (datetime)" value="datetime"/>
                <el-option label="网页搜索 (web_search)" value="web_search"/>
                <el-option label="知识库检索 (knowledge_base)" value="knowledge_base"/>
                <el-option label="代码执行 (code_execution)" value="code_execution"/>
                <el-option label="图片生成 (image_generation)" value="image_generation"/>
                <el-option label="图片识别 (image_recognition)" value="image_recognition"/>
                <el-option label="天气查询 (weather)" value="weather"/>
              </el-select>
            </el-form-item>
          </template>

          <template v-if="editForm.type === 'custom'">
            <el-form-item label="URL" prop="url">
              <el-input v-model="editForm.url" placeholder="https://api.example.com/tool"/>
            </el-form-item>
            <el-form-item label="HTTP方法">
              <el-select v-model="editForm.httpMethod" style="width: 150px">
                <el-option label="POST" value="POST"/>
                <el-option label="GET" value="GET"/>
              </el-select>
            </el-form-item>
            <el-form-item label="认证类型">
              <el-select v-model="editForm.authType" style="width: 150px">
                <el-option label="无" value="none"/>
                <el-option label="Bearer Token" value="bearer"/>
                <el-option label="Basic Auth" value="basic"/>
              </el-select>
            </el-form-item>
            <el-form-item label="认证值" v-if="editForm.authType !== 'none'">
              <el-input v-model="editForm.authValue" placeholder="token 或密码" show-password/>
            </el-form-item>
            <el-form-item label="参数Schema">
              <el-input v-model="editForm.schemaJson" type="textarea" :rows="4"
                        placeholder='{"type":"object","properties":{"key":{"type":"string"}},"required":["key"]}'/>
            </el-form-item>
          </template>

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
import {listByPage, add, update, deleteBatch, execute} from "@/api/ai/tool-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight, Tools} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const testDialogVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);

const editForm = ref<any>({type: 'builtin', enableStatus: 1, sort: 0, httpMethod: 'POST', authType: 'none'});
const testToolName = ref<string>('');
const testArgs = ref<string>('');
const testResult = ref<string>('');

const rules = reactive({
  name: [{required: true, min: 1, max: 100, message: '请输入工具名称', trigger: 'blur'}]
});

onMounted(() => {
  loadTableData();
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiTool: pageParam.value.searchObject
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
  editForm.value = {type: 'builtin', enableStatus: 1, sort: 0, httpMethod: 'POST', authType: 'none'};
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
  ElMessageBox.confirm('确认删除该工具?', '提示', {
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
  ElMessageBox.confirm('确认删除所选工具?', '提示', {
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

const onTestTool = (row: any) => {
  testToolName.value = row.builtinHandler || row.name;
  testArgs.value = '';
  testResult.value = '';
  testDialogVisible.value = true;
}

const doTest = () => {
  let params: any;
  try {
    params = testArgs.value ? JSON.parse(testArgs.value) : {};
  } catch {
    ElMessage.error('参数格式错误，请输入有效的JSON');
    return;
  }
  execute({toolName: testToolName.value, arguments: params}).then((res: any) => {
    testResult.value = res.data || '执行成功(无返回值)';
  }).catch((err: any) => {
    testResult.value = '执行失败: ' + err.message;
  });
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>
