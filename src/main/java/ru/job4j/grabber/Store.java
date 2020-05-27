package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.List;

public interface Store {
    void save(List<Post> posts);
    List<Post> getAll();
    void saveDate(LocalDateTime dateTime);
}
