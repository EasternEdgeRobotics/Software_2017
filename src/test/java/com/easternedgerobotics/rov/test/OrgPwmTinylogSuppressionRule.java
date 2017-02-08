package com.easternedgerobotics.rov.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

public final class OrgPwmTinylogSuppressionRule implements TestRule {
    private final Class<?> loggerKey;

    public OrgPwmTinylogSuppressionRule(final Class<?> loggerKey) {
        this.loggerKey = loggerKey;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Level currentLevel = Logger.getLevel(loggerKey);
                Configurator.currentConfig().level(loggerKey, Level.ERROR).activate();
                base.evaluate();
                Configurator.currentConfig().level(loggerKey, currentLevel).activate();
            }
        };
    }
}
