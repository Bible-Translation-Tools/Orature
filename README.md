# otter-jvm
JVM specific packages for the otter project
Supports Windows, MacOs, Linux, and any other operating system that supports the desktop JVM


# Setup
Requires java version 8 (note: use of the openjdk will require importing openjfx as a dependency)

- Clone this, otter-common and kotlin-resource-container into the same directory (note settings.gradle in this repo).
- Run the gradle task ```generateSampleJooqSchemaSource``` in order to generate jooq classes
- Run the gradle task ```kaptKotlin```

 From there you should be able to build and run, the entrypoint is under org.wycliffeassociates.otter.jvm.app.Mainkt

