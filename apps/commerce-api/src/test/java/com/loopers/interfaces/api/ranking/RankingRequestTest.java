package com.loopers.interfaces.api.ranking;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RankingRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        assertNotNull(validator, "Validator should be initialized");
    }

    @Nested
    @DisplayName("SearchRankings validation")
    class SearchRankingsValidation {

        @Test
        @DisplayName("valid when date is null, page and size positive")
        void validWhenDateNullAndPageSizePositive() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(null) // @PastOrPresent allows null
                    .page(1)
                    .size(10)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertTrue(violations.isEmpty(), "Expected no violations when date is null and page/size are positive");
        }

        @Test
        @DisplayName("valid when date is today (present)")
        void validWhenDateIsToday() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(2)
                    .size(25)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertTrue(violations.isEmpty(), "Expected no violations for present date");
        }

        @Test
        @DisplayName("valid when date is in the past")
        void validWhenDateInPast() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now().minusDays(7))
                    .page(3)
                    .size(50)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertTrue(violations.isEmpty(), "Expected no violations for past date");
        }

        @Test
        @DisplayName("violation when date is in the future")
        void violationWhenDateInFuture() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now().plusDays(1))
                    .page(1)
                    .size(10)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertFalse(violations.isEmpty(), "Expected violations for future date");
            assertTrue(violations.stream().anyMatch(v -> "date".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'date' field");
        }

        @Test
        @DisplayName("violations when page is null and size is valid")
        void violationsWhenPageNull() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(null) // @NotNull
                    .size(10)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertFalse(violations.isEmpty(), "Expected violations for null page");
            assertTrue(violations.stream().anyMatch(v -> "page".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'page' field");
        }

        @Test
        @DisplayName("violations when size is null and page is valid")
        void violationsWhenSizeNull() {
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(1)
                    .size(null) // @NotNull
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> violations = validator.validate(req);
            assertFalse(violations.isEmpty(), "Expected violations for null size");
            assertTrue(violations.stream().anyMatch(v -> "size".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'size' field");
        }

        @Test
        @DisplayName("violations when page is zero or negative")
        void violationsWhenPageNonPositive() {
            RankingRequest.SearchRankings zero = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(0) // @Positive forbids 0
                    .size(10)
                    .build();

            RankingRequest.SearchRankings negative = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(-5)
                    .size(10)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> vZero = validator.validate(zero);
            Set<ConstraintViolation<RankingRequest.SearchRankings>> vNeg = validator.validate(negative);

            assertFalse(vZero.isEmpty(), "Expected violations for page=0");
            assertTrue(vZero.stream().anyMatch(v -> "page".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'page' for zero");

            assertFalse(vNeg.isEmpty(), "Expected violations for page<0");
            assertTrue(vNeg.stream().anyMatch(v -> "page".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'page' for negative");
        }

        @Test
        @DisplayName("violations when size is zero or negative")
        void violationsWhenSizeNonPositive() {
            RankingRequest.SearchRankings zero = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(1)
                    .size(0)
                    .build();

            RankingRequest.SearchRankings negative = RankingRequest.SearchRankings.builder()
                    .date(LocalDate.now())
                    .page(1)
                    .size(-10)
                    .build();

            Set<ConstraintViolation<RankingRequest.SearchRankings>> vZero = validator.validate(zero);
            Set<ConstraintViolation<RankingRequest.SearchRankings>> vNeg = validator.validate(negative);

            assertFalse(vZero.isEmpty(), "Expected violations for size=0");
            assertTrue(vZero.stream().anyMatch(v -> "size".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'size' for zero");

            assertFalse(vNeg.isEmpty(), "Expected violations for size<0");
            assertTrue(vNeg.stream().anyMatch(v -> "size".equals(v.getPropertyPath().toString())),
                    "Expected violation on 'size' for negative");
        }

        @Test
        @DisplayName("builder sets values and getters return them")
        void builderAndGetters() {
            LocalDate d = LocalDate.now().minusDays(2);
            RankingRequest.SearchRankings req = RankingRequest.SearchRankings.builder()
                    .date(d)
                    .page(4)
                    .size(40)
                    .build();

            assertEquals(d, req.getDate(), "Date getter should return builder-provided date");
            assertEquals(4, req.getPage(), "Page getter should return builder-provided page");
            assertEquals(40, req.getSize(), "Size getter should return builder-provided size");
        }
    }
}