/*
 * Copyright (C) 2011 Google Inc.
 * Copyright (C) 2015 Christopher Friedt <chrisfriedt@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sliderule.api;

import org.sliderule.model.*;

import java.io.*;

/**
 * Interface for processing results as they complete. Callers must invoke {@link #close()} after
 * all trials have been {@linkplain #processTrial processed}.
 */
public interface ResultProcessor extends Closeable {
  void processTrial(Trial trial);
}
