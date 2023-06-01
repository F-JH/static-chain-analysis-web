<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElTree, ElScrollbar, ElDropdown, ElDropdownItem, ElTooltip } from "element-plus";
import { More, Link, FolderOpened } from '@element-plus/icons-vue';

import type { PropType } from "vue";
import type { fileNode } from "../../include/chain";

export default defineComponent({
    name: "FileTree",
    props: {
        itemMore:{
            type: Boolean,
            required: false,
            default: true
        },
        fileTree: {
            type: Object as PropType<fileNode[]>,
            required: true,
            default: ''
        },
        slotNum: {
            type: Number,
            required: false,
            default: 0
        },
        showLeaf: {
            type: Boolean,
            required: false,
            default: true
        }
    },
    emits: [
        'nodeClick',
        'reName',
        'moveFloder',
        'deleteItem',
        'editItem'
    ],
    components: {
        ElDropdownItem, ElDropdown,
        ElTree,
        ElScrollbar,
        More,
        Link,
        FolderOpened,
        ElTooltip
    },
    setup(props, { emit }){
        const treeData: fileNode[] = reactive<fileNode[]>(props.fileTree)
        const treeRef = ref<InstanceType<typeof ElTree> | null>(null)

        onMounted(()=>{
            treeRef.value?.filter(null)
        })

        return { treeData, treeRef }
    },
    methods: {
        nodeClick(node:fileNode){
            console.log('show leaf', this.showLeaf)
            this.$emit('nodeClick', node)
        },
        reName(data:any){
            this.$emit('reName', data)
        },
        moveFloder(data:any){
            this.$emit('moveFloder', data)
        },
        deleteItem(data:any){
            this.$emit('deleteItem', data)
        },
        editItem(data:any){
            this.$emit('editItem', data)
        },
        statusColor(status:number){
            switch (status) {
                case 0:
                    return 'rgb(140 148 136)'
                case 1:
                    return 'rgb(255, 200, 0)'
                case 2:
                    return 'rgb(125, 200, 86)'
                case 3:
                    return 'rgb(222, 58, 85)'
            }
        },
        statusText(status:number){
            switch (status) {
                case 0:
                    return '未下载'
                case 1:
                    return '下载中'
                case 2:
                    return '正常'
                case 3:
                    return '失败'
            }
        },
        filterMethod(value:any, data:any, node:any){
            console.log('filter', data)
            return data.isDirectory || this.showLeaf
        }
    }
})
</script>

<template>
    <el-tree
        ref="treeRef"
        draggable
        default-expand-all
        :data="treeData"
        :filter-node-method="filterMethod"
    >
        <template v-slot:default="{ node, data }">
            <span class="custom-tree-node">
                <el-icon><Link v-if="!data.isDirectory" /><FolderOpened v-else-if="data.isDirectory" /></el-icon>
                <el-tooltip effect="dark" :content="statusText(data.status)" placement="top">
                    <span v-if="!data.isDirectory" class="point" :style="{ marginLeft:'2px', backgroundColor: statusColor(data.status) }"></span>
                </el-tooltip>
                <span style="margin-left: 5px; width: 100%; text-align: left" @click="nodeClick(data)" :title="data.name">{{ data.name }}</span>
                <span v-if="itemMore">
                    <div @click.stop>
                        <el-dropdown trigger="click">
                        <el-icon><More /></el-icon>
                        <template #dropdown>
<!--                            <el-dropdown-item v-if="!data.isDirectory && this.$slots.dirSelection1">-->
<!--&lt;!&ndash;                                给外部的文件夹层级提供添加额外的一个按钮功能&ndash;&gt;-->
<!--                                <slot name="dirSelection1" />-->
<!--                            </el-dropdown-item>-->
                            <div v-if="data.isDirectory">
                                <el-dropdown-item v-for="slot in Array(slotNum).fill(0).map((_, index) => index+1)">
                                    <slot :name="'dirSelection'+slot" />
                                </el-dropdown-item>
                            </div>
<!--                            默认的四个功能-->
                            <el-dropdown-item @click="editItem(data)">编辑</el-dropdown-item>
                            <el-dropdown-item @click="reName(data)">重命名</el-dropdown-item>
                            <el-dropdown-item @Click="moveFloder(data)">移动至</el-dropdown-item>
                            <el-dropdown-item @click="deleteItem(data)">删除</el-dropdown-item>
                        </template>
                    </el-dropdown>
                    </div>
                </span>
            </span>
        </template>
    </el-tree>
</template>

<style>
.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}
.point{
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
}
</style>
