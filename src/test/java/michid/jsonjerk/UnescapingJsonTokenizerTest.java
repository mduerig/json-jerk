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

import michid.jsonjerk.Token.Type;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class UnescapingJsonTokenizerTest {

    private static final Token[] TOKENS = new Token[] {
        new Token(Type.STRING, "foobar", 0),
        new Token(Type.STRING, "foo\\bar", 9),
        new Token(Type.STRING, "foobar", 20),
        new Token(Type.STRING, "foobar", 33),
        new Token(Type.STRING, "foo\bbar", 48),
        new Token(Type.STRING, "foo\tbar", 59),
        new Token(Type.STRING, "foo\nbar", 70),
        new Token(Type.STRING, "foo\fbar", 81),
        new Token(Type.STRING, "foo\rbar", 92),
        new Token(Type.STRING, "foo\"bar", 103),
    };

    private static final Token EOF_TOKEN = new Token(Type.EOF, "", 113);

    @Test
    public void test() {
        JsonTokenizer tokenizer = new UnescapingJsonTokenizer("\"foobar\" \"foo\\\\bar\" " +
                "\"foo\\x62bar\" \"foo\\u0062bar\" \"foo\\bbar\" \"foo\\tbar\" \"foo\\nbar\" " +
                "\"foo\\fbar\" \"foo\\rbar\" \"foo\\\"bar\"");

        for (Token token : TOKENS) {
            Token t = tokenizer.read();
            assertEquals(token, t);
            assertEquals(token.pos(),  t.pos());
        }
        Token t = tokenizer.read();
        assertEquals(EOF_TOKEN, t);
        assertEquals(EOF_TOKEN.pos(), t.pos());
    }
}
