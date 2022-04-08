package com.eyetyping.eyetyping2.utils;

import com.eyetyping.eyetyping2.customComponets.GroupButton;
import com.eyetyping.eyetyping2.customComponets.SecundaryButton;
import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class ButtonsUtils {

    private ButtonsUtils(){}


    public static List<GroupButton> createGroupButtons(VariableGroups groups, int numberOfGroups){
        List<GroupButton> buttonList = new ArrayList<>();
        if(groups.getVariableGroupName().equals(VariableGroups.BIG_GROUPS.getVariableGroupName()) ||
                groups.getVariableGroupName().equals(VariableGroups.MEDIUM_GROUPS.getVariableGroupName())){
            for (int i = 0; i < numberOfGroups; i++) {
                String buttonText = StringUtils.getSubstringFromLetterString2(numberOfGroups, i);
                GroupButton button = new GroupButton(buttonText);
                buttonList.add(button);
            }
        } else{
            //create variable groups
        }
        return buttonList;

    }


    public static HashMap<String, SecundaryButton> createAlphabetButtons(String subButtons,Consumer<MouseEvent> enterMouse, Consumer<MouseEvent> moveMouse, Consumer<MouseEvent> exitMouse) {
        HashMap<String, SecundaryButton> alphabet = new HashMap<>();
        for (char c: subButtons.toCharArray()) {
            SecundaryButton button = new SecundaryButton( c!= ' ' ? Character.toString(c) : "SPACE");
            button.setGroupName(GroupNames.SECOND_ROW.getGroupName());
            button.setOnMouseEntered(enterMouse::accept); //adicionar a funcao listener
            button.setOnMouseMoved(moveMouse::accept);
            button.setOnMouseExited(exitMouse::accept);
            alphabet.put(Character.toString(c), button);
        }
        return alphabet;
    }


    public static List<SecundaryButton> createSuggestedWordButtons(int numberOfButtons){
        List<SecundaryButton> buttons = new ArrayList<>();
        for (int i = 0; i < numberOfButtons; i++){
            SecundaryButton button = new SecundaryButton("Word " + (i+1));
            button.setGroupName(GroupNames.WORDS_ROW.getGroupName());
            buttons.add(button);
        }
        return buttons;
    }


    public static List<SecundaryButton> createThirdRowButtons(int totalGroups, GroupNames groupName) {
        List<SecundaryButton> buttons = new ArrayList<>();
        for (int i = 0; i < totalGroups; i++) {
            SecundaryButton button = new SecundaryButton();
            button.setGroupName(groupName.getGroupName());
            buttons.add(button);
        }
        return buttons;
    }
}