<script lang="ts">
import {defineComponent, reactive, ref} from 'vue';
import {
    ElRow, ElCol, ElTable, ElTableColumn, ElCard, ElInput, ElDropdown, ElDropdownItem, ElButton, ElDialog, ElForm, ElFormItem,
    ElSelect, ElOption, ElTabs, ElTabPane, ElDescriptions, ElDescriptionsItem, ElMessage, ElNotification,
    type FormRules, ElTooltip, ElDrawer
} from "element-plus";
import type { FormInstance } from "element-plus";
import {
    Search, Refresh, CirclePlusFilled, CirclePlus, FolderAdd, DataAnalysis, QuestionFilled, Eleme
} from "@element-plus/icons-vue";
import FileTree from "@/components/FileTree.vue";
import BranchUrl from "@/url/BranchUrl";
import ProjectUrl from "@/url/ProjectUrl";
import ReportUrl from "@/url/ReportUrl";
import AnalysisUrl from "@/url/AnalysisUrl";
import { sleep } from "../../../include/Urils";

import type { fileNode, gitInfo } from "../../../include/chain";
import CredentialUrl from "@/url/CredentialUrl";

export default defineComponent({
    name: "Branch",
    computed: {
        Eleme() {
            return Eleme
        }
    },
    components: {
        ElTooltip, FileTree, ElRow, ElCol, ElTable, ElTableColumn, ElCard, ElInput, Search, Refresh, CirclePlusFilled, ElDropdown,
        ElDropdownItem, ElButton, CirclePlus, FolderAdd, ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElTabs,
        ElTabPane, ElDescriptions, ElDescriptionsItem, DataAnalysis, QuestionFilled, ElDrawer
    },
    setup(){
        const isLoading = ref(false)
        const pullStatus = reactive<any>({})
        const fileKey = ref(0)
        const parentNodeName = ref('')
        const treeData = reactive<fileNode[]>([])
        const ruleFormRef = ref<FormInstance>()
        const searchInput = ref('')
        const addGitDialog = ref(false)
        const addDirDialog = ref(false)
        const showAnalysisResultDialog = ref(false)
        const selectCredentialsProviderId = ref<number>()
        const selectTreeValue = reactive<number[]>([])
        const showQuick = ref(true)
        const tabOn = ref<number>()
        const gitInfos = reactive<any>({})
        const compareBranch = reactive<any>({}) // {id: {base: 'xx', baseTag: 'xx-xx', compare: 'yy', compareTag: 'yy-yy'}}
        const branchInfos = reactive<any>({})
        const commitIds = reactive<any>({}) // {id: {master: [x,y], test: [z, h]}}
        const selectionsLoading = ref(false)
        const enableSelectCommitId = reactive<any>({})
        const confirmSelectBranch = reactive<any>({})
        const isAnalysis = reactive<any>({})
        const showDrawer = ref(false)
        const taskList = reactive<any>({})
        const reportList = reactive<any[]>([])
        const isLoadReport = ref(false)

        const tmpCredentialsProviderIds = reactive<any>({
            isFirst: true,
            data: []
        })

        const tabList = reactive<fileNode[]>([])

        const gitForm = reactive<{
            name: string,
            url: string,
            parentId: any,
            credentialsProviderId: any
        }>({
            name: '',
            url: '',
            parentId: null,
            credentialsProviderId: null
        })
        const dirForm = reactive<{
            name: string,
            parentId: any
        }>({
            name: '',
            parentId: null
        })

        const formRules = reactive<FormRules>({
            name: [
                {required: true, message: '请输入项目名称', trigger: 'blur'},
                { min: 0, max: 200, message: '0~200个字符', trigger: 'blur' }
            ],
            url: [
                {required: true, message: '请输入链接', trigger: 'blur'}
            ],
            parentId: [
                {required: true, message: '请选择', trigger: 'blur'}
            ],
            credentialsProviderId: [],
            selectPath: []
        })

        return {
            treeData, searchInput, addGitDialog, addDirDialog, gitForm, formRules, dirForm, selectTreeValue,
            showQuick, selectCredentialsProviderId, tmpCredentialsProviderIds, tabList, tabOn, gitInfos,
            compareBranch, branchInfos, commitIds, ruleFormRef, parentNodeName, fileKey, isLoading, pullStatus,
            selectionsLoading, enableSelectCommitId, confirmSelectBranch, isAnalysis, showDrawer, taskList,
            showAnalysisResultDialog, reportList, isLoadReport
        }
    },
    methods: {
        selectNode(node:fileNode){
            this.parentNodeName = node.name
            if (this.addGitDialog){
                this.gitForm.parentId = node.id
            }else if (this.addDirDialog){
                this.dirForm.parentId = node.id
            }
        },
        searchItem(){
            if (this.tmpCredentialsProviderIds.isFirst){
                this.service.get(CredentialUrl.getCredential).then((res:any) => {
                    this.tmpCredentialsProviderIds.data.splice(0, this.tmpCredentialsProviderIds.length)
                    for (let item of res.data){
                        this.tmpCredentialsProviderIds.data.push(item)
                    }
                })
                this.tmpCredentialsProviderIds.isFirst = false
            }
        },
        getTree(){
            this.service.get(BranchUrl.getGitTree).then((res:any) => {
                this.treeData.splice(0, this.treeData.length)
                this.treeData.push(res.data)
            })
        },
        getGitInfo(id:number, credentialId:number){
            // 获取git详细信息
            this.gitInfos[id] = {
                id: id,
                credentialsProvider: '未获取到',
                createTime: '未获取到',
                updateTime: '未获取到',
                lastSyncTime: '未获取到'
            }

            this.isLoading = true
            this.service.post(BranchUrl.getGitInfo + '?nodeId=' + id).then((res:any) => {
                this.gitInfos[id].createTime = res.data.createTime || '未获取到'
                this.gitInfos[id].updateTime = res.data.updateTime || '未获取到'
                this.gitInfos[id].path = res.data.path || '未获取到'
                this.gitInfos[id].lastSyncTime = res.data.lastSyncTime || '未获取到'
            })

            this.service.post(CredentialUrl.getCredentialInfo + '?credentialId=' + credentialId).then((res:any) => {
                this.gitInfos[id].credentialsProvider = res.data || '未获取到'
            })
            this.isLoading = false
        },
        async getBranchInfo(id:number){
            if (this.branchInfos[id] === undefined || this.branchInfos[id].length === 0){
                this.selectionsLoading = true
                await this.service.post(BranchUrl.getBranchs + '?nodeId=' + id).then((res:any) => {
                    this.branchInfos[id] = res.data
                    this.commitIds[id] = {}
                    this.selectionsLoading = false
                }, (error:any) => {
                    this.selectionsLoading = false
                    this.branchInfos[id] = []
                })

            }
        },
        async getCommitIds(id:number, branch:string){
            if (this.commitIds[id] === undefined || this.commitIds[id][branch] === undefined){
                this.selectionsLoading = true
                console.log('get commit id')
                if (this.commitIds[id] === undefined)
                    this.commitIds[id] = {}

                this.service.post(BranchUrl.getCommitIds + '?nodeId=' + id + '&branchName=' + branch).then((res:any) => {
                    this.commitIds[id][branch] = []
                    for (let item of res.data){
                        console.log(item)
                        this.commitIds[id][branch].push({commitId: item[0], message: item[1]})
                    }
                    // res.data.forEach((val:any, idx:any, array:any) => {
                    //     console.log(val)
                    //     this.commitIds[id][branch].push(val[0], val[1])
                    // })
                    this.selectionsLoading = false
                }, (err:any)=>{
                    this.selectionsLoading = false
                })
            }
        },
        nodeClick(node:fileNode){
            if (!node.isDirectory){
                // 关闭快捷菜单
                this.showQuick = false

                for (let item of this.tabList){
                    // 如果已打开选项卡，则跳转过去
                    if (item.id === node.id){
                        this.tabOn = item.id
                        return
                    }
                }
                // 初始化一些东西
                this.confirmSelectBranch[node.id] = false
                this.tabList.push(node)
                this.tabOn = node.id
                this.getGitInfo(node.id, node.credentialId)
                this.compareBranch[node.id] = {
                    base: '',
                    baseCommitId: '',
                    compare: '',
                    compareCommitId: ''
                }
                this.pullStatus[node.id] = false
                this.isAnalysis[node.id] = false
            }
        },
        reflashTree(){
        },
        async newFloder(formEl:FormInstance | undefined){
            // 新建目录
            await formEl?.validate((valid, fields) => {
                console.log(this.dirForm)
                if (valid){
                    let data = {
                        tree: {
                            parentId: this.dirForm.parentId,
                            name: this.dirForm.name,
                            isDirectory: true
                        },
                        name: this.dirForm.name,
                        gitUrl: null,
                        credentialsProviderId: null
                    }
                    this.service.post(BranchUrl.addNode, data).then((res:any) => {
                        ElMessage({
                            message: '添加成功',
                            type: 'success'
                        })
                        // 清空dirFrom
                        this.dirForm.name = ''
                        this.dirForm.parentId = null
                        this.parentNodeName = ''
                        this.fileKey++

                        this.addDirDialog = false
                        this.getTree()
                    })
                }
            })
        },
        async addGitProject(formEl:FormInstance | undefined){
            await formEl?.validate((valid, fields) => {
                if (valid){
                    // 添加git
                    let data = {
                        tree: {
                            parentId: this.gitForm.parentId,
                            name: this.gitForm.name,
                            isDirectory: false
                        },
                        name: this.gitForm.name,
                        gitUrl: this.gitForm.url,
                        credentialsProviderId: this.gitForm.credentialsProviderId
                    }
                    this.service.post(BranchUrl.addNode, data).then((res:any) => {
                        ElMessage({
                            message: '添加成功',
                            type: 'success'
                        })
                        // 清空gitForm
                        this.gitForm.name = ''
                        this.gitForm.url = ''
                        this.gitForm.parentId = null
                        this.gitForm.credentialsProviderId = null
                        this.parentNodeName = ''
                        this.fileKey++

                        this.addGitDialog = false
                        this.getTree()
                    })
                }
            })
        },
        reName(data:any){this.notRealized()},
        moveFloder(data:any){this.notRealized()},
        deleteItem(data:any){
            // 删除文件夹
            this.service.post(BranchUrl.deleteNode + '?nodeId=' + data.id).then((res:any) => {
                ElMessage({
                    message: '删除成功',
                    type: 'success'
                })
                this.getTree()
            })
        },
        editItem(data:any){this.notRealized()},
        removeTabItem(nodeId:number | string){
            console.log(nodeId)
            for (let i=0; i<this.tabList.length; i++){
                if (this.tabList[i].id === nodeId){
                    if (nodeId === this.tabOn && this.tabList.length > 1){
                        this.tabList.splice(i, 1)
                        this.tabOn = this.tabList[this.tabList.length-1].id
                    }else{
                        this.tabList.splice(i, 1)
                    }
                    break
                }
            }
            if (this.tabList.length === 0){
                this.showQuick = true
            }
            // 清除分支信息
            if (this.branchInfos[nodeId] !== undefined && this.branchInfos[nodeId].length > 0){
                this.branchInfos[nodeId].splice(0, this.branchInfos[nodeId].length)
            }
            this.enableSelectCommitId[nodeId] = false
        },
        async callAnalysis(id:number){
            this.isAnalysis[id] = true
            let taskId;
            await this.service.post(AnalysisUrl.callAnalysis + '?nodeId=' + id, this.compareBranch[id]).then((res:any)=>{
                taskId = res.data
            }, (err:any)=>{
                this.isAnalysis[id] = false
            })

            while (this.isAnalysis[id]){
                this.service.get(ReportUrl.getTaskStatus + '?taskId=' + taskId).then((res:any) => {
                    if (res.data.status === 2 || res.data.status === 3) {
                        this.isAnalysis[id] = false
                        ElMessage({
                            message: '分析完成',
                            type: 'success'
                        })
                    }
                })
                await sleep(10000)
            }
        },
        showAnalysisReport(nodeId:number){
            this.showDrawer = true
            this.isLoading = true
            if (this.taskList[nodeId] === undefined){
                this.taskList[nodeId] = []
            }
            if (this.taskList[nodeId].length !== 0){
                this.taskList[nodeId].splice(0, this.taskList[nodeId].length)
            }
            this.service.post(ReportUrl.getDiffTaskInfos + '?nodeId=' + nodeId).then((res:any)=>{
                for (let i=0; i<res.data.length; i++){
                    this.taskList[nodeId].push(res.data[i])
                }
                this.isLoading = false
            })
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
        async pull(node:fileNode){
            // 对项目进行pull

            this.pullStatus[node.id] = true
            let taskId;
            await this.service.post(ProjectUrl.pull + '?nodeId=' + node.id).then((res:any) => {
                taskId = res.data
            })
            // 轮询检查任务状态
            while (this.pullStatus[node.id]){
                this.service.get(ReportUrl.getTaskStatus + '?taskId=' + taskId).then((res:any) => {
                    node.status = res.data.status
                    if (res.data.status === 2 || res.data.status === 3) {
                        this.pullStatus[node.id] = false
                    }
                })
                await sleep(2000)
            }
        },
        async confirmSelectBranchs(nodeId:number){
            this.confirmSelectBranch[nodeId] = true
            if (this.compareBranch[nodeId].base === '' || this.compareBranch[nodeId].compare === ''){
                ElMessage({
                    message: '基准分支和对比分支都要选择！',
                    type: 'error'
                })
                this.confirmSelectBranch[nodeId] = false
            }else {
                // 先检查是否已有创建分支文件，若没有则直接查找commitid
                this.service.post(BranchUrl.checkIfBranchDirExists + '?nodeId=' + nodeId, this.compareBranch[nodeId]).then(async (res:any)=>{
                    console.log(res.data)
                    if (res.data[0] !== null && res.data[1] !== null){
                        let taskStatus = 0
                        while (true){
                            if (res.data[0] !== null) {
                                await this.service.get(ReportUrl.getTaskStatus + '?taskId=' + res.data[0]).then((res: any) => {
                                    if (res.data.status === 2 || res.data.status === 3) {
                                        taskStatus |= 1
                                    }
                                })
                            }else {
                                taskStatus |= 1
                            }
                            if (res.data[1] !== null) {
                                await this.service.get(ReportUrl.getTaskStatus + '?taskId=' + res.data[1]).then((res: any) => {
                                    if (res.data.status === 2 || res.data.status === 3) {
                                        taskStatus |= 2
                                    }
                                })
                            }else{
                                taskStatus |= 2
                            }

                            if (taskStatus === 3) break
                            await sleep(2000)
                        }
                    }
                    this.confirmSelectBranch[nodeId] = false
                    this.enableSelectCommitId[nodeId] = true

                })
            }
        },
        getReportDetail(nodeId:number, taskId:number){
            this.showAnalysisResultDialog = true
            this.isLoadReport = true

            this.service.post(ReportUrl.getReports + '?taskId=' + taskId).then((res:any)=>{
                this.reportList.splice(0, this.reportList.length)
                for (let i=0; i<res.data.length; i++){
                    this.reportList.push(res.data[i])
                }
            })
        },
        test(targetName:string){
            console.log(targetName)
        },
        notRealized(){
            ElNotification({
                title: 'Warning',
                message: '开发者太懒了，这个功能还没实现...',
                type: 'warning'
            })
        },
        isAbleCall(nodeId:number){
            if (
                this.compareBranch[nodeId].base !== '' && this.compareBranch[nodeId].baseCommitId !== ''
                && this.compareBranch[nodeId].compare !== '' && this.compareBranch[nodeId].compareCommitId !== ''
            ){
                return false
            }else {
                return true
            }
        }
    },
    created() {
        console.log('version 2')
        this.getTree();
    }
})
</script>

<template>
    <el-row class="row">
        <el-col :span="9" style="border-style: none solid none none; border-color: #9d9d9d; border-width: 2px;">
            <div class="treeHead">
                <div class="input">
                    <el-input
                        v-model="searchInput"
                        placeholder="输入关键字搜索"
                    >
                        <template #prepend>
                            <el-icon><Search /></el-icon>
                        </template>
                    </el-input>
                </div>
                <div class="button" @click="reflashTree"><el-icon><Refresh /></el-icon></div>
                <div class="button">
                    <el-dropdown trigger="click">
                        <el-icon><CirclePlusFilled /></el-icon>
                        <template #dropdown>
                            <el-dropdown-item @click="addDirDialog = true">新建目录</el-dropdown-item>
                            <el-dropdown-item @click="addGitDialog = true">添加git项目</el-dropdown-item>
                        </template>
                    </el-dropdown>
                </div>
            </div>
            <div class="tree">
                <FileTree
                    :fileTree="treeData"
                    @node-click="nodeClick"
                    @re-name="reName"
                    @move-floder="moveFloder"
                    @delete-item="deleteItem"
                    @edit-item="editItem"
                    :slot-num="2"
                >
                    <template #dirSelection1>
                        <div @click="addGitDialog = true">添加git项目</div>
                    </template>
                    <template #dirSelection2>
                        <div @click="addDirDialog = true">新增目录</div>
                    </template>
                </FileTree>
            </div>
        </el-col>
        <el-col :span="15">
            <div class="info" v-if="showQuick">
                <div class="quickPanel">
                    <h3>快捷操作</h3>
                    <div style="display: flex; justify-content: center; align-items: center;">
                        <el-button
                            class="addButton"
                            @click="addGitDialog = true"
                        >
                            <el-icon style="margin-right: 5px"><CirclePlus /></el-icon>
                            添加git项目
                        </el-button>
                        <el-button
                            class="addButton"
                            @click="addDirDialog = true"
                        >
                            <el-icon style="margin-right: 5px"><FolderAdd /></el-icon>
                            新建文件夹
                        </el-button>
                    </div>
                </div>
            </div>
            <el-tabs
                type="border-card"
                closable
                v-model="tabOn"
                @tab-remove="removeTabItem"
                style="width: 100%; height: 100%; background-color: white"
                v-loading="isLoading"
            >
                <el-tab-pane v-for="item in tabList" :label="item.name" :name="item.id">
                    <div style="width: 100%; margin-bottom: 30px">
                        <div style="border-bottom: 1px solid #ddd; margin-bottom: 20px; margin-top: 10px; padding: 0 0 20px; position: relative; display: flex">
                            <span style="font-size: 16px; margin-left: 20px; font-weight: bold;">基本信息</span>
                        </div>
                        <el-descriptions
                            direction="horizontal"
                            :column="2"
                        >
                            <el-descriptions-item label="名称">{{ item.name }}</el-descriptions-item>
                            <el-descriptions-item label="状态">{{ statusText(item.status) }}</el-descriptions-item>
                            <el-descriptions-item label="git-url">{{ item.gitUrl }}</el-descriptions-item>
                            <el-descriptions-item label="凭据">{{ gitInfos[item.id].credentialsProvider }}</el-descriptions-item>
                            <el-descriptions-item label="创建时间">{{ gitInfos[item.id].createTime }}</el-descriptions-item>
                            <el-descriptions-item label="更新时间">{{ gitInfos[item.id].updateTime }}</el-descriptions-item>
                            <el-descriptions-item label="上次同步时间">
                                {{ gitInfos[item.id].lastSyncTime }}
                                <el-button style="margin-left: 10px" @click="pull(item)" :loading-icon="Eleme" :loading="pullStatus[item.id]">
                                    <el-icon v-if="!pullStatus[item.id]"><Refresh /></el-icon>
                                    clone/pull
                                </el-button>
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>
                    <div style="width: 100%; margin-bottom: 30px">
                        <div style="border-bottom: 1px solid #ddd; margin-bottom: 20px; margin-top: 10px; padding: 0 0 20px; position: relative; display: flex">
                            <span style="font-size: 16px; margin-left: 20px; font-weight: bold;">差异分析</span>
                        </div>
                        <div style="display: flex; margin-bottom: 20px">
                            <span class="text-span">
                                基准分支
                                <el-tooltip effect="dark" content="基准分支表示你将以此分支作为代码比对的基础" placement="top">
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </span>
                            <el-select
                                v-model="compareBranch[item.id].base"
                                filterable
                                placeholder="选择基准分支"
                                @click="getBranchInfo(item.id)"
                                :loading="selectionsLoading"
                            >
                                <el-option v-for="branch in branchInfos[item.id]" :label="branch" :value="branch"></el-option>
                            </el-select>
                            <div style="width: 66px"></div>
                            <div v-if="!enableSelectCommitId[item.id]">
                                <el-button type="primary" @click="confirmSelectBranchs(item.id)" :loading="confirmSelectBranch[item.id]">确认选择分支</el-button>
                                <el-tooltip effect="dark" content="注意：确认选择后将会copy两份代码并分别切换到基准分支和对比分支" placement="top">
                                    <el-icon><QuestionFilled color="#818186" /></el-icon>
                                </el-tooltip>
                            </div>
                            <span class="text-span" v-if="enableSelectCommitId[item.id]">
                                选择CommitId
                                <el-tooltip content="精确选择具体的commit tag，建议查看git log定位" placement="top">
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </span>
                            <el-select
                                v-model="compareBranch[item.id].baseCommitId"
                                filterable
                                placeholder="选择commitId"
                                @click="getCommitIds(item.id, compareBranch[item.id].base)"
                                v-if="enableSelectCommitId[item.id]"
                                :loading="selectionsLoading"
                            >
                                <el-option v-for="commit in commitIds[item.id][compareBranch[item.id].base]" :label="commit.commitId" :value="commit.commitId" :title="commit.message"></el-option>
                            </el-select>
                        </div>
                        <div style="display: flex; margin-bottom: 20px">
                            <span class="text-span">
                                对比分支
                                <el-tooltip effect="dark" content="对比分支表示你将以此分支作为代码变更后的新版本，最终分析结果表示的是对比分支相对于基准分支存在哪些改动" placement="top">
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </span>
                            <el-select
                                v-model="compareBranch[item.id].compare"
                                filterable
                                placeholder="选择对比分支"
                                @click="getBranchInfo(item.id)"
                                :loading="selectionsLoading"
                            >
                                <el-option v-for="branch in branchInfos[item.id]" :label="branch" :value="branch"></el-option>
                            </el-select>
                            <div style="width: 66px"></div>
                            <span class="text-span" v-if="enableSelectCommitId[item.id]">
                                选择CommitId
                                <el-tooltip effect="dark" content="精确选择具体的commit tag，建议查看git log定位" placement="top">
                                    <el-icon><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </span>
                            <el-select
                                v-model="compareBranch[item.id].compareCommitId"
                                filterable
                                placeholder="选择commitId"
                                @click="getCommitIds(item.id, compareBranch[item.id].compare)"
                                v-if="enableSelectCommitId[item.id]"
                                :loading="selectionsLoading"
                            >
                                <el-option v-for="commit in commitIds[item.id][compareBranch[item.id].compare]" :label="commit.commitId" :value="commit.commitId" :title="commit.message"></el-option>
                            </el-select>
                        </div>
<!--                        <div style="display: flex; margin-bottom: 20px">-->
<!--                            -->
<!--                        </div>-->
                        <div style="display: flex; margin-bottom: 20px">
                            <el-button @click="callAnalysis(item.id)" type="primary" :loading="isAnalysis[item.id]" :disabled="isAbleCall(item.id)">
                                执行分析
                            </el-button>
                            <el-button @click="showAnalysisReport(item.id)">
                                <el-icon><DataAnalysis /></el-icon>
                                查看分析结果
                            </el-button>
                        </div>
                        <el-drawer v-model="showDrawer" title="分析报告" direction="rtl" size="50%">
                            <el-table :data="taskList[item.id]">
                                <el-table-column property="createTime" label="时间" width="200" />
                                <el-table-column property="id" label="Id" width="80" />
                                <el-table-column property="detailInfo" label="任务详情" width="800" />
                                <el-table-column fixed="right" label="操作" width="120">
                                    <template #default="scope">
                                        <el-button type="success" @click="getReportDetail(item.id, scope.row.id)">
                                            查看详情
                                        </el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-drawer>
                    </div>
                    <div style="width: 100%; margin-bottom: 30px">
                        <div style="border-bottom: 1px solid #ddd; margin-bottom: 20px; margin-top: 10px; padding: 0 0 20px; position: relative; display: flex">
                            <span style="font-size: 16px; margin-left: 20px; font-weight: bold;">链路追踪</span>
                        </div>
                        <div style="margin-top: 30px">开发者太懒了，这个部分还没写</div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-col>
    </el-row>
    <el-dialog
        v-model="addGitDialog"
        style="margin-top: 15vh;width: 600px;"
    >
        <template #header>添加git项目</template>
        <el-form
            ref="ruleFormRef"
            :model="gitForm"
            style="display: flex;
            flex-wrap: wrap;"
            label-position="right"
            :rules="formRules"
        >
            <el-form-item label="项目名称" class="formItem" label-width="80" prop="name">
                <el-input v-model="gitForm.name"></el-input>
            </el-form-item>
            <el-form-item label="Git路径" class="formItem" label-width="80" prop="url">
                <el-input v-model="gitForm.url"></el-input>
            </el-form-item>
            <el-form-item label="选择凭据" class="formItem" label-width="80" prop="credentialsProviderId">
                <el-select v-model="gitForm.credentialsProviderId" style="width: 500px;" @click="searchItem">
                    <el-option v-for="item in tmpCredentialsProviderIds['data']" :label="item['name']" :value="item['id']" />
                </el-select>
            </el-form-item>
            <el-form-item label="选择路径" class="formItem" label-width="80" prop="parentId">
                <el-select v-model="gitForm.parentId" style="width: 500px">
                    <el-option :value="gitForm.parentId" :key="gitForm.parentId" :label="parentNodeName" style="height: auto;">
                        <FileTree :key="fileKey" :file-tree="treeData" :item-more="false" @node-click="selectNode" :show-leaf="false"></FileTree>
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialogFooter">
                <el-button @click="addGitDialog=false">取消</el-button>
                <el-button type="primary" @click="addGitProject(ruleFormRef)">确定</el-button>
            </span>
        </template>
    </el-dialog>
    <el-dialog
        v-model="addDirDialog"
        style="margin-top: 15vh;width: 600px;"
    >
        <template #header>新建文件夹</template>
        <el-form
            ref="ruleFormRef"
            :model="dirForm"
            style="display: flex;
            flex-wrap: wrap;"
            label-position="right"
            :rules="formRules"
        >
            <el-form-item label="名称" class="formItem" label-width="80" prop="name">
                <el-input v-model="dirForm.name"></el-input>
            </el-form-item>
            <el-form-item label="选择路径" class="formItem" label-width="80" prop="parentId">
                <el-select v-model="dirForm.parentId" style="width: 500px">
                    <el-option :value="dirForm.parentId" :key="dirForm.parentId" :label="parentNodeName" style="height: auto;">
                        <FileTree :key="fileKey" :file-tree="treeData" :item-more="false" @node-click="selectNode" :show-leaf="false"></FileTree>
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialogFooter">
                <el-button @click="addDirDialog=false">取消</el-button>
                <el-button type="primary" @click="newFloder(ruleFormRef)">确定</el-button>
            </span>
        </template>
    </el-dialog>
    <el-dialog v-model="showAnalysisResultDialog" style="margin-top: 15vh;width: 1200px; max-height: 900px" title="涉及代码改动的API">
        <el-table :loading="isLoadReport" :data="reportList" stripe style="width: 1200px;" max-height="800">
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="apiName" label="API" />
        </el-table>
    </el-dialog>
</template>

<style>
.row{
    height: 100%;
}
.tree{
    margin-top: 10px;
    height: 100%;
    width: 100%;
    max-height: 1000px;
    border-style: solid none none none;
    border-color: #9d9d9d;
    border-width: 2px;
}
.treeHead {
    display: flex;
    justify-content: space-between;
    height: 32px;
}
.input{
    display: flex;
    line-height: 32px;
    width: 90%;
}
.button{
    display: flex;
    cursor: pointer;
    margin: auto;
}
.info{
    background-color: rgba(0,0,0,.1);
    height: 100%;
    width: 100%;
    z-index: -99;
}
.quickPanel {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    flex-direction: column;
}
.addButton{
    width: 180px;
    height: 64px;
}
.formItem{
    width: 100%;
    margin-right: 0;
    display: flex;
}
.dialogFooter{
    padding: 10px 20px 20px;
    text-align: right;
    box-sizing: border-box;
}
.el-tabs__content{
    height: 100%;
}
.el-tabs__nav-scroll{
    background-color: #f2f2f2;
}
.el-tab-pane{
    height: 100%;
}
.text-span{
    margin-top: auto;
    margin-right: 15px;
    margin-bottom: auto;
}
</style>
