Json Jerk
=========
Json Jerk is a flexible and fast JSON parser. It consists of several composable parts
for tokenizing, (un)escaping, parsing, and handling of semantic actions. Furthermore
it provides a light weight and type safe object model for JSON documents.

A simple example
----------------
The following example shows the simplest way to parse a JSON string:

    String json = "{\"hello\":\"world\"}";
    JsonObject jsonObject = FullJsonParser.parseObject(new UnescapingJsonTokenizer(json));

`FullJsonParser` is a utility class which provides methods for parsing (parts of) JSON
documents. Its `parseObject` method takes a `JsonTokenizer` as argument and tries to
parse an object from the input. In this case we've used a `UnescapingJsonTokenizer`
instance for the tokenizer. This class unescapes all escape sequences it encounters.
While this is generally the right thing to do, it can be very expensive in terms of
performance. If we'd rather want the tokenizer not to unescape, we would use an instance
of `DefaultJsonTokenizer`.

If parsing is successful, it results in a `JsonObject` instance. `JsonObject` is a
subclass of `JsonValue` and represents a JSON object. There is also a `JsonArray`
subclass and a `JsonAtom` subclass which represent JSON arrays and primitive JSON
values, respectively.

The object model
----------------
To see how to use the `JsonValue` classes consider this more complex example:

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
    StringBuilder sb = new StringBuilder();
    printObject(jsonObject, sb);

The `printObject` method prints all members of the JSON object by calling `printValue`
for each of them. The `printValue` method in turn determines the actual type of the
`JsonValue` and calls one of the respective print methods.

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

Using a visitor on the object model
-----------------------------------
Another way to travers the object model is by implementing a 'JsonValue.Visitor' and
passing it to the `accept` method of `JsonValue`. The following code - as the previous -
translate the object model back to JSON.

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

Both examples use the `escape` method of `JsonValue` for escaping strings.

Although these examples generate JSON from `JsonValue`, there is no need to
do this manually in general. In fact, the `toJson` method of `JsonValue` does
exactly this: it returns a JSON string.

Using call backs
----------------
The previous examples all created an object model of `JsonValue`s from a JSON
string. This is not the only way to parse a JSON document though. With Json
Jerk we can also use an `JsonHandler` to get notified of parse events. Inside
our `JsonHandler` we can then do whatever we want to handle these events. We
could for example build a custom object model.

Suppose we want to separately collect the keys of all values, object and
arrays of a JSON document. That is, we want to flat map these keys. Here is
how:

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

The `JsonParser` calls the corresponding call back on the `JsonHandler` whenever
it parses the respective JSON element. In contrast to other JSON parser, Json
Jerk does not do recursive decent automatically. That is, when it encounters
an object or an array, it will call the corresponding call back of the
`JsonHandler`. It is then the handlers responsibility to parser the object or
array itself. The easiest way to do this is to use the `JsonParser` and the
`JsonTokenizer` passed to the handler. This is exactly what the method's super
method does. So calling super is actually even easier.

Suppose we want to collect the keys as above but this time we want skip all keys
from nested structures. To achieve this we need to change the parsing of nested
structures: instead of calling super which effectively continues parsing with the
current parser, we need to parse the nested structures with a different parser
which does nothing but skip the whole structure:

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

In this example the super calls have been replaced by calls to new parsers which
have an empty `JsonHandler` and thus do nothing but skip the current array or
object.

In more complex scenarios, nested objects and arrays may be parsed with specific
`JsonHandler`s. The `FullJsonParser` used in earlier examples to build the object
models from JSON documents is implemented in this way: it uses a `JsonParser` and
supplied it with different `JsonHandler` instances to build the hierarchical
object model consisting of `JsonValue` objects.

Be lazy: level order parsing
----------------------------
Not having the parser to recursively descent on nested structures opens up some
unexpected possibilities: we don't actually need to parse a nested object on a
call to `JsonHandler.object`. What we could do instead, is to keep the state
of the `JsonTokenizer` and parse it later when needed.

This is what the `LevelOrderJsonParser` does. From the outside, this parser look
the same as the `FullJsonParser`. In contrast to the latter however, it does not
parse nested structures right away but rather keeps a reference to a parser for
them. When a nested structure is accessed through the corresponding `JsonValue`,
that parser is invoked to parse the structure. This lazy approach  results in a
level order traversal of the JSON document compared to the post order traversal
done by the `FullJsonParser`. Level order traversal can be beneficial if one
needs to find all sub objects of a given objects without wanting to cope with
deeper levels.