package org.sonar.plugins.pmd;

import org.sonar.api.resources.Language;

public class PmdLanguage implements Language {
    @Override
    public String getKey() {
        return "pmd";
    }

    @Override
    public String getName() {
        return "Pmd";
    }

    @Override
    public String[] getFileSuffixes() {
        return new String[]{".java"};
    }
}
