package eu.darkbot.ter.dks.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Predicate;

public final class NumberOnlyTypeAdapterFactory implements TypeAdapterFactory {

    private static final TypeAdapterFactory instance = new NumberOnlyTypeAdapterFactory();
    private static final TypeToken<Float> floatTypeToken = TypeToken.get(Float.class);
    private static final TypeToken<Double> doubleTypeToken = TypeToken.get(Double.class);

    private NumberOnlyTypeAdapterFactory() { }

    public static TypeAdapterFactory getInstance() {
        return instance;
    }

    @Override
    @Nullable
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        final Class<? super T> rawType = typeToken.getRawType();
        final TypeAdapter<? extends Number> typeAdapter;
        if (rawType == Float.class) {
            typeAdapter = NumberOnlyTypeAdapter.create(gson.getDelegateAdapter(this, floatTypeToken), v -> Float.isNaN(v), v -> Float.isInfinite(v));
        } else if (rawType == Double.class) {
            typeAdapter = NumberOnlyTypeAdapter.create(gson.getDelegateAdapter(this, doubleTypeToken), v -> Double.isNaN(v), v -> Double.isInfinite(v));
        } else {
            return null;
        }
        @SuppressWarnings("unchecked")
        final TypeAdapter<T> castTypeAdapter = (TypeAdapter<T>) typeAdapter;
        return castTypeAdapter;
    }

    private static final class NumberOnlyTypeAdapter<T extends Number> extends TypeAdapter<T> {

        private final TypeAdapter<? super T> delegate;
        private final Predicate<? super T> isNan;
        private final Predicate<? super T> isInfinite;

        private NumberOnlyTypeAdapter(TypeAdapter<? super T> delegate, Predicate<? super T> isNan, Predicate<? super T> isInfinite) {
            this.delegate = delegate;
            this.isNan = isNan;
            this.isInfinite = isInfinite;
        }

        private static <T extends Number> TypeAdapter<T> create(final TypeAdapter<? super T> delegate, final Predicate<? super T> isNan, final Predicate<? super T> isInfinite) {
            return new NumberOnlyTypeAdapter<T>(delegate, isNan, isInfinite).nullSafe();
        }

        @Override
        public void write(final JsonWriter jsonWriter, final T value) throws IOException {
            if (!isNan.test(value) && !isInfinite.test(value)) {
                delegate.write(jsonWriter, value);
                return;
            }
            jsonWriter.nullValue();
        }

        @Override
        public T read(final JsonReader jsonReader) {
            throw new UnsupportedOperationException("TODO");
        }

    }

}