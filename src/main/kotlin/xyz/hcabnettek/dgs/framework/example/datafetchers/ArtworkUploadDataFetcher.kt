package xyz.hcabnettek.dgs.framework.example.datafetchers

import xyz.hcabnettek.dgs.framework.generated.DgsConstants
import xyz.hcabnettek.dgs.framework.generated.types.Image
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.streams.toList

@DgsComponent
class ArtworkUploadDataFetcher {
    @DgsData(parentType = DgsConstants.Mutation_TYPE, field = DgsConstants.MUTATION.AddArtwork)
    fun uploadArtwork(@InputArgument("showId") showId: Int, @InputArgument("upload") multipartFile: MultipartFile): List<Image> {
        val uploadDir = Paths.get("uploaded-images")
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        Files.newOutputStream(
                uploadDir.resolve(
                        "show-$showId-${UUID.randomUUID()}.${multipartFile.originalFilename?.substringAfterLast(".")}"
                )
        ).use { it.write(multipartFile.bytes) }

        return Files.list(uploadDir)
                .filter { it.fileName.toString().startsWith("show-$showId-") }
                .map { it.fileName.toString() }
                .map { Image(url = it) }.toList()
    }
}