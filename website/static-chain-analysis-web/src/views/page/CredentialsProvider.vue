<script lang="ts">
import CredentialUrl from '../../url/CredentialUrl'
import type { credentailInfo } from '../../../include/chain'
import { defineComponent, ref, reactive } from 'vue'
import 'element-plus/es/components/notification/style/css'
import 'element-plus/es/components/message-box/style/css'
import type { FormInstance } from "element-plus";
import { ElInput, ElButton, ElDialog, ElTable, ElTableColumn, ElForm, ElFormItem, ElNotification, ElMessageBox } from "element-plus"

const infoNotify = () => ElNotification({
    title: 'info',
    message: '删除成功',
    type: 'info'
})

export default defineComponent({
    name: "CredentialsProvider",
    components: {
        ElFormItem,
        ElForm,
        ElInput,
        ElButton,
        ElDialog,
        ElTable,
        ElTableColumn
    },
    setup(){
        const ruleFormRef = ref<FormInstance>()
        const addCredentialsProviderDialog = ref(false)
        const searchName = ref('')
        const credentialsProviderList = reactive<credentailInfo[]>([])
        const diaLogTitle = ref('')

        const credentialsProviderForm = reactive<credentailInfo>({
            name: '',
            username: '',
            password: '',
            publicKey: '',
            privateKey: '',
            passphrase: ''
        })

        const formRules = reactive({
            name: [
                {required: true, message: '请输入名称', trigger: 'blur'},
                { min: 0, max: 20, message: '0~20个字符', trigger: 'blur' }
            ],
            username: [
                {required: true, message: '请输入用户名', trigger: 'blur'}
            ],
            password: [],
            publicKey: [],
            privateKey: []
        })


        return { diaLogTitle, ruleFormRef, searchName, credentialsProviderList, addCredentialsProviderDialog, credentialsProviderForm, formRules }
    },
    methods: {
        editItem(row:any){
            this.credentialsProviderForm.id = row.id
            this.resetFormData(row.name, row.username, row.password, row.passphrase, row.publicKey, row.privateKey)
            this.diaLogTitle = '编辑凭据'
            this.addCredentialsProviderDialog = true
        },
        deleteItem(row:any){
            ElMessageBox.confirm(
                '是否要删除？',
                'Waring',
                {
                    confirmButtonText: '删除',
                    cancelButtonText: '取消',
                    type: 'warning',
                }
            ).then(() => {
                this.service.delete(CredentialUrl.deleteCredential + '?id=' + row.id).then((res:any) => {
                    infoNotify()
                    this.searchItem()
                })
            }).catch(() => {})
        },
        async putCredentialsProvider(formEl: FormInstance | undefined){
            await formEl?.validate((valid, fields) => {
                if (valid){
                    let url;
                    if (this.diaLogTitle === '编辑凭据'){
                        url = CredentialUrl.editCredential
                    }else {
                        url = CredentialUrl.addCredential
                    }
                    this.service.post(url, this.credentialsProviderForm).then((res:any) => {
                        this.resetFormData()
                        this.addCredentialsProviderDialog = false
                        this.searchItem()
                    })
                }
            })
        },
        searchItem(){
            this.service.get(CredentialUrl.getCredential + '?key=' + this.searchName).then((res:any) => {
                console.log(res)
                this.credentialsProviderList.splice(0, this.credentialsProviderList.length)
                for (let item of res.data){
                    this.credentialsProviderList.push(item)
                }
            })
        },
        resetFormData(
            name:string = '',
            username:string = '',
            password:string = '',
            passphrase:string = '',
            publicKey:string = '',
            privateKey:string = ''
        ){
            this.credentialsProviderForm.name = name
            this.credentialsProviderForm.username = username
            this.credentialsProviderForm.password = password
            this.credentialsProviderForm.passphrase = passphrase
            this.credentialsProviderForm.publicKey = publicKey
            this.credentialsProviderForm.privateKey = privateKey
        }
    },
    mounted() {
        // 获取数据
        this.searchItem()
    }
})
</script>

