<template>
  <table-manage>
    <template #search>
      <el-form-item label="文档名称">
        <el-input placeholder="名称" v-model="pageParam.searchObject.name"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select placeholder="选择状态" style="width: 200px" v-model="pageParam.searchObject.status">
          <el-option label="待处理" :value="0"/>
          <el-option label="处理中" :value="1"/>
          <el-option label="已完成" :value="2"/>
          <el-option label="失败" :value="3"/>
        </el-select>
      </el-form-item>
      <el-form-item label="知识库">
        <el-select placeholder="选择知识库" style="width: 200px" v-model="pageParam.searchObject.kbId" filterable clearable>
          <el-option v-for="kb in kbList" :key="kb.id" :label="kb.name" :value="kb.id"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onUpload">上传文档</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%">
        <el-table-column prop="name" min-width="250" label="文档名称" show-overflow-tooltip/>
        <el-table-column prop="type" width="80" label="类型">
          <template #default="scope">
            <el-tag>{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" width="100" label="大小">
          <template #default="scope">
            {{ formatSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="chunkCount" width="100" label="分块数"/>
        <el-table-column prop="kbName" width="150" label="知识库" show-overflow-tooltip/>
        <el-table-column prop="status" width="100" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">处理中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="success">已完成</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" width="180" label="创建时间"/>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="scope">
            <el-button :icon="RefreshRight" size="small" @click="onReIndex(scope.row.id)" :disabled="scope.row.status === 1">重新索引</el-button>
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
      <el-dialog v-model="dialogFormVisible" title="上传文档" width="500px">
        <el-form ref="formRef" label-width="100px" :model="uploadForm" :rules="rules">
          <el-form-item prop="file" label="选择文件">
            <el-upload ref="uploadRef" :auto-upload="false" :on-change="onFileChange" :limit="1" drag>
              <el-icon class="el-icon--upload"><upload-filled/></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或<em>点击选择</em></div>
              <template #tip>
                <div class="el-upload__tip">支持 PDF / DOCX / TXT / MD 格式</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="知识库">
            <el-select v-model="uploadForm.kbId" placeholder="可选" filterable clearable style="width: 100%">
              <el-option v-for="kb in kbList" :key="kb.id" :label="kb.name" :value="kb.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="分块策略">
            <el-select v-model="uploadForm.chunkStrategy" style="width: 100%">
              <el-option label="按段落" value="paragraph"/>
              <el-option label="按大小" value="size"/>
              <el-option label="递归分割" value="recursive"/>
            </el-select>
          </el-form-item>
          <el-form-item label="分块大小">
            <el-input-number v-model="uploadForm.chunkSize" :min="100" :max="2000" :step="100"/>
          </el-form-item>
          <el-form-item label="重叠字数">
            <el-input-number v-model="uploadForm.chunkOverlap" :min="0" :max="200" :step="10"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="doUpload">上传</el-button>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {listByPage, uploadFile, reIndex, remove} from "@/api/ai/document-api";
import {listEnabled} from "@/api/ai/knowledge-base-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm, ElMessage, ElMessageBox, ElUpload} from "element-plus";
import {DeleteFilled, Plus, Search, RefreshRight, UploadFilled} from "@element-plus/icons-vue";
import type {UploadProps, UploadInstance} from "element-plus";

type FormInstance = InstanceType<typeof ElForm>
const formRef = ref<FormInstance>();
const uploadRef = ref<UploadInstance>();

const dialogFormVisible = ref<boolean>(false);
const uploading = ref<boolean>(false);
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
const loading = ref<boolean>(true);
const kbList = ref<any[]>([]);
const selectedFile = ref<File | null>(null);

const uploadForm = ref<any>({chunkStrategy: 'paragraph', chunkSize: 500, chunkOverlap: 50});

const rules = reactive({});

onMounted(() => {
  loadTableData();
  loadKbList();
});

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    document: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const loadKbList = () => {
  reqCommonFeedback(listEnabled(), (data: any) => {
    kbList.value = data || [];
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

const onFileChange: UploadProps['onChange'] = (uploadFile) => {
  selectedFile.value = uploadFile.raw as File;
}

const onUpload = () => {
  selectedFile.value = null;
  uploadForm.value = {chunkStrategy: 'paragraph', chunkSize: 500, chunkOverlap: 50};
  dialogFormVisible.value = true;
}

const doUpload = () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件');
    return;
  }
  uploading.value = true;
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  if (uploadForm.value.kbId) formData.append('kbId', uploadForm.value.kbId);
  formData.append('chunkStrategy', uploadForm.value.chunkStrategy);
  formData.append('chunkSize', String(uploadForm.value.chunkSize));
  formData.append('chunkOverlap', String(uploadForm.value.chunkOverlap));

  reqCommonFeedback(uploadFile(formData), () => {
    ElMessage.success('上传成功，文档正在后台处理');
    dialogFormVisible.value = false;
    uploading.value = false;
    loadTableData();
  }, () => {
    uploading.value = false;
  });
}

const onReIndex = (id: string) => {
  ElMessageBox.confirm('确认重新索引该文档?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    reqCommonFeedback(reIndex(id), () => {
      ElMessage.success('重新索引已触发');
      loadTableData();
    });
  });
}

const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除该文档?', '提示', {
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

const formatSize = (bytes: number) => {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024;
    i++;
  }
  return size.toFixed(1) + ' ' + units[i];
}
</script>

<style scoped></style>
