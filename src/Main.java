
import java.util.ArrayList;
import java.util.List;
import ui.ConsoleInterface;

public class Main {

    public static List<String> availableIAs() {
        List<String> ret = new ArrayList<String>();
        ret.add("random");
        return ret;
    }

    public static void main(String[] args) {
        String mode = "HvM";
        String[] ia = {"random", "random"};
        String[] ia_opts = {"", ""};

        int ia_index = 0; // number of times the -ia argument was found
        int ia_opts_index = 0;
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")) 
            {
                printHelp();
                return;
            }
            else if (arg.equals("-m") || arg.equals("--mode")) 
            {
                if ((i + 1) == args.length) {
                    System.out.println("[Error] " + arg + " argument : missing mode");
                    return;
                }

                i += 1;

                mode = args[i];
                if (!mode.equals("HvM") && !mode.equals("HvH") && !mode.equals("MvM")) {
                    System.out.println("[Warning] " + arg + " argument : invalid mode '" + mode + "' using default mode HvM");
                    mode = "HvM";
                }

            }
            else if (arg.equals("-ia")) 
            {
                if (ia_index == 2) {
                    System.out.println("[Error] " + arg + " argument : cannot be provided more than twice");
                    return;
                }

                if ((i + 1) == args.length) {
                    System.out.println("[Error] " + arg + " argument : missing value");
                    return;
                }

                i += 1;

                ia[ia_index] = args[i];
                if (!availableIAs().contains(ia[ia_index])) {
                    System.out.println("[Warning] " + arg + " argument : invalid ia '" + ia[ia_index] + "' using default ia random");
                    ia[ia_index] = "random";
                }

                ia_index += 1;
            }
            else if (arg.equals("--ia-opts")) {
                if (ia_opts_index == 2) {
                    System.out.println("[Error] " + arg + " argument : cannot be provided more than twice");
                    return;
                }
  
                if ((i + 1) == args.length) {
                    System.out.println("[Error] " + arg + " argument : missing value");
                    return;
                }

                i += 1;

                ia_opts[ia_opts_index] = args[i];
                
                ia_opts_index += 1;
            }
            else
            {
                System.out.println("[Warning] unknown argument '" + arg + "' will be ignored");
            }
        }

        ConsoleInterface ui = new ConsoleInterface(mode, ia[0], ia_opts[0], ia[1], ia_opts[1]);

        ui.exec();
    }

    public static void printHelp() {
        System.out.println("Omegathello command line arguments : ");

        System.out.println("-h");
        System.out.println("--help");
        System.out.println("\t\tShow this help");

        System.out.println("-m <mode>");
        System.out.println("--mode <mode>");
        System.out.println("\t\tSet the game mode, possible values for <mode> are : ");
        System.out.println("\t\t  - HvM : human versus machine (default)");
        System.out.println("\t\t  - HvH : human versus human");
        System.out.println("\t\t  - MvM : machine versus machine");
        
        System.out.println("-ia <value>");
        System.out.println("\t\tSets the game IA, can be used twice if mode is MvM, possible values for <value> are : ");
        System.out.println("\t\t  - random : ia plays randomly (default)");
        
        System.out.println("--ia-opts <value>");
        System.out.println("\t\tSets the game IA options, can be used twice if mode is MvM, possible values for <value> depends on selected IA.");
    }
}
