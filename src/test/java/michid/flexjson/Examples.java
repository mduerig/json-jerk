package michid.flexjson;

import michid.flexjson.JsonValue.JsonArray;
import michid.flexjson.JsonValue.JsonAtom;
import michid.flexjson.JsonValue.JsonObject;
import michid.flexjson.JsonValue.Type;
import michid.flexjson.JsonValue.Visitor;
import org.junit.Test;

import java.util.Map.Entry;

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


}
