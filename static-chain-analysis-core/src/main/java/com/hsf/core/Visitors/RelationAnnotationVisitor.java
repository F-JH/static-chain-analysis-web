package com.hsf.core.Visitors;

import com.hsf.core.Enums.JdkVersionEnum;
import org.objectweb.asm.AnnotationVisitor;

import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM7;

public class RelationAnnotationVisitor extends AnnotationVisitor {
    // 记录自己的requestMappingValue
    private final Set<String> paths;
    // 上一级的requestMappingValue(一般是类的RequestMapping)
    private final Set<String> parentPaths;
    // 判断是否有value
    private boolean hasValue = false;
    private JdkVersionEnum jdkVersionEnum;

    public RelationAnnotationVisitor(JdkVersionEnum jdkVersionEnum, AnnotationVisitor av, Set<String> paths, Set<String> parentPaths){
        super(jdkVersionEnum.getCode(), av);
        this.paths = paths;
        this.parentPaths = parentPaths;
        this.jdkVersionEnum = jdkVersionEnum;
    }

    @Override
    public AnnotationVisitor visitArray(String name){
        // 主要提供给 annotationVisitor0 访问
        if(name.equals("value")){
            hasValue = true;
            return new RelationAnnotationVisitor(jdkVersionEnum,super.visitArray(name), paths, parentPaths);
        }
        return super.visitArray(name);
    }

    @Override
    public void visit(String name, Object value){
        // 主要提供给 annotationVisitor1 访问，annotationVisitor1 用于访问数组类型的注解参数
        if(parentPaths.size() > 0){
            for(String parentPath:parentPaths)
                paths.add(parentPath + (String) value);
        }else{
            paths.add((String) value);
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnd(){
        if(!hasValue && paths.size() == 0){
            paths.addAll(parentPaths);
        }
        super.visitEnd();
    }
}
