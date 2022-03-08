package com.cyberark.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberark.conjur.springboot.core.env.ConjurConfig;

// This class doesn't contain real tests, just placeholders
// so the test pipeline could be configured.
// Please remove this class (and file) when real tests
// are available.
public class TestFake {
    @Test
    void testFake(){
        assertEquals(1,1);
    }

    @Test
    void testConjurConfigGetInstance(){
        ConjurConfig cc = ConjurConfig.getInstance();
        assertTrue(cc instanceof ConjurConfig);
    }
}
