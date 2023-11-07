package org.sonar.plugins.cia.sootdemo;

import java.util.Random;

public class Fizz {
    public void fizz() {
        Buzz buzz = new Buzz();
        Random random = new Random();
        int i = random.nextInt(100);
        Buzz.buzz();
        if (i % 2 == 0) {
            buzz.buzz0();
        } else {
            buzz.buzz1();
        }
    }

}
