<template>
    <el-container class="index-con">
        <el-header class="index-header">
            <top></top>
        </el-header>
        <el-container class="main-con">
            <el-aside :class="showclass">
                <left @get-head="getHead"></left>
            </el-aside>
            <el-main :style="{backgroundImage: imgUrl}" style="background-size: 100% 100%">
                <el-card class="box-card" :body-style="cardStyle">
                    <template #header>
                        <div class="card-header">
                            <span>{{ cardTitle }}</span>
                        </div>
                    </template>
                    <router-view></router-view>
                </el-card>
            </el-main>
        </el-container>
    </el-container>
</template>

<script lang="ts">
import { ElContainer, ElHeader, ElAside, ElMain } from "element-plus";
import { defineComponent, ref, reactive } from "vue";
import left from '@/views/navigation/left.vue';
import top from '@/views/navigation/top.vue';
import type { CSSProperties } from "@vue/runtime-dom";

export default defineComponent({
    name: 'HomeView',
    components: {
        ElContainer,
        ElHeader,
        ElAside,
        ElMain,
        left,
        top
    },
    setup() {
        const showclass = ref('asideshow')
        const imgUrl = ref('../../public/top_img.jpeg')
        const cardTitle = ref('static chain analysis')

        function getHead(title:string){
            cardTitle.value = title
        }

        const cardStyle:CSSProperties = reactive<CSSProperties>({
            height: '90%',
        } as CSSProperties)

        return { showclass, imgUrl, getHead, cardTitle, cardStyle }
    }
})
</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.index-con {
    height: 100%;
    width: 100%;
    box-sizing: border-box;
}
.main-con{
    height: 100%;
    width: 100%;
    box-sizing: border-box;
}
.aside {
    width: 64px !important;
    height: 100%;
    background-color: #334157;
    margin: 0px;
}
.asideshow {
    width: 180px !important;
    height: 100%;
    background-color: #334157;
    margin: 0px;
    box-shadow: 2px 0 6px rgb(0 21 41 / 35%);
}
.index-header,
.index-main {
    padding: 0px;
    border-left: 2px solid #333;
}
.box-card{
    height: 100%;
    width: 100%;
}
</style>
