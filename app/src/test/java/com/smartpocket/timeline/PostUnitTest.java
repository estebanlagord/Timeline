package com.smartpocket.timeline;

import com.smartpocket.timeline.model.Post;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PostUnitTest {
    @Test
    public void timeConversion() throws Exception {
        Post post = new Post();
        assertEquals("16/04/2016 02:34:56", post.convertTimeFromUtc("2016-04-16T05:34:56+0000")); //yyyy-MM-dd'T'HH:mm:ss+0000
    }

    @Test
    public void timeConversionInvalidFormat() {
        Post post = new Post();
        String inValue = "2016-04-16 05:34:56+0000";
        String result = post.convertTimeFromUtc(inValue);
        assertEquals(inValue, result);
    }
}