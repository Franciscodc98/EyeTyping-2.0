package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.services.DataService;
import com.eyetyping.eyetyping2.services.SuggestionsService;
import com.eyetyping.eyetyping2.services.WritingService;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import com.eyetyping.eyetyping2.utils.Maths;
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
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.eyetyping.eyetyping2.utils.ButtonsUtils.createColoredText;

@Getter
@Setter
public class KeyboardController implements Initializable {

    public static final String SPACE = "SPACE";

    private boolean canWrite = false;
    private boolean training = false;

    //User data
    private static final VariableGroups GROUPS = VariableGroups.MEDIUM_GROUPS;

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
    private List<SecondaryButton> deleteOptions = new ArrayList<>();
    private SecondaryButton spaceButton;

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private String focussedSuggestion = "";
    private String focussedGroup = "";

    private Scene mainScene = null;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //split gaze margin
    private static final long SLIP_MARGIN = 250;
    private TimerTask gazeMargin;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;

    //dwell time
    private static final double DWELL_TIME = 800; //dwell time for selection in ms
    private Timer timer = new Timer();
    private TimerTask progressBarProgress;
    private TimerTask slipMargin;
    private double progressTimerAux = 0;

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
        groupsButtonList = ButtonsUtils.createGroupButtons(GROUPS, TOTAL_GROUPS);
        int widthAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(widthAux);
            button.setLayoutY(screenHeight-buttonHeight);
            button.setOnMouseEntered(this::groupsButtonEnterEvent);
            button.setOnMouseExited(this::groupButtonExitEvent);
            widthAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(groupsButtonList);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), this::secondaryButtonsEnterEvent, this::buttonsExitEvent))));
    }

    private void setupSuggestedWordButtons(){
        suggestedWordsButtons = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS);
        int xCoordinateAux = 0;
        for (SecondaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY() + WORDS_MARGIN);
            button.setOnMouseEntered(this::suggestedWordEnterEvent);
            button.setOnMouseExited(this::suggestedWordsExitEvent);
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedWordsButtons);
    }

    private void setupThirdRowButtons(){
        thirdRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.THIRD_ROW);
        for (SecondaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::thirdRowButtonsEnterEvent);
            button.setOnMouseExited(this::buttonsExitEvent);
        }
    }

    private void setupFourthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.FOURTH_ROW);
        for (SecondaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::fourthRowButtonsEnterEvent);
            button.setOnMouseExited(this::buttonsExitEvent);
        }
    }

    private void setupTextArea(){
        double separatorY = 85;
        separator = new Line(0,separatorY, windowDimensions.getWidth(), separatorY);
        separator.setStrokeWidth(3);
        deleteOptions.add(SecondaryButton.asRoot("Delete word"));
        deleteOptions.add(SecondaryButton.asRoot("Delete letter"));
        deleteOptions.forEach(deleteButton -> {
            deleteButton.setOnMouseEntered(this::suggestedWordEnterEvent);
            deleteButton.setOnMouseExited(this::suggestedWordsExitEvent);
            deleteButton.setGroupName("delete");
        });
        spaceButton = SecondaryButton.asRoot(SPACE);
        spaceButton.setOnMouseEntered(this::suggestedWordEnterEvent);
        spaceButton.setOnMouseExited(this::suggestedWordsExitEvent);
        spaceButton.setGroupName(SPACE);
        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        resizeTextAreaContent();
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
        rootAnchor.getChildren().addAll(deleteOptions);
        rootAnchor.getChildren().add(spaceButton);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() ->
                {
                    groupsButtonList.forEach(groupButton -> {
                        if (!groupButton.equals(focussedButton))
                            groupButton.setFocussed(false);
                    });
                    focussedButton.setFocussed(true);
                    if(!recentSecondaryRowButtons.isEmpty())
                        clearSecondaryButtons();
                    for (char c: groupString.toCharArray()) {
                        recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
                    }
                    int widthAux = 0;
                    for (SecondaryButton button: recentSecondaryRowButtons) {
                        button.setPrefSize(buttonWidth, buttonHeight);
                        button.setLayoutX(widthAux);
                        button.setLayoutY(screenHeight- (2*buttonHeight));
                        button.setGraphic(createColoredText(writingService.getCurrentTypingWord()));
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
    }

    private void secondaryButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(secondaryButton, DWELL_TIME);
        updateSuggestionTimer(secondaryButton);
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    alphabetButtons.forEach((s, button) -> {
                                if (!button.equals(secondaryButton))
                                    button.setFocussed(false);
                            });
                    thirdRowButtons.forEach(button -> button.setFocussed(false));
                    secondaryButton.setFocussed(true);
                    if(!secondaryButton.getText().equals(SPACE)){
                        clearThirdRowButtons();
                        fillSuggestedWords(secondaryButton, GroupNames.THIRD_ROW.getGroupName());
                    }
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
        clearForthRowButtons();
    }

    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton thirdRowButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(thirdRowButton, DWELL_TIME);
        updateSuggestionTimer(thirdRowButton);
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    thirdRowButtons.forEach(button -> {
                        thirdRowButton.setFocussed(true);
                        if (!button.equals(thirdRowButton))
                            button.setFocussed(false);
                    });
                    forthRowButtons.forEach(button -> button.setFocussed(false));
                        clearForthRowButtons();
                        fillSuggestedWords(thirdRowButton, GroupNames.FOURTH_ROW.getGroupName());
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
    }

    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton fourthRowButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(fourthRowButton, DWELL_TIME);
        slipMargin = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    forthRowButtons.forEach(button -> {
                        fourthRowButton.setFocussed(true);
                        if (!button.equals(fourthRowButton))
                            button.setFocussed(false);
                    });
                    updateSuggestionTimer(fourthRowButton);
                });
            }
        };
        timer.schedule(slipMargin, SLIP_MARGIN);
    }

    private void suggestedWordEnterEvent(MouseEvent mouseEvent){
        SecondaryButton wordButton = (SecondaryButton)(mouseEvent.getSource());
        if(!wordButton.getText().isEmpty()){
            startProgress(wordButton, 600);
            wordButton.setFocussed(true);
        }
    }

    private void startProgress(SecondaryButton secondaryButton, double time) {
        if(canWrite) {
            progressBarProgress = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        double progress = Maths.normalizeBetween0and1(0, time, progressTimerAux);
                        if (progress < 1.0) {
                            secondaryButton.setProgress(progress);
                        } else {
                            buzzer.play(0.1);
                            String buttonText = secondaryButton.getText();
                            secondaryButton.updateBackgroundColor();
                            if (!secondaryButton.getGroupName().equals("delete")) {
                                if (!buttonText.equals(SPACE)) {
                                    dataService.incrementGroupAccess(secondaryButton.getGroupName());
                                    if (secondaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())) {//words
                                        setWrittenWordsText(buttonText, true);
                                        emptyRecommendedWords();
                                        clearAllPopupButtons();
                                        dataService.lastTypedTime();
                                        progressBarProgress.cancel();
                                        dataService.incrementKeyStrokes(buttonText.length()+1);
                                    } else { //letters
                                        setWrittenWordsText(buttonText, false);
                                        dataService.lastTypedTime();
                                        dataService.incrementKeyStrokes(buttonText.length());
                                    }
                                } else {//space
                                    setWrittenWordsText(" ", false);
                                    dataService.incrementKeyStrokes(1);
                                }
                            } else {
                                if (buttonText.equals("Delete letter")) {
                                    wordsWritten.setText(writingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                                    dataService.incrementLetterDeletes();
                                    updateSuggestedWords();
                                    dataService.incrementKeyStrokes(1);
                                } else {
                                    dataService.incrementKeyStrokes(writingService.getLastWordLength());
                                    wordsWritten.setText(writingService.deleteWord().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                                    dataService.incrementWordDeletes();
                                }
                            }
                            clearAllPopupButtons();
                            secondaryButton.setProgress(0);
                            progressTimerAux = 0;
                        }
                    });
                    progressTimerAux += 20;
                }
            };
            timer.scheduleAtFixedRate(progressBarProgress, 0, 20); //updates progress Bar every 20ms
        }
    }

    private void buttonsExitEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        secondaryButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        slipMargin.cancel();
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void groupButtonExitEvent(MouseEvent mouseEvent){
        slipMargin.cancel();
    }

    private void suggestedWordsExitEvent(MouseEvent mouseEvent){
        SecondaryButton wordButton = (SecondaryButton)(mouseEvent.getSource());
        wordButton.setFocussed(false);
        wordButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void updateSuggestionTimer(SecondaryButton button){
        focussedSuggestion = button.getText();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                            if(focussedSuggestion.equals(button.getText())){
                                updateSuggestedWordsOnHover(button);
                            }
                        }
                );

            }
        }, SLIP_MARGIN-150);

    }

    private void fillSuggestedWords(SecondaryButton button, String groupName){
        List<String> suggestedLetters = suggestionsService.getSuggestionListForSuggestedLetters(writingService.getCurrentTypingWord(), button.getText());
        if(!suggestedLetters.isEmpty()){
            if(groupName.equals(GroupNames.THIRD_ROW.getGroupName())){
                int i = 0;
                List<String> suggestions = suggestionsService.sortedMostCommonSubstrings(suggestedLetters, 2);
                fillSuggestionAux(button, suggestions, thirdRowButtons);
            } else if(groupName.equals(GroupNames.FOURTH_ROW.getGroupName())){
                int i = 0;
                List<String> suggestions = suggestionsService.sortedMostCommonSubstrings(suggestedLetters, 3);
                fillSuggestionAux(button, suggestions, forthRowButtons);
            }
        }
        resizeButtons();
    }

    private void fillSuggestionAux(SecondaryButton button, List<String> suggestions, List<SecondaryButton> buttonsRow) {
        int i = 0;
        for(SecondaryButton suggestion : buttonsRow){
            if(suggestions.size() > i){
                suggestion.setParentButton(button);
                suggestion.setText(suggestions.get(i));
                suggestion.setGraphic(createColoredText(writingService.getCurrentTypingWord()));
                rootAnchor.getChildren().add(suggestion);
            } else{
                rootAnchor.getChildren().remove(suggestion);
            }
            i++;
        }
    }

    private void updateSuggestedWords() {
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

    private void emptyRecommendedWords(){
        for (SecondaryButton button : suggestedWordsButtons)
            button.setText("");
    }

    private void clearAllPopupButtons(){
        clearSecondaryButtons();
        clearThirdRowButtons();
        clearForthRowButtons();
    }

    private void clearSecondaryButtons(){
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
        int xCoordinateAux = SIDE_MARGIN;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(screenHeight-buttonHeight);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = SIDE_MARGIN;
        for (SecondaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = SIDE_MARGIN;
        for (SecondaryButton button : recentSecondaryRowButtons) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(screenHeight - (2*buttonHeight) - 3);
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = SIDE_MARGIN;
        for (SecondaryButton button : thirdRowButtons) {
            if(button.getParentButton() != null){
                button.setPrefSize(buttonWidth, buttonHeight);
                button.setLayoutX(xCoordinateAux);
                button.setLayoutY(button.getParentButton().getLayoutY() - (button.getPrefHeight()/2) - buttonHeight);
            }
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = SIDE_MARGIN;
        for (SecondaryButton button : forthRowButtons) {
            if(button.getParentButton() != null){
                button.setPrefSize(buttonWidth, buttonHeight);
                button.setLayoutX(xCoordinateAux);
                button.setLayoutY(button.getParentButton().getLayoutY() - (button.getPrefHeight()/2) - buttonHeight);
            }
            xCoordinateAux+=buttonWidth;
        }
    }

    private void resizeTextAreaContent(){
        double textAreaWidth = windowDimensions.getWidth()*0.4; //60% screen width
        wordsToWrite.setPrefSize(textAreaWidth, 10);
        wordsToWrite.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsToWrite.setLayoutY(10); //10px margin top
        wordsWritten.setPrefSize(textAreaWidth, 10);
        wordsWritten.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsWritten.setLayoutY(50);
        separator.setEndX(windowDimensions.getWidth());
        int aux = 0;
        for (SecondaryButton deleteButton : deleteOptions) {
            deleteButton.setPrefSize(windowDimensions.getWidth()*0.15, windowDimensions.getHeight()*0.085);
            deleteButton.setLayoutX(aux + (windowDimensions.getWidth()*0.45));
            deleteButton.setLayoutY(windowDimensions.getHeight()*0.005);
            aux+= deleteButton.getPrefWidth() + 10;
        }
        spaceButton.setPrefSize(windowDimensions.getWidth()*0.2, windowDimensions.getHeight()*0.085);
        spaceButton.setLayoutX(aux + 10 + (windowDimensions.getWidth()*0.47));
        spaceButton.setLayoutY(windowDimensions.getHeight()*0.005);

    }

    private void setWrittenWordsText(String text, boolean isWord){
        if(isWord)
            wordsWritten.setText(writingService.addWord(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
        else
            wordsWritten.setText(writingService.addLetters(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
    }

    private void updateSuggestedWordsOnHover(SecondaryButton button) {
        if(!button.getText().equals(SPACE) && !button.getText().equals(",") && !button.getText().equals(".") && !button.getText().equals("!") && !button.getText().equals("?")){
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
            buttonWidth = (screenWidth - 2 * SIDE_MARGIN)/TOTAL_GROUPS;
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
            }else if(event.getCode() == KeyCode.CONTROL && dataService.getTotalPhrasesRetrieved() <= 8)
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
            if(dataService.getTotalPhrasesRetrieved() < 8){
                dataService.setPaused(true);
                wordsWritten.setTimerOnFeedback(false);
                dataService.saveDataToCsv(dataService.csvLineData(wordsToWrite.getText(), writingService.getTextString()));
                wordsToWrite.setText(dataService.getPhraseFromDataset());
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