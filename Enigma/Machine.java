package enigma;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Hiva Mohammadzadeh
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _rotorIterator = allRotors.iterator();
        _allRotors = new HashMap<String, Rotor>();

        while (_rotorIterator.hasNext()) {
            Rotor temp = _rotorIterator.next();
            _allRotors.put(temp.name(), temp);
        }

        _rotors = new ArrayList<Rotor>();

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor. Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new ArrayList<>();
        for (int i = 0; i < rotors.length; i++) {
            _rotors.add(_allRotors.get(rotors[i]));
        }
        if (rotors.length > _allRotors.size()) {
            throw new EnigmaException("Not enough rotors for this machine.");
        }
        if (!(_rotors.get(0) instanceof Reflector)) {
            throw new EnigmaException("The first rotor is not a reflector");
        }
        if (!(_rotors.get(_rotors.size() - 1) instanceof MovingRotor)) {
            throw new EnigmaException("The rightmost rotor not a moving rotor");
        }
        int j = 0;
        int countMovingRotors = 0;
        while (j < _rotors.size()) {
            if (_rotors.get(j) instanceof MovingRotor) {
                countMovingRotors += 1;
            }
            j++;
        }
        int i = 0;
        int count = 0;
        while (i < _rotors.size()) {
            if (_rotors.get(i) instanceof Reflector) {
                count += 1;
            }
            i++;
        }
        if (countMovingRotors > numPawls()) {
            throw new EnigmaException("More Moving Rotors than pawls.");
        } else if (count > 1) {
            throw new EnigmaException("More than 1 reflector.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numRotors - 1) {
            throw new EnigmaException("Wrong setting length");
        }
        int start = _numRotors - setting.length();
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.contains(alphabet().toChar(i))) {
                throw new EnigmaException(" string not in alphabet");
            }
            if (_rotors.get(start + i).reflecting()) {
                _rotors.get(start + i).set(0);
            } else {
                _rotors.get(start + i).set(setting.charAt(i));
            }
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] a = new boolean[_rotors.size()];
        for (int i = 0; i < _rotors.size() - 1; i++) {
            if (!_rotors.get(i).rotates()) {
                a[i] = false;
            } else if (_rotors.get(i + 1).atNotch()) {
                a[i] = true;
                a[i + 1] = true;
            }
        }
        a[_rotors.size() - 1] = true;
        for (int i = 0; i < _rotors.size(); i++) {
            if (a[i]) {
                _rotors.get(i).advance();
            }
        }
    }

    void setRing(String ring) {
        int start = _numRotors - ring.length();
        for (int i = 0; i < ring.length(); i++) {
            _rotors.get(start + i).setRing(ring.charAt(i));
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _rotors.size() - 1; i >= 0; i--) {
            c = _rotors.get(i).convertForward(c);
        }
        for (int i = 1; i < _rotors.size(); i++) {
            c = _rotors.get(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            result += _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Integer for the number of rotors.*/
    private int _numRotors;
    /** Integer for the number of pawls.*/
    private int _pawls;
    /** Object array to keep all the rotors.*/
    private HashMap<String, Rotor> _allRotors;
    /** Set up the plugboard.*/
    private Permutation _plugboard;
    /** Iterator to iterate through the collection.*/
    private Iterator<Rotor> _rotorIterator;
    /** Array list to keep track of the current rotors.*/
    private ArrayList<Rotor> _rotors;

}
