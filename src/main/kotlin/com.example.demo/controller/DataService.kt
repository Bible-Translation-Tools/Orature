package com.example.demo.controller

import tornadofx.Controller;

class DataService: Controller() {
    fun numbers() = listOf("one", "two", "three");
}