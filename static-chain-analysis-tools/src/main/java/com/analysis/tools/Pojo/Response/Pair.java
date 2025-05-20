package com.analysis.tools.Pojo.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<A, B> {
    A first;
    B second;
}
