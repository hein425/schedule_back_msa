package com.example.diaryService.dto.response;

import com.example.diaryService.constant.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class DiaryResponseCategoryDto {

    private Long idx;

    private String title;

    private LocalDate date;

    private Category category;

}