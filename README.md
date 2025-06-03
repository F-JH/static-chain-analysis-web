# static-chain-analysis-web

1.创建数据表，参考Analysis.sql

2.修改application配置，参考application-dev.yml

3.打包，运行static-chain-analysis-admin-1.0-SNAPSHOT.jar

4.网页默认运行在8089端口: http://localhost:8089

注意：

* 所有分析的代码会clone并保存在 tmp/ 目录下
* 关于凭证：如果项目的git url是http格式，则会使用 [username-password] 凭证拉取，如果是ssh链接（比如 git@github.com:xxxxxx）则必须使用公钥+私钥拉取代码，注意是公钥和私钥都要填
