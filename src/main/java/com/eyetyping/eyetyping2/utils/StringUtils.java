package com.eyetyping.eyetyping2.utils;

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


    public static void main(String[] args) {
        int test = 8;
        for (int i = 0; i < test; i++) {
            System.out.println(getSubstringFromLetterString(test,i));
        }
    }

}