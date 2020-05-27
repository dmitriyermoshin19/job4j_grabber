package ru.job4j.grabber;

import org.quartz.Scheduler;

public interface Grab {
    void init(Parse parse, Store store, Scheduler scheduler);
}
