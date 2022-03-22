package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.services.DataService;
import com.eyetyping.eyetyping2.services.SuggestionsService;
import com.eyetyping.eyetyping2.services.WrittingService;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class KeyboardController implements Initializable {

    //Services
    private final SuggestionsService suggestionsService = SuggestionsService.getInstance();
    private final WrittingService writtingService = WrittingService.getInstance();
    private final DataService dataService = DataService.getInstance();

    private int TOTAL_GROUPS;
    private VariableGroups variableGroups = null;
    private final HashMap<String, ActionButton> actionButtonsHashMap = new HashMap<>();
    private List<SecundaryButton> suggestedWordsButtons = new ArrayList<>();
    private List<GroupButton> groupsButtonList = new ArrayList<>();
    private final HashMap<String, SecundaryButton> alphabetButtons = new HashMap<>();
    private final List<SecundaryButton> recentSecondaryRowButtons = new ArrayList<>();
    private List<SecundaryButton> thirdRowButtons = new ArrayList<>();
    private List<SecundaryButton> forthRowButtons = new ArrayList<>();

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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addRootAnchorListeners();
        calculateGroupsSize(VariableGroups.MEDIUM_GROUPS); //Alterar isto para os diferentes tipos de layout
        groupsButtonList = ButtonsUtils.createGroupButtons(variableGroups, TOTAL_GROUPS);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), this::secundaryButtonsEnterEvent))));
        setupTextArea();
        setupGroupButtons();
        setupSuggestedWordButtons();
        setupThirdRowButtons();
        setupForthRowButtons();
    }

    private void calculateGroupsSize(VariableGroups variableGroups){
        this.variableGroups = variableGroups;
        if(variableGroups.getVariableGroupName().equals(VariableGroups.BIG_GROUPS.getVariableGroupName()))
            TOTAL_GROUPS = 4;
        else
            TOTAL_GROUPS = 6;
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
        suggestedWordsButtons = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS, GroupNames.WORDS_ROW);
        int xCoordinateAux = 0;
        for (SecundaryButton button : suggestedWordsButtons) {
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY());
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedWordsButtons);

    }


    private void setupThirdRowButtons(){
        thirdRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.THIRD_ROW);
        for (SecundaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::thirdRowButtonsEnterEvent);
        }
    }

    private void setupForthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.FOURTH_ROW);
        for (SecundaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::fourthRowButtonsEnterEvent);
        }
    }

    private void setupTextArea(){
        double separatorY = 85;
        separator = new Line(0,separatorY, windowDimensions.getWidth(), separatorY);
        separator.setStrokeWidth(3);
        ActionButton deleteButton = new ActionButton("delete");
        actionButtonsHashMap.put(deleteButton.getAction(),deleteButton);
        deleteButton.setOnMouseEntered(this::deleteButtonEnterEvent);
        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        resizeTextAreaContent();
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
        rootAnchor.getChildren().add(deleteButton);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        clearThirdRowButtons();
        clearForthRowButtons();
        focussedButtonForReverse = null;
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
            if(!secundaryButton.getText().equals("SPACE"))
                fillSuggestedWords(secundaryButton, GroupNames.THIRD_ROW.getGroupName());
        }
    }


    private void fillSuggestedWords(SecundaryButton button, String groupName){
        List<String> suggestedLetters = suggestionsService.getSuggestionList(button.getText());
        if(suggestedLetters.size() > 0){
            if(groupName.equals(GroupNames.THIRD_ROW.getGroupName())){
                int i = 0;
                List<String> suggestions = suggestionsService.sortedMostCommonSubstrings(suggestedLetters, 2);
                for(SecundaryButton suggestion : thirdRowButtons){
                    if(suggestions.size() > i){
                        suggestion.setParentButton(button);
                        suggestion.setText(suggestions.get(i));
                        rootAnchor.getChildren().add(suggestion);
                    } else{
                        rootAnchor.getChildren().remove(suggestion);
                    }
                    i++;
                }
            } else if(groupName.equals(GroupNames.FOURTH_ROW.getGroupName())){
                int i = 0;
                List<String> suggestions = suggestionsService.sortedMostCommonSubstrings(suggestedLetters, 3);
                for(SecundaryButton suggestion : forthRowButtons){
                    if(suggestions.size() > i){
                        suggestion.setParentButton(button);
                        suggestion.setText(suggestions.get(i));
                        rootAnchor.getChildren().add(suggestion);
                    } else{
                        rootAnchor.getChildren().remove(suggestion);
                    }
                    i++;
                }
            }
        }
        resizeButtons();
    }


    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton thirdRowButton = (SecundaryButton)(mouseEvent.getSource());
        checkReverseCrossing(thirdRowButton);
        clearForthRowButtons();
        if(focussedButtonForReverse != thirdRowButton){
            focussedButtonForReverse = thirdRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(thirdRowButton.getText(), thirdRowButton);
            rootAnchor.getChildren().add(openReverse);
            fillSuggestedWords(thirdRowButton, GroupNames.FOURTH_ROW.getGroupName());
        }
    }

    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton fourthRowButton = (SecundaryButton)(mouseEvent.getSource());
        checkReverseCrossing(fourthRowButton);
        if(focussedButtonForReverse != fourthRowButton){
            focussedButtonForReverse = fourthRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(fourthRowButton.getText(), fourthRowButton);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void deleteButtonEnterEvent(MouseEvent mouseEvent){
        ActionButton deleteButton = (ActionButton)(mouseEvent.getSource());
        checkReverseCrossing(deleteButton);
        if(focussedButtonForReverse != deleteButton) {
            focussedButtonForReverse = deleteButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons("confirm", deleteButton);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton wordButton = (SecundaryButton)(mouseEvent.getSource());
        checkReverseCrossing(wordButton);
        if(focussedButtonForReverse != wordButton) {
            focussedButtonForReverse = wordButton;
            if (openReverse != null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(wordButton.getText(), wordButton);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void clearAllPopupButtons(){
        clearSecundaryButtons();
        clearThirdRowButtons();
        clearForthRowButtons();
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
        for (SecundaryButton button : suggestedWordsButtons) {
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
        for (SecundaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SecundaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }


    }

    private void resizeTextAreaContent(){
        double textAreaWidth = windowDimensions.getWidth()*0.6; //60% screen width
        wordsToWrite.setPrefSize(textAreaWidth, 10);
        wordsToWrite.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsToWrite.setLayoutY(10); //10px margin top
        wordsWritten.setPrefSize(textAreaWidth, 10);
        wordsWritten.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsWritten.setLayoutY(50);
        separator.setEndX(windowDimensions.getWidth());
        ActionButton button = actionButtonsHashMap.get("delete");
        button.setPrefSize(windowDimensions.getWidth()*0.1, windowDimensions.getHeight()*0.085);
        button.setLayoutX(windowDimensions.getWidth()*0.75);
        button.setLayoutY(windowDimensions.getHeight()*0.005);

    }

    private void checkReverseCrossing(Button button){
        if(openReverse!= null){
            if(openReverse.isReverseCrossing() && button.equals(openReverse.getParentButton())){
                String buttonText = button.getText();
                if(button instanceof SecundaryButton secundaryButton){
                    if(secundaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                        if(!secundaryButton.getText().equals("")){
                            wordsWritten.setText(writtingService.addWord(buttonText).stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                            clearAllPopupButtons();
                        }
                    }else{
                        if(buttonText.equals("SPACE")){
                            wordsWritten.setText(writtingService.addLetters(" ").stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                        }else{
                            wordsWritten.setText(writtingService.addLetters(buttonText).stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                            updateSuggestedWords();
                        }
                    }
                }else if(button instanceof ActionButton){
                    if(buttonText.equals("delete"))
                        wordsWritten.setText(writtingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                }
            }
        }


    }

    private void updateSuggestedWords() {
        List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord());
        int i = 0;
        for (SecundaryButton suggestedWordButton : suggestedWordsButtons) {
            if(suggestedWords.size() > i){
                suggestedWordButton.setText(suggestedWords.get(i));
                i++;
            }else{
                suggestedWordButton.setText("");
            }
        }
    }

    private void addRootAnchorListeners(){
        rootAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            screenHeight = newValue.doubleValue();
            buttonHeight = screenHeight/10;
            windowDimensions.setHeight(newValue.doubleValue());
            resizeTextAreaContent();
            resizeButtons();
        });
        rootAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            screenWidth = newValue.doubleValue();
            buttonWidth = screenWidth/TOTAL_GROUPS;
            windowDimensions.setWidth(newValue.doubleValue());
            resizeTextAreaContent();
            resizeButtons();
        });
    }

    public void setKeyListener(Scene scene) {
        scene.setOnKeyPressed(event -> {
            System.out.println("Clicked");
            System.out.println(event.getCode());
            if(event.getCode() == KeyCode.CONTROL && !dataService.isStarted()){
                dataService.startTimer();
                wordsToWrite.setText(dataService.getDataset().removeFirst());
            }else if(event.getCode() == KeyCode.CONTROL && !dataService.getDataset().isEmpty()){
                wordsToWrite.setText(dataService.getDataset().removeFirst());
            } else if(event.getCode() == KeyCode.CONTROL && dataService.getDataset().isEmpty()){
                wordsToWrite.setText("Experiment is finished, thank you!");
                dataService.stopTimer();
                dataService.saveData(variableGroups,"Francisco Cardoso", 22);
            }
        });
    }

    private void mouseMovementEvent(MouseEvent mouseEvent) {
        System.out.println("X:" + mouseEvent.getX() + ", Y:" + mouseEvent.getY());
    }

    public static void main(String[] args) {
    }
}