# otter
Otter is a modular suite of Oral Translation libraries and distributable software.

# Desktop Workbook App
The workbook app runs on OpenJDK 11. Installers for Windows, Debian-based Linux, and Mac are available in the Releases section of the repo on GitHub.

# Building/dev
Requires java version 11. JavaFX is included as a gradle dependency

- Clone this repo
- Run the gradle task ```:jvm:workbookapp:generateSampleJooqSchemaSource``` in order to generate jooq classes
- Run the gradle task ```:jvm:workbookapp:kaptKotlin```

 From there you should be able to build and run, the entrypoint is under org.wycliffeassociates.otter.jvm.workbookapp.Mainkt

- Run the gradle task ```:jvm:workbookapp:build``` to build an executable shadow Jar file. 