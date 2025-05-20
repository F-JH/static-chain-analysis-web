package com.analysis.admin.Code;

public enum TaskType {
    CLONE("clone", "clone 项目"),
    PULL("pull", "pull"),
    DIFF("diff", "执行分析"),
    COPY("copy", "copy文件夹"),
    COMPILE("compile", "编译项目")
    ;

    public final String code;
    public final String detail;

    TaskType(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }
}
