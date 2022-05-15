package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.enums.GroupNames;
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
        if(!finished){
            try(FileWriter writer = new FileWriter("src/main/resources/testResults/reverseCrossing_" + userId + ".csv", true)){
                if (writer.isFileEmpty()) {
                    writer.writeDataFromListToCsv(Arrays.stream(getCsvHeader()).toList());
                }
                writer.writeDataFromListToCsv(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                totalWordDeletes = 0;
                totalLetterDeletes = 0;
                accessesData.forEach((s, integer) -> accessesData.replace(s, 0));
            }
        }
    }

    private String [] getCsvHeader(){
        String headerAux = "Id, Technique, Phrase Number ,Given Phrase, Typed Phrase, Minimum String Distance, Error Rate, Tempo (s), Characters written (including spaces), Words Written, WPM, Deleted letters, Deleted words";
        String[] both = Arrays.copyOf(headerAux.split(", "), headerAux.split(", ").length + accessesData.keySet().toArray(new String[0]).length);
        System.arraycopy(accessesData.keySet().toArray(new String[0]), 0, both, headerAux.split(", ").length, accessesData.keySet().toArray(new String[0]).length);
        return both;
    }

    public List<String> csvLineData(String originalPhrase, String typedPhrase){
        String fixedTypedPhrase = fixTypedPhrase(typedPhrase);
        double seconds = (lastTypedTime - startTime)/1_000_000_000D;
        int words = calculateWordsTyped(fixedTypedPhrase);
        double wpm = (seconds/60) == 0 ? 0 : words/(seconds/60);
        List<String> data = new ArrayList<>();
        data.add(String.valueOf(userId));
        data.add("Reverse Crossing");
        data.add(String.valueOf(getTotalPhrasesRetrieved()));
        data.add(originalPhrase);
        data.add(fixedTypedPhrase);
        data.add(String.valueOf(calculateMSD(originalPhrase, fixedTypedPhrase)));
        data.add(String.valueOf(msdErrorRate(originalPhrase, fixedTypedPhrase)));
        data.add(String.valueOf(seconds));
        data.add(String.valueOf(fixedTypedPhrase.length()));
        data.add(String.valueOf(words));
        data.add(String.valueOf(wpm));
        data.add(String.valueOf(totalLetterDeletes));
        data.add(String.valueOf(totalWordDeletes));
        accessesData.forEach((k, v) -> data.add(String.valueOf(v)));
        return data;
    }

    private double msdErrorRate(String str1, String str2){
        int msd = calculateMSD(str1, str2);
        double max = Math.max(str1.length(), str2.length());
        return (msd/max);
    }

    private int calculateWordsTyped(String phrase){
        List<String> words = Arrays.stream(phrase.split(" ")).toList();
        if(!words.isEmpty() && !words.get(0).isEmpty())
            return words.size();
        else
            return 0;
    }

    private String fixTypedPhrase(String str){
        StringBuilder sb = new StringBuilder(str);
        if(str.length()>1 && str.charAt(str.length()-1) == ' ')
            sb.replace(str.length()-1,str.length(), "");
        return sb.toString();
    }

    private int calculateMSD(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    //complement to MSD
    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    //complement to MSD
    private int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

    public void incrementTotalPhrasesRetried() {
        totalPhrasesRetrieved++;
    }

}