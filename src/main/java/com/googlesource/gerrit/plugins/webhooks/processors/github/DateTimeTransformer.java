// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.webhooks.processors.github;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.function.Function;
import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeTransformer implements Function<PersonIdent, String> {
  private static final Logger log = LoggerFactory.getLogger(DateTimeTransformer.class);

  @Override
  public String apply(PersonIdent ident) {
    if (ident.getWhen() == null) {
      return null;
    }

    Instant instant = ident.getWhen().toInstant();
    ZoneId zoneId =
        ident.getTimeZone() != null
            ? ident.getTimeZone().toZoneId()
            : TimeZone.getDefault().toZoneId();
    ZonedDateTime date = ZonedDateTime.ofInstant(instant, zoneId);

    try {
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(date);
    } catch (DateTimeException e) {
      log.info("Parsing date [{}] failed", ident.getWhen(), e);
    }
    return null;
  }
}
