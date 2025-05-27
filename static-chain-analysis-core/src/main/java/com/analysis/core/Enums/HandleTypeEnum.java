package com.analysis.core.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HandleTypeEnum {
    ANNOTATION_HANDLE,
    CLASS_HANDLE,
    METHOD_HANDLE,
    METHOD_ANNOTATION_HANDLE,
    ANNOTATION_VISIT_ARRAY_HANDLE,
    ANNOTATION_VISIT_HANDLE,
    ANNOTATION_VISIT_END_HANDLE,
    ;
}
