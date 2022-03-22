package com.eyetyping.eyetyping2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupNames {

    GROUP_ROW("groupRow"), FIRST_ROW("secondRow"), SECOND_ROW("secondRow"), THIRD_ROW("thirdRow"), FOURTH_ROW("fourthRow"), WORDS_ROW("wordsRow");

    private final String groupName;
}