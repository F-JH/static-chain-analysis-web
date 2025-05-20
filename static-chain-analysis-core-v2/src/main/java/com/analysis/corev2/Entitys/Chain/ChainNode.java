package com.analysis.corev2.Entitys.Chain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

@Data
@Builder
public class ChainNode {
    private Map<String , ChainNode> nexts;
    private Map<String , ChainNode> prevs;

    private String currentClassName;
    private String currentMethodName;

    @Override
    public String toString(){
        return currentClassName + METHOD_SPLIT + currentMethodName;
    }
}
