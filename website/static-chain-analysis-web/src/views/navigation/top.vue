<template>
  <el-menu class="el-menu" mode="horizontal" background-color="#334157" text-color="#fff" active-text-color="#fff" style="width: 100%">
    <el-menu-item style="width: 238px" :index="'/'">
      <el-icon>
        <ElemeFilled />
      </el-icon>
      static chain analysis
    </el-menu-item>
  </el-menu>
</template>

<script lang="ts">
import { ElMenu, ElMenuItem, ElIcon } from "element-plus";
import { ElemeFilled } from "@element-plus/icons-vue";
import { defineComponent } from "vue";

export default defineComponent({
    name: 'top',
    components: {
        ElMenu,
        ElMenuItem,
        ElIcon,
        ElemeFilled
    }
})
</script>

<style scoped>

</style>
