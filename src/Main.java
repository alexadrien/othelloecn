
import java.util.ArrayList;
import java.util.List;
import ui.ConsoleInterface;

public class Main {

    public static List<String> availableAIs() {
        List<String> ret = new ArrayList<String>();
        ret.add("random");
        return ret;
    }

    public static void main(String[] args) {
        String mode = "HvM";
        String[] ai = {"random", "random"};
        String[] ai_opts = {"", ""};

        int ai_index = 0; // number of times the -ai argument was found
        int ai_opts_index = 0;
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
            else if (arg.equals("-ai")) 
            {
                if (ai_index == 2) {
                    System.out.println("[Error] " + arg + " argument : cannot be provided more than twice");
                    return;
                }

                if ((i + 1) == args.length) {
                    System.out.println("[Error] " + arg + " argument : missing value");
                    return;
                }

                i += 1;

                ai[ai_index] = args[i];
                if (!availableAIs().contains(ai[ai_index])) {
                    System.out.println("[Warning] " + arg + " argument : invalid ia '" + ai[ai_index] + "' using default ia random");
                    ai[ai_index] = "random";
                }

                ai_index += 1;
            }
            else if (arg.equals("--ai-opts")) {
                if (ai_opts_index == 2) {
                    System.out.println("[Error] " + arg + " argument : cannot be provided more than twice");
                    return;
                }
  
                if ((i + 1) == args.length) {
                    System.out.println("[Error] " + arg + " argument : missing value");
                    return;
                }

                i += 1;

                ai_opts[ai_opts_index] = args[i];
                
                ai_opts_index += 1;
            }
            else
            {
                System.out.println("[Warning] unknown argument '" + arg + "' will be ignored");
            }
        }

        ConsoleInterface ui = new ConsoleInterface(mode, ai[0], ai_opts[0], ai[1], ai_opts[1]);

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
        
        System.out.println("-ai <value>");
        System.out.println("\t\tSets the game AI, can be used twice if mode is MvM, possible values for <value> are : ");
        System.out.println("\t\t  - random : ai plays randomly (default)");
        
        System.out.println("--ai-opts <value>");
        System.out.println("\t\tSets the game AI options, can be used twice if mode is MvM, possible values for <value> depends on selected IA.");
    }
}
