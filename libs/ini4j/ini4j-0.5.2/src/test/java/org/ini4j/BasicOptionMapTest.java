/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j;

import org.ini4j.sample.Dwarf;
import org.ini4j.sample.DwarfBean;

import org.ini4j.test.DwarfsData;
import org.ini4j.test.DwarfsData.DwarfData;
import org.ini4j.test.Helper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.URI;

public class BasicOptionMapTest extends Ini4jCase
{
    private static final String FOO = "foo";
    private static BasicOptionMap _map;

    static
    {
        _map = new BasicOptionMap();
        _map.putAll(Helper.newDwarfsOpt());
    }

    @Test public void test_bug_2817403() throws Exception
    {
        OptionMap map = new BasicOptionMap();

        map.add("player.name", "Joe");
        map.add("player.greeting", "Hi ${player.name}!");
        map.add("player.domain", "foo.bar");
        map.add("player.email", "${player.name}@${player.domain}");

        //
        assertEquals("Joe", map.fetch("player.name"));
        assertEquals("Hi Joe!", map.fetch("player.greeting"));
        assertEquals("foo.bar", map.fetch("player.domain"));
        assertEquals("Joe@foo.bar", map.fetch("player.email"));
    }

    @Test public void testAddPutNullAndString()
    {
        OptionMap map = new BasicOptionMap();
        Object o;

        // null
        o = null;
        map.add(Dwarf.PROP_AGE, o);
        assertNull(map.get(Dwarf.PROP_AGE));
        map.put(Dwarf.PROP_AGE, new Integer(DwarfsData.doc.age));
        assertNotNull(map.get(Dwarf.PROP_AGE));
        map.add(Dwarf.PROP_AGE, o, 0);
        assertNull(map.get(Dwarf.PROP_AGE, 0));
        map.put(Dwarf.PROP_AGE, new Integer(DwarfsData.doc.age), 0);
        assertNotNull(map.get(Dwarf.PROP_AGE, 0));
        map.put(Dwarf.PROP_AGE, o, 0);
        assertNull(map.get(Dwarf.PROP_AGE, 0));
        map.remove(Dwarf.PROP_AGE);
        map.put(Dwarf.PROP_AGE, o);
        assertNull(map.get(Dwarf.PROP_AGE));

        // str
        map.remove(Dwarf.PROP_AGE);
        o = String.valueOf(DwarfsData.doc.age);
        map.add(Dwarf.PROP_AGE, o);
        assertEquals(o, map.get(Dwarf.PROP_AGE));
        map.remove(Dwarf.PROP_AGE);
        map.put(Dwarf.PROP_AGE, o);
        assertEquals(o, map.get(Dwarf.PROP_AGE));
        o = String.valueOf(DwarfsData.happy.age);
        map.add(Dwarf.PROP_AGE, o, 0);
        assertEquals(new Integer(DwarfsData.happy.age), (Integer) map.get(Dwarf.PROP_AGE, 0, int.class));
        o = String.valueOf(DwarfsData.doc.age);
        map.put(Dwarf.PROP_AGE, o, 0);
        assertEquals(DwarfsData.doc.age, (int) map.get(Dwarf.PROP_AGE, 0, int.class));
    }

