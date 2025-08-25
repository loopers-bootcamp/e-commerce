package com.loopers.infrastructure.payment.client;

public record PgApiResponse<T>(Metadata meta, T data) {

    static PgApiResponse<Object> success() {
        return new PgApiResponse<>(Metadata.success(), null);
    }

    static <T> PgApiResponse<T> success(T data) {
        return new PgApiResponse<>(Metadata.success(), data);
    }

    static PgApiResponse<Object> fail(String errorCode, String errorMessage) {
        return new PgApiResponse<>(
                Metadata.fail(errorCode, errorMessage),
                null
        );
    }

    // -------------------------------------------------------------------------------------------------

    public record Metadata(Result result, String errorCode, String message) {
        public enum Result {
            SUCCESS, FAIL
        }

        public static Metadata success() {
            return new Metadata(Result.SUCCESS, null, null);
        }

        public static Metadata fail(String errorCode, String errorMessage) {
            return new Metadata(Result.FAIL, errorCode, errorMessage);
        }
    }

}
