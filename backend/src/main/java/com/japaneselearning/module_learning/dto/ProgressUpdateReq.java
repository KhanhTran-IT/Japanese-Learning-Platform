package com.japaneselearning.module_learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateReq {
    
    @Min(value = 0, message = "watchedPercent cannot be less than 0")
    @Max(value = 100, message = "watchedPercent cannot be greater than 100")
    private Double watchedPercent;
    
    private Boolean isCompleted;
}
