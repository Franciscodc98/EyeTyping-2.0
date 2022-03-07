package com.eyetyping.eyetyping2.utils;

import com.eyetyping.eyetyping2.customComponets.GroupButton;
import com.eyetyping.eyetyping2.customComponets.SecundaryButton;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class ButtonsUtils {

    private ButtonsUtils(){}


    public static List<GroupButton> createGroupButtons(int numberOfGroups){
        List<GroupButton> buttonList = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            String buttonText = StringUtils.getSubstringFromLetterString(numberOfGroups, i);
            GroupButton button = new GroupButton(buttonText);
            buttonList.add(button);
        }
        return buttonList;
    }


    public static HashMap<String, SecundaryButton> createAlphabetButtons(String subButtons, Button groupParent, Consumer<MouseEvent> enterMouse, Consumer<MouseEvent> exitMouse) {
        HashMap<String, SecundaryButton> alphabet = new HashMap<>();
        for (char c: subButtons.toCharArray()) {
            SecundaryButton button = new SecundaryButton(Character.toString(c), groupParent);
            button.setOnMouseEntered(enterMouse::accept); //adicionar a funcao listener
            button.setOnMouseExited(exitMouse::accept); //adicionar a funcao listener
            alphabet.put(button.getText(), button);
        }
        return alphabet;
    }
}