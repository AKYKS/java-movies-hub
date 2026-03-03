package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MoviesStore {
    List<Movie> movies;

    public MoviesStore() {
        this.movies = new ArrayList<>();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public Movie getMovie(int id) {
        for (Movie movie : movies) {
            if (id == movie.getId()) {
                return movie;
            }
        }
        return null;
    }

    public void addMovie(String title, int year) {
        int id = movies.size() + 1;
        boolean Found = true;
        while (Found) {
            int finalId = id;
            long count = movies.stream()
                    .filter(movie -> movie.getId() == finalId)
                    .count();
            if (count > 0) {
                id++;
            } else {
                Found = false;
            }
        }
        Movie movie = new Movie(id, title, year);
        this.movies.add(movie);
    }

    public void removeMovie(int id) {
        movies.removeIf(movie -> id == movie.getId());
    }

    public void clearMovies() {
        this.movies.clear();
    }

}