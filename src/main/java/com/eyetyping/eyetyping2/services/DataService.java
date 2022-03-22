package com.eyetyping.eyetyping2.services;

import com.eyetyping.eyetyping2.enums.VariableGroups;
import com.eyetyping.eyetyping2.utils.GlobalVariables;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

@Getter
public class DataService{

    private static DataService singleton = null;
    //Timer variables
    private long startTime;
    private long endTime;
    private boolean started = false;

    //Dataset variables
    LinkedList<String> dataset = new LinkedList<>();

    //Results variables


    private DataService(){
        loadDataset();
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

    public void startTimer(){
        System.out.println("Timer started");
        startTime = System.nanoTime();
        started = true;
    }

    public void stopTimer(){
        System.out.println("Timer stopped");
        endTime = System.nanoTime();
        started = false;
    }

    /**
     *
     * @return time elapsed in milissecons
     */
    public long getTimeElapsed(){
        if (endTime != 0 && startTime != 0)
            return (endTime - startTime) / 1000000;
        else
            return 0;
    }


    public static DataService getInstance(){
        if(singleton == null)
            singleton = new DataService();
        return singleton;
    }

    public static void main(String[] args) {
    }

    public void saveData(VariableGroups layoutVariable, String userName, int age) {

    }
}