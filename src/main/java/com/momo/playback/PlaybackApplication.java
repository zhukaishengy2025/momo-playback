package com.momo.playback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlaybackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaybackApplication.class, args);
    }
}
