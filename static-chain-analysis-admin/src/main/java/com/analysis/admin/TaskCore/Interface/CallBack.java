package com.analysis.admin.TaskCore.Interface;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public interface CallBack {
    public void run();
    public Boolean isSuccess();
    public void setResult(Boolean isSuccess);
}
