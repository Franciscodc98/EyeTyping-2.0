package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.utils.FileWriter;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

@Getter
public class DataService{

    private static DataService singleton = null;
    private WrittingService writtingService;

    //Timer variables
    private long startTime;
    private long endTime;
    private long timeElapsed;
    private boolean started = false;
    private boolean finished = false;

    private boolean saved = false;

    //Dataset variables
    private final LinkedList<String> dataset = new LinkedList<>();
    private final HashMap<String, Integer> accessesData = new HashMap<>();

    private int totalAccesses = 0;
    private int totalDeletes = 0;

    private DataService(){
        loadDataset();
        for (GroupNames group :GroupNames.values())
            accessesData.put(group.getGroupName(), 0);
        accessesData.forEach((s, integer) -> System.out.println(s + "|" + integer));
    }

    private void loadDataset() {
        File datasetTxt = new File(GlobalVariables.PHRASES_PATH);
        try {
            Scanner reader = new Scanner(datasetTxt);
            while (reader.hasNext()){
                String line = reader.nextLine().toUpperCase();
                dataset.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }




    public void incrementGroupAccess(String groupName){
        accessesData.put(groupName, accessesData.get(groupName) + 1);
    }

    public void decrementGroupAccess(String groupName){
        accessesData.put(groupName, accessesData.get(groupName) - 1);
    }

    public void incrementTotalAccess(){
        totalAccesses++;
    }

    public void incrementDeletes() {
        totalDeletes++;
    }

    public void startTimer(){
        startTime = System.nanoTime();
        started = true;
    }

    public void stopTimer(){
        endTime = System.nanoTime();
        timeElapsed = (endTime - startTime) / 1_000_000;
        started = false;
        finished = true;
    }

    public void timerFinished(int time){
        timeElapsed = (long) time * 60 * 1000;
        started = false;
        finished = true;
    }

    public static DataService getInstance(){
        if(singleton == null)
            singleton = new DataService();
        return singleton;
    }

    public static void main(String[] args) {
    }

    public void saveData(VariableGroups layoutVariable, String userName, int age, WrittingService writtingService) {
        this.writtingService = writtingService;
        try(FileWriter fileWriter = new FileWriter("FirstBatch_" + layoutVariable + "_" + userName);){

            fileWriter.WritePhrase("User: " + userName + ", age: " + age + " years old.");
            fileWriter.WritePhrase("Variable: " + layoutVariable.getVariableGroupName());
            fileWriter.WritePhrase("Total time: " + getTimeElapsed() + "ms");
            fileWriter.WritePhrase("Total words written: " + writtingService.getTotalWordsWritten());
            fileWriter.WritePhrase("Total accesses: " + totalAccesses);
            fileWriter.WritePhrase("Total deletions: " + totalDeletes);
            accessesData.forEach((groupName, count) -> {
                fileWriter.WritePhrase(groupName + ": " + count);
            });
            writtingService.getWrittenPhrases().forEach(fileWriter::WritePhrase);
            saved = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}