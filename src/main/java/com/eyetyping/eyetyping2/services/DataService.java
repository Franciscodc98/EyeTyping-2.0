package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.utils.FileWriter;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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
    private int totalLetterDeletes = 0;
    private int totalWordDeletes = 0;

    private DataService(){
        loadDataset();
        for (GroupNames group :GroupNames.values())
            accessesData.put(group.getGroupName(), 0);
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

    public void incrementLetterDeletes() {
        totalLetterDeletes++;
    }

    public void incrementWordDeletes() {
        totalWordDeletes++;
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

    public void saveDataToTxt(VariableGroups layoutVariable, String userName, int age, WrittingService writtingService) {
        this.writtingService = writtingService;
        try(FileWriter fileWriter = new FileWriter("FirstBatch_" + layoutVariable + "_" + userName)){

            fileWriter.WritePhrase("User: " + userName + ", age: " + age + " years old.");
            fileWriter.WritePhrase("Total time: " + getTimeElapsed() + "ms");
            fileWriter.WritePhrase("Total words written: " + writtingService.getTotalWordsWritten());
            fileWriter.WritePhrase("Total accesses: " + totalAccesses);
            fileWriter.WritePhrase("Total deletions: " + totalLetterDeletes);
            accessesData.forEach((groupName, count) -> {
                fileWriter.WritePhrase(groupName + ": " + count);
            });
            writtingService.getWrittenPhrases().forEach(fileWriter::WritePhrase);
            saved = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void saveDataToCsv(VariableGroups layoutVariable, String userName, int age, WrittingService writtingService){
        this.writtingService = writtingService;


    }

    private String [] getCsvHeader(){
        String headerAux = "First Name, Last Name, Layout, Total Time, Total words written, Total accesses, Total deletions";
        String[] both = Arrays.copyOf(headerAux.split(", "), headerAux.split(", ").length + accessesData.keySet().toArray(new String[0]).length);
        System.arraycopy(accessesData.keySet().toArray(new String[0]), 0, both, headerAux.split(", ").length, accessesData.keySet().toArray(new String[0]).length);
        return both;
    }

    public static void main(String[] args) {
        DataService dataService = DataService.getInstance();
        Arrays.stream(dataService.getCsvHeader()).iterator().forEachRemaining(System.out::println);
    }

}