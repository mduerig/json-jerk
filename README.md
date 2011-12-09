Flex Json
=========

Flex Json is a flexible and fast JSON parser. Flex Json consists of several
composable parts for tokenizing, (un)escaping, parsing, and handling of semantic actions.
Furthermore it provides a light weight and type safe object model for JSON documents.

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
of `DefaultJsonTokenizer`. If parsing is successful, it results in a `JsonObject`
instance. `JsonObject` is a subclass of `JsonValue` and represents a JSON object. There
is also a `JsonArray` subclass and a `JsonAtom` subclass which represent JSON arrays
and primitive JSON values, respectively.

The Object model
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
passing it to `accept` method of `JsonValue`. The following code - as the previous -
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


