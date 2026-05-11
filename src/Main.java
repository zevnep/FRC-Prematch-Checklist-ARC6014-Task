import java.util.ArrayList;
import java.util.Scanner;

public class Main{
    //Battery Data
    static String[] batteryHealth = new String[3];
    static double[] batteryVoltage = new double [3];
    static boolean[] batteryEntered = new boolean [3];
    //Entry Point
    public static void  main( String[]args) {
        Scanner scanner = new Scanner(System.in);

        ArrayList<String> items = new ArrayList<>();
        ArrayList<Boolean> completed = new ArrayList<>();

        System.out.println("=======================================");
        System.out.println("FRC ARC 6014 LAST CHECK UP BEFORE MATCH");
        System.out.println("=======================================");

        boolean running = true;
        //Main Menu Loop
        while (running){
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1) Add Item");
            System.out.println("2) Start Check");
            System.out.println("3) Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1":
                    addItem(scanner,items,completed);
                    break;

                case "2":
                    //Makes it mandatory for there to be 3 items before entering check mode
                    if (items.size() < 3){
                        System.out.println("[!] You need at least 3 items to start off the checklist.");
                        System.out.println("  You currently have only : "+ items.size());
                    } else {
                        //Run the full check phase
                        runFullCheckSequence(scanner, items, completed);
                    }
                    break;
                case "3":
                    System.out.println("You are all set to go.\n Good luck at the match :)");
                    running = false;
                    break;

                default:
                    System.out.println("[!] Invalid option. Enter 1,2, or 3 please.");
            }

        }

