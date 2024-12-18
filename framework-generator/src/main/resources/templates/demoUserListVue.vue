<template>
  <div class="">
    <m-header :title="'我的贷款'"></m-header>

    <van-tabs v-model="state" @click="reloadDemoList()" sticky>
      <van-tab :title="$t(`全部`)" key="all" name="all"></van-tab>
      <template v-for="(label,code) in dictEnumMapAll['EnumLoanOrderState']">
        <van-tab :title="$t(`EnumLoanOrderState_${code}`)" :key="code" :name="code"></van-tab>
      </template>
    </van-tabs>

    <van-empty v-if="!demos || demos.length === 0" :description="$t(`No_More_Data`)"/>
    <div class="container" v-else>
      <template v-for="(demoOne,index) in list">
        <demo :key="index" :demo="demoOne" @click="jump('/user/demoDetail?id='+demoOne.id)"></demo>
      </template>
      <van-pagination mode="simple" v-model="demoParam.curPage" :total-items="demoParam.total" :items-per-page="demoParam.pageSize"
                      :show-page-size="5" force-ellipses @change="getDemoList">
      </van-pagination>
    </div>

    <van-dialog v-model="showDemo"
                :title="(demoFormOption==='add'? $t('添加'): $t('修改')) + $t('${label}')"
                @confirm="commit"
                >
      <demoUserForm :demoForm="demoForm" @afterSubmit="showDemo = false" @afterCancel="showDemo = false"></demoUserForm>
    </van-dialog>
  </div>
</template>

<script>
import demoUserForm from './demoUserFormVue.vue'

export default {
  name: "demoUserListVue",
  components: {
    demoUserForm
  },
  computed: {},
  data() {
    return {
      demos: [],
      demoForm: {},
      demoFormOption: 'add',
      demoTotal: 0,
      showDemo: false,
      demoParam: {
        curPage: 1,
        pageSize: 20,
        state: 'all',
      },
      demoSelected: {},
      state_type: {
        'INIT': 'primary',
      }
    }
  },
  created() {
    this.getDemoList()
  },
  methods: {
    async getDemoList() {
      let data = await this.$get('/user/demo/page',
          {
            state: this.state === 'all' ? null : this.state,
            curPage: this.curPage,
            pageSize: this.pageSize
          })
      this.total = parseInt(data.total)
      let list = data.records
      if (this.curPage === 1) {
        this.list = list
      } else {
        this.list = this.list.concat(list)
      }
    },
    reloadDemoList() {
      this.demoParam.curPage = 1
      this.getDemoList()
    },
    commit() {
    },
  }
}
</script>
<style lang="scss" scoped>

</style>
