package io.petros.movies.data.network.rest

import io.petros.movies.data.network.WebService
import io.petros.movies.data.network.rest.mapper.movie.MoviesMapper
import io.petros.movies.domain.model.movie.MoviesResultPage
import io.reactivex.Single
import javax.inject.Inject

class RestClient @Inject constructor(
    private val restApi: RestApi
) : WebService {

    override fun loadMovies(): Single<MoviesResultPage> {
        return restApi.loadMovies()
            .map { MoviesMapper.transform(it) }
    }

}