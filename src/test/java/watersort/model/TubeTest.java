package watersort.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TubeTest {

    @Test
    void testPushPop() {
        Tube tube = new Tube();
        tube.push(1);
        tube.push(2);

        assertEquals(2, tube.size());
        assertEquals(2, tube.topColor());

        assertEquals(2, tube.pop());
        assertEquals(1, tube.size());
        assertEquals(1, tube.topColor());

        assertEquals(1, tube.pop());
        assertTrue(tube.isEmpty());
    }

    @Test
    void testCapacity() {
        Tube tube = new Tube();
        tube.push(1);
        tube.push(2);
        tube.push(3);
        tube.push(4);

        assertTrue(tube.isFull());

        assertThrows(IllegalStateException.class, () -> tube.push(5));
    }

    @Test
    void testEmptyTube() {
        Tube tube = new Tube();
        assertTrue(tube.isEmpty());

        assertThrows(IllegalStateException.class, tube::pop);
        assertThrows(IllegalStateException.class, tube::topColor);
    }

    @Test
    void testIsSorted() {
        Tube sortedTube = new Tube(1, 1, 1, 1);
        assertTrue(sortedTube.isSorted());

        Tube mixedTube = new Tube(1, 1, 2, 1);
        assertFalse(mixedTube.isSorted());

        Tube notFullTube = new Tube(1, 1, 1);
        assertFalse(notFullTube.isSorted());
    }

    @Test
    void testTopColorCount() {
        Tube tube = new Tube(1, 2, 2, 2);
        assertEquals(3, tube.topColorCount());

        Tube tube2 = new Tube(1, 1, 3, 2);
        assertEquals(1, tube2.topColorCount());

        Tube emptyTube = new Tube();
        assertEquals(0, emptyTube.topColorCount());
    }

    @Test
    void testIsUniform() {
        Tube tube = new Tube(1, 1, 1);
        assertTrue(tube.isUniform());

        Tube emptyTube = new Tube();
        assertTrue(emptyTube.isUniform());

        Tube mixedTube = new Tube(1, 2);
        assertFalse(mixedTube.isUniform());
    }

    @Test
    void testDeepCopy() {
        Tube original = new Tube(1, 2);
        Tube copy = original.deepCopy();

        assertEquals(original, copy);
        assertNotSame(original, copy);

        copy.push(3);

        assertEquals(2, original.size());
        assertEquals(3, copy.size());
        assertNotEquals(original, copy);
    }

    @Test
    void testEqualsAndHashCode() {
        Tube tube1 = new Tube(1, 2, 3);
        Tube tube2 = new Tube(1, 2, 3);
        Tube tube3 = new Tube(1, 2, 4);
        Tube tube4 = new Tube(1, 2);

        assertEquals(tube1, tube1);
        assertEquals(tube1, tube2);
        assertNotEquals(tube1, tube3);
        assertNotEquals(tube1, tube4);
        assertNotEquals(tube1, null);
        assertNotEquals(tube1, new Object());

        assertEquals(tube1.hashCode(), tube2.hashCode());
        assertNotEquals(tube1.hashCode(), tube3.hashCode());
    }
}
