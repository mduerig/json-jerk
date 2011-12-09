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

package michid.flexjson;

import michid.flexjson.JsonValue.JsonArray;
import michid.flexjson.JsonValue.JsonAtom;
import michid.flexjson.JsonValue.JsonObject;
import michid.flexjson.JsonValue.Type;
import michid.flexjson.JsonValue.Visitor;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Examples {

    @Test
    public void simple() {
        String json = "{\"hello\":\"world\"}";
        JsonObject jsonObject = FullJsonParser.parseObject(new UnescapingJsonTokenizer(json));
    }

    @Test
    public void objectModel() {
        String json =
            "{\"string\":\"value\"," +
            "\"number\":1.42," +
            "\"null\":null," +
            "\"false\":false," +
            "\"true\":true," +
            "\"nestedArray\":[[],[[],[]]]," +
            "\"array\":[1,2,3]," +
            "\"object\":{" +
                "\"size\":212," +
                "\"empty\":\"\"," +
                "\"array\":[1,2,3,{\"number\":142,\"array\":[1,2,3,{}]}]," +
                "\"object\":{}" +
            "}}";

        JsonObject jsonObject = FullJsonParser.parseObject(new UnescapingJsonTokenizer(json));
        StringBuilder jsonAgain = new StringBuilder();
        printObject(jsonObject, jsonAgain);
    }

    private static void printValue(JsonValue jsonValue, StringBuilder sb) {
        switch (jsonValue.type()) {
            case OBJECT: 
                printObject(jsonValue.asObject(), sb);
                break;
            case ARRAY:
                printArray(jsonValue.asArray(), sb);
                break;
            default:
                printAtom(jsonValue.asAtom(), sb);
        }
    }

    private static void printObject(JsonObject object, StringBuilder sb) {
        sb.append('{');
        String separator = "";
        for (Entry<String, JsonValue> pair : object.value().entrySet()) {
            sb.append(separator);
            separator = ",";
            sb.append('"').append(pair.getKey()).append("\":");
            printValue(pair.getValue(), sb);
        }
        sb.append('}');
    }

    private static void printArray(JsonArray array, StringBuilder sb) {
        sb.append('[');
        String separator = "";
        for (JsonValue value : array.value()) {
            sb.append(separator);
            separator = ",";
            printValue(value, sb);
        }
        sb.append(']');
    }

    private static void printAtom(JsonAtom atom, StringBuilder sb) {
        if (atom.type() == Type.STRING) {
            sb.append('"').append(JsonValue.escape(atom.value())).append('"');
        }
        else {
            sb.append(atom.value());
        }
    }

    @Test
    public void objectModelVisitor() {
        String json =
                "{\"string\":\"value\"," +
                        "\"number\":1.42," +
                        "\"null\":null," +
                        "\"false\":false," +
                        "\"true\":true," +
                        "\"nestedArray\":[[],[[],[]]]," +
                        "\"array\":[1,2,3]," +
                        "\"object\":{" +
                        "\"size\":212," +
                        "\"empty\":\"\"," +
                        "\"array\":[1,2,3,{\"number\":142,\"array\":[1,2,3,{}]}]," +
                        "\"object\":{}" +
                        "}}";

        JsonObject jsonObject = FullJsonParser.parseObject(new UnescapingJsonTokenizer(json));

        final StringBuilder sb = new StringBuilder();
        jsonObject.accept(new Visitor() {
            @Override
            public void visit(JsonAtom atom) {
                if (atom.type() == Type.STRING) {
                    sb.append('"').append(JsonValue.escape(atom.value())).append('"');
                }
                else {
                    sb.append(atom.value());
                }
            }

            @Override
            public void visit(JsonArray array) {
                sb.append('[');
                String separator = "";
                for (JsonValue value : array.value()) {
                    sb.append(separator);
                    separator = ",";
                    value.accept(this);
                }
                sb.append(']');
            }

            @Override
            public void visit(JsonObject object) {
                sb.append('{');
                String separator = "";
                for (Entry<String, JsonValue> entry : object.value().entrySet()) {
                    sb.append(separator);
                    separator = ",";
                    sb.append('"').append(entry.getKey()).append("\":");
                    entry.getValue().accept(this);
                }
                sb.append('}');
            }
        });
    }

    @Test
    public void jsonHandler() {
        String json = "{\"one\":1,\"two\":{\"three\":3,\"four\":[]}}";
        final Set<String> atoms = new HashSet<String>();
        final Set<String> objects = new HashSet<String>();
        final Set<String> arrays = new HashSet<String>();

        new JsonParser(new JsonHandler(){
            @Override
            public void atom(Token key, Token value) {
                atoms.add(key.text());
            }

            @Override
            public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                objects.add(key.text());
                super.object(parser, key, tokenizer);
            }

            @Override
            public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                arrays.add(key.text());
                super.array(parser, key, tokenizer);
            }
        }).parseObject(new UnescapingJsonTokenizer(json));
    }

    @Test
    public void jsonHandler2() {
        String json = "{\"one\":1,\"two\":{\"three\":3,\"four\":[]}}";
        final Set<String> atoms = new HashSet<String>();
        final Set<String> objects = new HashSet<String>();
        final Set<String> arrays = new HashSet<String>();

        new JsonParser(new JsonHandler(){
            @Override
            public void atom(Token key, Token value) {
                atoms.add(key.text());
            }

            @Override
            public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                objects.add(key.text());
                new JsonParser(new JsonHandler()).parseObject(tokenizer);
            }

            @Override
            public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
                arrays.add(key.text());
                new JsonParser(new JsonHandler()).parseArray(tokenizer);
            }
        }).parseObject(new UnescapingJsonTokenizer(json));
    }

}
