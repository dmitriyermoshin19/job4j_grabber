package ru.job4j.grabber;

import java.util.List;

public interface Parse {
    Post detail(String link);
    List<Post> parser(String link);
}
