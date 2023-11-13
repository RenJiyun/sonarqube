package org.sonar.plugins.cia.cg;

import java.util.ArrayList;
import java.util.List;

@MyService("buzz")
public class Buzz {

    public static void buzz() {
        System.out.println("Buzz");
    }

    public void buzz0() {
        buzz();
        System.out.println("Buzz0");
    }

    public List<String> buzz0(String s) {
        System.out.println("Buzz0-" + s);
        return new ArrayList<>();
    }

    public void buzz1() {
        System.out.println("Buzz1");
    }
}
