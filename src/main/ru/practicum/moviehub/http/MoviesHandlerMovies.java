package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandlerMovies extends BaseHttpHandler {

    MoviesStore store;

    public MoviesHandlerMovies(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathSplit = path.split("/");
        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            if (pathSplit.length == 2) {
                String query = ex.getRequestURI().getQuery();
                if (query != null) {
                    String[] queryArr = query.split("&");
                    String[] values = queryArr[0].split("=");

                    if (values[0].equalsIgnoreCase("year") && isInteger(values[1])) {
                        int year = Integer.parseInt(values[1]);
                        List<Movie> moviesCurYear = store.getMovies().stream()
                                .filter(m -> m.getYear() == year)
                                .toList();

                        String json = gson.toJson(moviesCurYear);
                        sendJson(ex, 200, json);
                    } else {
                        String error = gson.toJson(new ErrorResponse("Ошибка валидации",
                                new String[]{"Некорректный параметр запроса — 'year'"}));
                        sendJson(ex, 400, error);
                    }
                } else {
                    String json = gson.toJson(store.getMovies());
                    sendJson(ex, 200, json);
                }
            } else {
                if (isInteger(pathSplit[2])) {
                    for (Movie m : store.getMovies()) {
                        if (m.getId() == Integer.parseInt(pathSplit[2])) {
                            String json = gson.toJson(m, Movie.class);
                            sendJson(ex, 200, json);
                            return;
                        }
                    }
                    sendMovieNotFound(ex);
                } else {
                    sendUncorrectedId(ex);
                }
            }
        } else if (method.equalsIgnoreCase("POST")) {
            InputStream inputStream = ex.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);

            if (!jsonElement.isJsonObject()) {
                String error = gson.toJson(new ErrorResponse("Ошибка Content-Type",
                        new String[]{"данные должны передаваться в формате JSON"}));
                sendJson(ex, 415, error);
                return;
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int year = jsonObject.get("year").getAsInt();
            String title = jsonObject.get("title").getAsString();

            if (year >= 1888 && year <= 2026 && !title.isBlank() && title.length() <= 100) {
                store.addMovie(title, year);
                String json = gson.toJson(store.getMovies().getLast(), Movie.class);
                sendJson(ex, 201, json);
            } else {
                String error = gson.toJson(new ErrorResponse("Ошибка валидации",
                        new String[]{"название не должно быть пустым", "год должен быть между 1888 и 2026"}));
                sendJson(ex, 422, error);
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            if (isInteger(pathSplit[2])) {
                for (Movie m : store.getMovies()) {
                    if (m.getId() == Integer.parseInt(pathSplit[2])) {
                        store.removeMovie(m.getId());
                        sendNoContent(ex);
                        return;
                    }
                }
                sendMovieNotFound(ex);
            } else {
                sendUncorrectedId(ex);
            }
        } else {
            sendMethodNotAllowed(ex);
        }
    }
}
