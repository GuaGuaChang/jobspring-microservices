package com.jobspring.application.dto;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
public class UpdateStatusBody {
    @NotNull
    private Integer status;
}