<template>
    <div class="search">
        <div class="search-lcd" style="display: inline-flex">
            <div class="search-item">
                <label style="width: auto">搜索: </label>
                <el-input placeholder="输入凭据名称搜索" v-model="searchName" style="width: 280px"></el-input>
            </div>
            <div class="search-option">
                <el-button type="primary" @click="searchItem">查询</el-button>
            </div>
        </div>
    </div>
    <div style="margin-top: 10px; padding: 10px 0; display: inline-flex; width: 100%">
        <el-button type="primary" @click="resetFormData();diaLogTitle = '添加凭据';addCredentialsProviderDialog = true;">添加git凭据</el-button>
    </div>
    <div class="data-area">
        <el-table :data="credentialsProviderList">
<!--            <el-table-column type="expand">-->
<!--                <template #default="props">-->
<!--                    <div style="margin-left: 60px; margin-right: 300px; display: flex; flex-direction: column">-->
<!--                        <div style="width: 100%; display: flex; flex-direction: row; margin-bottom: 10px">-->
<!--                            <span style="margin-right: 10px">公钥:</span>-->
<!--                            <span>{{ props.row.publicKey }}</span>-->
<!--                        </div>-->
<!--                        <div style="width: 100%; display: flex; flex-direction: row; margin-bottom: 10px">-->
<!--                            <span style="margin-right: 10px">私钥:</span>-->
<!--                            <span>{{ props.row.privateKey }}</span>-->
<!--                        </div>-->
<!--                    </div>-->
<!--                </template>-->
<!--            </el-table-column>-->
            <el-table-column label="名称" prop="name" />
            <el-table-column label="用户名" prop="username" />
            <el-table-column label="密码" prop="password" />
            <el-table-column label="公钥" prop="publicKey" />
            <el-table-column label="私钥" prop="privateKey" />
            <el-table-column label="passphrase" prop="passphrase" />
            <el-table-column label="操作">
                <template #default="scope">
                    <el-button type="primary" @click="editItem(scope.row)">编辑</el-button>
                    <el-button @click="deleteItem(scope.row)">删除</el-button>
                </template>
            </el-table-column>>
        </el-table>
    </div>
    <el-dialog
        v-model="addCredentialsProviderDialog"
        style="margin-top: 15vh;width: 700px;"
    >
        <template #header>{{ diaLogTitle }}</template>
        <el-form
            ref="ruleFormRef"
            :model="credentialsProviderForm"
            style="display: flex;
            flex-wrap: wrap;"
            label-position="right"
            :rules="formRules"
        >
            <el-form-item label="名称" class="formItem" label-width="80" prop="name">
                <el-input v-model="credentialsProviderForm.name" style="width: 550px" placeholder="输入凭据别名"></el-input>
            </el-form-item>
            <el-form-item label="用户名" class="formItem" label-width="80" prop="username">
                <el-input v-model="credentialsProviderForm.username" style="width: 550px" placeholder="用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" class="formItem" label-width="80" prop="password">
                <el-input v-model="credentialsProviderForm.password" style="width: 550px" placeholder="输入密码"></el-input>
            </el-form-item>
            <el-form-item label="passphrase" class="formItem" label-width="80" prop="passphrase">
                <el-input v-model="credentialsProviderForm.passphrase" style="width: 550px" placeholder="输入passphrase"></el-input>
            </el-form-item>
            <el-form-item label="公钥" class="formItem" label-width="80" prop="publicKey">
                <el-input v-model="credentialsProviderForm.publicKey" style="width: 550px" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }"></el-input>
            </el-form-item>
            <el-form-item label="私钥" class="formItem" label-width="80" prop="privateKey">
                <el-input v-model="credentialsProviderForm.privateKey" style="width: 550px" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialogFooter">
                <el-button @click="addCredentialsProviderDialog=false">取消</el-button>
                <el-button type="primary" @click="putCredentialsProvider(ruleFormRef)">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<style scoped>
.search{
    border-bottom: 1px solid #ddd;
    width: 100%;
}
.search-lcd{
    align-items: center;
    display: flex;
    flex-wrap: wrap;
    vertical-align: top;
    margin-right: auto;
    width: 100%;
}
.search-item label{
    align-items: center;
    display: inline-flex;
    margin: 10px 15px 10px 10px;
    vertical-align: middle;
}
.search-option{
    align-items: center;
    display: inline-flex;
    flex: 1;
    height: 52px;
    justify-content: flex-start;
    margin: 0 16px;
    white-space: nowrap;
    width: 100%;
}
.data-area{
    border: 1px solid #ddd;
    margin: 10px 0;
    padding: 10px;
}
.formItem{
    width: 100%;
    margin-right: 0;
    display: flex;
}
</style>
