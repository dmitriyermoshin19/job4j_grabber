package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.Objects;

public class Post implements Comparable<Post> {
    private String name;
    private String text;
    private String link;
    private LocalDateTime date;

    public Post(String name, String text, LocalDateTime date, String link) {
        this.name = name;
        this.text = text;
        this.link = link;
        this.date = date;
    }

    public String getName() {
        return this.name;
    }

    public String getText() {
        return this.text;
    }

    public String getLink() {
        return this.link;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    @Override
    public int compareTo(Post another) {
        return Integer.compare(this.date.getDayOfYear(), another.date.getDayOfYear());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post v = (Post) o;
        return Objects.equals(this.name, v.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return System.lineSeparator()
                + "Job " + System.lineSeparator()
                + "name='" + name + '\'' + System.lineSeparator()
                + "text: " + text + System.lineSeparator()
                + "date: " + date + System.lineSeparator()
                + "link: " + link;
    }
}
