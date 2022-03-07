package com.eyetyping.eyetyping2.controllers;

import com.eyetyping.eyetyping2.customComponets.*;
import com.eyetyping.eyetyping2.utils.ButtonsUtils;
import com.eyetyping.eyetyping2.utils.WindowDimensions;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class KeyboardController implements Initializable {

    public static final int TOTAL_GROUPS = 6;
    private List<GroupButton> groupsButtonList = new ArrayList<>();
    private final HashMap<String, SecundaryButton> alphabetButtons = new HashMap<>();
    private final List<SecundaryButton> recentSecondaryRowButtons = new ArrayList<>();

    private final WindowDimensions windowDimensions = new WindowDimensions();
    private double screenWidth;
    private double screenHeight;
    private double buttonWidth;
    private double buttonHeight;

    private String focussedGroup = "";
    private SecundaryButton focussedSecundary;
    private ReverseCrossingButtons openReverse;

    @FXML
    private AnchorPane rootAnchor;
    private Line separator;

    private DisplayTextLabel wordsToWrite;
    private TextWrittenLabel wordsWritten;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addRootAnchorListeners();
        groupsButtonList = ButtonsUtils.createGroupButtons(TOTAL_GROUPS);
        groupsButtonList.forEach((button -> alphabetButtons.putAll(ButtonsUtils.createAlphabetButtons(button.getText(), button, this::secundaryButtonsEnterEvent, this::secundaryButtonsExitEvent))));
        setupInitialButtons();
        setupTextArea();
    }

    private void setupInitialButtons() {
        int widthAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(widthAux);
            button.setLayoutY(screenHeight-buttonHeight);
            button.setOnMouseEntered(this::groupsButtonEnterEvent);
            button.setOnMouseExited(this::groupsButtonExitEvent);
            button.setOnMouseClicked((event -> wordsToWrite.displayPhrase()));
            widthAux+=buttonWidth;
            rootAnchor.getChildren().add(button);
        }
    }

    private void setupTextArea(){
        separator = new Line(0,85, windowDimensions.getWidth(), 85);
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
        if(!focussedGroup.equals(groupString)){
            focussedButton.setFocussed(true);
            focussedButton.updateFeedback();
            if(!recentSecondaryRowButtons.isEmpty())
                clearSecundaryButtons();
            for (char c: groupString.toCharArray()) {
                recentSecondaryRowButtons.add(alphabetButtons.get(Character.toString(c)));
            }
            int widthAux = 0;
            double alphabetButtonWidth = screenWidth/recentSecondaryRowButtons.size();
            for (Button button: recentSecondaryRowButtons) {
                button.setPrefSize(alphabetButtonWidth, buttonHeight);
                button.setLayoutX(widthAux);
                button.setLayoutY(screenHeight- (2*buttonHeight));
                rootAnchor.getChildren().add(button);
                widthAux+=alphabetButtonWidth;
            }
            focussedGroup = groupString;
        }
    }

    private void groupsButtonExitEvent(MouseEvent mouseEvent) {
        GroupButton focussedButton = (GroupButton) mouseEvent.getSource();
        String groupString = focussedButton.getText();
        focussedButton.setFocussed(false);
        focussedButton.updateFeedback();
        if(!focussedGroup.equals(groupString)){
            clearSecundaryButtons();
        }

    }

    private void secundaryButtonsEnterEvent(MouseEvent mouseEvent){
        SecundaryButton secundaryButton = (SecundaryButton)(mouseEvent.getSource());
        if(focussedSecundary!= secundaryButton){
            focussedSecundary = secundaryButton;
            if(openReverse!=null)
                rootAnchor.getChildren().remove(openReverse);
            openReverse = new ReverseCrossingButtons(secundaryButton.getText(), secundaryButton);
            rootAnchor.getChildren().add(openReverse);

        }
    }
    private void secundaryButtonsExitEvent(MouseEvent mouseEvent){
        /* SecundaryButton secundaryButton = (SecundaryButton)(mouseEvent.getSource());
        ReverseCrossingButtons reverseCrossingButton = null;
        for (ReverseCrossingButtons r: reverseCrossingButtonsList) {
            if(r.getText().equals(secundaryButton.getText())){
                reverseCrossingButton = r;
                break;
            }
        }
        if(reverseCrossingButton!=null){
            reverseCrossingButtonsList.remove(reverseCrossingButton);
            rootAnchor.getChildren().remove(reverseCrossingButton);
        } */
    }

    private void clearSecundaryButtons(){
        recentSecondaryRowButtons.forEach((button -> rootAnchor.getChildren().remove(button)));
        recentSecondaryRowButtons.clear();
    }

    private void resizeButtons(){
        int widthAux = 0;
        for (Button button : groupsButtonList) {
            button.setPrefSize(buttonWidth,buttonHeight);
            button.setLayoutX(widthAux);
            button.setLayoutY(screenHeight-buttonHeight);
            widthAux+=buttonWidth;
        }
        alphabetButtons.forEach((key, value) -> value.setPrefSize(buttonWidth,buttonHeight));

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