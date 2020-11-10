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

package Premain;

import Ozone.Desktop.SharedBootstrap;
import io.sentry.Sentry;


public class InstallerEntryPoint {

    //Standalone only
    public static void main(String[] args) {
        try {
            SharedBootstrap.classloaderNoParent();
            SharedBootstrap.loadStandalone();
            SharedBootstrap.loadMain("Main.OzoneInstaller", args);
        }catch (Throwable t) {
            Sentry.captureException(t);
            t.printStackTrace();
        }
    }
}
