package com.example.demo.controller

import tornadofx.Controller;
//I'm keeping usernames in a separate class so I can grab them in the View package
//It's the best way I can think of to simulate what we'll be doing with the database
//Basically encouraging myself to keep the architecture clean
class DataService: Controller() {
    fun numbers() = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine");
}