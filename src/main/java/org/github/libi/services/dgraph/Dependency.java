/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.services.dgraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dependency {

  public enum Type {
    API,
    IMPLEMENTATION;

    private static final Map<String, Type> MAP = Collections.unmodifiableMap(
        Arrays.stream(Type.values())
            .collect(Collectors.toMap(Type::toString, Function.identity())));

    public static Optional<Type> fromString(String s) {
      if (s == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(MAP.get(s));
    }
  }

  private Type type;

  private boolean transitive = false;

  private int dist = 0;

}
