<template>
  <table-manage>
    <template #search>
      <el-form-item label="名称">
        <el-input placeholder="Key 名称" v-model="pageParam.searchObject.name"/>
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
      <el-button :icon="Plus" type="primary" @click="onCreate">生成 Key</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" width="180" label="名称"/>
        <el-table-column prop="keyPrefix" width="160" label="Key 前缀">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.keyPrefix }}****</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" width="120" label="所属用户"/>
        <el-table-column label="速率限制" width="150">
          <template #default="scope">
            <span>RPM: {{ scope.row.rpmLimit || '-' }}</span><br/>
            <span>TPM: {{ scope.row.tpmLimit || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="月度 Token" width="180">
          <template #default="scope">
            <span v-if="scope.row.monthlyTokenQuota">
              {{ scope.row.tokensUsedThisMonth || 0 }} / {{ scope.row.monthlyTokenQuota }}
            </span>
            <el-tag v-else type="info">不限</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" width="170" label="过期时间"/>
        <el-table-column prop="enableStatus" width="90" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enableStatus === 1 ? 'success' : 'info'">
              {{ scope.row.enableStatus === 1 ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="onShowUsage(scope.row)">用量</el-button>
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
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id ? '编辑' : '生成'} API Key`" width="680px">
        <el-form ref="formRef" label-width="140px" :model="editForm" :rules="rules">
          <el-form-item prop="name" label="Key 名称">
            <el-input v-model="editForm.name" placeholder="用于区分用途"/>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2"/>
          </el-form-item>
          <el-form-item label="允许模型 ID">
            <el-input v-model="editForm.allowedModelIds" placeholder="逗号分隔; 留空=全部允许"/>
          </el-form-item>
          <el-form-item label="IP 白名单">
            <el-input v-model="editForm.allowedIpList" placeholder="逗号分隔; 留空=不限"/>
          </el-form-item>
          <el-form-item label="RPM 限制">
            <el-input-number v-model="editForm.rpmLimit" :min="0" placeholder="每分钟请求数"/>
          </el-form-item>
          <el-form-item label="TPM 限制">
            <el-input-number v-model="editForm.tpmLimit" :min="0" placeholder="每分钟 Token 数"/>
          </el-form-item>
          <el-form-item label="月度 Token 配额">
            <el-input-number v-model="editForm.monthlyTokenQuota" :min="0"/>
          </el-form-item>
          <el-form-item label="过期时间">
            <el-date-picker v-model="editForm.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"/>
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

      <el-dialog v-model="plainKeyDialogVisible" title="API Key 已生成 (仅显示一次)" width="640px" :close-on-click-modal="false">
        <el-alert type="warning" :closable="false" show-icon>
          请立即复制并妥善保管此 Key, 关闭后将无法再次查看完整内容。
        </el-alert>
        <div style="margin-top: 16px;">
          <el-input v-model="plainKey" readonly type="textarea" :rows="3"/>
        </div>
        <template #footer>
          <el-button type="primary" @click="onCopyPlainKey">复制</el-button>
          <el-button @click="plainKeyDialogVisible = false">我已保存</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="usageDialogVisible" title="Key 用量统计 (近 30 天)" width="720px">
        <el-table :data="usageList" style="width: 100%" max-height="420">
          <el-table-column prop="statDate" label="日期" width="140"/>
          <el-table-column prop="requestCount" label="请求数" width="120"/>
          <el-table-column prop="successCount" label="成功" width="120"/>
          <el-table-column prop="failCount" label="失败" width="120"/>
          <el-table-column prop="totalTokens" label="总 Token"/>
          <el-table-column prop="totalCost" label="费用"/>
        </el-table>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage, generate, update, deleteBatch, statRecent} from "@/api/ai/api-key-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Search, RefreshRight} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();

const dialogFormVisible = ref<boolean>(false);
const plainKeyDialogVisible = ref<boolean>(false);
const usageDialogVisible = ref<boolean>(false);
const plainKey = ref<string>('');
const usageList = ref<any[]>([]);
const multipleSelection = ref<any[]>([]);
const pageParam = ref<any>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<any>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);

const editForm = ref<any>({enableStatus: 1, sort: 0});

const rules = reactive({
  name: [{required: true, message: '请输入 Key 名称', trigger: 'blur'}]
});

onMounted(() => loadTableData());

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  const param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aiApiKey: pageParam.value.searchObject
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
    if (editForm.value.id) {
      reqSuccessFeedback(update(editForm.value), '修改成功', () => {
        loadTableData();
        dialogFormVisible.value = false;
      });
    } else {
      reqCommonFeedback(generate(editForm.value), (data: any) => {
        ElMessage.success('生成成功');
        dialogFormVisible.value = false;
        loadTableData();
        if (data && data.plainKey) {
          plainKey.value = data.plainKey;
          plainKeyDialogVisible.value = true;
        }
      });
    }
  });
}
const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除该 Key?', '提示', {type: 'warning'}).then(() => {
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
const onShowUsage = (row: any) => {
  reqCommonFeedback(statRecent(row.id, 30), (data: any) => {
    usageList.value = data || [];
    usageDialogVisible.value = true;
  });
}
const onCopyPlainKey = async () => {
  try {
    await navigator.clipboard.writeText(plainKey.value);
    ElMessage.success('已复制到剪贴板');
  } catch (e) {
    ElMessage.warning('复制失败, 请手动选择文本');
  }
}
const handleSelectionChange = (arr: any) => {multipleSelection.value = arr;}
</script>

<style scoped></style>
