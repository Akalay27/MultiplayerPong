package com.adam.udptesting;

import java.io.Serializable;
public class Dog implements Serializable {

    private String name;
    private String breed;
    private int age;

    public Dog(String name, String breed, int age){
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    public void hello() {
        System.out.println("Hello! I'm " + name + ", a " + (age) + " year old " + breed + ".");
    }

}
