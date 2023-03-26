import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args){
        // Project resources path
        String file = "src\\game_data.txt";
        // Array list with all the lines (turns)
        ArrayList<GameSessionTurn> ArrayGST = readInputFile(file);   // GST stands for Game Session Turn
        // Final array list with all the faults
        ArrayList<GameSessionTurn> WrongGST = checkIntegers(ArrayGST);

        checkHands(ArrayGST, WrongGST);
        checkActions(ArrayGST, WrongGST);

        sortArrayList(WrongGST);
        writeOutputFile(WrongGST);
    }

    // None of the integer values (timestamp, game session ID, player ID) can be negative
    // It can be 0 theoretically, but none of the given input files contain a 0 in these values
    public static ArrayList<GameSessionTurn> checkIntegers(ArrayList<GameSessionTurn> ArrayList){
        ArrayList<GameSessionTurn> WrongGST = new ArrayList<>();
        for (GameSessionTurn GST: ArrayList){
            if(GST.gameSessionID < 1 || GST.timestamp < 1 || GST.playerID < 1){
                addNewFaultyTurn(WrongGST, GST);
            }
        }
        return WrongGST;
    }

    // Check if all cards standards were satisfied
    public static void checkHands(ArrayList<GameSessionTurn> ArrayList, ArrayList<GameSessionTurn> WrongGST) {

        for (GameSessionTurn GST: ArrayList){
            if(twoStringsDuplicates(splitString(GST.dealerHand), splitString(GST.playerHand))
                    || checkCardStandard(splitString(GST.dealerHand))
                    || checkCardStandard(splitString(GST.playerHand))
                    || !checkPlayerHands(splitString(GST.playerHand))) {

                addNewFaultyTurn(WrongGST, GST);
            }
        }
    }

    // Check if an action is correct
    public static void checkActions(ArrayList<GameSessionTurn> ArrayList, ArrayList<GameSessionTurn> WrongGST){

        // In this method it is needed to check a property of the next object, so GSTNext was created
        for (int i = 0; i < ArrayList.size(); i++) {
            GameSessionTurn GST = ArrayList.get(i);
            GameSessionTurn GSTNext = null;
            if (i + 1 < ArrayList.size()) {
                GSTNext = ArrayList.get(i + 1);
            }

            // Total amount of points for every participant
            int dealerHand = handsTotal(splitString(GST.dealerHand));
            int playerHand = handsTotal(splitString(GST.playerHand));

            // Current action
            switch (GST.action) {
                case "P Win" -> {
                    // If the requirements of player's win were not satisfied
                    if (!((playerHand > dealerHand && playerHand <= 21 && dealerHand >= 17)
                            || (playerHand == dealerHand) || (dealerHand > 21 && playerHand <= 21))) {
                        addNewFaultyTurn(WrongGST, GST);
                    }
                }
                case "P Lose" -> {
                    // If the requirements of dealer's win were not satisfied
                    if (!((dealerHand > playerHand && dealerHand <= 21) //P Lose
                            || (dealerHand >= 17 && dealerHand <= 21 && playerHand > 21))) {
                        addNewFaultyTurn(WrongGST, GST);
                    }
                }
                case "P Hit" -> {
                    // If in the next game session turn, the new card was not added to the player
                    if ((playerHand > 21) || (GSTNext != null && Objects.equals(GST.playerHand, GSTNext.playerHand))) {
                        addNewFaultyTurn(WrongGST, GST);
                    }
                }
                case "D Hit" -> {
                    // If in the next game session turn, the new card was not added to the dealer
                    if ((dealerHand >= 17) || (GSTNext != null && Objects.equals(GST.dealerHand, GSTNext.dealerHand))) {
                        addNewFaultyTurn(WrongGST, GST);
                    }
                }
            }
        }
    }

    // Add a line with a fault to a final array list
    public static void addNewFaultyTurn(ArrayList<GameSessionTurn> WrongGST, GameSessionTurn GST ){
        boolean isAdded = false;
        for (int i = 0; i < WrongGST.size(); i++) {
            GameSessionTurn existingGameSessionTurn = WrongGST.get(i);
            // If an object with a certain gameSessionID exists
            if (existingGameSessionTurn.getGameSessionID() == GST.getGameSessionID()) {
                // If timestamp of a new object is lesser
                // (The logic: the first faulty turn in a game session is preferred,
                // because after that, all the next moves can be incorrect)
                if (GST.getTimestamp() < existingGameSessionTurn.getTimestamp()) {
                    WrongGST.set(i, GST);
                }
                isAdded = true;
                break;
            }
        }
        // If an object with a certain gameSessionID does not exist
        if (!isAdded) {
            WrongGST.add(GST);
        }

    }

    // Convert a string "xx-yy-zz" to an array of strings "xx", "yy" and "zz"
    public static String[] splitString(String input) {
        String[] parts = input.split("-");
        String[] result = new String[parts.length];
        System.arraycopy(parts, 0, result, 0, parts.length);
        return result;
    }

    // Check if two string arrays contain duplicates
    public static boolean twoStringsDuplicates(String[] arr1, String[] arr2) {

        if(oneStringDuplicates(arr1) || oneStringDuplicates(arr2)){
            return true;
        }
        for (String str1 : arr1) {
            for (String str2 : arr2) {
                if (str1.equalsIgnoreCase(str2)) {
                    // If duplicate found, return true
                    return true;
                }
            }
        }

        // No duplicates found
        return false;
    }

    // Check if one string array contains duplicates
    public static boolean oneStringDuplicates(String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].equalsIgnoreCase(arr[j])) {
                    // If duplicate found, return true
                    return true;
                }
            }
        }
        // No duplicates found
        return false;
    }

    // Check if card fits the standard
    public static boolean checkCardStandard(String[] cards){
        for (String s : cards) {
            int length = s.length();

            // Any card must not be larger than 3 symbols
            // Only "?" (Unknown card) can be 1 symbol large
            // Only "10" card can be 3 symbols large
            if ((length > 3) || (length < 2 && !s.startsWith("?")) || (length == 3 && !s.startsWith("10"))) {
                return true;
            }


            if (length == 2 || length == 3) {
                char lastChar = s.toUpperCase().charAt(length - 1);
                switch (lastChar) {
                    case 'S':  // Spade
                    case 'C':  // Clubs
                    case 'H':  // Heart
                    case 'D':  // Diamond
                        // Do nothing, valid case
                        break;
                    default:
                        // Invalid case
                        return true;
                }
                char firstChar = s.toUpperCase().charAt(0);
                switch (firstChar) {
                    case '1':
                        if (s.charAt(1) != '0')
                            return true;
                        break;
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'J':
                    case 'Q':
                    case 'K':
                    case 'A':
                        // Do nothing, valid case
                        break;
                    default:
                        // Invalid case
                        return true;
                }
            }
        }
        return false;
    }

    // Player hand cannot contain "?" (Unknown card)
    public static boolean checkPlayerHands(String[] arr){
        for (String s : arr) {
            if (s.startsWith("?")) {
                return false;
            }
        }
        return true;
    }

    // Count the result of a participant's hand
    public static int handsTotal(String[] cards){
        int result = 0;

        for(String card : cards){
            switch(card.toUpperCase().charAt(0)){
                case '?':
                    break;
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    result += Character.getNumericValue(card.charAt(0));
                    break;
                case 'J':
                case 'Q':
                case 'K':
                    result += 10;
                    break;
                case 'A':
                    result += 11;
                    break;
                case '1':
                    if (card.charAt(1) == '0')
                        result += 10;
                    break;
            }
        }

        return result;
    }

    // Sort array list by gameSessionID property
    public static void sortArrayList(ArrayList<GameSessionTurn> ArrayList){
        Comparator<GameSessionTurn> comparator = new Comparator<>() {
            public int compare(GameSessionTurn g1, GameSessionTurn g2) {
                // Compare by gameSessionID
                int result = Integer.compare(g1.getGameSessionID(), g2.getGameSessionID());
                if (result == 0) {
                    // If gameSessionID is the same, compare by timestamp
                    result = Integer.compare(g1.getTimestamp(), g2.getTimestamp());
                }
                return result;
            }
        };

        ArrayList.sort(comparator);

    }

    // Reads input file to an array list
    public static ArrayList<GameSessionTurn> readInputFile(String fileName) {
        ArrayList<GameSessionTurn> ArrayList = new ArrayList<>();
        File file = new File(fileName);
        if(file.exists()){
            try {

                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line = reader.readLine();

                while(line != null) {
                    String[] splitLine = line.split(",");
                    if(splitLine.length == 6) {
                        try {
                            int timestamp = Integer.parseInt(splitLine[0]);
                            int gameSessionID = Integer.parseInt(splitLine[1]);
                            int playerID = Integer.parseInt(splitLine[2]);
                            String action = splitLine[3];
                            String dealerHand = splitLine[4];
                            String playerHand = splitLine[5];
                            GameSessionTurn GST = new GameSessionTurn(timestamp, gameSessionID, playerID, action, dealerHand, playerHand);
                            ArrayList.add(GST);
                        } catch(NumberFormatException e) {
                            // Ignore lines that are not in the correct format
                        }
                    }
                    // Next line
                    line = reader.readLine();
                }
                reader.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("File was not found.");
        }

        return ArrayList;
    }

    // Writes a final array list to an output file
    public static void writeOutputFile(ArrayList<GameSessionTurn> ArrayList) {

        String fileName = "analyzer_results.txt";

        if (ArrayList.isEmpty()) {
           try (FileWriter writer = new FileWriter(fileName)) {
               // Return an empty file
               writer.write("");
           } catch (IOException e) {
               e.printStackTrace();
           }

        }else{
            try (FileWriter writer = new FileWriter(fileName)){
                for (GameSessionTurn GST: ArrayList) {
                    writer.write(GST + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}