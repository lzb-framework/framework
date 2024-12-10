<template>
<!--
  delete from auth_route where id in (${entityId}01,${entityId}02,${entityId}03);
  INSERT INTO `auth_route` (`id`, `create_time`, `update_time`, `deleted`, `parent_id`, `type`, `name`, `url`, `regular`, `perms`, `icon`, `component_path`, `view_path`, `ext`, `sort`, `enabled`) VALUES
  (${entityId}01, '2022-01-01 00:00:00', '2022-01-01 00:00:00', 0, 1000, 2, '${label}', '/${module}/${entityName}', '', '${entityName}:page', '', '', '', '', 1, 1),
  (${entityId}02, '2022-01-01 00:00:00', '2022-01-01 00:00:00', 0, 1000, 3, '${label}保存', '', '', '${entityName}:save', '', '', '', '', 10, 1),
  (${entityId}03, '2022-01-01 00:00:00', '2022-01-01 00:00:00', 0, 1000, 4, '${label}删除', '', '', '${entityName}:deleteById', '', '', '', '', 10, 1)
  ;
-->
  <div class="app-container">
    <zparent :is-edit="true">
      <!--GG  #foreach($field in ${fields})           GG-->
      <!--GG    #if(${field.uiType}=='relationCode')  GG-->
      <!--GG <zselect v-model="demoListQuery.enum0001" entityName="${field.columnName.replace("Code", "").replace("Id", "")}" listLabel="name" placeholder="${field.label}" /> GG-->
      <!--GG    #elseif($field.uiType == 'base'
                    || ${field.uiType}=='image')        GG-->
      <!--GG    #else                                 GG-->
      <!--GG        #if(${field.type}=='smallint'
                       || ${field.type}=='mediumint'
                       || ${field.type}=='int'
                       || ${field.type}=='integer'
                       || ${field.type}=='bigint'
                       || ${field.type}=='float'
                       || ${field.type}=='double'
                       || ${field.type}=='decimal')    GG-->
<!--      <zinput v-model="demoListQuery.number01" type="number" placeholder="${field.label}"/>-->
      <!--GG        #elseif((${field.type}=='char'
                      || ${field.type}=='varchar'
                      || ${field.type}=='tinytext') && !$field.javaTypeEnumClass )    GG-->
      <zinput v-model="demoListQuery.string01" placeholder="${field.label}"/>
      <!--GG        #elseif(${field.type}=='text'
                      || ${field.type}=='longtext')    GG-->
      <zinput v-model="demoListQuery.string02" type="input"  placeholder="${field.label}"/>
      <!--GG          #elseif(${field.type}=='varchar' && $field.javaTypeEnumClass )  GG-->
      <zselect v-model="demoListQuery.enum0001" classname="EnumDictType"  placeholder="${field.label}" />
      <!--GG        #elseif(${field.type}=='tinyint')  GG-->
      <zbool v-model="demoListQuery.boolean1"  placeholder="${field.label}" />
      <!--GG        #elseif(${field.type}=='date')  GG-->
<!--      <zinput v-model="demoListQuery.date0001"  placeholder="${field.label}"/>-->
      <!--GG        #elseif(${field.type}=='time')   GG-->
<!--      <zinput v-model="demoListQuery.time0001"  placeholder="${field.label}"/>-->
      <!--GG        #elseif(${field.type}=='datetime')  GG-->
<!--      <zinput v-model="demoListQuery.datetime" w="180"  placeholder="${field.label}"/>-->
      <!--GG        #else                     GG-->
      <!--GG        #end                      GG-->
      <!--GG     #end                         GG-->
      <!--GG  #end                                    GG-->
      <zdatepicker v-model="demoDateRange" size="small" />
      <el-button type="primary" icon="el-icon-search" @click="demoSearch" >查询</el-button>
      <el-button v-if="hasPerm('demo:save')" type="primary" icon="el-icon-plus" @click="demoHandleAdd" >添加</el-button>
    </zparent>
    <el-table v-loading="demoListLoading"
              :is-edit="false"
              :data="demos"
              @sort="demoChangeSort"
              @sort-change="demoChangeSort"
              :header-cell-style="demoHandleTheadStyle"
              border
              fit
              highlight-current-row
              style="width: 100%;"
    >
      <!--GG  #foreach($field in ${fields})     GG-->
     <el-table-column
         label="${field.label}"
         prop="${field.columnName}"
         align="center"
         :width="${field.tableColumnWidth}"
      <!--GG    #if(${field.type}=='smallint'
                     || ${field.type}=='mediumint'
                     || ${field.type}=='int'
                     || ${field.type}=='integer'
                     || ${field.type}=='bigint'
                     || ${field.type}=='float'
                     || ${field.type}=='double'
                     || ${field.type}=='decimal')  GG-->
      sortable="custom"
      <!--GG    #end  GG-->

      >
       <template v-slot="scope">
         <!--GG    #if(${field.uiType}=='relationCode')  GG-->
         <!--GG <zselect v-model="scope.row.enum0001" entityName="${field.columnName.replace("Code", "").replace("Id", "")}" listLabel="name"/> GG-->
         <!--GG    #elseif(${field.uiType}=='image')  GG-->
         <zupload v-model="scope.row.string01" module="cfg" w="100" h="100" />
         <!--GG    #elseif(${field.type}=='smallint'
                     || ${field.type}=='mediumint'
                     || ${field.type}=='int'
                     || ${field.type}=='integer'
                     || ${field.type}=='bigint'
                     || ${field.type}=='float'
                     || ${field.type}=='double'
                     || ${field.type}=='decimal')    GG-->
          <zinput v-model="scope.row.number01" type="number" />
          <!--GG    #elseif((${field.type}=='char'
                      || ${field.type}=='varchar'
                      || ${field.type}=='tinytext') && !$field.javaTypeEnumClass )    GG-->
          <zinput v-model="scope.row.string01" />
          <!--GG    #elseif(${field.type}=='text'
                      || ${field.type}=='longtext')    GG-->
          {{scope.row.string02}}
