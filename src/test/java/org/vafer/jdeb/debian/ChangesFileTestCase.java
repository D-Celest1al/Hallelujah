/*
 * Copyright 2013 The jdeb developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vafer.jdeb.debian;

import junit.framework.TestCase;
import org.vafer.jdeb.changes.ChangeSet;
import org.vafer.jdeb.debian.BinaryPackageControlFile;
import org.vafer.jdeb.debian.ChangesFile;

public final class ChangesFileTestCase extends TestCase {

    public void testToString() throws Exception {
        BinaryPackageControlFile packageControlFile = new BinaryPackageControlFile();
        packageControlFile.set("Package", "test-package");
        packageControlFile.set("Description", "This is\na description\non several lines");
        packageControlFile.set("Version", "1.0");

        final ChangesFile changes = new ChangesFile(packageControlFile, new ChangeSet[0]);

        assertEquals("1.0", changes.get("Version"));
    }

}