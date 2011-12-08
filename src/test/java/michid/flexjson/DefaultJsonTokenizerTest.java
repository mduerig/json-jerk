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

import michid.flexjson.DefaultJsonTokenizer;
import michid.flexjson.JsonTokenizer;
import michid.flexjson.Token;
import michid.flexjson.Token.Type;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DefaultJsonTokenizerTest {

    private static final Token[] TOKENS = new Token[] {
        new Token(Type.BEGIN_OBJECT, "{", 0),
        new Token(Type.END_OBJECT, "}", 2),
        new Token(Type.BEGIN_ARRAY, "[", 4),
        new Token(Type.END_ARRAY, "]", 6),
        new Token(Type.COLON, ":", 8),
        new Token(Type.COMMA, ",", 10),
        new Token(Type.TRUE, "true", 12),
        new Token(Type.FALSE, "false", 17),
        new Token(Type.NULL, "null", 23),
        new Token(Type.STRING, "string", 28),
        new Token(Type.NUMBER, "-12.34e-56", 37),
        new Token(Type.STRING, "ab\\\"", 48),
        new Token(Type.STRING, "ab\\\\", 55),
        new Token(Type.STRING, "", 62),
        new Token(Type.STRING, "\\\"", 65),
        new Token(Type.STRING, "\\\\", 70),
        new Token(Type.UNKNOWN, "qwe", 75)
};

    private static final String TOKEN_STRING = join(TOKENS);

    private static final Token EOF_TOKEN = new Token(Type.EOF, "", TOKEN_STRING.length());

    @Test
    public void testRead() {
        JsonTokenizer tokenizer = new DefaultJsonTokenizer(TOKEN_STRING);

        for (Token token : TOKENS) {
            int pos = tokenizer.pos();
            assertEquals(token, tokenizer.read());
            assertEquals(pos, token.pos());
        }
        assertEquals(EOF_TOKEN, tokenizer.read());
    }

    @Test
    public void testPeek() {
        JsonTokenizer tokenizer = new DefaultJsonTokenizer(TOKEN_STRING);

        for (Token token : TOKENS) {
            Token peeked = tokenizer.peek();
            assertEquals(token, peeked);
            assertEquals(token.pos(), peeked.pos());

            for (Token t : TOKENS) {
                if (t.type() != token.type()) {
                    assertFalse(tokenizer.peek(t.type()));
                }
            }
            assertEquals(peeked, tokenizer.read(peeked.type()));
        }
        assertEquals(EOF_TOKEN, tokenizer.peek());
    }

    @Test
    public void testSkip() {
        JsonTokenizer tokenizer = new DefaultJsonTokenizer(TOKEN_STRING);

        for (Token token : TOKENS) {
            Token peeked = tokenizer.peek();
            assertEquals(token, peeked);

            for (Token t : TOKENS) {
                if (t.type() != token.type()) {
                    assertFalse(tokenizer.skip(t.type()));
                }
            }
            assertTrue(tokenizer.skip(token.type()));
        }
        assertEquals(EOF_TOKEN, tokenizer.peek());
    }

    //------------------------------------------< private >---

    private static String join(Token[] tokens) {
        StringBuilder json = new StringBuilder();
        for (Token token : tokens) {
            json.append(getString(token)).append(' ');
        }
        return json.toString();
    }

    private static String getString(Token token) {
        return token.type() == Type.STRING
            ? '\"' + token.text() + '\"'
            : token.text();
    }

}