<!--          show-overflow-tooltip-->
          <!--GG    #elseif(${field.type}=='varchar' && $field.javaTypeEnumClass )  GG-->
          <zselect v-model="scope.row.enum0001" classname="EnumDictType" />
          <!--GG    #elseif(${field.type}=='tinyint')  GG-->
          <zbool v-model="scope.row.boolean1" />
          <!--GG    #elseif(${field.type}=='date')  GG-->
          <zinput v-model="scope.row.date0001" />
          <!--GG    #elseif(${field.type}=='time')   GG-->
          <zinput v-model="scope.row.time0001" />
          <!--GG    #elseif(${field.type}=='datetime')  GG-->
          <zinput v-model="scope.row.datetime" w="150" />
          <!--GG    #else                     GG-->
          <!--GG    #end                      GG-->
        </template>
      </el-table-column>
      <!--GG  #end                                    GG-->
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button v-if="hasPerm('demo:save')" size="mini" type="primary" @click="demoHandleUpdate(scope.row)">编辑</el-button>
          <el-button v-if="hasPerm('demo:deleteById')" size="mini" type="danger" @click="demoHandleDelete(scope.row['${keyFields}'])">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pa-4"
      :total="demoTotal"
      :current-page.sync="demoListQuery.curPage"
      :page-size.sync="demoListQuery['pageSize']"
      v-bind="$attrs"
      @size-change="demoPage"
      @current-change="demoPage"
      align="center"
      layout="total, sizes, prev, pager, next, jumper"
    />

    <el-dialog title="${label}" v-if="demoDialogFormVisible" :visible.sync="demoDialogFormVisible" :is-edit="true" width="80%" append-to-body>
      <el-form ref="demoDataForm" :rules="demoRules" :model="demoFormObj" label-position="left" label-width="160px" style="margin: 0 5%;">
        <!--GG  #foreach($field in ${fields})     GG-->
        <el-form-item label="${field.label}" prop="${field.columnName}">
          <!--GG    #if(${field.uiType}=='relationCode')  GG-->
          <!--GG <zselect v-model="demoFormObj.enum0001" entityName="${field.columnName.replace("Code", "").replace("Id", "")}" listLabel="name" /> GG-->
          <!--GG    #elseif(${field.uiType}=='textarea')  GG-->
          <zinput v-model="demoFormObj.string01" type="textarea" />
          <!--GG    #elseif(${field.uiType}=='image')  GG-->
          <zupload v-model="demoFormObj.string01" module="cfg" w="100" h="100" />
          <!--GG    #elseif(${field.type}=='smallint'
                      || ${field.type}=='mediumint'
                      || ${field.type}=='int'
                      || ${field.type}=='integer'
                      || ${field.type}=='bigint'
                      || ${field.type}=='float'
                      || ${field.type}=='double'
                      || ${field.type}=='decimal')    GG-->
          <zinput v-model="demoFormObj.number01" type="number" />
          <!--GG    #elseif((${field.type}=='char'
                      || ${field.type}=='varchar'
                      || ${field.type}=='tinytext') && !$field.javaTypeEnumClass )    GG-->
          <!--GG   <zinput v-model="demoFormObj.string01"
                          #if(${field.columnName}=='codecode')
                            :disabled="demoFormOption !== 'add'"
                          #end
                           /> GG-->
          <!--GG    #elseif(${field.type}=='text'
                      || ${field.type}=='longtext')    GG-->
          <Tinymce v-model="demoFormObj.string02" module="cfg" :height="500" />
          <!--GG    #elseif(${field.type}=='varchar' && $field.javaTypeEnumClass )  GG-->
          <zselect v-model="demoFormObj.enum0001" classname="EnumDictType" />
          <!--GG    #elseif(${field.type}=='tinyint')  GG-->
          <zradio v-model="demoFormObj.boolean1"
                  #if(${field.uiType}=='base')
                  :disabled="demoFormOption!=='add'"
                  #end
          />
          <!--GG    #elseif(${field.type}=='date')  GG-->
          <zinput v-model="demoFormObj.date0001" />
          <!--GG    #elseif(${field.type}=='time')   GG-->
          <zinput v-model="demoFormObj.time0001" />
          <!--GG    #elseif(${field.type}=='datetime')  GG-->
          <!--GG   <zdatetime v-model="demoFormObj.datetime" w="200"
                          #if(${field.uiType}=='base')
                            :disabled="true"
                          #end
                           /> GG-->
          <!--GG    #else                     GG-->
          <!--GG    #end                      GG-->
        </el-form-item>
        <!--GG  #end                                    GG-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="demoDialogFormVisible = false">取消</el-button>
        <el-button v-if="hasPerm('demo:save')" type="primary" @click="demoSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Tinymce from '@/components/Tinymce'

