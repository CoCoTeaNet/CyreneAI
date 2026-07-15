<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="模板名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="分类">
        <el-input placeholder="分类" v-model="pageParam.searchObject.category"/>
      </el-form-item>
      <el-form-item label="场景">
        <el-select placeholder="场景" style="width: 160px" v-model="pageParam.searchObject.scene" clearable>
          <el-option label="System" value="system"/>
          <el-option label="User" value="user"/>
          <el-option label="Mixed" value="mixed"/>
        </el-select>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">添加模板</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="180" label="名称"/>
        <el-table-column prop="description" min-width="200" label="描述" show-overflow-tooltip/>
        <el-table-column prop="category" width="120" label="分类"/>
        <el-table-column prop="scene" width="100" label="场景"/>
        <el-table-column prop="currentVersion" width="90" label="当前版本">
          <template #default="scope">v{{ scope.row.currentVersion }}</template>
        </el-table-column>
        <el-table-column prop="enableStatus" width="90" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="320">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="MagicStick" size="small" @click="onRender(scope.row)">调试</el-button>
            <el-button :icon="Clock" size="small" @click="onVersions(scope.row)">版本</el-button>
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
      <!-- 编辑 -->
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '添加'}提示词模板`" width="720px">
        <el-form ref="formRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="名称">
            <el-input v-model="editForm.name" placeholder="模板名称"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="模板用途"/>
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="editForm.category" placeholder="如: 客服、代码、写作"/>
          </el-form-item>
          <el-form-item label="场景">
            <el-radio-group v-model="editForm.scene">
              <el-radio label="system">System</el-radio>
              <el-radio label="user">User</el-radio>
              <el-radio label="mixed">Mixed</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="content" label="模板内容">
            <el-input v-model="editForm.content" type="textarea" :rows="8"
                      placeholder="使用 {{variable}} 语法引用变量，例如 你好 {{name}}"/>
          </el-form-item>
          <el-form-item label="变量列表(JSON)">
            <el-input v-model="editForm.variables" type="textarea" :rows="3"
                      placeholder='["name","topic"]'/>
          </el-form-item>
          <el-form-item label="变更说明" v-if="editForm.id">
            <el-input v-model="editForm.changeNote" placeholder="内容变化时会生成新版本"/>
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

      <!-- 变量调试 -->
      <el-dialog v-model="renderDialogVisible" title="变量调试" width="700px">
        <el-form label-width="100px">
          <el-form-item label="模板">
            <el-tag>{{ renderTemplateName }}</el-tag>
          </el-form-item>
          <el-form-item label="变量(JSON)">
            <el-input v-model="renderVars" type="textarea" :rows="4"
                      placeholder='{"name":"张三"}'/>
          </el-form-item>
          <el-form-item label="渲染结果">
            <el-input v-model="renderResult" type="textarea" :rows="8" readonly/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button type="primary" @click="doRender">渲染</el-button>
          <el-button @click="renderDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 版本历史 -->
      <el-dialog v-model="versionDialogVisible" title="版本历史" width="800px">
        <el-table :data="versionList" style="width: 100%">
          <el-table-column prop="version" width="80" label="版本">
            <template #default="scope">v{{ scope.row.version }}</template>
          </el-table-column>
          <el-table-column prop="changeNote" label="变更说明" show-overflow-tooltip/>
          <el-table-column prop="createTime" width="180" label="创建时间"/>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="onViewVersion(scope.row)">查看</el-button>
              <el-button size="small" type="warning" @click="onRollback(scope.row)">回滚</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 12px;" v-if="viewingVersion">
          <div style="margin-bottom: 6px;">v{{ viewingVersion.version }} 内容:</div>
          <el-input v-model="viewingVersion.content" type="textarea" :rows="8" readonly/>
        </div>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {
  listByPage, add, update, deleteBatch,
  render as renderApi, listVersions, getVersion, rollback
} from "@/api/ai/prompt-template-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight, MagicStick, Clock} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const renderDialogVisible = ref<boolean>(false);
const versionDialogVisible = ref<boolean>(false);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<any>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);

const editForm = ref<any>({scene: 'system', enableStatus: 1, sort: 0});
const renderTemplateName = ref<string>('');
const renderTemplateId = ref<string>('');
const renderContent = ref<string>('');
const renderVars = ref<string>('');
const renderResult = ref<string>('');

const versionTemplateId = ref<string>('');
const versionList = ref<any[]>([]);
const viewingVersion = ref<any>(null);

const rules = reactive({
  name: [{required: true, message: '请输入模板名称', trigger: 'blur'}],
  content: [{required: true, message: '请输入模板内容', trigger: 'blur'}]
});

onMounted(() => loadTableData());

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiPromptTemplate: pageParam.value.searchObject
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
  editForm.value = {scene: 'system', enableStatus: 1, sort: 0};
  dialogFormVisible.value = true;
}
const onEdit = (row: any) => {
  editForm.value = {...row};
  dialogFormVisible.value = true;
}
const doUpdate = (formEl: any) => {
  formEl.validate((valid: any) => {
    if (!valid) return;
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
  });
}
const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除该模板?', '提示', {type: 'warning'}).then(() => {
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
const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}

// ==== 变量调试 ====
const onRender = (row: any) => {
  renderTemplateName.value = row.name;
  renderTemplateId.value = row.id;
  renderContent.value = row.content;
  renderVars.value = '';
  renderResult.value = '';
  renderDialogVisible.value = true;
}
const doRender = () => {
  let vars: any = {};
  if (renderVars.value) {
    try { vars = JSON.parse(renderVars.value); }
    catch { return ElMessage.error('变量必须为合法JSON'); }
  }
  renderApi({templateId: renderTemplateId.value, variables: vars}).then((res: any) => {
    renderResult.value = res.data || '';
  });
}

// ==== 版本 ====
const onVersions = (row: any) => {
  versionTemplateId.value = row.id;
  viewingVersion.value = null;
  versionDialogVisible.value = true;
  listVersions(row.id).then((res: any) => {
    versionList.value = res.data || [];
  });
}
const onViewVersion = (row: any) => {
  getVersion(versionTemplateId.value, row.version).then((res: any) => {
    viewingVersion.value = res.data;
  });
}
const onRollback = (row: any) => {
  ElMessageBox.prompt('输入变更说明后回滚到 v' + row.version, '回滚提示', {
    confirmButtonText: '回滚', cancelButtonText: '取消'
  }).then((res: any) => {
    reqSuccessFeedback(rollback(versionTemplateId.value, row.version, res && res.value), '回滚成功', () => {
      versionDialogVisible.value = false;
      loadTableData();
    });
  }).catch(() => {});
}
</script>

<style scoped></style>
