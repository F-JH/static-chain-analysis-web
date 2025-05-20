package com.analysis.corev2.Entitys.DTO.Handler;

import com.analysis.corev2.Enums.HandleTypeEnum;
import com.analysis.corev2.Handler.BaseHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class HandleDTO<T> {
    private HandleTypeEnum handleType;
    private T handleData;
}
