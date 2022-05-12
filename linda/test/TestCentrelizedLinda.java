package linda.test;

import linda.Tuple;
import linda.shm.CentralizedLinda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class TestCentrelizedLinda {

    private CentralizedLinda linda;

    @BeforeEach
    void initTest() {
        linda = new CentralizedLinda();
    }

    @Test
    void TestTryTakeEmpty() {
        Tuple motif = new Tuple(int.class, int.class);
        Tuple tuple = linda.tryTake(motif);
        assertNull(tuple);
    }

    @Test
    void TestTryTake() {
        Tuple t1 = new Tuple(1, 2);
        Tuple t2 = new Tuple(3, 4);

        linda.write(t1);
        linda.write(t2);

        Tuple motif = new Tuple(Integer.class, Integer.class);

        Tuple tuple = linda.tryTake(motif);
        assertNotNull(tuple);
        assertTrue(t1 == tuple || t2 == tuple);

        tuple = linda.tryTake(motif);
        assertNotNull(tuple);
        assertTrue(t1 == tuple || t2 == tuple);

        tuple = linda.tryTake(motif);
        assertNull(tuple);
    }

    @Test
    void TestTryReadEmpty() {
        Tuple motif = new Tuple(Integer.class, Integer.class);
        Tuple tuple = linda.tryRead(motif);
        assertNull(tuple);
    }

    @Test
    void TestTryRead() {
        Tuple t1 = new Tuple(1, 2);
        Tuple t2 = new Tuple(3, 4);

        linda.write(t1);
        linda.write(t2);

        Tuple motif = new Tuple(Integer.class, Integer.class);

        Tuple tuple = linda.tryRead(motif);
        assertNotNull(tuple);
        assertTrue(t1 != tuple && t2 != tuple);
        assertTrue(motif.contains(tuple));
    }

    @Test
    void TestTakeAllEmpty() {
        Tuple motif = new Tuple(Integer.class, Integer.class);

        Collection<Tuple> tuples = linda.takeAll(motif);

        assertEquals(0, tuples.size());
    }

    @Test
    void TestTakeAll() {
        Tuple t1 = new Tuple(1, 2);
        Tuple t2 = new Tuple(3, 4);

        linda.write(t1);
        linda.write(t2);

        Tuple motif = new Tuple(Integer.class, Integer.class);

        Collection<Tuple> tuples;

        tuples = linda.takeAll(motif);
        assertEquals(2, tuples.size());
        for (Tuple tuple : tuples)
            assertTrue(motif.contains(tuple));

        tuples = linda.takeAll(motif);
        assertEquals(0, tuples.size());
    }

    @Test
    void TestReadAllEmpty() {
        Tuple motif = new Tuple(Integer.class, Integer.class);

        Collection<Tuple> tuples = linda.readAll(motif);

        assertEquals(0, tuples.size());
    }

    @Test
    void TestReadAll() {
        Tuple t1 = new Tuple(1, 2);
        Tuple t2 = new Tuple(3, 4);

        linda.write(t1);
        linda.write(t2);

        Tuple motif = new Tuple(Integer.class, Integer.class);

        Collection<Tuple> tuples;

        tuples = linda.readAll(motif);
        assertEquals(2, tuples.size());
        for (Tuple tuple : tuples) {
            assertTrue(tuple != t1 && tuple != t2);
            assertTrue(motif.contains(tuple));
        }

        tuples = linda.takeAll(motif);
        assertNotEquals(0, tuples.size());
    }
}