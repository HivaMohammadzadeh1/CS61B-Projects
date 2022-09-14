package enigma;
import org.junit.Test;

import static org.junit.Assert.*;


public class FixedRotorTest {

    private FixedRotor fixedRotor;

    @Test(expected = EnigmaException.class)
    public void testFixed() {
        Alphabet test = new Alphabet("ABCD");
        Permutation p = new Permutation("(BACD)", test);
        fixedRotor = new FixedRotor("baba", p);
        fixedRotor.set(5);
    }

    @Test(expected = EnigmaException.class)
    public void testFixed1() {
        Alphabet test = new Alphabet("ABCD");
        Permutation p = new Permutation("(BACD)", test);
        fixedRotor = new FixedRotor("baba", p);
        fixedRotor.set('B');
    }

    @Test()
    public void testFixed2() {
        Alphabet test = new Alphabet("ABCD");
        Permutation p = new Permutation("(BACD)", test);
        fixedRotor = new FixedRotor("baba", p);
        int before = fixedRotor.setting();
        fixedRotor.advance();
        int after = fixedRotor.setting();
        assertEquals(before, after);

    }


}
