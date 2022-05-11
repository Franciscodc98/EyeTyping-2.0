package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.enums.GroupNames;
import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.utils.FileWriter;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Getter
@Setter
public class DataService{

    private static DataService singleton = null;
    private final Random random = new Random();

    private int userId;

    //Timer variables
    private long startTime;
    private long lastTypedTime;
    private boolean started = false;
    private boolean paused = true;
    private boolean finished = false;

    //Dataset variables
    private final LinkedList<String> dataset = new LinkedList<>();
    private final LinkedList<String> orderedPhrasesUsed = new LinkedList<>();
    private final LinkedHashMap<String, Integer> accessesData = new LinkedHashMap<>();

    private int totalLetterDeletes = 0;
    private int totalWordDeletes = 0;

    private int totalPhrasesRetrieved = 0;

    private DataService(){
        loadUserId();
        loadDataset();
        for (GroupNames group :GroupNames.values())
            accessesData.put(group.getGroupName(), 0);
    }

    private void loadUserId(){
        File datasetTxt = new File(GlobalVariables.USER_ID_PATH);
        try(Scanner reader = new Scanner(datasetTxt)) {
            while (reader.hasNext())
                userId = Integer.parseInt(reader.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void incrementUserId(){
        try(FileWriter fileWriter = new FileWriter(GlobalVariables.USER_ID_PATH, false)){
            fileWriter.writePhrase(userId + 1 + "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadDataset() {
        File datasetTxt = new File(GlobalVariables.PHRASES_PATH);
        try(Scanner reader = new Scanner(datasetTxt)) {
            while (reader.hasNext()){
                String line = reader.nextLine().toUpperCase();
                dataset.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getPhraseFromDataset(){
        if(!dataset.isEmpty()) {
            String phrase = dataset.remove(random.nextInt(dataset.size()));
            orderedPhrasesUsed.add(phrase);
            totalPhrasesRetrieved+=1;
            return phrase;
        }
        throw new NoSuchElementException("Phrase dataset is empty");
    }

    public void incrementGroupAccess(String groupName){
        accessesData.put(groupName, accessesData.get(groupName) + 1);
    }

    public void incrementLetterDeletes() {
        totalLetterDeletes++;
    }

    public void incrementWordDeletes() {
        totalWordDeletes++;
    }

    public void startTimer(){
        startTime = System.nanoTime();
        lastTypedTime = startTime;
        started = true;
    }

    public void lastTypedTime(){
        lastTypedTime = System.nanoTime();
    }

    public static DataService getInstance(){
        if(singleton == null)
            singleton = new DataService();
        return singleton;
    }


    public void saveDataToCsv(List<String> data){
        try(FileWriter writer = new FileWriter("src/main/resources/testResults/dwellTime" + userId + ".csv", true)){
            if (writer.isFileEmpty())
                writer.writeDataFromListToCsv(Arrays.stream(getCsvHeader()).toList());
            writer.writeDataFromListToCsv(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            totalWordDeletes = 0;
            totalLetterDeletes = 0;
            accessesData.forEach((s, integer) -> accessesData.replace(s, 0));
        }
    }

    private String [] getCsvHeader(){
        String headerAux = "Id, Given Phrase, Typed Phrase, Error metric 1, Error metric 2, Tempo (s), Characters written (including spaces), Words Written, WPM, Deleted letters, Deleted words";
        String[] both = Arrays.copyOf(headerAux.split(", "), headerAux.split(", ").length + accessesData.keySet().toArray(new String[0]).length);
        System.arraycopy(accessesData.keySet().toArray(new String[0]), 0, both, headerAux.split(", ").length, accessesData.keySet().toArray(new String[0]).length);
        return both;
    }

    public List<String> csvLineData(String originalPhrase, String typedPhrase){
        double seconds = (lastTypedTime - startTime)/1_000_000_000D;
        int words = calculateWordsTyped(typedPhrase);
        double wpm = (seconds/60) == 0 ? 0 : words/(seconds/60);
        List<String> data = new ArrayList<>();
        data.add(String.valueOf(userId));
        data.add(originalPhrase);
        data.add(typedPhrase);
        data.add(String.valueOf(0)); //Error metric 1
        data.add(String.valueOf(0)); //Error metric 2
        data.add(String.valueOf(seconds));
        data.add(String.valueOf(typedPhrase.length()));
        data.add(String.valueOf(words));
        data.add(String.valueOf(wpm));
        data.add(String.valueOf(totalLetterDeletes));
        data.add(String.valueOf(totalWordDeletes));
        accessesData.forEach((k, v) -> data.add(String.valueOf(v)));
        return data;
    }

    private int calculateWordsTyped(String phrase){
        List<String> words = Arrays.stream(phrase.split(" ")).toList();
        if(!words.isEmpty() && !words.get(0).isEmpty())
            return words.size();
        else
            return 0;
    }

    public static void main(String[] args) {
        DataService dataService = DataService.getInstance();
        //Arrays.stream(dataService.getCsvHeader()).iterator().forEachRemaining(System.out::println);
        System.out.println(dataService.getUserId());
    }



}