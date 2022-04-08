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

    //Timer variables
    private long startTime;
    private long endTime;
    private long timeElapsed;
    private boolean started = false;
    private boolean finished = false;

    private boolean savedTxt = false;
    private boolean savedCsv = false;

    //Dataset variables
    private final LinkedList<String> dataset = new LinkedList<>();
    private final LinkedList<String> orderedPhrasesUsed = new LinkedList<>();
    private final LinkedHashMap<String, Integer> accessesData = new LinkedHashMap<>();

    private int totalLetterDeletes = 0;
    private int totalWordDeletes = 0;

    private int totalPhrasesRetrieved = 0;

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

    public String getPhraseFromDataset(){
        Random r = new Random();
        if(!dataset.isEmpty()) {
            String phrase = dataset.remove(r.nextInt(dataset.size()));
            orderedPhrasesUsed.add(phrase);
            totalPhrasesRetrieved+=1;
            return phrase;
        }
        throw new NoSuchElementException("Phrase dataset is empty");
    }

    public void incrementGroupAccess(String groupName){
        accessesData.put(groupName, accessesData.get(groupName) + 1);
        System.out.println("Incremented: " + groupName);
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
        try(FileWriter fileWriter = new FileWriter("FirstBatch_" + layoutVariable + "_" + userName)){

            fileWriter.writePhrase("User: " + userName + ", age: " + age + " years old.");
            fileWriter.writePhrase("Total time: " + getTimeElapsed() + "ms");
            fileWriter.writePhrase("Total words written: " + writtingService.getTotalWordsWritten());
            fileWriter.writePhrase("Total words deletions: " + totalWordDeletes);
            fileWriter.writePhrase("Total letter deletions: " + totalLetterDeletes);
            accessesData.forEach((groupName, count) -> {
                fileWriter.writePhrase(groupName + ": " + count);
            });
            List<String> writtenPhrases = writtingService.getWrittenPhrases();
            for(int i = 0; i < writtenPhrases.size(); i++) {
                fileWriter.writePhrase(orderedPhrasesUsed.get(i));
                fileWriter.writePhrase(writtenPhrases.get(i));
            }
            //writtingService.getWrittenPhrases().forEach(fileWriter::writePhrase);
            savedTxt = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void saveDataToCsv(VariableGroups layoutVariable, String userName, int age, WrittingService writtingService){
        try(FileWriter writer = new FileWriter("src/main/resources/ReverseCrossingData.csv")){
            if(writer.isFileEmpty()){
                writer.writeDataFromListToCsv(Arrays.stream(getCsvHeader()).toList());
            }
            writer.writeDataFromListToCsv(dataToSave(layoutVariable, userName, age, writtingService));
            savedCsv = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private String [] getCsvHeader(){
        String headerAux = "First Name, Age, Layout, Total Time(ms), Total words written, Total word deletions, Total letter deletions";
        String[] both = Arrays.copyOf(headerAux.split(", "), headerAux.split(", ").length + accessesData.keySet().toArray(new String[0]).length);
        System.arraycopy(accessesData.keySet().toArray(new String[0]), 0, both, headerAux.split(", ").length, accessesData.keySet().toArray(new String[0]).length);
        return both;
    }

    private List<String> dataToSave(VariableGroups layoutVariable, String userName, int age, WrittingService writtingService){
        List<String> data = new ArrayList<>();
        data.add(userName);
        data.add(String.valueOf(age));
        data.add(layoutVariable.getVariableGroupName());
        data.add(String.valueOf(getTimeElapsed()));
        data.add(String.valueOf(writtingService.getTotalWordsWritten()));
        data.add(String.valueOf(totalWordDeletes));
        data.add(String.valueOf(totalLetterDeletes));
        accessesData.forEach((k, v) -> data.add(String.valueOf(v)));
        return data;
    }

    public static void main(String[] args) {
        DataService dataService = DataService.getInstance();
        Arrays.stream(dataService.getCsvHeader()).iterator().forEachRemaining(System.out::println);
    }



}