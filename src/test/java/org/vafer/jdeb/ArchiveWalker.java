/*
 * Copyright 2014 The jdeb developers.
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

package org.vafer.jdeb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

/**
 * Support class for inspecting the content of an archive.
 * 
 * @author Emmanuel Bourg
 */
public class ArchiveWalker {
    
    public static void walk(ArchiveInputStream in, ArchiveVisitor visitor) throws IOException {
        try {
            ArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                byte[] content = new byte[(int) entry.getSize()];
                if (entry.getSize() > 0) {
                    int length = in.read(content);
                    if (length != entry.getSize()) {
                        throw new IOException("Couldn't read entry " + entry.getName() + " : read " + length + ", expected " + entry.getSize());
                    }
                }
                
                visitor.visit(entry, content);
            }
        } finally {
            in.close();
        }
    }

    public static boolean walkFileAr(File archive, final ArchiveVisitor<ArArchiveEntry> visitor) throws IOException {
    	final AtomicBoolean found = new AtomicBoolean(false);
        ArArchiveInputStream in = new ArArchiveInputStream(new FileInputStream(archive));
        ArchiveWalker.walk(in, new ArchiveVisitor<ArArchiveEntry>() {
            public void visit(ArArchiveEntry entry, byte[] content) throws IOException {
                found.set(true);
                visitor.visit(entry, content);
            }
        });
        return found.get();
    }

    public static boolean walkFileTar(File archive, final ArchiveVisitor<TarArchiveEntry> visitor) throws IOException {
    	final AtomicBoolean found = new AtomicBoolean(false);
        TarArchiveInputStream in = new TarArchiveInputStream(new FileInputStream(archive));
        ArchiveWalker.walk(in, new ArchiveVisitor<TarArchiveEntry>() {
            public void visit(TarArchiveEntry entry, byte[] content) throws IOException {
                found.set(true);
                visitor.visit(entry, content);
            }
        });
        return found.get();
    }

    public static boolean walkControl(final File archive, final ArchiveVisitor<TarArchiveEntry> visitor) throws IOException {
        return walkEmbeddedInAr(archive, "control.tar", visitor, Compression.GZIP);
    }

    public static boolean walkPackage(final File archive, final ArchiveVisitor<TarArchiveEntry> visitor) throws IOException {
        return walkEmbeddedInTar(archive, "package", visitor, Compression.GZIP, ".tgz");
    }

    public static boolean walkData(final File archive, final ArchiveVisitor<TarArchiveEntry> visitor, final Compression compression) throws IOException {
        return walkEmbeddedInAr(archive, "data.tar", visitor, compression);
    }

    public static boolean walkEmbeddedInAr(final File archive, final String name, final ArchiveVisitor<TarArchiveEntry> visitor, final Compression compression) throws IOException {
    	return walkEmbeddedInAr(archive, name, visitor, compression, null);
    }
    
    public static boolean walkEmbeddedInAr(final File archive, final String name, final ArchiveVisitor<TarArchiveEntry> visitor, final Compression compression, final String extension) throws IOException {
        final AtomicBoolean found = new AtomicBoolean(false);
        ArArchiveInputStream in = new ArArchiveInputStream(new FileInputStream(archive));
        
        final String packageName;
        if (extension != null) {
        	packageName = name + extension;
        } else {
        	packageName = name + compression.getExtension();
        }
        
        ArchiveWalker.walk(in, new ArchiveVisitor<ArArchiveEntry>() {
            public void visit(ArArchiveEntry entry, byte[] content) throws IOException {
                if (entry.getName().equals(packageName)) {
                    InputStream in = new ByteArrayInputStream(content);
                    if (compression == Compression.GZIP) {
                        in = new GZIPInputStream(in);
                    } else if (compression == Compression.XZ) {
                        in = new XZCompressorInputStream(in);
                    } else if (compression == Compression.BZIP2) {
                        in = new BZip2CompressorInputStream(in);
                    }
                    
                    ArchiveWalker.walk(new TarArchiveInputStream(in), new ArchiveVisitor<TarArchiveEntry>() {
                        public void visit(TarArchiveEntry entry, byte[] content) throws IOException {
                            found.set(true);
                            visitor.visit(entry, content);
                        }
                    });
                }
            }
        });
        
        return found.get();
    }

    public static boolean walkEmbeddedInTar(final File archive, final String name, final ArchiveVisitor<TarArchiveEntry> visitor, final Compression compression) throws IOException {
    	return walkEmbeddedInTar(archive, name, visitor, compression, null);
    }
    
    public static boolean walkEmbeddedInTar(File archive, final String name, final ArchiveVisitor<TarArchiveEntry> visitor, final Compression compression, final String extension) throws IOException {
        final AtomicBoolean found = new AtomicBoolean(false);
        TarArchiveInputStream in = new TarArchiveInputStream(new FileInputStream(archive));
        
        final String packageName;
        if (extension != null) {
        	packageName = name + extension;
        } else {
        	packageName = name + compression.getExtension();
        }
        
        ArchiveWalker.walk(in, new ArchiveVisitor<TarArchiveEntry>() {
            public void visit(TarArchiveEntry entry, byte[] content) throws IOException {
                if (entry.getName().equals(packageName)) {
                    InputStream in = new ByteArrayInputStream(content);
                    if (compression == Compression.GZIP) {
                        in = new GZIPInputStream(in);
                    } else if (compression == Compression.XZ) {
                        in = new XZCompressorInputStream(in);
                    } else if (compression == Compression.BZIP2) {
                        in = new BZip2CompressorInputStream(in);
                    }
                    
                    ArchiveWalker.walk(new TarArchiveInputStream(in), new ArchiveVisitor<TarArchiveEntry>() {
                        public void visit(TarArchiveEntry entry, byte[] content) throws IOException {
                            found.set(true);
                            visitor.visit(entry, content);
                        }
                    });
                }
            }
        });
        
        return found.get();
    }
}
