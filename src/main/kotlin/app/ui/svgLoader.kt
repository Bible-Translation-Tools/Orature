package app.ui

import afester.javafx.svg.SvgLoader

//function to load an svg with the svg file path located at resources folder

fun svgLoader(svgFileName: String?) = SvgLoader().loadSvg(Thread.currentThread().contextClassLoader.getResource("$svgFileName.svg").path)
