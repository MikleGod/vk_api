package com.example.music.vk_api.Entities;

import java.util.Set;

/**
 * Created by Mikle on 16.01.2018.
 */

public class Students {
    private static Set<String> names;
    private static Set<String> ides;
    private static Set<String> vkIdes;

    public static Set<String> getVkIdes() {
        return vkIdes;
    }

    public static void setVkIdes(Set<String> vkIdes) {
        Students.vkIdes = vkIdes;
    }

    public static Set<String> getNames() {
        return names;
    }

    public static void setNames(Set<String> names) {
        Students.names = names;
    }

    public static Set<String> getIdes() {
        return ides;
    }

    public static void setIdes(Set<String> ides) {
        Students.ides = ides;
    }
}
