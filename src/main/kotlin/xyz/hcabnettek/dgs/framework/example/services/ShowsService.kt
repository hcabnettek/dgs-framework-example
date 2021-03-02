package xyz.hcabnettek.dgs.framework.example.services

import org.springframework.stereotype.Service
import xyz.hcabnettek.dgs.framework.generated.types.Show

interface ShowsService {
    fun shows(): List<Show>
}

@Service
class BasicShowsService : ShowsService {
    override fun shows(): List<Show> {
        return listOf(
                Show(id = 1, title = "Stranger Things", releaseYear = 2016),
                Show(id = 2, title = "Ozark", releaseYear = 2017),
                Show(id = 3, title = "The Crown", releaseYear = 2016),
                Show(id = 4, title = "Dead to Me", releaseYear = 2019),
                Show(id = 5, title = "Orange is the New Black", releaseYear = 2013)
        )
    }
}