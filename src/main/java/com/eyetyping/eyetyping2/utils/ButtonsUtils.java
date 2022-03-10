package com.eyetyping.eyetyping2.utils;

import com.eyetyping.eyetyping2.customComponets.GroupButton;
import com.eyetyping.eyetyping2.customComponets.SecundaryButton;
import com.eyetyping.eyetyping2.customComponets.SuggestedLetterButton;
import com.eyetyping.eyetyping2.customComponets.WordButton;
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
            String buttonText = StringUtils.getSubstringFromLetterString2(numberOfGroups, i);
            GroupButton button = new GroupButton(buttonText);
            buttonList.add(button);
        }
        return buttonList;
    }


    public static HashMap<String, SecundaryButton> createAlphabetButtons(String subButtons, Button groupParent, Consumer<MouseEvent> enterMouse) {
        HashMap<String, SecundaryButton> alphabet = new HashMap<>();
        for (char c: subButtons.toCharArray()) {
            SecundaryButton button = new SecundaryButton(Character.toString(c), groupParent);
            button.setOnMouseEntered(enterMouse::accept); //adicionar a funcao listener
            alphabet.put(button.getText(), button);
        }
        return alphabet;
    }


    public static List<WordButton> createSuggestedWordButtons(int numberOfButtons){
        List<WordButton> buttons = new ArrayList<>();
        for (int i = 0; i < numberOfButtons; i++)
            buttons.add(new WordButton("Word " + (i+1)));
        return buttons;
    }


    public static List<SuggestedLetterButton> createThirdRowButtons(int totalGroups, String groupName) {
        List<SuggestedLetterButton> buttons = new ArrayList<>();
        for (int i = 0; i < totalGroups; i++) {
            buttons.add(new SuggestedLetterButton(groupName));
        }
        return buttons;
    }
}