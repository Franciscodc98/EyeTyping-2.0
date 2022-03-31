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
    private String focussedSuggestion = "";
    private DeleteButton deleteButton;
    private ArrayList<ReverseCrossingDelete> deleteOptions = new ArrayList<ReverseCrossingDelete>();

    private Scene mainScene = null;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    //TextArea Variables
    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;

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
        for (SecundaryButton button : suggestedWordsButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setLayoutX(xCoordinateAux);
            button.setLayoutY(separator.getStartY());
            button.setOnMouseEntered(this::suggestedWordButtonsEnterEvent);
            button.setOnMouseMoved(this::mouseMovementEvent);
            xCoordinateAux+=buttonWidth;
        }
        rootAnchor.getChildren().addAll(suggestedWordsButtons);
    }

    private void setupThirdRowButtons(){
        thirdRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.THIRD_ROW);
        for (SecundaryButton button : thirdRowButtons) {
            button.setPrefSize(buttonWidth, buttonHeight);
            button.setOnMouseEntered(this::thirdRowButtonsEnterEvent);
            button.setOnMouseMoved(this::mouseMovementEvent);
            button.setOnMouseExited(this::thirdButtonsExitEvent);
        }
    }

    private void setupFourthRowButtons(){
        forthRowButtons = ButtonsUtils.createThirdRowButtons(TOTAL_GROUPS, GroupNames.FOURTH_ROW);
        for (SecundaryButton button : forthRowButtons) {
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
        deleteButton = new DeleteButton();
        deleteButton.setOnMouseEntered(this::deleteButtonEnterEvent);
        deleteButton.setOnMouseMoved(this::mouseMovementEvent);
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
        removeDeleteOptionsReverseCrossing();
        updateSuggestionTimer(secundaryButton);
        checkReverseCrossing(secundaryButton);
        clearThirdRowButtons();
        clearForthRowButtons();
        if(focussedButtonForReverse != secundaryButton){
            focussedButtonForReverse = secundaryButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(secundaryButton.getText(), secundaryButton, this::mouseMovementEvent);
            rootAnchor.getChildren().add(openReverse);
            if(!secundaryButton.getText().equals("SPACE"))
                fillSuggestedWords(secundaryButton, GroupNames.THIRD_ROW.getGroupName());
        }
    }

    private void thirdRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton thirdRowButton = (SecundaryButton)(mouseEvent.getSource());
        removeDeleteOptionsReverseCrossing();
        updateSuggestionTimer(thirdRowButton);
        checkReverseCrossing(thirdRowButton);
        clearForthRowButtons();
        if(focussedButtonForReverse != thirdRowButton){
            focussedButtonForReverse = thirdRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(thirdRowButton.getText(), thirdRowButton, this::mouseMovementEvent);
            rootAnchor.getChildren().add(openReverse);
            fillSuggestedWords(thirdRowButton, GroupNames.FOURTH_ROW.getGroupName());
        }
    }

    private void fourthRowButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton fourthRowButton = (SecundaryButton)(mouseEvent.getSource());
        removeDeleteOptionsReverseCrossing();
        updateSuggestionTimer(fourthRowButton);
        checkReverseCrossing(fourthRowButton);
        if(focussedButtonForReverse != fourthRowButton){
            focussedButtonForReverse = fourthRowButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(fourthRowButton.getText(), fourthRowButton, this::mouseMovementEvent);
            rootAnchor.getChildren().add(openReverse);
        }
    }

    private void secundaryButtonsExitEvent(MouseEvent mouseEvent){
        focussedSuggestion = "";
    }

    private void thirdButtonsExitEvent(MouseEvent mouseEvent){
        focussedSuggestion = "";
    }

    private void fourthButtonsExitEvent(MouseEvent mouseEvent){
        focussedSuggestion = "";
    }

    private void removeDeleteOptionsReverseCrossing(){
        if(rootAnchor.getChildren().containsAll(deleteOptions)){
            rootAnchor.getChildren().removeAll(deleteOptions);
        }
    }

    private void updateSuggestionTimer(Button button){
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

    private void deleteButtonEnterEvent(MouseEvent mouseEvent){
        DeleteButton deleteButton = (DeleteButton)(mouseEvent.getSource());
        if(deleteOptions.isEmpty()){
            deleteOptions.add(new ReverseCrossingDelete(deleteButton, this::mouseMovementEvent, false));
            deleteOptions.add(new ReverseCrossingDelete(deleteButton, this::mouseMovementEvent, true));
        }
        //checkReverseCrossing(deleteButton);
        checkReverseCrossingDelete(deleteButton);
        if(focussedButtonForReverse != deleteButton) {
            focussedButtonForReverse = deleteButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            rootAnchor.getChildren().addAll(deleteOptions);
        }
    }

    private void suggestedWordButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton wordButton = (SecundaryButton)(mouseEvent.getSource());
        removeDeleteOptionsReverseCrossing();
        checkReverseCrossing(wordButton);
        if(focussedButtonForReverse != wordButton) {
            focussedButtonForReverse = wordButton;
            if (openReverse != null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(wordButton.getText(), wordButton, this::mouseMovementEvent);
            rootAnchor.getChildren().add(openReverse);
        }
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
        for (ReverseCrossingDelete button : deleteOptions)
            button.refreshSize();


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
        deleteButton.setPrefSize(windowDimensions.getWidth()*0.1, windowDimensions.getHeight()*0.085);
        deleteButton.setLayoutX(windowDimensions.getWidth()*0.75);
        deleteButton.setLayoutY(windowDimensions.getHeight()*0.005);

    }

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
    }

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

    private void setWrittenWordsText(String text, boolean word){
        if(word)
            wordsWritten.setText(writtingService.addWord(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
        else
            wordsWritten.setText(writtingService.addLetters(text).stream().map(c -> Character.toString(c)).collect(Collectors.joining())+"|");
    }

    private void updateSuggestedWordsOnReverseCrossing() {
        List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord());
        System.out.println(writtingService.getCurrentTypingWord());
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

    private void updateSuggestedWordsOnHover(Button button) {
        if(!button.getText().equals("SPACE")){
            List<String> suggestedWords = suggestionsService.getSuggestionList(writtingService.getCurrentTypingWord() + button.getText());
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