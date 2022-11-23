package com.github.spygameserver;

import com.github.spygameserver.auth.website.SparkWebsiteHandler;

import java.util.Scanner;

public class SpyGameServer {

    public static void main(String[] args) {
        SparkWebsiteHandler sparkWebsiteHandler = new SparkWebsiteHandler(null, null);

        Scanner scanner = new Scanner(System.in);
        while (!scanner.nextLine().trim().equals("exit")) {

        }

        sparkWebsiteHandler.shutdown();
    }

}
