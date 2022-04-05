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
import com.eyetyping.eyetyping2.utils.Maths;
import com.eyetyping.eyetyping2.utils.Position2D;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Line;
import lombok.Data;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class KeyboardController implements Initializable {

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
    private List<SecondaryButton> deleteOptions = new ArrayList<>();

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private String focussedGroup = "";
    private String focussedSuggestion = "";

    private Scene mainScene = null;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;

    //dwell time
    private static final double DWELL_TIME = 1000; //dwell time for selection in ms
    private Timer timer = new Timer();
    private TimerTask progressBarProgress;
    private double progressTimerAux = 0;

    //mouse


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        calculateGroupsSize(VariableGroups.MEDIUM_GROUPS); //Alterar isto para os diferentes tipos de layout
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
            //button.setOnMouseClicked((event -> wordsToWrite.displayPhrase()));
            button.setOnMouseMoved(this::mouseMovementEvent);
            widthAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(groupsButtonList);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), this::secundaryButtonsEnterEvent, this::mouseMovementEvent, this::secundaryButtonsExitEvent))));
    }

    private void setupSuggestedWordButtons(){
        suggestedWordsButtons = ButtonsUtils.createSuggestedWordButtons(TOTAL_GROUPS, GroupNames.WORDS_ROW);
        int xCoordinateAux = 0;
        for (SecondaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY());
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            button.setOnMouseMoved(this::mouseMovementEvent);
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
            button.setOnMouseMoved(this::mouseMovementEvent);
            button.setOnMouseExited(this::thirdButtonsExitEvent);
        }
    }

    private void setupFourthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.FOURTH_ROW);
        for (SecondaryButton button : forthRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::fourthRowButtonsEnterEvent);
            button.setOnMouseMoved(this::mouseMovementEvent);
            button.setOnMouseExited(this::fourthButtonsExitEvent);
        }
    }

    private void setupTextArea(){
        double separatorY = 85;
        separator = new Line(0,separatorY, windowDimensions.getWidth(), separatorY);
        separator.setStrokeWidth(3);
        deleteOptions.add(SecondaryButton.asRoot("Delete word"));
        deleteOptions.add(SecondaryButton.asRoot("Delete letter"));
        deleteOptions.forEach((deleteButton) -> {
            deleteButton.setOnMouseEntered(this::deleteButtonsEnterEvent);
            deleteButton.setOnMouseExited(this::deleteButtonsExitEvent);
            deleteButton.setGroupName("delete");
        });

        wordsToWrite = new DisplayTextLabel();
        wordsWritten = new TextWrittenLabel();
        resizeTextAreaContent();
        rootAnchor.getChildren().add(separator);
        rootAnchor.getChildren().add(wordsToWrite);
        rootAnchor.getChildren().add(wordsWritten);
        rootAnchor.getChildren().addAll(deleteOptions);
    }

    private void groupsButtonEnterEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        clearThirdRowButtons();
        clearForthRowButtons();
        if(!focussedGroup.equals(groupString)){
            if(!recentSecondaryRowButtons.isEmpty())
                clearSecundaryButtons();
            for (char c: groupString.toCharArray()) {
                recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
            }
            int widthAux = 0;
            for (SecondaryButton button: recentSecondaryRowButtons) {
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
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(secondaryButton);
        updateSuggestionTimer(secondaryButton);
        clearThirdRowButtons();
        clearForthRowButtons();
        if(!secondaryButton.getText().equals("SPACE"))
            fillSuggestedWords(secondaryButton, GroupNames.THIRD_ROW.getGroupName());
    }

    private void startProgress(SecondaryButton secondaryButton) {
        progressBarProgress = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    double progress = Maths.normalizeBetween0and1(0, DWELL_TIME, progressTimerAux);
                    if (progress < 1.0){
                        secondaryButton.setProgress(progress);
                    } else{
                        String buttonText = secondaryButton.getText();
                        if(!secondaryButton.getGroupName().equals("delete")){
                            if(!buttonText.equals("SPACE")){
                                if(secondaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
                                    setWrittenWordsText(buttonText, true);
                                    clearAllPopupButtons();
                                }else
                                    setWrittenWordsText(buttonText, false);
                            }else{
                                setWrittenWordsText(" ", false);
                            }
                        }else{
                            if(buttonText.equals("Delete letter")){
                                wordsWritten.setText(writtingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                                dataService.incrementLetterDeletes();
                            }else{
                                wordsWritten.setText(writtingService.deleteWord().stream().map(c -> Character.toString(c)).collect(Collectors.joining()) + "|");
                                dataService.incrementWordDeletes();
                            }
                        }
                        secondaryButton.setProgress(0);
                        progressTimerAux = 0;
                    }
                });
                progressTimerAux +=20;
            }
        };
        timer.scheduleAtFixedRate(progressBarProgress, 0, 20); //updates progress Bar every 20ms
    }

    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton thirdRowButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(thirdRowButton);
        updateSuggestionTimer(thirdRowButton);
        clearForthRowButtons();
        fillSuggestedWords(thirdRowButton, GroupNames.FOURTH_ROW.getGroupName());
    }

    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton fourthRowButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(fourthRowButton);
        updateSuggestionTimer(fourthRowButton);
    }

    private void secundaryButtonsExitEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        secondaryButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void thirdButtonsExitEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        secondaryButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void fourthButtonsExitEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        secondaryButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void deleteButtonsExitEvent(MouseEvent mouseEvent){
        SecondaryButton secondaryButton = (SecondaryButton)(mouseEvent.getSource());
        secondaryButton.setProgress(0);
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void suggestedWordsExitEvent(MouseEvent mouseEvent){
        SecondaryButton suggestionWordButton = (SecondaryButton)(mouseEvent.getSource());
        suggestionWordButton.setProgress(0);
        focussedSuggestion = "";
        progressTimerAux = 0;
        if(progressBarProgress !=null)
            progressBarProgress.cancel();
    }

    private void updateSuggestionTimer(SecondaryButton button){
        focussedSuggestion = button.getText();
        Timer timer = new Timer();
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
        }, 250);

    }

    private void fillSuggestedWords(SecondaryButton button, String groupName){
        List<String> suggestedLetters = suggestionsService.getSuggestionList(button.getText());
        if(suggestedLetters.size() > 0){
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

    private void deleteButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton deleteButton = (SecondaryButton) (mouseEvent.getSource());
        startProgress(deleteButton);
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        SecondaryButton wordButton = (SecondaryButton)(mouseEvent.getSource());
        startProgress(wordButton);
    }

    private void clearAllPopupButtons(){
        clearSecundaryButtons();
        clearThirdRowButtons();
        clearForthRowButtons();
    }

    private void clearSecundaryButtons(){
        focussedGroup = "";
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
            if(button.getParentButton() != null){
                button.setPrefSize(buttonWidth, buttonHeight);
                button.setLayoutX(xCoordinateAux);
                button.setLayoutY(button.getParentButton().getLayoutY() - (button.getPrefHeight()/2) - buttonHeight);
            }
            xCoordinateAux+=buttonWidth;
        }
        xCoordinateAux = 0;
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
        double textAreaWidth = windowDimensions.getWidth()*0.6; //60% screen width
        wordsToWrite.setPrefSize(textAreaWidth, 10);
        wordsToWrite.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsToWrite.setLayoutY(10); //10px margin top
        wordsWritten.setPrefSize(textAreaWidth, 10);
        wordsWritten.setLayoutX(windowDimensions.getWidth()*0.005); //5% left margin
        wordsWritten.setLayoutY(50);
        separator.setEndX(windowDimensions.getWidth());
        int aux = 0;
        for (SecondaryButton deleteButton : deleteOptions) {
            deleteButton.setPrefSize(windowDimensions.getWidth()*0.1, windowDimensions.getHeight()*0.085);
            deleteButton.setLayoutX(aux + (windowDimensions.getWidth()*0.7));
            deleteButton.setLayoutY(windowDimensions.getHeight()*0.005);
            aux+= deleteButton.getPrefWidth() + 10;
        }
    }

    /**
     private void checkReverseCrossing(Button button){
     deleteButton.setReversing(false);
     if(openReverse!= null){
     if(openReverse.isReverseCrossing() && button.equals(openReverse.getParentButton())){
     String buttonText = button.getText();
     dataService.incrementTotalAccess();
     if(button instanceof SecundaryButton secundaryButton){
     dataService.incrementGroupAccess(secundaryButton.getGroupName());
     if(secundaryButton.getGroupName().equals(GroupNames.WORDS_ROW.getGroupName())){
     if(!secundaryButton.getText().equals("")){ //se a palavra não for string vazia
     setWrittenWordsText(buttonText, true);
     clearAllPopupButtons();
     }
     }else{
     if(buttonText.equals("SPACE")){
     setWrittenWordsText(" ", false);
     }else{ //todas as letras que não o espaço
     setWrittenWordsText(buttonText, false);
     updateSuggestedWordsOnReverseCrossing();
     }
     }
     }
     openReverse.setReverseCrossing(false);
     }
     }
     }  **/


    private void checkReverseCrossingDelete(DeleteButton button){
        if(button.isReversing()){
            if(button.getAction().equals("letter")){
                wordsWritten.setText(writtingService.deleteLetter().stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                dataService.incrementLetterDeletes();
            }else{
                wordsWritten.setText(writtingService.deleteWord().stream().map(c -> Character.toString(c)).collect(Collectors.joining()));
                dataService.incrementWordDeletes();
            }
        }
    }

    private void setWrittenWordsText(String text, boolean isWord){
        if(isWord)
            wordsWritten.setText(writtingService.addWord(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
        else
            wordsWritten.setText(writtingService.addLetters(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
    }


    private void updateSuggestedWordsOnReverseCrossing() {
        List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord());
        System.out.println(writtingService.getCurrentTypingWord());
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


    private void updateSuggestedWordsOnHover(SecondaryButton button) {
        if(!button.getText().equals("SPACE")){
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
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dataService.timerFinished(TIME);
                        Platform.runLater(() -> finished());
                    }
                }, TIME * 60 *1000);
                if(CONNECT_SERVER)
                    connections.connect("localhost", 3000);
                dataService.startTimer();
                wordsToWrite.setText(dataService.getDataset().removeFirst());
            }else if(event.getCode() == KeyCode.CONTROL && !dataService.getDataset().isEmpty() && !dataService.isFinished()){
                wordsToWrite.setText(dataService.getDataset().removeFirst());
                writtingService.nextPhrase();
            } else if(event.getCode() == KeyCode.CONTROL && dataService.getDataset().isEmpty() && !dataService.isFinished()){
                dataService.stopTimer();
                finished();
            } else if(event.getCode() == KeyCode.BACK_SPACE){
                finished();
            }
        });
    }

    private void finished() {
        if(!dataService.isSaved()){
            wordsToWrite.setText("Experiment is finished, thank you!");
            dataService.saveDataToTxt(variableGroups,"Francisco Cardoso", 22, writtingService);
        }
        if(connections.isRunning())
            connections.setRunning(false);
    }


    private void mouseMovementEvent(MouseEvent mouseEvent) {
        if(mouseEvent.getSource() instanceof Button button){
        }


        /**
         if(mouseEvent.getSource() instanceof Button button)
         mouseService.updateList(new Position2D((button.getLayoutX() + mouseEvent.getX()), (button.getLayoutY() + mouseEvent.getY())));
         else
         mouseService.updateList(new Position2D(mouseEvent.getX(), mouseEvent.getY()));
         if(mouseService.getLastMouseCoords().isFull())
         refreshNewMouse(mouseService.averagePosition());
         **/
    }

    public void setupCloneMouse(){
        //Cursor cursor = Cursor.OPEN_HAND;
        //mainScene.cursorProperty().setValue();
    }

    public void refreshNewMouse(Position2D pos) {
        new Robot().mouseMove(pos.getX(), pos.getY());
    }

    public static void main(String[] args) {
    }
}