package com.eyetyping.eyetyping2.utils;

import java.util.Arrays;
import java.util.List;

public class DataUtils {

    private DataUtils(){}

    //Entry speed

    public static double wordsPerMinute(int phraseLength, double timeInSeconds){
        return ((Math.abs(phraseLength) - 1) / timeInSeconds) * 60 * (1 / 5D);
    }

    /**
     *
     * @param wordsPerMinute words per minute
     * @param errorRateMSD error rate from MSD ranging from 0.0 to 1.0
     * @return adjusted words per minute
     */
    public static double adjustedWordsPerMinute(double wordsPerMinute, double errorRateMSD){
        return (wordsPerMinute * Math.pow((1 - errorRateMSD),1));
    }

    public static double keystrokesPerSecond(int keystrokes, double timeInSeconds){
        return (Math.abs(keystrokes)-1)/timeInSeconds;
    }




    //Error rate

    public static double keystrokesPerCharacter(int keystrokes, int numberOfCharacters){
        return ((double)Math.abs(keystrokes)/Math.abs(numberOfCharacters));
    }

    public static double msdErrorRate(String str1, String str2){
        return calculateMSD(str1, str2)/(double)Math.max(str1.length(), str2.length());
    }

    public static int calculateWordsTyped(String phrase){
        List<String> words = Arrays.stream(phrase.split(" ")).toList();
        if(!words.isEmpty() && !words.get(0).isEmpty())
            return words.size();
        else
            return 0;
    }

    public static int calculateMSD(String x, String y) {
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
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    //complement to MSD
    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }


    public static void main(String[] args) {
        System.out.println(wordsPerMinute(1 + 5*5,60));
    }

}