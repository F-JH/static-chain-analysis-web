create database if not exists Analysis;
use `Analysis`;

create table if not exists credential_info(
    id int(8) unsigned not null primary key auto_increment,
    name varchar(30) not null,
    username varchar(20) not null,
    password varchar(20),
    public_key varchar(4096),
    private_key varchar(4096),
    passphrase varchar(50),
    is_delete boolean default false
) AUTO_INCREMENT = 0 comment='凭据信息';

--左右值编码存储树形结构
create table if not exists file_tree_info (
    node_id int(11) not null primary key auto_increment,
    parent_id int(11) unsigned not null default 0,
    name varchar(255) comment '名字',
    lft int(11) not null default 0 comment '左值',
    rgt int(11) not null default 0 comment '右值',
    is_directory boolean not null default true,
    git_url varchar(255),
    credential_id int(8) comment '选择的凭据',
    status int(1) not null default 0 comment '0 未下载 1 有问题 2 正常',
    key `idx_left_right` (`lft`, `rgt`)
) comment '文件目录树' default CHARSET=utf8;

insert into file_tree_info (parent_id, name, lft, rgt, is_directory) values (0, '根目录', 1, 2, true);

create table if not exists project_git_info (
    id int(8) unsigned not null primary key auto_increment,
    node_id int(8) comment '对应的node id',
    path varchar(256) comment '本地存储路径',
    `create_time` datetime(3) not null default current_timestamp(3),
    `update_time` datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
    `last_sync_time` datetime(3)
) comment 'git项目信息';

create table if not exists task_info (
    id int(12) unsigned not null primary key auto_increment,
    type varchar(20) comment '任务类型：clone/pull/diff/copy',
    node_id int(8) comment '关联的 node id',
    detail_info varchar(200) comment '任务详情，json格式',
    `create_time` datetime(3) not null default current_timestamp(3),
    `update_time` datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
    status int(1) not null default 0 comment '0 初始化， 1 运行中， 2 执行成功， 3 执行失败， 4 重试中'
) comment '任务详情表';

create table if not exists branch_dir_info (
    project_id int(8) unsigned not null,
    branch_name varchar(100) comment '分支名',
    path varchar(256) comment '具体路径',
    `create_time` datetime(3) not null default current_timestamp(3),
    `update_time` datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
    `last_sync_time` datetime(3),
    running_task_id int(12) comment '正在跑的copy任务id，如果没有则为null',
    index id_name (project_id, branch_name)
) comment '分支路径表';

create table if not exists analysis_simple_report (
    task_id int(12) not null,
    type varchar(6) not null comment 'http 或 dubbo',
    api_name varchar(200) not null comment '需要测试的api，包括http和dubbo两种',
    `create_time` datetime(3) not null default current_timestamp(3),
    `update_time` datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
    index task_id_key (task_id)
)comment '简单版的结果报告，只输出涉及到的api';




-- 下面是分析详情表，本期先不做
create table if not exists analysis_method_relationship (
    task_id int(12) unsigned not null,
    class_name varchar(200) not null comment 'class name',
    method_name varchar(100) not null comment '函数名',
    method_call_class_name varchar(200) comment '调用函数的类名',
    method_call_method_name varchar(200) comment '调用函数名',
    index task_id_key (task_id)
)comment '分析结果-调用关系';

create table if not exists analysis_interface_record (
    task_id int(12) unsigned not null,
    interface_class_name varchar(200) not null,
    implements_entry_class_name varchar(200),
    index task_id_key (task_id)
)comment 'interface与实体类的映射关系';

create table if not exists analysis_interface_method_record (
    task_id int(12) unsigned not null,
    interface_class_name varchar(200) not null,
    method_name varchar(100),
    is_abstract boolean not null comment '是否是abstract函数',
    index task_id_key (task_id)
)comment 'interface的方法集合';

create table if not exists analysis_api_record (
    task_id int(12) unsigned not null,
    type varchar(6) not null comment 'http 或 dubbo',
    class_name varchar(200) not null,
    method_name varchar(100) not null,
    uri varchar(300) not null comment 'http的uri为url，dubbo直接为方法完整名',
    index task_id_key (task_id)
)comment '对外提供api的方法详情，包括具体的http及dubbo函数，以及http url';

create table if not exists analysis_abstract_method_record (
    task_id int(12) unsigned not null,
    class_name varchar(200) not null,
    method_name varchar(100) not null,
    is_abstract boolean not null
)comment '抽象类记录';
