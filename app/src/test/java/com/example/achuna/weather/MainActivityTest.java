package com.example.achuna.weather;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Achuna on 1/15/2018.
 */
public class MainActivityTest {
    @Test
    public void tempConverter() throws Exception {
        String temp = "37";

        int convertTo = 0;
        int unit = 1;
        MainActivity converter = new MainActivity();


        String actual = converter.tempConverter(temp, convertTo, unit);

        assertEquals("99°C", actual);

    }

}