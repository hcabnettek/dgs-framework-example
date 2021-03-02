package xyz.hcabnettek.dgs.framework.example.datafetchers

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.InputArgument
import xyz.hcabnettek.dgs.framework.example.services.ShowsService
import xyz.hcabnettek.dgs.framework.generated.DgsConstants
import xyz.hcabnettek.dgs.framework.generated.types.Show

@DgsComponent
class ShowsDataFetcher (private val showsService: ShowsService){

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Shows)
    fun shows(@InputArgument("titleFilter") titleFilter : String?): List<Show> {
        return if(titleFilter != null) {
            showsService.shows().filter { it.title?.contains(titleFilter) == true }
        } else {
            showsService.shows()
        }
    }
}
