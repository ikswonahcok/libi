/*
 * Copyright (c) 2022. Bartłomiej Kochanowski
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 *
 */

package org.github.libi.cmdtree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class GradleCommandTreeReader {

  private final NewLineListener newLineListener;

  public GradleCommandTreeReader(NewLineListener newLineListener) {
    this.newLineListener = newLineListener;
  }

  public void readLines(Process process) throws IOException {
    try (var bis = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      readLines(bis.lines());
    }
  }

  public void readLines(Stream<String> lines) {
    lines.forEach(this::readLine);
  }

  private void readLine(String line) {
    newLineListener.onNewLine(getLineTreeLevel(line), getLineText(line));
  }

  private int getLineTreeLevel(String line) {
    int pos = line.indexOf("---");
    if (pos < 0) {
      return 0;
    }
    return (pos - 1) / 5 + 1;
  }

  private String getLineText(String line) {
    var level = getLineTreeLevel(line);
    if (level == 0) {
      return line;
    }
    return line.substring(5 * level);
  }

  public interface NewLineListener {

    void onNewLine(int treeLevel, String text);
  }
}
