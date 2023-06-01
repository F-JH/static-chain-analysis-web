# static-chain-analysis-web

1.创建数据表，参考Analysis.sql

2.修改application配置，参考application-dev.yml

3.打包，运行static-chain-analysis-admin-1.0-SNAPSHOT.jar

4.网页默认运行在8089端口: http://localhost:8089

注意：

* 所有分析的代码会clone并保存在 tmp/ 目录下
* 由于不太会用jgit，目前只会通过 username-password 的方式管理git项目，虽然页面上有可以配置公钥，但没用
