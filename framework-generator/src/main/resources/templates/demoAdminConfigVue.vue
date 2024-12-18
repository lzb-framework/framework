<template>
  <div v-if="tableConfigUnitInner.loaded" class="pa-4">
    <!--    <el-alert type="success">默认统一为BANK,仅用于展示</el-alert>-->
    <!--搜索-->
    <areaSearch
        :table-config-unit="tableConfigUnitInner"
        :page-params.sync="pageParams"
        @getPage="getPage"
        @showForm="showForm"
    />
    <!--表格-->
    <areaTable
        ref="table"
        :table-config-unit="tableConfigUnitInner"
        :page-params="pageParams"
        @showForm="showForm"
    />
    <!--编辑弹窗-->
    <areaFormDialog
        v-model="dialogFormVisible"
        :table-config-unit="tableConfigUnitInner"
        :form-data="formData"
        :form-data-option="formDataOption"
        @saveSuccess="getPage();dialogFormVisible=false"
    />
  </div>
</template>
<script>
import configEntity from '@/parent-ui/src/main/business/admin/configEntity.vue'

export default {
  name: 'payCardType',
  extends: configEntity,
  data() {
    return {
      tableConfigUnit: {
        entityName: '${entityName}',
      }
    }
  },
}
</script>
