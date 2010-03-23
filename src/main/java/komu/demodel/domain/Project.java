/*
 * Copyright (C) 2010 Juha Komulainen. All rights reserved.
 */
package komu.demodel.domain;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import komu.demodel.utils.Check;

public final class Project {

    private final String name;
    private final List<InputSource> inputSources = new ArrayList<InputSource>();

    public Project(String name) {
        Check.notNull(name, "name");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addInputSource(InputSource inputSource) {
        Check.notNull(inputSource, "inputSource");

        inputSources.add(inputSource);
    }

    public List<InputSource> getInputSources() {
        return unmodifiableList(inputSources);
    }
}
