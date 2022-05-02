package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.eyetracker.Connections;
import com.eyetyping.eyetyping2.services.DataService;
import com.eyetyping.eyetyping2.services.MouseService;
import com.eyetyping.eyetyping2.services.SuggestionsService;
import com.eyetyping.eyetyping2.services.WrittingService;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import lombok.Data;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class KeyboardController implements Initializable {

    public static final String SPACE = "SPACE";

    //User information
    //User data
    private final String USERNAME = "Francisco Cardoso";
    private final int AGE = 23;
    private final VariableGroups GROUP_VARIABLE = VariableGroups.MEDIUM_GROUPS;
    private final int SESSION_NUMBER = 1;

    //Services
    private final SuggestionsService suggestionsService = SuggestionsService.getInstance();
    private final WrittingService writtingService = WrittingService.getInstance();
    private final DataService dataService = DataService.getInstance();
    private final MouseService mouseService = MouseService.getSingleton();

    private Connections connections = new Connections();
    private static final boolean CONNECT_SERVER = false; //alterar aqui se for para ligar o servidor do eyetracker ou nao


    private static final int TIME = 10;

    private int TOTAL_GROUPS;
    private int TOTAL_SECUNDARY_GROUPS;
    private int TOTAL_BUTTONS_PER_ROW;
    private VariableGroups variableGroups = null;
    private List<SecondaryButton> suggestedWordsButtons = new ArrayList<>();
    private List<GroupButton> groupsButtonList = new ArrayList<>();
    private final HashMap<String, SecondaryButton> alphabetButtons = new HashMap<>();
    private final List<SecondaryButton> recentSecondaryRowButtons = new ArrayList<>();
    private List<SecondaryButton> thirdRowButtons = new ArrayList<>();
    private List<SecondaryButton> forthRowButtons = new ArrayList<>();
    private SecondaryButton spaceButton;

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private Button focussedButtonForReverse;
    private ReverseCrossingButtons openReverse;
    private DeleteButton deleteButton;
    private ArrayList<ReverseCrossingDelete> deleteOptions = new ArrayList<>();

    private Scene mainScene = null;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;

    Timer timer = new Timer();
    private TimerTask timeout = new TimerTask() {
        @Override
        public void run() {
            dataService.timerFinished(TIME);
            Platform.runLater(KeyboardController.this::finished);
        }
    };
    private TimerTask slipMargin;
    private TimerTask updateSuggestedWords;

    //split gaze margin
    private static final long SLIP_MARGIN = 250;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        calculateGroupsSize(GROUP_VARIABLE); //Alterar isto para os diferentes tipos de layout
        setupButtons();
        addRootAnchorListeners();
        mouseService.setKeyboardController(this);
        mouseService.setWindowDimensions(windowDimensions);
    }

    private void calculateGroupsSize(VariableGroups variableGroups){
        this.variableGroups = variableGroups;
        if(variableGroups.getVariableGroupName().equals(VariableGroups.BIG_GROUPS.getVariableGroupName()))
            TOTAL_GROUPS = 4;
        else
            TOTAL_GROUPS = 6;
    }

    private void setupButtons(){
        setupGroupButtons();
        setupTextArea();
        setupSuggestedWordButtons();
        setupThirdRowButtons();
        setupFourthRowButtons();
    }

    private void setupGroupButtons() {
        groupsButtonList = ButtonsUtils.createGroupButtons(variableGroups, TOTAL_GROUPS);
        int widthAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(widthAux);
            button.setLayoutY(screenHeight-buttonHeight);
            button.setOnMouseEntered(this::groupsButtonEnterEvent);
            button.setOnMouseExited(this::groupButtonsExitEvent);
            widthAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(groupsButtonList);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), this::secondaryButtonsEnterEvent, this::secondaryButtonsExitEvent))));
    }

    private void setupSuggestedWordButtons(){
        suggestedWordsButtons = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS);
        int xCoordinateAux = 0;
        for (SecondaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY());
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedWordsButtons);
    }

    private void setupThirdRowButtons(){
        thirdRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.THIRD_ROW);
        for (SecondaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::thirdRowButtonsEnterEvent);
            button.setOnMouseExited(this::secondaryButtonsExitEvent);
        }
    }

    private void setupFourthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.FOURTH_ROW);
        for (SecondaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::fourthRowButtonsEnterEvent);
            button.setOnMouseExited(this::secondaryButtonsExitEvent);
        }
    }

    private void setupTextArea(){
        double separatorY = 85;
        separator = new Line(0,separatorY, windowDimensions.getWidth(), separatorY);
        separator.setStrokeWidth(3);
        deleteButton = new DeleteButton();
        deleteButton.setOnMouseEntered(this::deleteButtonEnterEvent);
        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        spaceButton = new SecondaryButton(SPACE);
        spaceButton.setOnMouseEntered(this::spaceButtonEnterEvent);
        spaceButton.setGroupName(SPACE);
        resizeTextAreaContent();
        rootAnchor.getChildren().add(spaceButton);
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
        rootAnchor.getChildren().add(deleteButton);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    deleteButton.setFocussed(false);
                    groupsButtonList.forEach(groupButton -> {
                        if (!groupButton.equals(focussedButton))
                            groupButton.setFocussed(false);
                    });
                    focussedButton.setFocussed(true);
                    if(!recentSecondaryRowButtons.isEmpty())
                        clearAllPopupButtons();
                    for (char c: groupString.toCharArray()) {
                        recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
                    }
                    int widthAux = 0;
                    for (SecondaryButton button: recentSecondaryRowButtons) {
                        button.setPrefSize(buttonWidth, buttonHeight);
                        button.setLayoutX(widthAux);
                        button.setLayoutY(screenHeight- (2*buttonHeight));
                        widthAux+=buttonWidth;
                    }
                    rootAnchor.getChildren().addAll(recentSecondaryRowButtons);
                    resizeButtons();
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
        clearThirdRowButtons();
        clearForthRowButtons();
        focussedButtonForReverse = null;
        rootAnchor.getChildren().remove(openReverse);
    }

    private void secondaryButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        updateSuggestionTimer(secondaryButton);
        checkReverseCrossing(secondaryButton);
        clearDeleteReverseButtons();
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    unfocusDeleteButton();
                    unfocusAlphabetButtons(secondaryButton);
                    unfocusThirdRowButtons();
                    secondaryButton.setFocussed(true);
                    if(focussedButtonForReverse != secondaryButton){
                        focussedButtonForReverse = secondaryButton;
                        if(openReverse!=null)
                            rootAnchor.getChildren().remove(openReverse);
                        openReverse = new ReverseCrossingButtons(secondaryButton.getText(), secondaryButton);
                        rootAnchor.getChildren().add(openReverse);
                    }
                    if(!secondaryButton.getText().equals(SPACE)){
                        clearThirdRowButtons();
                        clearForthRowButtons();
                        fillSuggestedWords(secondaryButton, GroupNames.THIRD_ROW.getGroupName());
                    }
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
    }

    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton thirdRowButton = (SecondaryButton)(mouseEvent.getSource());
        String letter = thirdRowButton.getText();
        updateSuggestionTimer(thirdRowButton);
        checkReverseCrossing(thirdRowButton);
        clearDeleteReverseButtons();
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    unfocusDeleteButton();
                    unfocusThirdRowButtons();
                    unfocusForthRowButtons();
                    thirdRowButton.setFocussed(true);
                    if(focussedButtonForReverse != thirdRowButton){
                        focussedButtonForReverse = thirdRowButton;
                        if(openReverse!=null)
                            rootAnchor.getChildren().remove(openReverse);
                        openReverse = new ReverseCrossingButtons(letter, thirdRowButton);
                        rootAnchor.getChildren().add(openReverse);
                    }
                    clearForthRowButtons();
                    fillSuggestedWords(thirdRowButton, GroupNames.FOURTH_ROW.getGroupName());
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
    }


    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton forthRowButton = (SecondaryButton)(mouseEvent.getSource());
        String letter = forthRowButton.getText();
        clearDeleteReverseButtons();
        updateSuggestionTimer(forthRowButton);
        checkReverseCrossing(forthRowButton);
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    unfocusDeleteButton();
                    unfocusForthRowButtons();
                    forthRowButton.setFocussed(true);
                    if(focussedButtonForReverse != forthRowButton){
                        focussedButtonForReverse = forthRowButton;
                        if(openReverse!=null)
                            rootAnchor.getChildren().remove(openReverse);
                        openReverse = new ReverseCrossingButtons(letter, forthRowButton);
                        rootAnchor.getChildren().add(openReverse);
                    }
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
    }

    private void groupButtonsExitEvent(MouseEvent mouseEvent){
        slipMargin.cancel();
    }

    private void secondaryButtonsExitEvent(MouseEvent mouseEvent){
        updateSuggestedWords.cancel();
        slipMargin.cancel();
    }

    private void clearDeleteReverseButtons(){
            rootAnchor.getChildren().removeAll(deleteOptions);
    }

    private void updateSuggestionTimer(Button button){
        updateSuggestedWords = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() ->
                        updateSuggestedWordsOnHover(button)
                );
            }
        };
        timer.schedule(updateSuggestedWords, 250);

    }

    private void fillSuggestedWords(SecondaryButton button, String groupName){
        List<String> suggestedLetters = suggestionsService.getSuggestionList(button.getText());
        if(!suggestedLetters.isEmpty()){
            if(groupName.equals(GroupNames.THIRD_ROW.getGroupName())){
                int i = 0;
                List<String> suggestions = suggestionsService.sortedMostCommonSubstrings(suggestedLetters, 2);
                for(SecondaryButton suggestion : thirdRowButtons){
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
                for(SecondaryButton suggestion : forthRowButtons){
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

    private void deleteButtonEnterEvent(MouseEvent mouseEvent){
        DeleteButton button = (DeleteButton)(mouseEvent.getSource());
        button.setFocussed(true);
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        //slipMargin.cancel();
        if(deleteOptions.isEmpty()){
            deleteOptions.add(new ReverseCrossingDelete(button,false));
            deleteOptions.add(new ReverseCrossingDelete(button,true));
        }
        checkReverseCrossingDelete(button);
        if(focussedButtonForReverse != button) {
            focussedButtonForReverse = button;
            if(openReverse!=null){
                rootAnchor.getChildren().remove(openReverse);
                openReverse = null;
            }
            rootAnchor.getChildren().addAll(deleteOptions);
        }
    }

    private void spaceButtonEnterEvent(MouseEvent mouseEvent){
        SecondaryButton button = (SecondaryButton) (mouseEvent.getSource());
        button.setFocussed(true);
        checkReverseCrossing(button);
        clearDeleteReverseButtons();
        if(focussedButtonForReverse != button) {
            focussedButtonForReverse = button;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(button.getText(), button);
            rootAnchor.getChildren().add(openReverse);
            unfocusDeleteButton();
            unfocusSuggestedWordsButton();
        }
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton wordButton = (SecondaryButton)(mouseEvent.getSource());
        if(!wordButton.getText().isEmpty()){
            unfocusDeleteButton();
            unfocusSpaceButton();
            suggestedWordsButtons.forEach(suggestedWord -> {
                if (!suggestedWord.equals(wordButton))
                    suggestedWord.setFocussed(false);
            });
            wordButton.setFocussed(true);
            clearDeleteReverseButtons();
            checkReverseCrossing(wordButton);
            if(focussedButtonForReverse != wordButton) {
                focussedButtonForReverse = wordButton;
                if (openReverse != null)
                    rootAnchor.getChildren().remove(openReverse);
                openReverse = new ReverseCrossingButtons(wordButton.getText(), wordButton);
                rootAnchor.getChildren().add(openReverse);
            }
        }
    }

    private void clearAllPopupButtons(){
        clearReverseButton();
        clearDeleteReverseButtons();
        clearSecondaryButtons();
        clearThirdRowButtons();
        clearForthRowButtons();
    }

    private void clearReverseButton(){
            rootAnchor.getChildren().remove(openReverse);
    }

    private void clearSecondaryButtons() {
            rootAnchor.getChildren().removeAll(recentSecondaryRowButtons);
            recentSecondaryRowButtons.clear();
    }

    private void clearThirdRowButtons(){
            rootAnchor.getChildren().removeAll(thirdRowButtons);
    }

    private void clearForthRowButtons(){
            rootAnchor.getChildren().removeAll(forthRowButtons);
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
        for (SecondaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SecondaryButton button : recentSecondaryRowButtons) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(screenHeight - (2*buttonHeight));
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SecondaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
        for (SecondaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(button.getParentButton().getLayoutY() - (ReverseCrossingButtons.getMargin()*2) - (button.getPrefHeight()/2) - buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        for (ReverseCrossingDelete button : deleteOptions)
            button.refreshSize();


    }

    private void resizeTextAreaContent(){
        double textAreaWidth = windowDimensions.getWidth()*0.4; //40% screen width
        wordsToWrite.setPrefSize(textAreaWidth, 10);
        wordsToWrite.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsToWrite.setLayoutY(10); //10px margin top
        wordsWritten.setPrefSize(textAreaWidth, 10);
        wordsWritten.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsWritten.setLayoutY(50);
        separator.setEndX(windowDimensions.getWidth());
        deleteButton.setPrefSize(windowDimensions.getWidth()*0.1, windowDimensions.getHeight()*0.085);
        deleteButton.setLayoutX(windowDimensions.getWidth()*0.55);
        deleteButton.setLayoutY(windowDimensions.getHeight()*0.005);
        spaceButton.setPrefSize(windowDimensions.getWidth()*0.15, windowDimensions.getHeight()*0.085);
        spaceButton.setLayoutX(windowDimensions.getWidth()*0.8);
        spaceButton.setLayoutY(windowDimensions.getHeight()*0.005);

    }

    private void checkReverseCrossing(Button button){
        deleteButton.setReversing(false);
        if(openReverse!= null && openReverse.isReverseCrossing() && button.equals(openReverse.getParentButton())){
            String buttonText = button.getText();
            if(button instanceof SecondaryButton secondaryButton){
                if(secondaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                    if(!secondaryButton.getText().equals("")){ //se a palavra não for string vazia
                        setWrittenWordsText(buttonText, true);
                        dataService.incrementGroupAccess(secondaryButton.getGroupName());
                    }
                }else{
                    if(buttonText.equals(SPACE)){
                        setWrittenWordsText(" ", false);
                    }else{ //todas as letras que não o espaço
                        setWrittenWordsText(buttonText, false);
                        updateSuggestedWordsOnReverseCrossing();
                        dataService.incrementGroupAccess(secondaryButton.getGroupName());
                    }
                }
                secondaryButton.updateBackgroundColor();
            }
            unfocusAllButtons();
            clearAllPopupButtons();
            openReverse.setReverseCrossing(false);
        }
    }

    private void checkReverseCrossingDelete(DeleteButton button){
        if(button.isReversing()){
            button.updateBackgroundColor();
            if(button.getAction().equals("letter")){
                wordsWritten.setText(writtingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                dataService.incrementLetterDeletes();
            }else{
                wordsWritten.setText(writtingService.deleteWord().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                dataService.incrementWordDeletes();
            }
        }
    }

    private void setWrittenWordsText(String text, boolean word){
        if(word)
            wordsWritten.setText(writtingService.addWord(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
        else
            wordsWritten.setText(writtingService.addLetters(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
    }

    private void updateSuggestedWordsOnReverseCrossing() {
        List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord());
        int i = 0;
        for (SecondaryButton suggestedWordButton : suggestedWordsButtons) {
            if(suggestedWords.size() > i){
                suggestedWordButton.setText(suggestedWords.get(i));
                i++;
            }else{
                suggestedWordButton.setText("");
            }
        }
    }

    private void updateSuggestedWordsOnHover(Button button) {
        if(!button.getText().equals(SPACE)){
            List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord() + button.getText());
            int i = 0;
            for (SecondaryButton suggestedWordButton : suggestedWordsButtons) {
                if(suggestedWords.size() > i){
                    suggestedWordButton.setText(suggestedWords.get(i));
                    i++;
                }else{
                    suggestedWordButton.setText("");
                }
            }
        }
    }

    private void unfocusAllButtons(){
        unfocusSuggestedWordsButton();
        unfocusDeleteButton();
        unfocusSpaceButton();
        unfocusGroupButtons();
        alphabetButtons.forEach((s, button) -> button.setFocussed(false));
    }

    private void unfocusSuggestedWordsButton(){
        suggestedWordsButtons.forEach(button ->
                button.setFocussed(false)
        );
    }

    private void unfocusDeleteButton(){
        if(deleteButton.isInFocus())
            deleteButton.setFocussed(false);
    }

    private void unfocusSpaceButton(){
        spaceButton.setFocussed(false);
    }

    private void unfocusAlphabetButtons(SecondaryButton secondaryButton) {
        alphabetButtons.forEach((s, button) -> {
            if (!button.equals(secondaryButton))
                button.setFocussed(false);
        });
    }

    private void unfocusThirdRowButtons() {
        thirdRowButtons.forEach(button -> {
            if(button.isInFocus())
                button.setFocussed(false);
        });
    }

    private void unfocusForthRowButtons() {
        forthRowButtons.forEach(button ->{
            if(button.isInFocus())
                button.setFocussed(false);
        });
    }

    private void unfocusGroupButtons(){
        groupsButtonList.forEach(groupButton -> groupButton.setFocussed(false));
    }


    private void addRootAnchorListeners(){
        rootAnchor.heightProperty().addListener((observable, oldValue, newValue) -> {
            screenHeight = newValue.doubleValue();
            buttonHeight = screenHeight/8.25;
            windowDimensions.setHeight(newValue.doubleValue());
            resizeTextAreaContent();
            resizeButtons();
            mouseService.setWindowDimensions(windowDimensions);
        });
        rootAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            screenWidth = newValue.doubleValue();
            buttonWidth = screenWidth/TOTAL_GROUPS;
            windowDimensions.setWidth(newValue.doubleValue());
            resizeTextAreaContent();
            resizeButtons();
            mouseService.setWindowDimensions(windowDimensions);
        });
    }

    public void setKeyListener() {
        mainScene.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.CONTROL && !dataService.isStarted() && !dataService.isFinished()){
                timer.schedule(timeout, TIME * 60 * 1000L);
                if(CONNECT_SERVER)
                    connections.connect("localhost", 3000);
                dataService.startTimer();
                wordsToWrite.setText(dataService.getPhraseFromDataset());
            }else if(event.getCode() == KeyCode.CONTROL && dataService.getTotalPhrasesRetrieved() < 10 && !dataService.isFinished()){
                wordsToWrite.setText(dataService.getPhraseFromDataset());
                writtingService.nextPhrase();
                wordsWritten.setText("");
            } else if(event.getCode() == KeyCode.CONTROL && dataService.getTotalPhrasesRetrieved() == 10 && !dataService.isFinished()){
                timeout.cancel();
                dataService.stopTimer();
                finished();
            } else if(event.getCode() == KeyCode.BACK_SPACE){
                finished();
            }
        });
    }

    private void finished() {
        wordsWritten.setText("All finished, thank you!");
        if(!dataService.isSavedTxt())
            dataService.saveDataToTxt(variableGroups,USERNAME, AGE, writtingService, SESSION_NUMBER);
        if(!dataService.isSavedCsv())
            dataService.saveDataToCsv(variableGroups,USERNAME, AGE, writtingService, SESSION_NUMBER);
        if(connections.isRunning())
            connections.setRunning(false);
    }

}