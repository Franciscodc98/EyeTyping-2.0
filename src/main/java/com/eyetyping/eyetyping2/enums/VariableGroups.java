package com.eyetyping.eyetyping2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VariableGroups {

    BIG_GROUPS("Big Groups"), MEDIUM_GROUPS("Medium Groups"), VOYALS_GROUPS("Voyals groups");

    private final String variableGroupName;
}