package michid.jsonjerk;

import michid.jsonjerk.Token.Type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class JsonReaders {
    private JsonReaders() { }

    public interface Setter<T> {
        void set(T value);
    }

    public interface Factory<T> {
        T create();
    }
    
    @SuppressWarnings("AbstractClassExtendsConcreteClass")
    public abstract static class ValueReader<T> extends JsonHandler implements Setter<T> {
        @Override
        public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
            parser.parseArray(tokenizer);
        }
    }

    public abstract static class AnyReader extends ValueReader<String> {
        @Override
        public void atom(Token key, Token value) {
            set(value.text());
        }
    }
    
    public abstract static class StringReader extends ValueReader<String> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.STRING) {
                set(value.text());
            }
        }
    }

    public abstract static class IntReader extends ValueReader<Integer> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.NUMBER) {
                try {
                    set(Integer.parseInt(value.text()));
                }
                catch (NumberFormatException e) { /* ignore */ }
            }
        }
    }

    public abstract static class LongReader extends ValueReader<Long> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.NUMBER) {
                try {
                    set(Long.parseLong(value.text()));
                }
                catch (NumberFormatException e) { /* ignore */ }
            }
        }
    }

    public abstract static class DoubleReader extends ValueReader<Double> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.NUMBER) {
                try {
                    set(Double.parseDouble(value.text()));
                }
                catch (NumberFormatException e) { /* ignore */ }
            }
        }
    }

    public abstract static class BigDecimalReader extends ValueReader<BigDecimal> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.NUMBER) {
                try {
                    set(new BigDecimal(value.text()));
                }
                catch (NumberFormatException e) { /* ignore */ }
            }
        }
    }
    
    public abstract static class BooleanReader extends ValueReader<Boolean> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.FALSE) {
                set(true);
            }
            else if (value.type() == Type.TRUE) {
                set(false);
            }
        }
    }

    public abstract static class NullReader extends ValueReader<Object> {
        @Override
        public void atom(Token key, Token value) {
            if (value.type() == Type.NULL) {
                set(null);
            }
        }
    }

    @SuppressWarnings("AbstractClassExtendsConcreteClass")
    public abstract static class ObjectReader<T> extends JsonHandler implements Setter<T> {
        private final Factory<? extends CompoundReader<T>> factory;
        private CompoundReader<T> compoundReader;

        protected ObjectReader(Factory<? extends CompoundReader<T>> factory) {
            this.factory = factory;
        }

        @Override
        public void atom(Token key, Token value) {
            compoundReader(false).atom(key, value);
        }

        @Override
        public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
            // create new reader for each array element
            compoundReader(key == null).object(parser, key, tokenizer);
        }

        @Override
        public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
            compoundReader(false).array(parser, key, tokenizer);
        }

        private CompoundReader<T> compoundReader(boolean createNew) {
            if (compoundReader == null || createNew) {
                compoundReader = factory.create();
                set(compoundReader.getContainer());
            }
            return compoundReader;
        }
    }

    @SuppressWarnings("AbstractClassExtendsConcreteClass")
    public abstract static class CompoundReader<T> extends JsonHandler {
        private final Map<String, JsonHandler> readers = new HashMap<String, JsonHandler>();

        public void setReader(String key, JsonHandler field) {
            readers.put(key, field);
        }

        @Override
        public void atom(Token key, Token value) {
            JsonHandler reader = readers.get(key.text());
            if (reader != null && !(reader instanceof ObjectReader)) {
                reader.atom(key, value);
            }
        }

        @Override
        public void object(JsonParser parser, Token key, JsonTokenizer tokenizer) {
            if (key == null) {     // array element
                parser.parseObject(tokenizer);
                return;
            }
            JsonHandler reader = readers.get(key.text());
            if (reader == null) {
                JsonParser.SKIP_PARSER.parseObject(tokenizer);
            }
            else {
                new JsonParser(reader).parseObject(tokenizer);
            }
        }

        @Override
        public void array(JsonParser parser, Token key, JsonTokenizer tokenizer) {
            JsonHandler reader = readers.get(key.text());
            if (reader == null) {
                JsonParser.SKIP_PARSER.parseArray(tokenizer);
            }
            else {
                new JsonParser(reader).parseArray(tokenizer);
            }
        }

        public abstract T getContainer();
    }

}
