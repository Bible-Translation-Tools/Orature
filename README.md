# Orature

Orature is an application for oral drafting, narration, and translation of the Bible, books (such as [Open Bible Stories](https://www.unfoldingword.org/open-bible-stories)), and translation helps/resources (such as notes and checking questions). Additionally, Orature can connect with third party applications for more expansive recording and editing options.
More information can be found [here.](https://bibletranslationtools.org/tool/orature/), as well as in the [wiki.](https://github.com/Bible-Translation-Tools/Orature/wiki)

<p float="left">
  <img src="https://raw.githubusercontent.com/jsarabia/orature-screenshots/main/Orature%203%20Home.png" width="48%" />
  <img src="https://raw.githubusercontent.com/jsarabia/orature-screenshots/main/Orature%203%20Narration.png" width="48%" /> 
</p>
<p float="left">
  <img src="https://raw.githubusercontent.com/jsarabia/orature-screenshots/main/Orature%203%20Chunking.png" width="48%" />
  <img src="https://raw.githubusercontent.com/jsarabia/orature-screenshots/main/Orature%203%20Peer%20Edit.png" width="48%" /> 
</p>

# Usage
Installers for Windows, Debian-based Linux, and Mac are available in the [Releases](https://github.com/Bible-Translation-Tools/Orature/releases) section of the repository.
Orature uses the [Door43 Resource Container](https://resource-container.readthedocs.io/en/latest/index.html) format (in zip) for providing source content to narrate, draft, or translate. Currently, Bible content is supported in USFM format, Bible Stories and translation helps are supported in Markdown. Source Audio is supported if contained in the resource container and referrenced in the [media manifest](https://resource-container.readthedocs.io/en/latest/media.html). Note that paths should be local with respect to the container root, not a URL. Supported audio formats include 44.1khz mono 16 bit WAV, and MP3.

# Developer
*Requires JDK 21 - Liberica JDK (full version is preferred). JavaFX is included as a gradle dependency*. 
To quickly get started with Orature, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Bible-Translation-Tools/Orature.git
   ```

2. **Run with gradle wrapper:**
	```
	./gradlew run	
	```

# Frequently Asked Questions (FAQ)

### Q: I cloned the repository, but the application is not running. What could be the issue?

**A:** Ensure you have Java version 11 or higher installed. Additionally, verify that you've completed the steps to generate Jooq classes and run Kotlin Kapt. If issues persist, double-check your environment settings and dependencies.

---

### Q: What should I do if I get a "Permission Denied" error when running the build command?

**A:** Ensure you have the necessary permissions to execute the build command. On Unix-based systems, you might need to run `chmod +x gradlew` to make the Gradle wrapper executable.

---

### Q: The application is not recognizing audio files. What could be the issue?

**A:** Verify that your audio files are in the correct format (44.1khz mono 16-bit WAV or MP3). Ensure paths are local to the container root, not URLs.

---

### Q: I want to contribute to the project. How can I get started?

**A:** Fork the repository, create a branch for your changes, and submit a pull request. Make sure to follow the coding standards outlined in the wiki.

---

## Directory Structure

Orature is made up of modular software that could be modified to run standalone. The main app modules are: recordingapp (a simple audio recorder), markerapp (an app for placing verse markers in an audio file), and workbookapp (Orature itself). These modules can be found under the jvm package.

All modules fall under the package name "otter" which roughly stands for Oral Translation Tools and Resources. 

As Orature is developed to be cross platform, with intentions to lead towards an Android version, the architecture is currently divided into two main sections: Common (intending to include anything that is completely cross platform) and JVM (which is specific to the desktop JVM platform). Thus, anything that is platform or device specific should define an interface in common, and implement that interface in the platform module (JVM or otherwise). It should be noted that common makes use of Java APIs and libraries, and is not common in the kotlin native sense. As such, common really should be considered a sort of "java common," and is only well suited to be "common" for desktop and Android, not iOS.

