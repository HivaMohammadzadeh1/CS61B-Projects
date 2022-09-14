package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Hiva Mohammadzadeh
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        String setting = _input.nextLine();
        String message = "";
        setUp(machine, setting);
        while (_input.hasNextLine()) {
            String nextInputLine = _input.nextLine();
            if (nextInputLine.length() == 0) {
                _output.println(nextInputLine);
            } else if (nextInputLine.contains("*")) {
                setUp(machine, nextInputLine);
            } else {
                message = machine.convert(nextInputLine.replaceAll(" ", ""));
                printMessageLine(message);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            _allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                _allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorName = _config.next();
            String rotorType = _config.next();
            if (rotorType == null || rotorType.startsWith("(")) {
                throw new EnigmaException("no type.");
            }
            String cycles = "";
            while (_config.hasNext("\\(.*\\)")) {
                cycles += _config.next();
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            if (rotorType.charAt(0) == 'M') {
                String notches = "";
                for (int i = 1; i < rotorType.length(); i++) {
                    notches += rotorType.charAt(i);
                }
                return new MovingRotor(rotorName, perm, notches);
            } else if (rotorType.charAt(0) == 'N') {
                return new FixedRotor(rotorName, perm);
            } else if (rotorType.charAt(0) == 'R') {
                return new Reflector(rotorName, perm);
            } else {
                throw new EnigmaException("no rotor");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] setArray = settings.split(" ");
        boolean reflect = false;
        if (!setArray[0].equals("*")) {
            throw new EnigmaException("illegal symbol start");
        }
        for (int i = 0; i < _allRotors.size(); i++) {
            Rotor r = _allRotors.get(i);
            if (r.reflecting()) {
                if (r.name().equals(setArray[1])) {
                    reflect = true;
                    break;
                }
            }
        }

        if (M.numRotors() + 2 > setArray.length) {
            throw new EnigmaException("wrong number of rotors");
        }
        if (!reflect) {
            throw new EnigmaException("wrong, reflector");
        }
        String set = setArray[M.numRotors() + 1];
        if (set.length() != M.numRotors() - 1) {
            throw new EnigmaException("wrong setting format 1");
        }
        for (int i = 0; i < set.length(); i++) {
            if (!_alphabet.contains(set.charAt(i))) {
                throw new EnigmaException("wrong setting format 2");
            }
        }
        String[] rotors = new String[M.numRotors()];
        String cycles = "";
        String ring = "";
        if (setArray[0].equals("*")) {
            for (int i = 0; i < rotors.length; i++) {
                rotors[i] = setArray[i + 1];
            }
            int newNumRotor = M.numRotors() + 2;
            for (int i = newNumRotor; i < setArray.length; i++) {
                if ((i == newNumRotor) && (setArray[i].charAt(0) != '(')) {
                    ring = setArray[i];
                } else {
                    cycles += setArray[i];
                }
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            M.insertRotors(rotors);
            M.setRing(ring);
            M.setPlugboard(perm);
            M.setRotors(set);
        } else {
            throw new EnigmaException("wrong setting format 3");
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            if (i + 5 < msg.length()) {
                _output.print(msg.substring(i, i + 5) + " ");
            } else {
                _output.println(msg.substring(i));
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Array list for all Rotors.*/
    private ArrayList<Rotor> _allRotors = new ArrayList<>();
}
