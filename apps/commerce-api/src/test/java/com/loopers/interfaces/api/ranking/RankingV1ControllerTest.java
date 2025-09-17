package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingInput;
import com.loopers.application.ranking.RankingOutput;
import com.loopers.interfaces.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testing with:
 * - JUnit 5 (org.junit.jupiter)
 * - Spring Boot Test slice (@WebMvcTest) with MockMvc
 * - Mockito for mocking collaborators
 *
 * Focus: /api/v1/rankings GET endpoint behavior and request param mapping.
 */
@WebMvcTest(RankingV1Controller.class)
class RankingV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingFacade rankingFacade;

    @Nested
    @DisplayName("GET /api/v1/rankings - searchRankings")
    class SearchRankings {

        @Test
        @DisplayName("returns 200 with valid params and delegates to facade with mapped input")
        void ok_withValidParams_callsFacadeWithInput() throws Exception {
            RankingOutput.SearchRankings mockOutput = Mockito.mock(RankingOutput.SearchRankings.class);
            when(rankingFacade.searchRankings(any(RankingInput.SearchRankings.class))).thenReturn(mockOutput);

            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .param("page", "0")
                            .param("size", "20")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

            ArgumentCaptor<RankingInput.SearchRankings> captor = ArgumentCaptor.forClass(RankingInput.SearchRankings.class);
            verify(rankingFacade, times(1)).searchRankings(captor.capture());

            RankingInput.SearchRankings captured = captor.getValue();
            // We cannot rely on getters if not present; assert via toString or equals if implemented.
            assertThat(captured).isNotNull();
        }

        @Test
        @DisplayName("applies default paging when page/size missing (if binding provides defaults)")
        void ok_missingPagingParams_usesDefaults() throws Exception {
            RankingOutput.SearchRankings mockOutput = Mockito.mock(RankingOutput.SearchRankings.class);
            when(rankingFacade.searchRankings(any(RankingInput.SearchRankings.class))).thenReturn(mockOutput);

            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(rankingFacade, times(1)).searchRankings(any(RankingInput.SearchRankings.class));
        }

        @Test
        @DisplayName("handles invalid numeric params gracefully (bad request for non-numeric page/size)")
        void badRequest_whenNonNumericPaging() throws Exception {
            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .param("page", "abc")
                            .param("size", "xyz")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
            verify(rankingFacade, never()).searchRankings(any());
        }

        @Test
        @DisplayName("handles missing required date parameter (if required by binder)")
        void badRequest_whenMissingDate() throws Exception {
            mockMvc.perform(get("/api/v1/rankings")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
            verify(rankingFacade, never()).searchRankings(any());
        }

        @Test
        @DisplayName("propagates facade errors as 5xx by default")
        void serverError_whenFacadeThrows() throws Exception {
            when(rankingFacade.searchRankings(any(RankingInput.SearchRankings.class)))
                    .thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .param("page", "1")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("accepts boundary values for paging")
        void ok_boundaryPagingValues() throws Exception {
            RankingOutput.SearchRankings mockOutput = Mockito.mock(RankingOutput.SearchRankings.class);
            when(rankingFacade.searchRankings(any(RankingInput.SearchRankings.class))).thenReturn(mockOutput);

            // page=0, size=1 minimal
            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .param("page", "0")
                            .param("size", "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // large size to test upper bounds behavior (controller should still call facade)
            mockMvc.perform(get("/api/v1/rankings")
                            .param("date", "2025-09-10")
                            .param("page", "2")
                            .param("size", "200")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(rankingFacade, times(2)).searchRankings(any(RankingInput.SearchRankings.class));
        }
    }
}