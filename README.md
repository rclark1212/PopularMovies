# PopularMovies

Android application which presents the user with a gridview of movies
sorted by user preference and allows the user to select a movie to see
more details. Supports multi-fragment layout for phone and tablet. 
Also supports viewing trailers, reviews and search.
The data backing this app is the TMDB movie database. This app will 
query the TMDB site using TMDB APIs and parse the information for
user consumption. This app uses a private TMDB API key and is for
educational purposes only.

## Installation

First, clone this repo. Second, this project developed with Android Studio 1.3.1 and is set up to support JB and later android OS.

To install and run you will need a TMDB API key. For instructions on obtaining a key, see:
https://www.themoviedb.org/documentation/api

Once you have a key, you will need to add a string "TMDB_API_KEY" (and set it to the TMDB API you have obtained) to your project. 

You should be able to build and run at this point.

## Usage

Usage should be self-explanitory. Launch app and you will see a gridview
of movies. Select a movie and a detail screen will show for the movie.

There is still missing functionality including:
- sorting by favorite
- trailer and reviews in the detail

## History

First iteration for phase 1 of nanodegree

## Credits

TMDB APIs (and database)
MyKong example of a custom imageadapter for gridview
Previous projects for android nanodegree
