package com.analysis.corev2.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.objectweb.asm.Opcodes.*;

@Getter
@AllArgsConstructor
public enum JdkVersionEnum {
    JDK1_5(0, ASM4, "1.5", "jdk1.5"),
    JDK1_6(1, ASM4, "1.6", "jdk1.6"),
    JDK1_7(2, ASM5, "1.7", "jdk1.7"),
    JDK1_8(3, ASM5, "1.8", "jdk1.8"),
    JDK9(4, ASM7, "9", "jdk9"),
    JDK11(6, ASM8, "11", "jdk11"),
    JDK15(7, ASM9, "15", "jdk15"),
    JDK17(8, ASM9, "17", "jdk17"),
    ;

    private final int num;
    private final int code;
    private final String version;
    private final String desc;
}
