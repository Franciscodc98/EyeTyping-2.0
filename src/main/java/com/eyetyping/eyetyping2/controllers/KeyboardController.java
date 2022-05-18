package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.services.DataService;
import com.eyetyping.eyetyping2.services.SuggestionsService;
import com.eyetyping.eyetyping2.services.WritingService;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Line;
import lombok.Data;

import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class KeyboardController implements Initializable {

    public static final String SPACE = "SPACE";

    private boolean canWrite = false;
    private boolean training = false;

    private static final VariableGroups GROUP_VARIABLE = VariableGroups.MEDIUM_GROUPS;
    private static final int SESSION_NUMBER = 1;

    //Services
    private final SuggestionsService suggestionsService = SuggestionsService.getInstance();
    private final WritingService writingService = WritingService.getInstance();
    private final DataService dataService = DataService.getInstance();

    private static final int TOTAL_GROUPS = 6;
    private static final int SIDE_MARGIN = 0;
    private static final int WORDS_MARGIN = 20;
    private List<SecondaryButton> suggestedWordsButtons = new ArrayList<>();
    private List<GroupButton> groupsButtonList = new ArrayList<>();
    private final HashMap<String, SecondaryButton> alphabetButtons = new HashMap<>();
    private final List<SecondaryButton> recentSecondaryRowButtons = new ArrayList<>();
    private List<SecondaryButton> thirdRowButtons = new ArrayList<>();
    private List<SecondaryButton> forthRowButtons = new ArrayList<>();
    private List<SecondaryButton> deleteButtons = new ArrayList<>();
    private SecondaryButton spaceButton;

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private Button focussedButtonForReverse;
    private ReverseCrossingButtons openReverse;

    private Scene mainScene = null;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;

    private Timer timer = new Timer();
    private TimerTask slipMargin;
    private TimerTask updateSuggestedWords;

    //split gaze margin
    private static final long SLIP_MARGIN = 250;

    //sound
    private AudioClip buzzer = new AudioClip(Paths.get(GlobalVariables.CONFIRM_SOUND_PATH).toUri().toString());


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupButtons();
        addRootAnchorListeners();
    }

    private void setupButtons(){
        setupGroupButtons();
        setupTextArea();
        setupSuggestedWordButtons();
        setupThirdRowButtons();
        setupFourthRowButtons();
    }

    private void setupGroupButtons() {
        groupsButtonList = ButtonsUtils.createGroupButtons(GROUP_VARIABLE, TOTAL_GROUPS);
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
            button.setLayoutY(separator.getStartY() + WORDS_MARGIN);
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
        setDeleteButtons();
        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        spaceButton = new SecondaryButton(SPACE);
        spaceButton.setOnMouseEntered(this::textAreaButtonsEnterEvent);
        spaceButton.setGroupName(SPACE);
        resizeTextAreaContent();
        rootAnchor.getChildren().add(spaceButton);
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
    }

    private void setDeleteButtons(){
        SecondaryButton deleteWord = new SecondaryButton("Del word");
        SecondaryButton deleteLetter = new SecondaryButton("Del letter");
        deleteButtons.add(deleteWord);
        deleteButtons.add(deleteLetter);
        for (SecondaryButton delete : deleteButtons) {
            delete.setOnMouseEntered(this::textAreaButtonsEnterEvent);
            delete.setGroupName("delete");
        }
        rootAnchor.getChildren().addAll(deleteButtons);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    groupsButtonList.forEach(groupButton -> {
                        if (!groupButton.equals(focussedButton))
                            groupButton.setFocussed(false);
                    });
                    focussedButton.setFocussed(true);
                    if(!recentSecondaryRowButtons.isEmpty())
                        clearAllPopupButtons();
                    for (char c: groupString.toCharArray())
                        recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
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
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        unfocusDeleteButtons();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
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
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        unfocusDeleteButtons();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
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
        updateSuggestionTimer(forthRowButton);
        checkReverseCrossing(forthRowButton);
        unfocusSuggestedWordsButton();
        unfocusSpaceButton();
        unfocusDeleteButtons();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
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
        List<String> suggestedLetters = suggestionsService.getSuggestionListForSuggestedLetters(writingService.getCurrentTypingWord(), button.getText());
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

    private void textAreaButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton button = (SecondaryButton) (mouseEvent.getSource());
        button.setFocussed(true);
        checkReverseCrossing(button);
        if(button.getText().equals(SPACE))
            unfocusDeleteButtons();
        else{
            unfocusSpaceButton();
            for (SecondaryButton delete :deleteButtons) {
                if(!delete.equals(button))
                    delete.setFocussed(false);
            }
        }
        if(focussedButtonForReverse != button) {
            focussedButtonForReverse = button;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(button.getText(), button);
            rootAnchor.getChildren().add(openReverse);
            unfocusSuggestedWordsButton();
        }
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton wordButton = (SecondaryButton)(mouseEvent.getSource());
        if(!wordButton.getText().isEmpty()){
            unfocusSpaceButton();
            unfocusDeleteButtons();
            suggestedWordsButtons.forEach(suggestedWord -> {
                if (!suggestedWord.equals(wordButton))
                    suggestedWord.setFocussed(false);
            });
            wordButton.setFocussed(true);
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

    private void emptyRecommendedWords(){
        for (SecondaryButton button : suggestedWordsButtons)
            button.setText("");
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
        double aux = wordsToWrite.getLayoutX() + wordsWritten.getPrefWidth() + windowDimensions.getWidth()*0.05;
        for (SecondaryButton delete : deleteButtons) {
            delete.setPrefSize(windowDimensions.getWidth()*0.09, windowDimensions.getHeight()*0.085);
            delete.setLayoutX(aux);
            delete.setLayoutY(windowDimensions.getHeight()*0.005);
            aux+= delete.getPrefWidth() + (delete.getPrefWidth() * 0.75) + windowDimensions.getWidth()*0.025;
        }
        spaceButton.setPrefSize(windowDimensions.getWidth()*0.09, windowDimensions.getHeight()*0.085);
        spaceButton.setLayoutX(aux);
        spaceButton.setLayoutY(windowDimensions.getHeight()*0.005);

    }

    private void checkReverseCrossing(SecondaryButton button){
        if(openReverse!= null && openReverse.isReverseCrossing() && button.equals(openReverse.getParentButton()) && canWrite){
            buzzer.play(0.1);
            String buttonText = button.getText();
                if(button.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                    if(!button.getText().equals("")){ //se a palavra não for string vazia
                        setWrittenWordsText(buttonText, true);
                        dataService.incrementGroupAccess(button.getGroupName());
                        emptyRecommendedWords();
                        unfocusAllButtons();
                        clearAllPopupButtons();
                        dataService.lastTypedTime();
                        dataService.incrementKeyStrokes(buttonText.length() + 1);
                    }
                }else{
                    switch (buttonText) {
                        case SPACE ->{
                            setWrittenWordsText(" ", false);
                            openReverse.clearBackground();
                            dataService.incrementKeyStrokes(1);
                        }
                        case "Del letter" -> {
                            wordsWritten.setText(writingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                            updateSuggestedWordsOnReverseCrossing();
                            dataService.incrementLetterDeletes();
                            openReverse.clearBackground();
                            dataService.incrementKeyStrokes(1);
                        }
                        case "Del word" -> {
                            dataService.incrementKeyStrokes(writingService.getLastWordLength());
                            wordsWritten.setText(writingService.deleteWord().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                            dataService.incrementWordDeletes();
                            openReverse.clearBackground();
                        }
                        default -> {  //todas as letras que não o espaço
                            setWrittenWordsText(buttonText, false);
                            updateSuggestedWordsOnReverseCrossing();
                            dataService.incrementGroupAccess(button.getGroupName());
                            unfocusAllButtons();
                            clearAllPopupButtons();
                            dataService.lastTypedTime();
                            dataService.incrementKeyStrokes(buttonText.length());
                        }
                    }
                }
            button.updateBackgroundColor();
            openReverse.setReverseCrossing(false);
        }
    }

    private void setWrittenWordsText(String text, boolean word){
        if(word)
            wordsWritten.setText(writingService.addWord(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
        else
            wordsWritten.setText(writingService.addLetters(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
    }

    private void updateSuggestedWordsOnReverseCrossing() {
        List<String> suggestedWords = suggestionsService.getSuggestionList(writingService.getCurrentTypingWord());
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
            List<String> suggestedWords = suggestionsService.getSuggestionList(writingService.getCurrentTypingWord() + button.getText());
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
        unfocusSpaceButton();
        unfocusGroupButtons();
        unfocusDeleteButtons();
        alphabetButtons.forEach((s, button) -> button.setFocussed(false));
    }

    private void unfocusSuggestedWordsButton(){
        suggestedWordsButtons.forEach(button ->
                button.setFocussed(false)
        );
    }

    private void unfocusSpaceButton(){
        spaceButton.setFocussed(false);
    }

    private void unfocusDeleteButtons(){
        deleteButtons.forEach(button -> button.setFocussed(false));
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
        });
        rootAnchor.widthProperty().addListener((observable, oldValue, newValue) -> {
            screenWidth = newValue.doubleValue();
            buttonWidth = screenWidth/TOTAL_GROUPS;
            windowDimensions.setWidth(newValue.doubleValue());
            resizeTextAreaContent();
            resizeButtons();
        });
    }

    public void setKeyListener() {
        mainScene.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.BACK_SPACE && !training && !dataService.isStarted()){
                wordsWritten.setTimerOnFeedback(false);
                wordsToWrite.setText(dataService.getPhraseFromDataset());
                training = true;
            }else if(event.getCode() == KeyCode.BACK_SPACE && training && !canWrite && !dataService.isStarted()) {
                canWrite = true;
                wordsWritten.setTimerOnFeedback(true);
                emptyRecommendedWords();
                clearAllPopupButtons();
            }else if(event.getCode() == KeyCode.BACK_SPACE && training && canWrite && !dataService.isStarted()){
                canWrite = false;
                wordsWritten.setTimerOnFeedback(false);
                wordsToWrite.setText(dataService.getPhraseFromDataset());
                wordsWritten.setText("");
                writingService.clearWrittenText();
            }

            if(event.getCode() == KeyCode.CONTROL && !dataService.isStarted()){
                wordsWritten.setTimerOnFeedback(false);
                dataService.setStarted(true);
                wordsToWrite.setText(dataService.getPhraseFromDataset());
                wordsWritten.setText("");
                writingService.clearWrittenText();
                dataService.incrementTotalPhrasesRetried();
            }else if(event.getCode() == KeyCode.CONTROL && dataService.getTotalPhrasesRetrieved() <= 5)
                controlPressed();
        });
    }

    private void controlPressed(){
        if(dataService.isPaused()){
            wordsWritten.setTimerOnFeedback(true);
            dataService.startTimer();
            dataService.setPaused(false);
            dataService.resetKeystrokes();
            canWrite = true;
            emptyRecommendedWords();
            clearAllPopupButtons();
        }else{
            if(dataService.getTotalPhrasesRetrieved() < 5){
                dataService.setPaused(true);
                wordsWritten.setTimerOnFeedback(false);
                dataService.saveDataToCsv(dataService.csvLineData(wordsToWrite.getText(), writingService.getTextString()));
                wordsToWrite.setText(dataService.getPhraseFromDataset());
                System.out.println(dataService.getKeyStrokes());
                dataService.incrementTotalPhrasesRetried();
                writingService.nextPhrase();
                wordsWritten.setText("");
                canWrite = false;
            }else{
                dataService.saveDataToCsv(dataService.csvLineData(wordsToWrite.getText(), writingService.getTextString()));
                wordsWritten.setTimerOnFeedback(false);
                finished();
            }
        }
    }

    private void finished(){
        dataService.setFinished(true);
        wordsToWrite.setText("Finished, thank you!");
        wordsWritten.setText("");
        dataService.incrementUserId();
        canWrite = false;
    }

}