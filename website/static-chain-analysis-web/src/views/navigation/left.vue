<template>
    <el-menu :collapse="collapsed" collapse-transition router :default-active="$route.path" unique-opened class="el-menu-vertical-demo" background-color="#334157" text-color="#fff" active-text-color="#ffd04b">
        <el-menu-item-group>
            <el-menu-item v-for="item in allmenu" :index="'/' + item.url" :key="item.menuid" @click="updateTitle(item.name)">
                <el-icon>
                    <component :is="item.icon" />
                </el-icon>
                <span>{{item.name}}</span>
            </el-menu-item>
        </el-menu-item-group>
    </el-menu>
</template>

<script lang="ts">
import { ref, reactive, defineComponent } from 'vue'
import { ElMenu, ElMenuItemGroup, ElMenuItem, ElIcon } from "element-plus";
import { Share, CreditCard, DataAnalysis } from "@element-plus/icons-vue";

export default defineComponent({
    name: 'left',
    emits: [
        'getHead'
    ],
    components: {
        ElMenu,
        ElMenuItem,
        ElMenuItemGroup,
        ElIcon,
        CreditCard,
        Share,
        DataAnalysis
    },
    setup(props, { emit }){
        const collapsed = ref(false)
        const title = ref('')
        const allmenu:any[] = reactive([
            {
                name: '凭据管理',
                menuid: 0,
                url: 'credentialsProvider',
                icon: 'CreditCard'
            },
            {
                name: '代码分析',
                menuid: 1,
                url: 'branch',
                icon: 'Share'
            }
        ])
        return { collapsed, allmenu }
    },
    methods: {
        updateTitle(name:string){
            this.$emit('getHead', name)
        }
    }
})
</script>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 180px;
    min-height: 400px;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
    border: none;
    text-align: left;
}
.el-menu-item-group__title {
    padding: 0px;
}
.el-menu-bg {
    background-color: #1f2d3d !important;
}
.el-menu {
    border: none;
}
.logobox {
    height: 40px;
    line-height: 40px;
    color: #9d9d9d;
    font-size: 20px;
    text-align: center;
    padding: 20px 0px;
}
.logoimg {
    height: 40px;
}
</style>
