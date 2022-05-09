/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class Artifact {

    private final String group;

    private final String name;

    @EqualsAndHashCode.Exclude
    private final Set<String> compileDependencies = new HashSet<>();

    @EqualsAndHashCode.Exclude
    private final Set<String> runtimeDependencies = new HashSet<>();

    @EqualsAndHashCode.Exclude
    private boolean isLibrary;

    public Artifact(String id) {
        var tokens = id.split(":");
        this.group = tokens[0];
        this.name = tokens[1];
    }

    public String getId() {
        return group + ":" + name;
    }
}
