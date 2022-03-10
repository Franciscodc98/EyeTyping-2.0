package com.eyetyping.eyetyping2.customComponets;

import javafx.scene.control.Button;

public class WordButton extends Button {

    private String word;

    public WordButton(String word){
        super(word);
        this.word = word;
        super.setStyle(getStyleCssFile());
    }

    /**
     * @return string with all css on a file
     */
    private String getStyleCssFile() {
        return "";
    }


}