/*
 * Copyright 2020 Itzbenz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Ozone.Desktop;

import java.util.HashMap;

public class Propertied {
    public static HashMap<String, String> h = new HashMap<>();

    static {
        try {
            for (String s : new String(ClassLoader.getSystemResourceAsStream("Manifest.properties").readAllBytes()).split("\n"))
                if (s.startsWith("#")) continue;
                else h.put(s.split("=")[0], s.split("=")[1]);
        }catch (Throwable ignored) {
        }
    }
}
