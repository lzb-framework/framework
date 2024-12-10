<template>
  <div class="">
    <van-form class="demoUserForm" @submit="confirm">
      <!--GG  #foreach($field in ${fields})           GG-->
      <div>
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
        <!--      <zfield v-model="demoListQuery.number01" type="number" placeholder="${field.label}"/>-->
        <!--GG        #elseif((${field.type}=='char'
                        || ${field.type}=='varchar'
                        || ${field.type}=='tinytext') && !$field.javaTypeEnumClass )    GG-->
        <zfield v-model="demoListQuery.string01" placeholder="${field.label}"/>
        <!--GG        #elseif(${field.type}=='text'
                        || ${field.type}=='longtext')    GG-->
        <zfield v-model="demoListQuery.string02" type="input" placeholder="${field.label}"/>
        <!--GG          #elseif(${field.type}=='varchar' && $field.javaTypeEnumClass )  GG-->
        <zselect v-model="demoListQuery.enum0001" classname="EnumDictType" placeholder="${field.label}"/>
        <!--GG        #elseif(${field.type}=='tinyint')  GG-->
        <zbool v-model="demoListQuery.boolean1" placeholder="${field.label}"/>
        <!--GG        #elseif(${field.type}=='date')  GG-->
        <!--      <zfield v-model="demoListQuery.date0001"  placeholder="${field.label}"/>-->
        <!--GG        #elseif(${field.type}=='time')   GG-->
        <!--      <zfield v-model="demoListQuery.time0001"  placeholder="${field.label}"/>-->
        <!--GG        #elseif(${field.type}=='datetime')  GG-->
        <!--      <zfield v-model="demoListQuery.datetime" w="180"  placeholder="${field.label}"/>-->
        <!--GG        #else                     GG-->
        <!--GG        #end                      GG-->
        <!--GG     #end                         GG-->
      </div>
      <!--GG  #end                                    GG-->
      <van-button
          class="btn btn-primary btn-block fw-600"
          type="submit"
      >
        {{ $t("确认") }}
      </van-button>
      <van-button
          class="btn btn-primary btn-block fw-600"
          @click="$emit('afterCancel')"
      >
        {{ $t("取消") }}
      </van-button>
    </van-form>
  </div>
</template>

<script>

export default {
  name: "demoUserForm",
  props: {
    demoUserForm: {},
  },
  components: {},
  computed: {},
  data() {
    return {
      demoLoading: false,
    }
  },
  created() {
  },
  methods: {
    confirm() {
      if (this.demoLoading) {
        return
      }
      this.demoLoading = true
      this.$post('/user/demo/submit', this.demoUserForm).then(res => {
        this.getToken()
        this.$toast
            .success(this.$t('Commit_Success'))
      }).finally(e => {
        this.demoLoading = true
        this.$emit('afterSubmit')
      })
    },
  }
}
</script>
<style lang="scss" scoped>

</style>
