package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.services.SuggestionsService;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.*;

public class KeyboardController implements Initializable {

    private final SuggestionsService suggestionsService = SuggestionsService.getInstance();

    public static final int TOTAL_GROUPS = 6;
    private List<WordButton> suggestedWords = new ArrayList<>();
    private List<WordButton> suggestedLetters = new ArrayList<>();
    private List<GroupButton> groupsButtonList = new ArrayList<>();
    private final HashMap<String, SecundaryButton> alphabetButtons = new HashMap<>();
    private final List<SecundaryButton> recentSecondaryRowButtons = new ArrayList<>();
    private List<SuggestedLetterButton> thirdRowButtons = new ArrayList<>();
    private List<SuggestedLetterButton> forthRowButtons = new ArrayList<>();

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private String focussedGroup = "";
    private Button focussedButtonForReverse;
    private ReverseCrossingButtons openReverse;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;
    private String currentWordAux = "";




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addRootAnchorListeners();
        groupsButtonList = ButtonsUtils.createGroupButtons(TOTAL_GROUPS);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), button, this::secundaryButtonsEnterEvent))));
        setupTextArea();
        setupGroupButtons();
        setupSuggestedWordButtons();
        setupSuggestLettersButtons();
        setupThirdRowButtons();
        setupForthRowButtons();
    }

    private void setupGroupButtons() {
        int widthAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(widthAux);
            button.setLayoutY(screenHeight-buttonHeight);
            button.setOnMouseEntered(this::groupsButtonEnterEvent);
            button.setOnMouseClicked((event -> wordsToWrite.displayPhrase()));
            widthAux+=buttonWidth;
            rootAnchor.getChildren().add(button);
        }
    }

    private void setupSuggestedWordButtons(){
        suggestedWords = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS);
        int xCoordinateAux = 0;
        for (WordButton button : suggestedWords) {
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY());
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedWords);

    }

    private void setupSuggestLettersButtons(){
        suggestedLetters = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS);
        int xCoordinateAux = 0;
        for (WordButton button : suggestedLetters) {
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY() + buttonHeight + (ReverseCrossingButtons.getMargin()*2) + (button.getPrefHeight()/2));
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedLetters);

    }

    private void setupThirdRowButtons(){
        thirdRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, "thirdRow");
        for (SuggestedLetterButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::thirdRowButtonsEnterEvent);
        }
    }

    private void setupForthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, "forthRow");
        for (SuggestedLetterButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::fourthRowButtonsEnterEvent);
        }
    }

    private void setupTextArea(){
        double separatorY = 85;
        separator = new Line(0,separatorY, windowDimensions.getWidth(), separatorY);
        separator.setStrokeWidth(3);
        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        resizeLabels();
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        clearThirdRowButtons();
        clearForthRowButtons();
        rootAnchor.getChildren().remove(openReverse);
        if(!focussedGroup.equals(groupString)){
            if(!recentSecondaryRowButtons.isEmpty())
                clearSecundaryButtons();
            for (char c: groupString.toCharArray()) {
                recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
            }
            int widthAux = 0;
            for (Button button: recentSecondaryRowButtons) {
                button.setPrefSize(buttonWidth, buttonHeight);
                button.setLayoutX(widthAux);
                button.setLayoutY(screenHeight- (2*buttonHeight));
                rootAnchor.getChildren().add(button);
                widthAux+=buttonWidth;
            }
            focussedGroup = groupString;
        }
    }

    private void secundaryButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton secundaryButton = (SecundaryButton)(mouseEvent.getSource());
        checkReverseCrossing(secundaryButton);
        clearThirdRowButtons();
        clearForthRowButtons();
        if(focussedButtonForReverse != secundaryButton){
            focussedButtonForReverse = secundaryButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(secundaryButton.getText(), secundaryButton);
            rootAnchor.getChildren().add(openReverse);
            List<String> suggestedLetters = suggestionsService.getSuggestionList(secundaryButton.getText());
            if(suggestedLetters.size()>0){
                for (int i = 0; i < thirdRowButtons.size(); i++) {
                    SuggestedLetterButton button = thirdRowButtons.get(i);
                    button.setParentButton(secundaryButton);
                    String word = suggestedLetters.get(i);
                    button.setText(word.substring(0,Math.min(2, word.length())).toUpperCase());

                }
                resizeButtons();
                rootAnchor.getChildren().addAll(thirdRowButtons);
            }
        }
    }

    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SuggestedLetterButton thirdRowButton = (SuggestedLetterButton)(mouseEvent.getSource());
        checkReverseCrossing(thirdRowButton);
        clearForthRowButtons();
        if(focussedButtonForReverse != thirdRowButton){
            focussedButtonForReverse = thirdRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(thirdRowButton.getText(), thirdRowButton);
            rootAnchor.getChildren().add(openReverse);
            List<String> suggestedLetters = suggestionsService.getSuggestionList(thirdRowButton.getText());
            for (int i = 0; i < forthRowButtons.size(); i++) {
                SuggestedLetterButton button = forthRowButtons.get(i);
                button.setParentButton(thirdRowButton);
                String word = suggestedLetters.get(i);
                button.setText(word.substring(0,Math.min(3, word.length())).toUpperCase());
            }
            resizeButtons();
            rootAnchor.getChildren().addAll(forthRowButtons);
        }
    }

    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SuggestedLetterButton fourthRowButton = (SuggestedLetterButton)(mouseEvent.getSource());
        checkReverseCrossing(fourthRowButton);
        if(focussedButtonForReverse != fourthRowButton){
            focussedButtonForReverse = fourthRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(fourthRowButton.getText(), fourthRowButton);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        WordButton wordButton = (WordButton)(mouseEvent.getSource());
        checkReverseCrossing(wordButton);
        if(focussedButtonForReverse != wordButton) {
            focussedButtonForReverse = wordButton;
            if (openReverse != null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(wordButton.getText(), wordButton);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void clearSecundaryButtons(){
        rootAnchor.getChildren().removeAll(recentSecondaryRowButtons);
        recentSecondaryRowButtons.clear();
    }

    private void clearThirdRowButtons(){
        thirdRowButtons.forEach((button -> rootAnchor.getChildren().remove(button)));
    }

    private void clearForthRowButtons(){
        forthRowButtons.forEach((button -> rootAnchor.getChildren().remove(button)));
    }

    private void resizeButtons(){
        int xCoordinateAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(screenHeight-buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (WordButton button : suggestedWords) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SecundaryButton button : recentSecondaryRowButtons) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(screenHeight - (2*buttonHeight));
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (WordButton button : suggestedLetters) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY() + buttonHeight + (ReverseCrossingButtons.getMargin()*2) + (button.getPrefHeight()/2));
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SuggestedLetterButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SuggestedLetterButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }


    }

    private void resizeLabels(){
        double textAreaWidth = windowDimensions.getWidth()*0.8; //90% screen width
        wordsToWrite.setPrefSize(textAreaWidth, 10);
        wordsToWrite.setLayoutX(windowDimensions.getWidth()*0.1); //10% margin right and left
        wordsToWrite.setLayoutY(10); //10px margin top
        wordsWritten.setPrefSize(textAreaWidth, 10);
        wordsWritten.setLayoutX(windowDimensions.getWidth()*0.1); //10% margin right and left
        wordsWritten.setLayoutY(50);
        separator.setEndX(windowDimensions.getWidth());
    }

    private void checkReverseCrossing(Button button){
        if(openReverse!= null){
            if(openReverse.isReverseCrossing() && button.equals(openReverse.getParentButton())){
                List<String> wordsWrittenList = wordsWritten.getWords();
                String fullText = "";
                if(!button.getText().equals(" ")){
                    if(button instanceof WordButton){
                        currentWordAux=button.getText() + " ";
                        for (String word: wordsWrittenList)
                            fullText+= word + " ";
                        fullText+=currentWordAux;
                        wordsWritten.setText(fullText);
                        wordsWrittenList.add(currentWordAux);
                    }else{
                        currentWordAux+=button.getText();
                        for (String word: wordsWrittenList)
                            fullText+= word + " ";
                        fullText+=currentWordAux;
                        wordsWritten.setText(fullText);
                        int aux = 0;
                        List<String> suggestedWordsList = suggestionsService.getSuggestionList(currentWordAux);
                        if(suggestedWordsList.size()>0){
                            for (WordButton wordButton :suggestedWords) {
                                if(aux < suggestedWordsList.size()){
                                    wordButton.setText(suggestedWordsList.get(aux).toUpperCase());
                                    aux++;
                                }
                            }
                        }
                    }
                }else{
                    wordsWrittenList.add(currentWordAux);
                    for (String word: wordsWrittenList)
                        fullText+= word + " ";
                    wordsWritten.setText(fullText);
                    currentWordAux="";
                }
            }
        }

    }

    private void addRootAnchorListeners(){
        rootAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            screenHeight = newValue.doubleValue();
            buttonHeight = screenHeight/10;
            windowDimensions.setHeight(newValue.doubleValue());
            resizeLabels();
            resizeButtons();
        });
        rootAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            screenWidth = newValue.doubleValue();
            buttonWidth = screenWidth/TOTAL_GROUPS;
            windowDimensions.setWidth(newValue.doubleValue());
            resizeLabels();
            resizeButtons();
        });
    }
}