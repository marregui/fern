package com.fern;

public class Main {
  public static final String VERSION = "1.0";

  public static void main(String[] args) {
    System.out.printf("This is %s %s, work in progress...\n", Main.class.getSimpleName(), VERSION);
    System.exit(0);
  }
}