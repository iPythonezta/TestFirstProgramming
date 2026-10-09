/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            Instant now = Instant.now();
            return new Timespan(now, now);
        }

        Instant start = tweets.get(0).getTimestamp();
        Instant end = start;

        for (Tweet tweet : tweets) {
            Instant current = tweet.getTimestamp();
            if (current.isBefore(start)) {
                start = current;
            }
            if (current.isAfter(end)) {
                end = current;
            }
        }

        return new Timespan(start, end);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentionedUsers = new HashSet<>();
        
        // A Twitter username typically consists of letters, numbers, and underscores.
        // We use a negative lookbehind (?<![A-Za-z0-9_]) to ensure the '@' is not 
        // immediately preceded by a valid username character.
        // The [A-Za-z0-9_]+ captures the username. 
        // Because the + quantifier is greedy, it will capture all valid characters,
        // which inherently fulfills the "not followed by a valid character" rule.
        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])@([A-Za-z0-9_]+)");

        for (Tweet tweet : tweets) {
            Matcher matcher = pattern.matcher(tweet.getText());
            while (matcher.find()) {
                // Usernames are case-insensitive, so we normalize them to lowercase
                String username = matcher.group(1).toLowerCase();
                mentionedUsers.add(username);
            }
        }

        return mentionedUsers;
    }

}