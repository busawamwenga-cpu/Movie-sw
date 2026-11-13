package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.model.MovieItem

@Composable
fun MoviePosterImage(
    movie: MovieItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (movie.posterUrl.isNotBlank()) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = contentScale,
            modifier = modifier,
            error = painterResource(id = movie.posterRes ?: R.drawable.img_onboarding_1),
            placeholder = painterResource(id = movie.posterRes ?: R.drawable.img_onboarding_1)
        )
    } else {
        Image(
            painter = painterResource(id = movie.posterRes ?: R.drawable.img_onboarding_1),
            contentDescription = movie.title,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
