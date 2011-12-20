package michid.jsonjerk;

import michid.jsonjerk.JsonReaders.CompoundReader;
import michid.jsonjerk.JsonReaders.Factory;
import michid.jsonjerk.JsonReaders.IntReader;
import michid.jsonjerk.JsonReaders.ObjectReader;
import michid.jsonjerk.JsonReaders.StringReader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonReaderExamples {
    private static final String JSON = '{' +
        "\"name\":\"Harry\"," +
        "\"age\":42," +
        "\"likes\":[\"cats\",\"rats\"]," +
        "\"father\":{" +
            "\"name\":\"Bret\",\"age\":68,\"likes\":[\"flowers\",\"kids\"]}," +
        "\"mother\":{" +
            "\"name\":\"Linda\",\"likes\":[],\"age\":66}," +
        "\"children\":[" +
            "{\"age\":12,\"name\":\"Ann\",\"likes\":[\"mommy\",\"Willy\"],\"father\":{\"name\":\"Harry\"}}," +
            "{\"name\":\"Willy\"}]" +
        '}';

    @Test
    public void jsonReader1() {
        Person1 harry = Person1.read(JSON);
        assertEquals("Harry", harry.name);
        assertEquals(42, harry.age);
        assertNotNull(harry.mother);
        assertNotNull(harry.father);
        assertEquals(2, harry.children.size());
        assertEquals(2, harry.likes.size());
        assertEquals("cats", harry.likes.get(0));
        assertEquals("rats", harry.likes.get(1));
        
        Person1 bret = harry.father;
        assertEquals("Bret", bret.name);
        assertEquals(68, bret.age);
        assertNull(bret.mother);
        assertNull(bret.father);
        assertEquals(2, bret.likes.size());
        assertEquals(0, bret.children.size());
        assertEquals("flowers", bret.likes.get(0));
        assertEquals("kids", bret.likes.get(1));
        
        Person1 linda = harry.mother;
        assertEquals("Linda", linda.name);
        assertEquals(66, linda.age);
        assertNull(linda.mother);
        assertNull(linda.father);
        assertEquals(0, linda.children.size());
        assertEquals(0, linda.likes.size());

        Person1 ann = harry.children.get(0);
        assertEquals("Ann", ann.name);
        assertEquals(12, ann.age);
        assertNull(ann.mother);
        assertNotNull(ann.father);
        assertEquals("Harry", ann.father.name);
        assertEquals(0, ann.children.size());
        assertEquals(2, ann.likes.size());
        assertEquals("mommy", ann.likes.get(0));
        assertEquals("Willy", ann.likes.get(1));

        Person1 willy = harry.children.get(1);
        assertEquals("Willy", willy.name);
        assertEquals(0, willy.age);
        assertNull(willy.mother);
        assertNull(willy.father);
        assertEquals(0, willy.children.size());
        assertEquals(0, willy.likes.size());
    }

    /**
     * Person which can deserialize itself from JSON
     */
    static class Person1 extends CompoundReader<Person1> {
        static final Factory<Person1> FACTORY = new Factory<Person1>() {
            @Override
            public Person1 create() {
                return new Person1();
            }
        };

        String name; {
            setReader("name", new StringReader() {
                @Override
                public void set(String value) {
                    name = value;
                }
            });
        }

        int age; {
            setReader("age", new IntReader() {
                @Override
                public void set(Integer value) {
                    age = value;
                }
            });
        }

        Person1 mother; {
            setReader("mother", new ObjectReader<Person1>(FACTORY) {
                @Override
                public void set(Person1 value) {
                    mother = value;
                }
            });
        }

        Person1 father; {
            setReader("father", new ObjectReader<Person1>(FACTORY) {
                @Override
                public void set(Person1 value) {
                    father = value;
                }
            });
        }

        List<Person1> children = new ArrayList<Person1>(); {
            setReader("children", new ObjectReader<Person1>(FACTORY) {
                @Override
                public void set(Person1 value) {
                    children.add(value);
                }
            });
        }

        List<String> likes = new ArrayList<String>(); {
            setReader("likes", new StringReader() {
                @Override
                public void set(String value) {
                    likes.add(value);
                }
            });
        }

        @Override
        public Person1 getContainer() {
            return this;
        }

        static Person1 read(String json) {
            Person1 person = new Person1();
            new JsonParser(person).parseObject(new DefaultJsonTokenizer(json));
            return person;
        }
    }


    @Test
    public void jsonReader2() {
        Person2 harry = new PersonReader().read(JSON);
        assertEquals("Harry", harry.name);
        assertEquals(42, harry.age);
        assertNotNull(harry.mother);
        assertNotNull(harry.father);
        assertEquals(2, harry.children.size());
        assertEquals(2, harry.likes.size());
        assertEquals("cats", harry.likes.get(0));
        assertEquals("rats", harry.likes.get(1));

        Person2 bret = harry.father;
        assertEquals("Bret", bret.name);
        assertEquals(68, bret.age);
        assertNull(bret.mother);
        assertNull(bret.father);
        assertEquals(2, bret.likes.size());
        assertEquals(0, bret.children.size());
        assertEquals("flowers", bret.likes.get(0));
        assertEquals("kids", bret.likes.get(1));

        Person2 linda = harry.mother;
        assertEquals("Linda", linda.name);
        assertEquals(66, linda.age);
        assertNull(linda.mother);
        assertNull(linda.father);
        assertEquals(0, linda.children.size());
        assertEquals(0, linda.likes.size());

        Person2 ann = harry.children.get(0);
        assertEquals("Ann", ann.name);
        assertEquals(12, ann.age);
        assertNull(ann.mother);
        assertNotNull(ann.father);
        assertEquals("Harry", ann.father.name);
        assertEquals(0, ann.children.size());
        assertEquals(2, ann.likes.size());
        assertEquals("mommy", ann.likes.get(0));
        assertEquals("Willy", ann.likes.get(1));

        Person2 willy = harry.children.get(1);
        assertEquals("Willy", willy.name);
        assertEquals(0, willy.age);
        assertNull(willy.mother);
        assertNull(willy.father);
        assertEquals(0, willy.children.size());
        assertEquals(0, willy.likes.size());
    }

    /**
     * Reader for deserializing Person2 instance from JSON
     */
    static class PersonReader extends CompoundReader<Person2> implements Factory<PersonReader> {
        Person2 person = new Person2();

        PersonReader() {
            setReader("name", new StringReader() {
                @Override
                public void set(String value) {
                    person.name = value;
                }
            });
            setReader("age", new IntReader() {
                @Override
                public void set(Integer value) {
                    person.age = value;
                }
            });
            setReader("mother", new ObjectReader<Person2>(this) {
                @Override
                public void set(Person2 value) {
                    person.mother = value;
                }
            });
            setReader("father", new ObjectReader<Person2>(this) {
                @Override
                public void set(Person2 value) {
                    person.father = value;
                }
            });
            setReader("likes", new StringReader() {
                @Override
                public void set(String value) {
                    person.likes.add(value);
                }
            });
            setReader("children", new ObjectReader<Person2>(this) {
                @Override
                public void set(Person2 value) {
                    person.children.add(value);
                }
            });
        }

        @Override
        public PersonReader create() {
            return new PersonReader();
        }

        @Override
        public Person2 getContainer() {
            return person;
        }

        public Person2 read(String json) {
            new JsonParser(this).parseObject(new DefaultJsonTokenizer(json));
            return person;
        }
    }

    static class Person2 {
        String name;
        int age;
        Person2 mother;
        Person2 father;
        List<String> likes = new ArrayList<String>();
        List<Person2> children = new ArrayList<Person2>();
    }

}
