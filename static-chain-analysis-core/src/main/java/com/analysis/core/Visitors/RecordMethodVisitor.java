package com.analysis.core.Visitors;

import com.analysis.core.Enums.JdkVersionEnum;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class RecordMethodVisitor extends AdviceAdapter {
    public RecordMethodVisitor(JdkVersionEnum jdkVersion, MethodVisitor mv, int access, String methodName, String desc){
        super(jdkVersion.getCode(), mv, access, methodName, desc);
    }

    @Override
    public void visitMaxs(int a, int b){
        // 这里不关心Type的报错，直接忽略
        try{
            super.visitMaxs(a, b);
        }catch (TypeNotPresentException ignored){
        }
    }
}
