interface fileNode{
    id: number,
    name: string,
    isDirectory: boolean, // false: git, true: directory
    status: number, // 0 未下载 | 1 下载中 | 2 正常 | 3 失败
    gitUrl: string,
    credentialId: number,
    children: fileNode[]
}

interface gitInfo{
    id: number,
    name: string,
    url: string,
    credentialsProvider: string
    createDate: string,
    updateDate: string
}

interface credentailInfo{
    id?: number,
    name: string,
    username: string,
    password: string,
    publicKey: string,
    privateKey: string,
    passphrase: string
}

export type {
    fileNode,
    gitInfo,
    credentailInfo
}
