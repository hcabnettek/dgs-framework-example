package xyz.hcabnettek.dgs.framework.example.datafetchers

import xyz.hcabnettek.dgs.framework.example.dataloaders.ReviewsDataLoader
import xyz.hcabnettek.dgs.framework.generated.client.AddReviewGraphQLQuery
import xyz.hcabnettek.dgs.framework.generated.client.AddReviewProjectionRoot
import xyz.hcabnettek.dgs.framework.generated.client.ShowsGraphQLQuery
import xyz.hcabnettek.dgs.framework.generated.client.ShowsProjectionRoot
import xyz.hcabnettek.dgs.framework.generated.types.Review
import xyz.hcabnettek.dgs.framework.generated.types.Show
import xyz.hcabnettek.dgs.framework.generated.types.SubmittedReview
import xyz.hcabnettek.dgs.framework.example.scalars.DateTimeScalarRegistration
import xyz.hcabnettek.dgs.framework.example.services.ReviewsService
import xyz.hcabnettek.dgs.framework.example.services.ShowsService
import com.jayway.jsonpath.TypeRef
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest
import graphql.ExecutionResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.OffsetDateTime
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ShowsDataFetcher::class, ReviewsDataFetcher::class, ReviewsDataLoader::class, DgsAutoConfiguration::class, DateTimeScalarRegistration::class])
class ShowsDataFetcherTest {
    @Autowired
    lateinit var dgsQueryExecutor: DgsQueryExecutor

    @MockBean
    lateinit var showsService: ShowsService

    @MockBean
    lateinit var reviewsService: ReviewsService

    @BeforeEach
    fun before() {
        `when`(showsService.shows()).thenAnswer { listOf(Show(id = 1, title = "mock title", releaseYear = 2020)) }
        `when`(reviewsService.reviewsForShows(listOf(1))).thenAnswer {
            mapOf(
                Pair(
                    1, listOf(
                        Review("DGS User", 5, OffsetDateTime.now()),
                        Review("DGS User 2", 3, OffsetDateTime.now()),
                    )
                )
            )
        }
    }

    @Test
    fun shows() {
        val titles: List<String> = dgsQueryExecutor.executeAndExtractJsonPath(
            """
            {
                shows {
                    title
                    releaseYear
                }
            }
        """.trimIndent(), "data.shows[*].title"
        )

        assertThat(titles).contains("mock title")
    }

    @Test
    fun showsWithException() {
        `when`(showsService.shows()).thenThrow(RuntimeException("nothing to see here"))

        val result = dgsQueryExecutor.execute(
            """
            {
                shows {
                    title
                    releaseYear
                }
            }
        """.trimIndent()
        )

        assertThat(result.errors).isNotEmpty
        assertThat(result.errors[0].message).isEqualTo("java.lang.RuntimeException: nothing to see here")
    }

    @Test
    fun showsWithQueryApi() {
        val graphQLQueryRequest =
            GraphQLQueryRequest(
                ShowsGraphQLQuery.Builder()
                    .build(),
                ShowsProjectionRoot().title()
            )
        val titles = dgsQueryExecutor.executeAndExtractJsonPath<List<String>>(
            graphQLQueryRequest.serialize(),
            "data.shows[*].title"
        )
        assertThat(titles).contains("mock title")
    }

    @Test
    fun showWithReviews() {
        val graphQLQueryRequest =
            GraphQLQueryRequest(
                ShowsGraphQLQuery.Builder()
                    .build(),
                ShowsProjectionRoot()
                    .title()
                    .reviews()
                    .username()
                    .starScore()
            )
        val shows = dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            graphQLQueryRequest.serialize(),
            "data.shows[*]",
            object : TypeRef<List<Show>>() {})
        assertThat(shows.size).isEqualTo(1)
        assertThat(shows[0].reviews?.size).isEqualTo(2)
    }

    @Test
    fun addReviewMutation() {

        val graphQLQueryRequest =
            GraphQLQueryRequest(
                AddReviewGraphQLQuery.Builder()
                    .review(SubmittedReview(1, "testuser", 5))
                    .build(),
                AddReviewProjectionRoot()
                    .username()
                    .starScore()
            )

        val executionResult = dgsQueryExecutor.execute(graphQLQueryRequest.serialize())
        assertThat(executionResult.errors).isEmpty()

        verify(reviewsService).reviewsForShow(1)
    }
}