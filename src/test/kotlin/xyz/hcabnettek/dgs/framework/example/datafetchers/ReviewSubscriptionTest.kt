package xyz.hcabnettek.dgs.framework.example.datafetchers

import xyz.hcabnettek.dgs.framework.generated.client.AddReviewGraphQLQuery
import xyz.hcabnettek.dgs.framework.generated.client.AddReviewProjectionRoot
import xyz.hcabnettek.dgs.framework.generated.types.Review
import xyz.hcabnettek.dgs.framework.generated.types.SubmittedReview
import xyz.hcabnettek.dgs.framework.example.scalars.DateTimeScalarRegistration
import xyz.hcabnettek.dgs.framework.example.services.DefaultReviewsService
import xyz.hcabnettek.dgs.framework.example.services.ShowsService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest
import graphql.ExecutionResult
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.concurrent.CopyOnWriteArrayList

@SpringBootTest(classes = [DefaultReviewsService::class, ReviewsDataFetcher::class, DgsAutoConfiguration::class, DateTimeScalarRegistration::class])
class ReviewSubscriptionTest {
    @Autowired
    lateinit var dgsQueryExecutor: DgsQueryExecutor

    @MockBean
    lateinit var showsService: ShowsService

    @Test
    fun reviewSubscription() {
        val executionResult = dgsQueryExecutor.execute("subscription { reviewAdded(showId: 1) {starScore} }")
        val reviewPublisher = executionResult.getData<Publisher<ExecutionResult>>()
        val reviews = CopyOnWriteArrayList<Review>()

        reviewPublisher.subscribe(object: Subscriber<ExecutionResult> {
            override fun onSubscribe(s: Subscription) {
                s.request(2)
            }

            override fun onNext(t: ExecutionResult) {
                val data = t.getData<Map<String, Any>>()
                reviews.add(jacksonObjectMapper().convertValue(data["reviewAdded"], Review::class.java))
            }

            override fun onError(t: Throwable?) {
            }

            override fun onComplete() {
            }
        })

        addReview()
        addReview()

        Assertions.assertThat(reviews.size).isEqualTo(2)

    }

    private fun addReview(): ExecutionResult {
        val graphQLQueryRequest =
            GraphQLQueryRequest(
                AddReviewGraphQLQuery.Builder()
                    .review(SubmittedReview(1, "testuser", 5))
                    .build(),
                AddReviewProjectionRoot()
                    .username()
                    .starScore()
            )

        return dgsQueryExecutor.execute(graphQLQueryRequest.serialize())
    }
}