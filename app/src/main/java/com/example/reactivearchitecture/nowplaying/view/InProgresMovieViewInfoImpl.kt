package com.example.reactivearchitecture.nowplaying.view

/**
 * Represents an in progress [MovieViewInfo]. All fields are empty.
 */
class InProgresMovieViewInfoImpl : MovieViewInfo {
    override val pictureUrl: String
        get() = ""
    override val title: String
        get() = ""
    override val releaseDate: String
        get() = ""
    override val rating: String
        get() = ""
    override val isHighRating: Boolean
        get() = false
}