/*
 * Copyright 2012 The Apache Software Foundation.
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
package org.vafer.jdeb.producers;

import java.io.IOException;

import org.vafer.jdeb.DataConsumer;
import org.vafer.jdeb.DataProducer;
import org.vafer.jdeb.mapping.Mapper;

/**
 * DataProducer representing a single file
 * For cross-platform permissions and ownerships you probably want to use a Mapper, too.
 *
 * @author Thomas Mortagne <thomas.mortagne@gmail.com>
 */
public final class DataProducerLink extends AbstractDataProducer implements DataProducer {

    private final String path;
    private final String linkName;
    private final boolean symlink;

    public DataProducerLink(final String name, final String linkName, final boolean symlink, String[] pIncludes, String[] pExcludes, Mapper[] pMapper) {
        super(pIncludes, pExcludes, pMapper);
        this.path = name;
        this.symlink = symlink;
        this.linkName = linkName;
    }

    public void produce( final DataConsumer pReceiver ) throws IOException {
        pReceiver.onEachLink(path, linkName, symlink, "root", 0, "root", 0);
    }

}
