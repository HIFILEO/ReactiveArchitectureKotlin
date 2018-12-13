/*
Copyright 2017 LEO LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.reactivearchitecture.nowplaying.adapter.nowplaying

import android.databinding.ViewDataBinding

import com.example.reactivearchitecture.core.adapter.BaseViewHolder
import com.example.reactivearchitecture.databinding.MovieItemBinding
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo
import com.squareup.picasso.Picasso

/**
 * [MovieViewInfo] holder.
 *
 * @param viewDataBinding - View to bind data to in [MovieViewHolder.bind].
 */
class MovieViewHolder(viewDataBinding: ViewDataBinding) : BaseViewHolder(viewDataBinding.root) {
    private val movieItemBinding: MovieItemBinding = viewDataBinding as MovieItemBinding

    /**
     * Bind the [MovieViewInfo] to the view holder.
     * @param movieViewInfo - data to bind with.
     */
    fun bind(movieViewInfo: MovieViewInfo) {
        movieItemBinding.movieViewInfo = movieViewInfo

        // Picasso
        Picasso.with(movieItemBinding.moviePosterImageView.context)
                .load(movieViewInfo.pictureUrl)
                .into(movieItemBinding.moviePosterImageView)
    }
}
