/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy for writtenBy:
     * 
     * Partition inputs based on:
     * - tweets.size(): 0, 1, > 1
     * - number of matching tweets: 0, 1, > 1
     * - username case: exact match, different case (usernames are case-insensitive)
     * - position of matches in list: contiguous, separated
     * 
     * Testing strategy for inTimespan:
     * 
     * Partition inputs based on:
     * - tweets.size(): 0, 1, > 1
     * - number of matching tweets: 0, 1, > 1
     * - tweet timestamp relative to timespan: strictly before, exactly at start, 
     *   strictly inside, exactly at end, strictly after
     * 
     * Testing strategy for containing:
     * 
     * Partition inputs based on:
     * - tweets.size(): 0, 1, > 1
     * - words.size(): 0, 1, > 1
     * - number of matching tweets: 0, 1, > 1
     * - word matching: exact match, case difference
     * - substring matching: target word is a substring of a word in the tweet (should NOT match)
     * - multiple words in list: tweet contains 1 of the words, tweet contains multiple of the words
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T09:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "alyssa", "talking about rivest is fun", d3);
    private static final Tweet tweet4 = new Tweet(4, "ALYSSA", "TALK to me", d4);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // --- Tests for writtenBy ---
    
    @Test
    public void testWrittenByEmptyList() {
        List<Tweet> writtenBy = Filter.writtenBy(Collections.emptyList(), "alyssa");
        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByNoResults() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "nobody");
        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }
    
    @Test
    public void testWrittenByCaseInsensitiveAndMultipleResults() {
        // "ALYSSA" should match "alyssa" and "ALYSSA" authors
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3, tweet4), "Alyssa");
        
        assertEquals("expected three tweets", 3, writtenBy.size());
        assertTrue("expected list to contain tweet1", writtenBy.contains(tweet1));
        assertTrue("expected list to contain tweet3", writtenBy.contains(tweet3));
        assertTrue("expected list to contain tweet4", writtenBy.contains(tweet4));
        assertEquals("expected same order", 0, writtenBy.indexOf(tweet1));
        assertEquals("expected same order", 1, writtenBy.indexOf(tweet3));
        assertEquals("expected same order", 2, writtenBy.indexOf(tweet4));
    }

    // --- Tests for inTimespan ---
    
    @Test
    public void testInTimespanEmptyList() {
        List<Tweet> inTimespan = Filter.inTimespan(Collections.emptyList(), new Timespan(d1, d2));
        assertTrue("expected empty list", inTimespan.isEmpty());
    }
    
    @Test
    public void testInTimespanNoResults() {
        Instant testStart = Instant.parse("2016-02-17T08:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T08:30:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));
        
        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }
    
    @Test
    public void testInTimespanBoundaries() {
        // d1 is exactly at the start, d2 is exactly at the end
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet4, tweet1, tweet2, tweet3), new Timespan(d1, d2));
        
        assertEquals("expected two tweets on the boundaries", 2, inTimespan.size());
        assertTrue("expected tweet1", inTimespan.contains(tweet1));
        assertTrue("expected tweet2", inTimespan.contains(tweet2));
    }

    // --- Tests for containing ---
    
    @Test
    public void testContainingEmptyList() {
        List<Tweet> containing = Filter.containing(Collections.emptyList(), Arrays.asList("talk"));
        assertTrue("expected empty list", containing.isEmpty());
    }
    
    @Test
    public void testContainingEmptyWordsList() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Collections.emptyList());
        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingNoResultsSubstringOnly() {
        // "talk" is a substring of "talking", but should not match as an isolated word
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet3), Arrays.asList("talk"));
        assertTrue("expected empty list because 'talk' is only a substring", containing.isEmpty());
    }

    @Test
    public void testContainingExactAndCaseInsensitive() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet4), Arrays.asList("TaLk"));
        
        assertEquals("expected three tweets with exact word match (case insensitive)", 3, containing.size());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2, tweet4)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
        assertEquals("expected same order", 1, containing.indexOf(tweet2));
    }
    
    @Test
    public void testContainingMultipleWordsMultipleResults() {
        // tweet1 contains "reasonable", tweet2 contains "hype", tweet3 contains neither
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet3), Arrays.asList("reasonable", "hype"));
        
        assertEquals("expected two tweets matching either word", 2, containing.size());
        assertTrue("expected tweet1", containing.contains(tweet1));
        assertTrue("expected tweet2", containing.contains(tweet2));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}