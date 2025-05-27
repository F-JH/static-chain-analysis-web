package com.analysis.core.Entitys.DTO.Handler;

import com.analysis.core.Enums.HandleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HandleDTO<T> {
    private HandleTypeEnum handleType;
    private T handleData;
}
