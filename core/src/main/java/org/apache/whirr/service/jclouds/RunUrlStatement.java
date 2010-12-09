/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.service.jclouds;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.scriptbuilder.domain.Statements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunUrlStatement implements Statement {

  private static final Logger LOG =
    LoggerFactory.getLogger(RunUrlStatement.class);
  
  private String runUrl;

  public RunUrlStatement(String runUrlBase, String url)
      throws IOException {
    this(runUrlBase, url, true);
  }
  
  public RunUrlStatement(String runUrlBase, String url, boolean checkUrlExists)
      throws IOException {
    URL runUrl = new URL(new URL(runUrlBase), url);
    if (checkUrlExists) {
      checkUrlExists(runUrl, "Runurl %s not found.", runUrl);
    }
    this.runUrl = runUrl.toExternalForm();
  }

  @Override
  public Iterable<String> functionDependecies(OsFamily family) {
    return ImmutableSet.<String>of("installRunUrl");
  }

  @Override
  public String render(OsFamily family) {
    return Statements.exec("runurl " + runUrl).render(family);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof RunUrlStatement) {
      RunUrlStatement that = (RunUrlStatement) o;
      return Objects.equal(runUrl, that.runUrl);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(runUrl);
  }
  
  public static void checkUrlExists(URL url, String errorMessageTemplate,
      Object... errorMessageArgs)
      throws IOException {
    if (!urlExists(url)) {
      throw new IllegalArgumentException(
          String.format(errorMessageTemplate, errorMessageArgs));
    }
  }
       
  private static boolean urlExists(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    try {
      connection.setRequestMethod("HEAD"); 
      connection.connect();
      int responseCode = connection.getResponseCode();
      LOG.debug("Response code {} from {}", responseCode, url);
      return responseCode == HttpURLConnection.HTTP_OK;
    } finally {
      connection.disconnect();
    }
  }
}