        scanner.close();
    }
    //Add Cheklist item
    /*
     * Prompts the user to enter new checklist item and adds it to both lists
     */
    static void addItem(Scanner scanner, ArrayList<String> items, ArrayList<Boolean> completed) {
        System.out.print("Enter checklist item: ");
        String item = scanner.nextLine().trim();

        if (item.isEmpty()) {
            System.out.println("[!] Item can't be empty. Please enter something.");
            return;
        }

        items.add(item);
        completed.add(false);
        System.out.println("[+] Item added: \"" + item + "\"");
    }
    //Full Check Sequence
    /**
     * Runs the check phase, battery selection, and final review in order.
     * Program doesn't accept match readiness unless all items are true
     * And the best possible battery has been selected
     * @param scanner
     * @param items
     * @param completed
     */

    static void runFullCheckSequence(Scanner scanner,
                                     ArrayList<String> items,
                                     ArrayList<Boolean> completed) {

        // Step 1: Iterate every item and ask yes/no
        runCheckPhase(scanner, items, completed);

        //Step 2: Selects the best battery
        int selectedBattery = runBatterySelection(scanner);

        //Step 3: Final review (resolves all false items)
        runFinalReview(scanner, items, completed);

        //Step 4: Confirms completion
        System.out.println("\n==============================================");
        System.out.println("YOU ARE READY FOR THE MATCH.\n GOOD LUCK :) ");
        System.out.println("Battery #"+ (selectedBattery + 1) + " selected "
                +batteryVoltage[selectedBattery] + "V"
                +batteryHealth[selectedBattery].toUpperCase());
        System.out.println("==============================================");
    }

    //Check Phase

    /*
     *Goes through all checklist items and asks the user yes/no.
     *All items are iterated no matter how many are false.
     */
    static void runCheckPhase(Scanner scanner,ArrayList<String> items, ArrayList<Boolean> completed) {
        System.out.println("\n--- CHECK PHASE---");

        for (int i = 0; i < items.size(); i++) {
            System.out.println("\nItem " + (i+1) + ": " + items.get(i));
            System.out.print("Is it completed? ");

            while (true) {
                String answer = scanner.nextLine().trim().toLowerCase();
                if (answer.equals("yes")) {
                    completed.set(i, true);
                    break;
                } else if (answer.equals("no")) {
                    completed.set(i, false);
                    break;
                } else {
                    System.out.print("[!] Please enter 'yes' or 'no' : ");
                }
            }
        }
        System.out.println("Okk. Check Phase complete :) ");
    }

    //Battery Selection

    /**
     * Collects voltage and health data for exactly 3 batteries,
     * then selects the best one based on how far away the voltage is from 13V below.
     * The battery health is a tie breaker
     * @param scanner
     * @return
     */
    static int runBatterySelection(Scanner scanner) {
        System.out.println(" \n ----BATTERY SELECTION---");
        System.out.println(" Enter data for 3 batteries.");

        for (int i = 0; i < 3; i++) {
            System.out.println("\n ---Battery #" + (i+1) +"---");


            double voltage = 0;
            boolean validVoltage = false;

            while (!validVoltage) {
                System.out.print("Voltage (12.3-13.0): ");
                String input = scanner.nextLine().trim();

                try {
                    voltage = Double.parseDouble(input);
                    if (voltage < 12.3 || voltage > 13.0) {
                        System.out.println("[!] Voltage needs to be between 12.3V and 13.0V");
                    } else {
                        validVoltage = true;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("That's not a valid number. Try to enter a valid one this time :)");
                }
            }

            batteryVoltage[i] = voltage;

            //Battery Health input
            String health = "";
            boolean validHealth = false;

            while (!validHealth) {
                System.out.print("Health (fair/ good): ");
                health = scanner.nextLine().trim().toLowerCase();
                if (health.equals("fair") || health.equals("good")) {
                    validHealth = true;
                } else {
                    System.out.println("[!] Enter exactly 'fair' or 'good'.");
                }
            }

            batteryHealth[i] = health;
            batteryEntered[i] = true;
        }

        //Selection logic
        //Priority: closest voltage to 13V from below.
        //If two voltages are within 0.1V of each other, prefer "good" health.
        int selectedIndex = 0;

        for (int i = 1; i < 3; i++) {
            double currentBest = batteryVoltage[selectedIndex];
            double runnerUp = batteryVoltage[i];

            double diffBest = 13.0 - currentBest; //How far below 13V
            double diffUp= 13.0 - runnerUp;

            boolean runnerUpCloser = diffUp < diffBest;
            boolean withinTieThreshold = Math.abs(diffUp - diffBest) <= 0.1;
            boolean runnerUpHealthBetter = batteryHealth[i].equals("good")
                    ||!batteryHealth[selectedIndex].equals("good");

            if (runnerUpCloser && !withinTieThreshold) {
                //It is closer to 13V so pick runnerUp
                selectedIndex = i;
            } else if (withinTieThreshold && runnerUpHealthBetter) {
                //Voltages are tied so health breaks the tie
                selectedIndex = i;
            }
            //Otherwise keep current best
        }

        System.out.println("\n selected Battery #" + (selectedIndex + 1)+ "- "
                + batteryVoltage[selectedIndex] + "V "
                + batteryHealth[selectedIndex].toUpperCase());

        return selectedIndex;
    }

    //Final Review

    /**
     * Shows all items that are still marked false,
     * and forces the user to resolve each one before declaring match readiness
     * @param scanner
     * @param items
     * @param completed
     */
    static void runFinalReview(Scanner scanner,
                               ArrayList<String> items,
                               ArrayList<Boolean> completed) {

        System.out.println("\n ----FINAL CHECK---");

        boolean anyIncomplete = true;

        //Keep looping until every item is resolved
        while ( anyIncomplete) {
            ArrayList<Integer> incompleteIndexes = new ArrayList<>();

            for (int i = 0; i < completed.size(); i++) {
                if (!completed.get(i)) {
                    incompleteIndexes.add(i);
                }
            }

            if (incompleteIndexes.isEmpty()) {
                anyIncomplete = false;
                break;
            }

            System.out.println("\nThese items are NOT complete:  ");

            for ( int idx : incompleteIndexes) {
                System.out.println(" [" + (idx + 1) + "] " + items.get(idx));
            }

            System.out.println("\nResolve each item now (please).");

            for (int idx : incompleteIndexes) {
                System.out.println("\nItem " + (idx + 1) + ": " + items.get(idx));
                System.out.print("Do you want to mark as complete? (yes/no): ");

                while (true) {
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.equals("yes")) {
                        completed.set(idx, true);
                        break;
                    } else if (answer.equals("no")) {
                        System.out.println("We've been over this." +
                                " Item must be completed before the match starts. " +
                                "Come on bro you can do it, try again.");
                        System.out.print("Do you want to mark as complete? (yes/no): ");
                    } else {
                        System.out.print("[!] Just enter 'yes' or 'no': ");
                    }
                }
            }
        }

        System.out.println("\nOK All items are resolved :)");
    }
}