<template>
  <div class="content-wrapper">
    <m-header :title="'认证信息'"></m-header>
    <van-form @submit="onSubmit">
      <zfield v-model="demo.name" :type="null" :label="$t('真实姓名')" :required="true"></zfield>
      <zfield v-model="demo.age" :type="'digit'" :label="$t('年龄')" :required="true"></zfield>
      <zfield v-model="demo.idCardImg1" :type="'image'" :label="$t('证件照片1')" :required="true" :imgCenter="true" imageOnEmpty="/static/exchange/identification/img11.png"></zfield>
      <zfield v-if="demo.state" :value="$t('EnumDemoState_'+ demo.state)" :type="null" :label="$t('状态')" :required="true" readonly></zfield>
      <div v-if="demo.state !== 'SUCCESS'" class="pa-2">
        <van-button block type="primary" native-type="submit">
          {{ $t(demo && demo.id ?'修改并提交':'提交') }}
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script>
export default {
  name: "demo",
  components: {},
  computed: {
    ...Vuex.mapGetters([
    ]),
  },
  data() {
    return {
      loading: false,
      demo: {},
      demoDefault: {},
    }
  },
  created() {
    // this.demo.id = this.$route.params.id
    this.getDemoByUserId()
  },
  methods: {
    async getDemoByUserId() {
      this.demo = await this.$get(`/user/demo/getByUserId`) || {...this.demoDefault}
    },
    async onSubmit() {
      if (this.loading) {
        return
      }
      this.loading = true
      try {
        await this.$post('/user/demo/saveOrUpdate', {...this.demo})
        this.$message.success(this.$t('save_success'))
        await this.getDemoByUserId()
        // this.$router.push({path: `/user/index`})
      } finally {
        this.loading = false
      }
    },
  }
}
</script>
<style lang="scss" scoped>

</style>
