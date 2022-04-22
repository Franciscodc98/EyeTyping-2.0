package com.eyetyping.eyetyping2.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String getSubstringFromLetterString(int numberOfGroups, int groupIndex){
        int groupSize = GlobalVariables.ALPHABET_LETTERS.length() / numberOfGroups;
        if(GlobalVariables.ALPHABET_LETTERS.length() % numberOfGroups == 0){
            int index =groupSize*groupIndex;
            return GlobalVariables.ALPHABET_LETTERS.substring(index, index + groupSize);
        }else {
            switch (numberOfGroups){
                case 3: switch (groupIndex){
                    case 0: return "ABCDEFGHI";
                    case 1: return "JKLMNOPQR";
                    case 2: return "STUVWXYZ";
                }
                case 4: switch (groupIndex){
                    case 0: return "ABCDEFG";
                    case 1: return "HIJKLMN";
                    case 2: return "OPQRST";
                    case 3: return "UVWXYZ";
                }
                case 5: switch (groupIndex){
                    case 0: return "ABCDEF";
                    case 1: return "GHIJK";
                    case 2: return "LMNOP";
                    case 3: return "QRSTU";
                    case 4: return "VWXYZ";
                }
                case 6: switch (groupIndex){
                    case 0: return "ABCDE";
                    case 1: return "FGHIJ";
                    case 2: return "KLMN";
                    case 3: return "OPQR";
                    case 4: return "STUV";
                    case 5: return "WXYZ";
                }
                case 7: switch (groupIndex){
                    case 0: return "ABCD";
                    case 1: return "EFGH";
                    case 2: return "IJKL";
                    case 3: return "MNOP";
                    case 4: return "QRST";
                    case 5: return "UVW";
                    case 6: return "XYZ";
                }

            }

        }
        return "Shouldn't come here";
    }

    public static String getSubstringFromLetterString2(int numberOfGroups, int groupIndex){
        int groupSize = GlobalVariables.ALPHABET_LETTERS.length() / numberOfGroups;
        if(GlobalVariables.ALPHABET_LETTERS.length() % numberOfGroups == 0){
            int index =groupSize*groupIndex;
            return GlobalVariables.ALPHABET_LETTERS_EXTENDED.substring(index, index + groupSize);
        }else {
            switch (numberOfGroups){
                case 4: switch (groupIndex){
                    case 0: return "ABCDEFGHI";
                    case 1: return "JKLMNOPQR";
                    case 2: return "STUVWXYZ";
                    case 4: return "!?.,";
                }
                case 5: switch (groupIndex){
                    case 0: return "ABCDEFG";
                    case 1: return "HIJKLMN";
                    case 2: return "OPQRST";
                    case 3: return "UVWXYZ";
                    case 4: return "!?.,";
                }
                case 6: switch (groupIndex){
                    case 0: return "ABCDEF";
                    case 1: return "GHIJK";
                    case 2: return "LMNOP";
                    case 3: return "QRSTU";
                    case 4: return "VWXYZ";
                    case 5: return "!?.,";
                }
                case 7: switch (groupIndex){
                    case 0: return "ABCDE";
                    case 1: return "FGHIJ";
                    case 2: return "KLMN";
                    case 3: return "OPQR";
                    case 4: return "STUV";
                    case 5: return "WXYZ";
                    case 6: return "!?.,";
                }
                case 8: switch (groupIndex){
                    case 0: return "ABCD";
                    case 1: return "EFGH";
                    case 2: return "IJKL";
                    case 3: return "MNOP";
                    case 4: return "QRST";
                    case 5: return "UVW";
                    case 6: return "XYZ";
                }

            }

        }
        return "Shouldn't come here";
    }

    //ignore
    public static void fixWordList(String url, int column){
        List<String> words = new ArrayList<>();
        List<String> fixed = new ArrayList<>();
        try {
            words = Files.readAllLines(Paths.get("src/main/resources/en_words_1_1-12.txt"));
            words.forEach(s -> {
                String [] aux = s.split(" ");
                fixed.add(aux[0]);
            });
            fixed.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        fixWordList("", 5);
    }

}