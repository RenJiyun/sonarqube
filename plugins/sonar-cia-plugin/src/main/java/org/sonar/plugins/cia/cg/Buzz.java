package org.sonar.plugins.cia.cg;

public class Buzz {

    public static void buzz() {
        System.out.println("Buzz");
    }

    public void buzz0() {
        buzz();
        System.out.println("Buzz0");
    }

    public void buzz1() {
        System.out.println("Buzz1");
    }
}
