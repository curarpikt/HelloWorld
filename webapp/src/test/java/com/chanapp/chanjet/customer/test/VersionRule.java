package com.chanapp.chanjet.customer.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class VersionRule implements TestRule {
    private static class VersionStatement extends Statement {
        private final Statement stat;
        private final Description desc;

        private VersionStatement(Statement stat, Description desc) {
            this.stat = stat;
            this.desc = desc;
        }

        @Override
        public void evaluate() throws Throwable {
            TestVersions anno = desc.getAnnotation(TestVersions.class);
            if (anno == null) {
                anno = desc.getTestClass().getAnnotation(TestVersions.class);
            }
            String[] versions = null;
            if (anno != null) {
                versions = anno.value();
            }
            if (versions == null || versions.length == 0) {
                versions = BaseTest.VERSIONS;
            }

            String test = "========================== " + desc.getClassName() + "#" + desc.getMethodName()
                    + "(): %s Test %s ==============================";

            for (String version : versions) {
                try {
                    if (BaseTest.switchTo(version)) { // validate version
                        // System.out.println(String.format(test, version,
                        // "Start"));

                        stat.evaluate();

                        System.out.println(String.format(test, version, "Success"));
                    }
                } catch (Throwable t) {
                    System.out.println(String.format(test, version, "Failure"));
                    throw t; // fail fast
                } finally {
                    BaseTest.switchToDefault();
                }
            }
        }
    }

    @Override
    public Statement apply(Statement stat, Description desc) {
        return new VersionStatement(stat, desc);
    }

}
