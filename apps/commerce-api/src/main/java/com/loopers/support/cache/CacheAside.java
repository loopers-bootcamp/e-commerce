package com.loopers.support.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class CacheAside {

    public static <T> LookupCache<T> lookupCache(OptionalSupplier<T> cacheGetter) {
        return lookupCache(cacheGetter.unwrap());
    }

    public static <T> LookupCache<T> lookupCache(Supplier<T> cacheGetter) {
        return new LookupCache<>(cacheGetter);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LookupCache<T> {
        private final Supplier<T> cacheGetter;

        public IsNullObject<T> isNullObject(Predicate<T> predicateToNullObject) {
            return new IsNullObject<>(this, predicateToNullObject);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IsNullObject<T> {
        private final LookupCache<T> prev;
        private final Predicate<T> isNullObject;

        public LookupFallback<T> lookupFallback(OptionalSupplier<T> fallbackGetter) {
            return lookupFallback(fallbackGetter.unwrap());
        }

        public LookupFallback<T> lookupFallback(Supplier<T> fallbackGetter) {
            return new LookupFallback<>(this, fallbackGetter);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LookupFallback<T> {
        private final IsNullObject<T> prev;
        private final Supplier<T> fallbackGetter;

        public SaveCache<T> saveCache(Consumer<T> cacheSaver) {
            return new SaveCache<>(this, cacheSaver);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SaveCache<T> {
        private final LookupFallback<T> prev;
        private final Consumer<T> saveCache;

        public Optional<T> getAsOptional() {
            return Optional.ofNullable(get());
        }

        public T get() {
            T cache = prev.prev.prev.cacheGetter.get();
            if (cache != null) {
                // 캐시 관통을 방지한다.
                boolean isNullObject = prev.prev.isNullObject.test(cache);
                if (isNullObject) {
                    return null;
                }

                return cache;
            }

            T fallbackData = prev.fallbackGetter.get();
            saveCache.accept(fallbackData);
            return fallbackData;
        }
    }

    // -------------------------------------------------------------------------------------------------

    public interface OptionalSupplier<T> extends Supplier<Optional<T>> {
        default Supplier<T> unwrap() {
            return () -> get().orElse(null);
        }
    }

}