export default {
  name: 'Demo',
  components: { Tinymce },
  filters: {
  },
  props: {
  },
  computed: {
  },
  async beforeRouteEnter (to, from, next) {
    // await store.dispatch('common/dictListComputeIfAbsent','sysStaffPosition')
    next((vm) => {
      // access to component public instance via `vm`
    })
  },
  data() {
    return {
      demoDefault: {
<!--GG  #foreach($field in ${fields})     GG-->
<!--GG    #if($field.uiType == 'base')   GG-->
<!--GG    #else  GG-->
<!--GG  '${field.columnName}': ${field.defaultValue},   GG-->
<!--GG    #end                            GG-->
<!--GG  #end                              GG-->
      },
      demos: [],
      demoTotal: 0,
      demoListLoading: false,
      demoDateRange: [null,null],
      demoListQuery: {
        <!--GG  #foreach($field in ${fields})     GG-->
        <!--GG    #if($field.uiType == 'base'  || ${field.uiType}=='image')  GG-->
        <!--GG    #else  GG-->
          '${field.columnName}': '',
        <!--GG    #end                            GG-->
        <!--GG  #end                              GG-->
        curPage: 1,
        pageSize: 20
      },
      demoFormObj: { enabled: true },
      demoFormOption: '',
      demoSortMap:{},
      demoDialogFormVisible: false,
      demoRules: {
        <!--GG  #foreach($field in ${fields})     GG-->
        <!--GG    #if($field.uiType == 'base'
                     || ${field.uiType}=='image')   GG-->
        <!--GG    #elseif($field.notNull)         GG-->
        '${field.columnName}': [{ required: true, message: '${field.label}不能为空', trigger: 'blur' }],
        <!--GG    #else  GG-->
        <!--GG    #end                            GG-->
        <!--GG  #end                              GG-->
      }
    }
  },
  created() {
    this.demoPage()
  },
  methods: {
    /**
     * 设置表头排序
     */
    demoHandleTheadStyle({row, column, rowIndex, columnIndex}) {
      let props = Object.keys(this.demoSortMap);
      for(let prop of props){
        if (prop === column.property) {
          column.order = this.demoSortMap[prop]
        }
      }
    },
    demoChangeSort(e) {
      if (e.order) {
        this.demoSortMap[e.prop] = e.order
      }else{
        delete this.demoSortMap[e.prop]
      }
      this.demoPage()
    },
    async demoPage() {
      let map = this.demoSortMap;

      this.demoListLoading = true
      if (this.demoDateRange && this.demoDateRange.length === 2) {
        this.demoListQuery.start = this.demoDateRange[0]
        this.demoListQuery.end = this.demoDateRange[1]
      }
      this.demoListQuery.orders = Object.keys(map).filter(key=>map[key]).map(key=>{ return {column: key, asc: map[key] === 'ascending'} })
      const data = await this.$page('demo', this.demoListQuery)
      this.demos = data.records
      this.demoTotal = parseInt(data.total)
      this.demoListLoading = false
    },
    demoSearch() {
      this.demoListQuery.curPage = 1
      this.demoPage()
    },

    demoHandleAdd() {
      this.demoFormOption = 'add'
      this.demoFormObj = { ...this.demoDefault }
      this.demoDialogFormVisible = true
      this.$nextTick(() => {
        this.$refs
          ['demoDataForm'].clearValidate()
      })
    },
    demoHandleUpdate(row) {
      this.demoFormOption = 'update'
      this.demoFormObj = Object.assign({}, row)
      this.demoDialogFormVisible = true
      this.$nextTick(() => {
        this.$refs
          ['demoDataForm'].clearValidate()
      })
    },
    demoSave() {
      // $a.b再加左括号,会velocity模板语法错误
      this.$refs
        ['demoDataForm']
        .validate((valid) => {
          if (valid) {
            const temp = Object.assign({}, this.demoFormObj)
            this.$saveOrUpdate('demo', temp).then(() => {
              this.demoDialogFormVisible = false
              this.$message.success('保存成功')
              this.demoPage()
            })
          }
        })
    },
    demoHandleDelete(id) {
      this.$confirm('确认删除${label}?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$deleteById('demo', id).then(() => {
          this.$message.success('删除成功')
          this.demoPage()
        })
      })
    },
    demoChangeState(row) {
      this.$updateById('demo', row).then(() => {
        this.demoDialogFormVisible = false
        this.$message.success('保存成功')
      })
    }
  }
}
</script>
