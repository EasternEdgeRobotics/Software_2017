package com.easternedgerobotics.rov.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SunUtilLoggingSuppressionRule implements TestRule {
    private final String loggerKey;

    public SunUtilLoggingSuppressionRule(final String loggerKey) {
        this.loggerKey = loggerKey;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Class<?> loggerClass = Class.forName("sun.util.logging.PlatformLogger");
                final Class<?> levelClass = Class.forName("sun.util.logging.PlatformLogger$Level");
                final Method getLogger = loggerClass.getMethod("getLogger", String.class);
                final Method getLevel = loggerClass.getMethod("level");
                final Method setLevel = loggerClass.getMethod("setLevel", levelClass);
                final Field levelSevereField = levelClass.getField("SEVERE");

                final Object logger = getLogger.invoke(null, loggerKey);
                final Object currentLevel = getLevel.invoke(logger);
                final Object levelOff = levelSevereField.get(null);
                setLevel.invoke(logger, levelOff);

                base.evaluate();

                setLevel.invoke(logger, currentLevel);
            }
        };
    }
}
