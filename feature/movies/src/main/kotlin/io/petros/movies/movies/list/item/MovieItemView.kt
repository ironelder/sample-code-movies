package io.petros.movies.movies.list.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.cardview.widget.CardView
import io.petros.movies.android_utils.getDimension
import io.petros.movies.core.image.glide.displayImage
import io.petros.movies.domain.model.movie.Movie
import io.petros.movies.movie_details.navigator.SharedElementMovie
import io.petros.movies.movies.R
import io.petros.movies.movies.databinding.MovieItemViewBinding

class MovieItemView : CardView {

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    @VisibleForTesting val binding = MovieItemViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        initView()
    }

    private fun initView() {
        val lp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = context.getDimension(R.dimen.cdItemHorizontalMargin)
        lp.rightMargin = context.getDimension(R.dimen.cdItemHorizontalMargin)
        lp.bottomMargin = context.getDimension(R.dimen.cdItemVerticalMargin)
        layoutParams = lp
        radius = context.getDimension(R.dimen.cdItemRadius).toFloat()
        cardElevation = context.getDimension(R.dimen.cdItemCardElevation).toFloat()
    }

    fun bind(movie: Movie) {
        bindMovie(movie)
    }

    private fun bindMovie(movie: Movie) {
        binding.ivItemBackdrop.displayImage(movie.backdrop)
        binding.tvItemTitle.text = movie.title
        binding.tvItemReleaseDate.text = movie.releaseDate()
        binding.tvItemVote.text = movie.vote()
    }

    fun bindCallback(movie: Movie, callback: MovieItemCallback?) {
        val sharedElementMovie = SharedElementMovie(movie, binding.ivItemBackdrop)
        setOnClickListener { callback?.onClick(sharedElementMovie) }
    }

}
