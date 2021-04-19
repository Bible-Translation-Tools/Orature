package org.wycliffeassociates.otter.jvm.workbookapp.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.wycliffeassociates.otter.common.OratureInfo;

import java.io.File;


public class ConfigureLogger {

    private final String FILE_LOGGER_REF = "logfile";
    private final String CONSOLE_LOGGER_REF = "stdout";
    private final String LOG_FILE_NAME = OratureInfo.SUITE_NAME.toLowerCase();
    private final String LOG_EXT = ".log";

    private File logDir;
    private ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
    private LayoutComponentBuilder layout = builder.newLayout("PatternLayout");

    public ConfigureLogger(File logDir) {
        this.logDir = logDir;
    }

    private void configureConsoleAppender() {
        AppenderComponentBuilder appender = builder.newAppender(CONSOLE_LOGGER_REF, "Console");
        appender.add(layout);
        builder.add(appender);
    }

    private void configureFileAppender() {
        AppenderComponentBuilder fileAppender = builder.newAppender(FILE_LOGGER_REF, "RollingFile");
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                .addComponent(builder.newComponent("SizeBasedTriggeringPolicy")
                .addAttribute("size", "128K"));

        String filename = new StringBuilder()
                .append(logDir.getAbsolutePath())
                .append("/")
                .append(LOG_FILE_NAME)
                .append(LOG_EXT)
                .toString();

        String rolloverPattern = new StringBuilder()
                .append(logDir.getAbsolutePath())
                .append("/")
                .append(LOG_FILE_NAME)
                .append("-%i.zip")
                .toString();

        fileAppender.addAttribute("fileName", filename);
        fileAppender.addAttribute("filePattern", rolloverPattern);
        fileAppender.addAttribute("append", true);
        fileAppender.addAttribute("bufferedIO", true);
        fileAppender.addAttribute("immediateFlush", true);
        fileAppender.add(layout);
        fileAppender.addComponent(triggeringPolicy);
        builder.add(fileAppender);
    }

    private void configureRootLogger() {
        RootLoggerComponentBuilder root = builder.newRootLogger(Level.INFO);
        root.add(builder.newAppenderRef(CONSOLE_LOGGER_REF));
        root.add(builder.newAppenderRef(FILE_LOGGER_REF));
        builder.add(root);
    }

    private void configurePatternLayout() {
        layout.addAttribute("pattern", "%highlight{[%p] %d %c{4}: %msg%n%throwable}");
    }

    public void configure() {
        configurePatternLayout();
        configureConsoleAppender();
        configureFileAppender();
        configureRootLogger();
        Configurator.initialize(builder.build());
    }
}