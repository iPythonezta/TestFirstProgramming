/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy for getTimespan:
     * 
     * Partition inputs based on:
     * - Number of tweets: 1, > 1
     * - Order of timestamps: chronological, reverse-chronological, unordered
     * - Distinctness of timestamps: all distinct, some identical, all identical
     * 
     * Testing strategy for getMentionedUsers:
     * 
     * Partition inputs based on:
     * - Number of tweets: 0, 1, > 1
     * - Number of mentions per tweet: 0, 1, > 1
     * - Position of mention: start, middle, end of tweet, standalone
     * - Case sensitivity: distinct users vs same user mentioned with different cases
     * - Mention validity: valid mention (@user) vs invalid mention (e.g., email address like user@domain.com)
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "alyssa", "What a great day @bbitdiddle!", d3);
    private static final Tweet tweet4 = new Tweet(4, "user4", "@Alyssa and @bbitdiddle are here @user4", d1);
    private static final Tweet tweet5 = new Tweet(5, "user5", "Email me at user@example.com for info", d2);
    private static final Tweet tweet6 = new Tweet(6, "user6", "@bbitdiddle @BBITDIDDLE", d3);
    

    // --- Tests for getTimespan ---
    
    @Test
    public void testGetTimespanOneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanThreeTweetsUnordered() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet3, tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanIdenticalTimestamps() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet4)); // Both at d1
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    // --- Tests for getMentionedUsers ---
    
    @Test
    public void testGetMentionedUsersEmptyList() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Collections.emptyList());
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    @Test
    public void testGetMentionedUsersOneMentionAtEnd() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3));
        
        assertEquals("expected one user", 1, mentionedUsers.size());
        assertTrue("expected to contain bbitdiddle", mentionedUsers.contains("bbitdiddle") || mentionedUsers.contains("BBITDIDDLE"));
    }
    
    @Test
    public void testGetMentionedUsersMultipleMentionsAndPositions() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet4));
        
        assertEquals("expected three users", 3, mentionedUsers.size());
        
        // Convert the result to lowercase for easier case-insensitive assertions
        Set<String> lowerCaseUsers = mentionedUsers.stream()
            .map(String::toLowerCase)
            .collect(java.util.stream.Collectors.toSet());
            
        assertTrue("expected alyssa", lowerCaseUsers.contains("alyssa"));
        assertTrue("expected bbitdiddle", lowerCaseUsers.contains("bbitdiddle"));
        assertTrue("expected user4", lowerCaseUsers.contains("user4"));
    }
    
    @Test
    public void testGetMentionedUsersEmailAddressIgnored() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet5));
        
        assertTrue("expected empty set since user@example.com is an email, not a mention", mentionedUsers.isEmpty());
    }
    
    @Test
    public void testGetMentionedUsersCaseInsensitiveDuplicates() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet6));
        
        assertEquals("expected exactly one user despite different cases", 1, mentionedUsers.size());
        
        String user = mentionedUsers.iterator().next();
        assertTrue("expected bbitdiddle (case-insensitive)", user.equalsIgnoreCase("bbitdiddle"));
    }
    
    @Test
    public void testGetMentionedUsersMultipleTweets() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet3, tweet6));
        
        assertEquals("expected exactly one unique user across tweets", 1, mentionedUsers.size());
        assertTrue("expected bbitdiddle", mentionedUsers.iterator().next().equalsIgnoreCase("bbitdiddle"));
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}