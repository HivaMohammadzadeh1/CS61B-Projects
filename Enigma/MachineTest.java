package enigma;

import java.util.HashMap;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Hiva Mohammadzadeh
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    private static final Alphabet AZ = new Alphabet(TestUtils.UPPER_STRING);

    private static final HashMap<String, Rotor> ROTORS = new HashMap<>();

    static {
        HashMap<String, String> nav = TestUtils.NAVALA;
        ROTORS.put("B", new Reflector("B", new Permutation(nav.get("B"), AZ)));
        ROTORS.put("Beta",
                new FixedRotor("Beta",
                        new Permutation(nav.get("Beta"), AZ)));
        ROTORS.put("III",
                new MovingRotor("III",
                        new Permutation(nav.get("III"), AZ), "V"));
        ROTORS.put("IV",
                new MovingRotor("IV", new Permutation(nav.get("IV"), AZ),
                        "J"));
        ROTORS.put("I",
                new MovingRotor("I", new Permutation(nav.get("I"), AZ),
                        "Q"));
    }

    private static final String[] ROTORS1 = { "B", "Beta", "III", "IV", "I" };
    private static final String SETTING1 = "AXLE";

    private Machine mach1() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);
        return mach;
    }

    @Test
    public void testInsertRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        assertEquals(5, mach.numRotors());
        assertEquals(3, mach.numPawls());
        assertEquals(AZ, mach.alphabet());
        assertEquals(ROTORS.get("B"), mach.getRotor(0));
        assertEquals(ROTORS.get("Beta"), mach.getRotor(1));
        assertEquals(ROTORS.get("III"), mach.getRotor(2));
        assertEquals(ROTORS.get("IV"), mach.getRotor(3));
        assertEquals(ROTORS.get("I"), mach.getRotor(4));
    }

    @Test
    public void testConvertChar() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(YF) (HZ)", AZ));
        assertEquals(25, mach.convert(24));
    }

    @Test
    public void testConvertMsg() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
    }

    @Test
    public void testSpecExampleSong() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
        assertEquals("BHCNSCXNUOAATZXSRCFYDGU",
                mach.convert("TOOKTHECAMERAOFROSEWOOD"));
        assertEquals("FLPNXGXIXTYJUJRCAUGEUNCFMKUF",
                mach.convert("MADEOFSLIDINGFOLDINGROSEWOOD"));
        assertEquals("WJFGKCIIRGXODJGVCGPQOH",
                mach.convert("NEATLYPUTITALLTOGETHER"));
        assertEquals("ALWEBUHTZMOXIIVXUEFPRPR",
                mach.convert("INITSCASEITLAYCOMPACTLY"));
        assertEquals("KCGVPFPYKIKITLBURVGTSFU",
                mach.convert("FOLDEDINTONEARLYNOTHING"));
        assertEquals("SMBNKFRIIMPDOFJVTTUGRZM",
                mach.convert("BUTHEOPENEDOUTTHEHINGES"));
        assertEquals("UVCYLFDZPGIBXREWXUEBZQJO",
                mach.convert("PUSHEDANDPULLEDTHEJOINTS"));
        assertEquals("YMHIPGRRE",
                mach.convert("ANDHINGES"));
        assertEquals("GOHETUXDTWLCMMWAVNVJVH",
                mach.convert("TILLITLOOKEDALLSQUARES"));
        assertEquals("OUFANTQACK",
                mach.convert("ANDOBLONGS"));
        assertEquals("KTOZZRDABQNNVPOIEFQAFS",
                mach.convert("LIKEACOMPLICATEDFIGURE"));
        assertEquals("VVICVUDUEREYNPFFMNBJVGQ",
                mach.convert("INTHESECONDBOOKOFEUCLID"));
    }

    @Test
    public void testSpecExample() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("FROMHISSHOULDERHIAWATHA",
                mach.convert("QVPQSOKOILPUBKJZPISFXDW"));
        assertEquals("TOOKTHECAMERAOFROSEWOOD",
                mach.convert("BHCNSCXNUOAATZXSRCFYDGU"));
        assertEquals("MADEOFSLIDINGFOLDINGROSEWOOD",
                mach.convert("FLPNXGXIXTYJUJRCAUGEUNCFMKUF"));
        assertEquals("NEATLYPUTITALLTOGETHER",
                mach.convert("WJFGKCIIRGXODJGVCGPQOH"));
        assertEquals("INITSCASEITLAYCOMPACTLY",
                mach.convert("ALWEBUHTZMOXIIVXUEFPRPR"));
        assertEquals("FOLDEDINTONEARLYNOTHING",
                mach.convert("KCGVPFPYKIKITLBURVGTSFU"));
        assertEquals("BUTHEOPENEDOUTTHEHINGES",
                mach.convert("SMBNKFRIIMPDOFJVTTUGRZM"));
        assertEquals("PUSHEDANDPULLEDTHEJOINTS",
                mach.convert("UVCYLFDZPGIBXREWXUEBZQJO"));
        assertEquals("ANDHINGES",
                mach.convert("YMHIPGRRE"));
        assertEquals("TILLITLOOKEDALLSQUARES",
                mach.convert("GOHETUXDTWLCMMWAVNVJVH"));
        assertEquals("ANDOBLONGS",
                mach.convert("OUFANTQACK"));
        assertEquals("LIKEACOMPLICATEDFIGURE",
                mach.convert("KTOZZRDABQNNVPOIEFQAFS"));
        assertEquals("INTHESECONDBOOKOFEUCLID",
                mach.convert("VVICVUDUEREYNPFFMNBJVGQ"));
    }

    @Test (expected = EnigmaException.class)
    public void testInsertRotors2() {
        String[] rotors2 = { "B", "Beta", "III", "IV", "I", "IX" };
        Machine mach = new Machine(AZ, 6, 3, ROTORS.values());
        mach.insertRotors(rotors2);

    }

    private String[] r = { "B", "Gamma", "Beta", "II" };
    @Test (expected = EnigmaException.class)
    public void testInsertRotors4() {
        Machine mach = new Machine(AZ, 11, 3, ROTORS.values());
        mach.insertRotors(r);
    }

    private String[] b = { "B", "Betta", "III", "I", "II", "IV" };
    @Test (expected = EnigmaException.class)
    public void testInsertRotors5() {
        Machine mach = new Machine(AZ, 6, 3, ROTORS.values());
        mach.insertRotors(b);
    }
}
