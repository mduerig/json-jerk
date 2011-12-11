/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package michid.jsonjerk;

import michid.jsonjerk.JsonValue.JsonArray;
import michid.jsonjerk.JsonValue.JsonAtom;
import michid.jsonjerk.JsonValue.JsonObject;
import michid.jsonjerk.JsonValue.Visitor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;

import static junit.framework.Assert.assertEquals;

public class JsonParserTest {
    private static final String JSON_IN = readFile("/test1.json");

    @Test
    public void testParser() {
        final StringBuilder jsonOut = new StringBuilder("{");
        new JsonParser(new JsonHandler() {
            @Override
            public void atom(Token key, Token value) {
                jsonOut.append(createKey(key));
                if (value.type() == Token.Type.STRING) {
                    jsonOut.append(quoteAndEscape(value.text()));
                }
                else {
                    jsonOut.append(value.text());
                }
            }

            @Override
            public void comma(Token token) {
                jsonOut.append(',');
            }

            @Override
            public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                jsonOut.append(createKey(key)).append('{');
                super.object(parser, key, tokenizer);
                jsonOut.append('}');
            }

            @Override
            public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                jsonOut.append(createKey(key)).append('[');
                super.array(parser, key, tokenizer);
                jsonOut.append(']');
            }

        }).parseObject(new UnescapingJsonTokenizer(JSON_IN));
        jsonOut.append('}');

        assertEquals(JSON_IN, jsonOut.toString());
    }

    @Test
    public void testParserWithBuilder() {
        final Stack<JsonValue> objects = new Stack<JsonValue>();
        objects.push(new JsonObject(new LinkedHashMap<String, JsonValue>()));

        new JsonParser(new JsonHandler() {
            @Override
            public void atom(Token key, Token value) {
               put(key, new JsonAtom(value));
            }

            @Override
            public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                JsonObject object = new JsonObject(new LinkedHashMap<String, JsonValue>());
                put(key, object);
                objects.push(object);
                super.object(parser, key, tokenizer);
                objects.pop();
            }

            @Override
            public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                JsonArray array = new JsonArray(new ArrayList<JsonValue>());
                put(key, array);
                objects.push(array);
                super.array(parser, key, tokenizer);
                objects.pop();
            }

            private void put(final Token key, final JsonValue value) {
                objects.peek().accept(new Visitor(){
                    @Override
                    public void visit(JsonArray array) {
                        array.add(value);
                    }
                    @Override
                    public void visit(JsonObject object) {
                        object.put(key.text(), value);
                    }
                });
            }

        }).parseObject(new UnescapingJsonTokenizer(JSON_IN));

        assertEquals(1, objects.size());
        assertEquals(JSON_IN, objects.peek().toJson());
    }


    @Test(expected = ParseException.class)
    public void testParseExceptionValue1() {
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parseObject(new UnescapingJsonTokenizer("{\"key\":}"));
    }

    @Test(expected = ParseException.class)
    public void testParseExceptionValue2() {
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parseObject(new UnescapingJsonTokenizer("{\"key\":[1,]}"));
    }

    @Test(expected = ParseException.class)
    public void testParseExceptionPair1() {
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parseObject(new UnescapingJsonTokenizer("{\"key\":1,}"));
    }

    @Test(expected = ParseException.class)
    public void testParseExceptionPair2() {
        JsonParser parser = new JsonParser(new JsonHandler());
        parser.parsePair(new UnescapingJsonTokenizer(""));
    }

    @Test
    public void testJson1() {
        JsonObject object1 = FullJsonParser.parseObject(new UnescapingJsonTokenizer(JSON_IN));
        assertEquals(JSON_IN, object1.toJson());

        JsonObject object2 = LevelOrderJsonParser.parseObject(new UnescapingJsonTokenizer(JSON_IN));
        assertEquals(JSON_IN, object2.toJson());

        assertEquals(object1, object2);
        assertEquals(object1.toJson(), object2.toJson());
    }

    @Test
    public void testJson2() {
        String json = readFile("/test2.json");
        JsonObject object1 = FullJsonParser.parseObject(new UnescapingJsonTokenizer(json));
        JsonObject object2 = LevelOrderJsonParser.parseObject(new UnescapingJsonTokenizer(json));
        assertEquals(object1, object2);
        assertEquals(object1.toJson(), object2.toJson());
    }

    //------------------------------------------< private >---

    private static String readFile(String fileName) {
        InputStream is = JsonParserTest.class.getResourceAsStream(fileName);
        if (is == null) {
            throw new RuntimeException("Resource not found: " + fileName);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String s;
            StringBuilder sb = new StringBuilder();
            while((s = reader.readLine()) != null) {
                sb.append(s);
            }

            reader.close();
            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createKey(Token key) {
        return key == null ? "" : quoteAndEscape(key.text()) + ':';
    }

    private static String quoteAndEscape(String text) {
        return '\"' + JsonValue.escape(text) + '\"';
    }

}
