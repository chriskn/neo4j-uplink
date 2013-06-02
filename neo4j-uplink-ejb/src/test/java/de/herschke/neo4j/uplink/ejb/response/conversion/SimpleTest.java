/*
 * This file is part of the Uplink Framework for Neo4j Server Connection.
 *
 * Copyright (c) by:
 *
 * Robert Herschke
 * Hauptstrasse 30
 * 65760 Eschborn
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.herschke.neo4j.uplink.ejb.response.conversion;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class SimpleTest {

    private transient Log log;

    @Test
    public void testConvertToDate() {
        Object d = ConvertUtils.convert(1L, Date.class);
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        assertThat((Date) d).isEqualTo(new Date(1L));

        d = ConvertUtils.convert("1977-05-07", Date.class);
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        final Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 7);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal)).isTrue();
    }

    @Test
    public void testConvertFromList() {
        List<String> strings = Arrays.asList("The Matrix", "The Matrix Reloaded", "The Matrix Revolutions");

        Object s = ConvertUtils.convert(strings, String.class);
        assertThat(s).isNotNull().isInstanceOf(String.class);
        assertThat((String) s).isEqualTo("The Matrix");

        strings = Arrays.asList("1977-05-07", "1979-09-04");

        Object d = ConvertUtils.convert(strings, Date.class);
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        final Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 7);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal)).isTrue();
    }

    @Test
    public void testConvertToArray() {
        Object l = ConvertUtils.convert("Neo", String[].class);
        assertThat(l).isInstanceOf(String[].class);
        assertThat((String[]) l).containsOnly("Neo");

        Object d = ConvertUtils.convert("1977-05-07", Date[].class);
        assertThat(d).isNotNull().isInstanceOf(Date[].class);
        final Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 7);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar(((Date[]) d)[0]), cal)).isTrue();
    }

    @Test
    public void testConvertToList() {
        Object l = ConvertUtils.convert("Neo", List.class);
        assertThat(l).isInstanceOf(List.class);
        assertThat((List) l).containsOnly("Neo");
    }

    @BeforeClass
    public static void before() {
        ConvertUtils.deregister(Date.class);
        final DateConverter conv = new DateConverter();
        conv.setPattern("yyyy-MM-dd");
        conv.setLocale(Locale.GERMAN);
        conv.setUseLocaleFormat(true);
        ConvertUtils.register(conv, Date.class);
        ConvertUtils.register(new ArrayConverter(Object[].class, conv), Date[].class);
        ConvertUtils.register(new CollectionConverter(Object.class), List.class);
        ConvertUtils.register(new CollectionConverter(Object.class), Set.class);
    }
}
