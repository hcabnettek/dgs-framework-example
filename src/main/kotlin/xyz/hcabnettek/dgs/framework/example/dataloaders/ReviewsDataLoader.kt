package xyz.hcabnettek.dgs.framework.example.dataloaders

import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.MappedBatchLoader
import xyz.hcabnettek.dgs.framework.example.services.ReviewsService
import xyz.hcabnettek.dgs.framework.generated.types.Review
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.streams.toList

@DgsDataLoader(name = "reviews")
class ReviewsDataLoader(private val reviewsService: ReviewsService): MappedBatchLoader<Int, List<Review>> {
    override fun load(keys: MutableSet<Int>): CompletionStage<Map<Int, List<Review>>> {
        return CompletableFuture.supplyAsync { reviewsService.reviewsForShows(keys.stream().toList()) }
    }

}