    @Test public void testFetch()
    {
        OptionMap map = new BasicOptionMap();

        Helper.addDwarf(map, DwarfsData.dopey, false);
        Helper.addDwarf(map, DwarfsData.bashful);
        Helper.addDwarf(map, DwarfsData.doc);

        // dopey
        assertEquals(DwarfsData.dopey.weight, map.fetch(Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        map.add(Dwarf.PROP_HEIGHT, map.get(Dwarf.PROP_HEIGHT));
        assertEquals(DwarfsData.dopey.height, map.fetch(Dwarf.PROP_HEIGHT, 1, double.class), Helper.DELTA);

        // sneezy
        map.clear();
        Helper.addDwarf(map, DwarfsData.happy);
        Helper.addDwarf(map, DwarfsData.sneezy, false);
        assertEquals(DwarfsData.sneezy.homePage, map.fetch(Dwarf.PROP_HOME_PAGE, URI.class));

        // null
        map = new BasicOptionMap();
        map.add(Dwarf.PROP_AGE, null);
        assertNull(map.fetch(Dwarf.PROP_AGE, 0));
    }

    @Test public void testFetchAllException()
    {
        OptionMap map = new BasicOptionMap();

        try
        {
            map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, String.class);
            missing(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException x)
        {
            //
        }
    }

    @Test public void testFromToAs() throws Exception
    {
        DwarfBean bean = new DwarfBean();

        _map.to(bean);
        Helper.assertEquals(DwarfsData.dopey, bean);
        OptionMap map = new BasicOptionMap();

        map.from(bean);
        bean = new DwarfBean();
        map.to(bean);
        Helper.assertEquals(DwarfsData.dopey, bean);
        Dwarf proxy = map.as(Dwarf.class);

        Helper.assertEquals(DwarfsData.dopey, proxy);
        map.clear();
        _map.to(proxy);
        Helper.assertEquals(DwarfsData.dopey, proxy);
    }

    @Test public void testFromToAsPrefixed() throws Exception
    {
        fromToAs(DwarfsData.bashful);
        fromToAs(DwarfsData.doc);
        fromToAs(DwarfsData.dopey);
        fromToAs(DwarfsData.grumpy);
        fromToAs(DwarfsData.happy);
        fromToAs(DwarfsData.sleepy);
        fromToAs(DwarfsData.sneezy);
    }

    @Test public void testGet()
    {
        OptionMap map = new BasicOptionMap();

        // bashful
        Helper.addDwarf(map, DwarfsData.bashful, false);
        assertEquals(DwarfsData.bashful.weight, map.get(Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        map.add(Dwarf.PROP_HEIGHT, map.get(Dwarf.PROP_HEIGHT));
        assertEquals(DwarfsData.bashful.height, map.get(Dwarf.PROP_HEIGHT, 1, double.class), Helper.DELTA);
        assertEquals(DwarfsData.bashful.homePage, map.fetch(Dwarf.PROP_HOME_PAGE, URI.class));
    }

    @Test public void testGetAllException()
    {
        OptionMap map = new BasicOptionMap();

        try
        {
            map.getAll(Dwarf.PROP_FORTUNE_NUMBER, String.class);
            missing(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException x)
        {
            //
        }
    }

    @Test public void testGetAndFetchDefaultValue()
    {
        OptionMap map = new BasicOptionMap();

        Helper.addDwarf(map, DwarfsData.dopey, false);
        Helper.addDwarf(map, DwarfsData.bashful);
        Helper.addDwarf(map, DwarfsData.doc);

        // fetch with type
        assertEquals(DwarfsData.dopey.weight, map.fetch(Dwarf.PROP_WEIGHT, double.class), Helper.DELTA);
        assertEquals(DwarfsData.dopey.weight, map.fetch(Dwarf.PROP_WEIGHT, double.class, 1.2), Helper.DELTA);
        map.remove(Dwarf.PROP_WEIGHT);
        assertEquals(1.2, map.fetch(Dwarf.PROP_WEIGHT, double.class, 1.2), Helper.DELTA);

        // get with type
        assertEquals(DwarfsData.dopey.age, (int) map.get(Dwarf.PROP_AGE, int.class));
        assertEquals(DwarfsData.dopey.age, (int) map.get(Dwarf.PROP_AGE, int.class, 11));
        map.remove(Dwarf.PROP_AGE);
        assertEquals(11, (int) map.get(Dwarf.PROP_AGE, int.class, 11));

        // get and fetch with strings
        assertEquals(DwarfsData.dopey.homePage.toString(), map.fetch(Dwarf.PROP_HOME_PAGE, FOO));
        assertEquals(DwarfsData.dopey.homePage.toString(), map.get(Dwarf.PROP_HOME_PAGE, FOO));
        map.remove(Dwarf.PROP_HOME_PAGE);
        assertEquals(FOO, map.fetch(Dwarf.PROP_HOME_PAGE, FOO));
        assertEquals(FOO, map.get(Dwarf.PROP_HOME_PAGE, FOO));
    }

    @Test public void testPropertyFirstUpper()
    {
        DwarfBean bean;
        OptionMap map = new BasicOptionMap(true);

        map.from(DwarfsData.bashful);
        assertTrue(map.containsKey("Age"));
        assertTrue(map.containsKey("Height"));
        assertTrue(map.containsKey("Weight"));
        assertTrue(map.containsKey("HomePage"));
        assertTrue(map.containsKey("HomeDir"));
        bean = new DwarfBean();
        map.to(bean);
        Helper.assertEquals(DwarfsData.bashful, bean);
        Helper.assertEquals(DwarfsData.bashful, map.as(Dwarf.class));
    }

    @Test public void testPut()
    {
        OptionMap map = new BasicOptionMap();

        map.add(Dwarf.PROP_AGE, new Integer(DwarfsData.sneezy.age));
        map.put(Dwarf.PROP_HEIGHT, new Double(DwarfsData.sneezy.height));
        map.add(Dwarf.PROP_HOME_DIR, DwarfsData.sneezy.homeDir);
        map.add(Dwarf.PROP_WEIGHT, new Double(DwarfsData.sneezy.weight), 0);
        map.put(Dwarf.PROP_HOME_PAGE, null);
        map.put(Dwarf.PROP_HOME_PAGE, DwarfsData.sneezy.homePage);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, new Integer(DwarfsData.sneezy.fortuneNumber[1]));
        map.add(Dwarf.PROP_FORTUNE_NUMBER, new Integer(DwarfsData.sneezy.fortuneNumber[2]));
        map.add(Dwarf.PROP_FORTUNE_NUMBER, new Integer(0));
        map.put(Dwarf.PROP_FORTUNE_NUMBER, new Integer(DwarfsData.sneezy.fortuneNumber[3]), 2);
        map.add(Dwarf.PROP_FORTUNE_NUMBER, new Integer(DwarfsData.sneezy.fortuneNumber[0]), 0);
        Helper.assertEquals(DwarfsData.sneezy, map.as(Dwarf.class));
    }

    @Test public void testPutAllException()
    {
        OptionMap map = new BasicOptionMap();

        try
        {
            map.putAll(Dwarf.PROP_FORTUNE_NUMBER, new Integer(0));
            missing(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException x)
        {
            //
        }
    }

    @Test public void testPutGetFetchAll()
    {
        OptionMap map = new BasicOptionMap();

        map.putAll(Dwarf.PROP_FORTUNE_NUMBER, DwarfsData.sneezy.fortuneNumber);
        assertEquals(DwarfsData.sneezy.fortuneNumber.length, map.length(Dwarf.PROP_FORTUNE_NUMBER));
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, map.getAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class));
        assertArrayEquals(DwarfsData.sneezy.fortuneNumber, map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class));
        map.putAll(Dwarf.PROP_FORTUNE_NUMBER, (int[]) null);
        assertEquals(0, map.length(Dwarf.PROP_FORTUNE_NUMBER));
        assertEquals(0, map.getAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class).length);
        assertEquals(0, map.fetchAll(Dwarf.PROP_FORTUNE_NUMBER, int[].class).length);
    }

    @Test public void testResolve() throws Exception
    {
        StringBuilder buffer;
        String input;

        // simple value
        input = "${height}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals("" + DwarfsData.dopey.getHeight(), buffer.toString());

        // system property
        input = "${@prop/user.home}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(System.getProperty("user.home"), buffer.toString());

        // system environment
        input = "${@env/PATH}";
        buffer = new StringBuilder(input);
        try
        {
            _map.resolve(buffer);
            assertEquals(System.getenv("PATH"), buffer.toString());
        }
        catch (Error e)
        {
            // retroweaver + JDK 1.4 throws Error on getenv
        }

        // unknown variable
        input = "${no such name}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // small input
        input = "${";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // incorrect references
        input = "${weight";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // empty references
        input = "jim${}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals(input, buffer.toString());

        // escaped references
        input = "${weight}";
        buffer = new StringBuilder(input);

        _map.resolve(buffer);
        assertEquals("" + DwarfsData.dopey.getWeight(), buffer.toString());
        input = "\\" + input;
        buffer = new StringBuilder(input);

        assertEquals(input, buffer.toString());
    }

    private void fromToAs(DwarfData dwarf)
    {
        String prefix = dwarf.name + '.';
        DwarfBean bean = new DwarfBean();

        _map.to(bean, prefix);
        Helper.assertEquals(dwarf, bean);
        OptionMap map = new BasicOptionMap();

        map.from(bean, prefix);
        bean = new DwarfBean();
        map.to(bean, prefix);
        Helper.assertEquals(dwarf, bean);
        Dwarf proxy = map.as(Dwarf.class, prefix);

        Helper.assertEquals(dwarf, proxy);
        map.clear();
        _map.to(proxy, prefix);
        Helper.assertEquals(dwarf, proxy);
    }
}
