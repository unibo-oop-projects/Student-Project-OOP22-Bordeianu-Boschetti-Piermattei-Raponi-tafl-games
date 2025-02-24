package taflgames;
//
/*Magic numbers checks disabled in order to allow quicker writing of the tests; the
 * numbers used in the creation of Positions and Vectors are not intended to be 
 * constants, but only results to verify computations by need.
 */
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import taflgames.common.api.FactoryHitbox;
import taflgames.common.code.ImplFactoryHitbox;
import taflgames.common.code.Position;
/**
 * testing factoryHitbox.
 */
class TestFactoryHitbox {
    // 
    /* CPD suppressed because tests are naturally repetitive and their purpose
     * should be clear enough.
     */
    @Test
    void testCreateBasicHitboxDistance() {
        final FactoryHitbox f = new ImplFactoryHitbox();
        final int d = 2;
        final Set<Position> out = new HashSet<>();
        out.add(new Position(d, 0));
        out.add(new Position(0, d));
        out.add(new Position(-d, 0));
        out.add(new Position(0, -d));
        assertEquals(out, f.createBasicHitboxDistance(d));

        final int d2 = 0;
        final Set<Position> out2 = new HashSet<>();
        out2.add(new Position(d2, 0));
        out2.add(new Position(0, d2));
        out2.add(new Position(-d2, 0));
        out2.add(new Position(0, -d2));
        assertEquals(out2, f.createBasicHitboxDistance(d2));
        assertThrows(IllegalArgumentException.class, () -> f.createBasicHitboxDistance(-9));
        assertNotEquals(f.createBasicHitboxDistance(d), null);
        assertNotEquals(f.createBasicHitboxDistance(d), new HashSet<>());
    }
    @Test
    void testCreateBasicHitbox() {
        final FactoryHitbox f = new ImplFactoryHitbox();
        final int d = 1;
        final Set<Position> out = new HashSet<>();
        out.add(new Position(d, 0));
        out.add(new Position(0, d));
        out.add(new Position(-d, 0));
        out.add(new Position(0, -d));
        assertEquals(out, f.createBasicHitbox());
        final int d2 = 2;
        final Set<Position> out2 = new HashSet<>();
        out2.add(new Position(d2, 0));
        out2.add(new Position(0, d2));
        out2.add(new Position(-d2, 0));
        out2.add(new Position(0, -d2));
        assertNotEquals(out2, f.createBasicHitbox());
        assertNotEquals(f.createBasicHitbox(), null);
        assertNotEquals(f.createBasicHitbox(), new HashSet<>());
    }
    @Test
    void testCreateArcherHitbox() {
        final FactoryHitbox f = new ImplFactoryHitbox();
        final Set<Position> out = new HashSet<>();
        int d2 = 1;
        out.add(new Position(d2, 0));
        out.add(new Position(0, d2));
        out.add(new Position(-d2, 0));
        out.add(new Position(0, -d2));
        d2 = 2;
        out.add(new Position(d2, 0));
        out.add(new Position(0, d2));
        out.add(new Position(-d2, 0));
        out.add(new Position(0, -d2));
        d2 = 3;
        out.add(new Position(d2, 0));
        out.add(new Position(0, d2));
        out.add(new Position(-d2, 0));
        out.add(new Position(0, -d2));
        final int range = 3;
        assertEquals(out, f.createArcherHitbox(range));
        assertNotEquals(f.createArcherHitbox(range), null);
        assertNotEquals(f.createArcherHitbox(range), new HashSet<>());
        assertThrows(IllegalArgumentException.class, () -> f.createArcherHitbox(0));
        assertThrows(IllegalArgumentException.class, () -> f.createArcherHitbox(-9));
    }
    // CPD-ON
}
