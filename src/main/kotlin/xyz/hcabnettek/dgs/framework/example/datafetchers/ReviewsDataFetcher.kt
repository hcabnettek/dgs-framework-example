package xyz.hcabnettek.dgs.framework.example.datafetchers

import xyz.hcabnettek.dgs.framework.example.dataloaders.ReviewsDataLoader
import xyz.hcabnettek.dgs.framework.generated.DgsConstants
import xyz.hcabnettek.dgs.framework.generated.types.Review
import xyz.hcabnettek.dgs.framework.generated.types.Show
import xyz.hcabnettek.dgs.framework.generated.types.SubmittedReview
import xyz.hcabnettek.dgs.framework.example.services.ReviewsService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import org.dataloader.DataLoader
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture

@DgsComponent
class ReviewsDataFetcher(private val reviewsService: ReviewsService) {

    /**
     * This datafetcher will be called to resolve the "reviews" field on a Show.
     * It's invoked for each individual Show, so if we would load 10 shows, this method gets called 10 times.
     * To avoid the N+1 problem this datafetcher uses a DataLoader.
     * Although the DataLoader is called for each individual show ID, it will batch up the actual loading to a single method call to the "load" method in the ReviewsDataLoader.
     * For this to work correctly, the datafetcher needs to return a CompletableFuture.
     */
    @DgsData(parentType = DgsConstants.SHOW.TYPE_NAME, field = DgsConstants.SHOW.Reviews)
    fun reviews(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Review>> {
        //Instead of loading a DataLoader by name, we can use the DgsDataFetchingEnvironment and pass in the DataLoader classname.
        val reviewsDataLoader: DataLoader<Int, List<Review>> = dfe.getDataLoader(ReviewsDataLoader::class.java)

        //Because the reviews field is on Show, the getSource() method will return the Show instance.
        val show : Show = dfe.getSource()

        //Load the reviews from the DataLoader. This call is async and will be batched by the DataLoader mechanism.
        return reviewsDataLoader.load(show.id)
    }

    @DgsData(parentType = DgsConstants.Mutation_TYPE, field = DgsConstants.MUTATION.AddReview)
    fun addReview(@InputArgument("review") reviewInput: SubmittedReview): List<Review> {
        reviewsService.saveReview(reviewInput)

        return reviewsService.reviewsForShow(reviewInput.showId)?: emptyList()
    }

    @DgsData(parentType = DgsConstants.Subscription_TYPE, field = DgsConstants.SUBSCRIPTION.ReviewAdded)
    fun reviewAddedSubscription(@InputArgument("showId") showId: Int): Publisher<Review> {
        return reviewsService.getReviewsPublisher()
    }
}