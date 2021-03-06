/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.shared.util;

import static org.corant.shared.normal.Defaults.ONE_MB;
import static org.corant.shared.util.Assertions.shouldBeTrue;
import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.ObjectUtils.defaultObject;
import static org.corant.shared.util.StreamUtils.asStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.Checksum;
import org.corant.shared.exception.CorantRuntimeException;

/**
 * Origin of code: org.apache.commons.io.FileUtils
 *
 * @author bingo 下午3:54:56
 *
 */
public class FileUtils {

  public static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 16;
  public static final String JAR_URL_SEPARATOR = "!/";
  public static final String FILE_URL_PREFIX = "file:";
  protected static final Logger logger = Logger.getLogger(FileUtils.class.getName());
  static final String[] JARS = new String[] {"jar", "war, ", "zip", "vfszip", "wsjar"};

  private FileUtils() {
    super();
  }

  public static Checksum checksum(final File file, final Checksum checksum) throws IOException {
    if (file.isDirectory()) {
      throw new IllegalArgumentException("Checksums can't be computed on directories");
    }
    try (InputStream is = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      int length;
      while ((length = is.read(buffer)) != -1) {
        checksum.update(buffer, 0, length);
      }
    }
    return checksum;
  }

  public static void copyFile(final File srcFile, final File destFile,
      final boolean preserveFileDate) throws IOException {
    if (destFile.exists() && destFile.isDirectory()) {
      throw new IOException("Destination '" + destFile + "' exists but is a directory");
    }
    try (FileInputStream fis = new FileInputStream(srcFile);
        FileChannel input = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(destFile);
        FileChannel output = fos.getChannel()) {
      final long size = input.size();
      long pos = 0;
      long count = 0;
      while (pos < size) {
        final long remain = size - pos;
        count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
        final long bytesCopied = output.transferFrom(input, pos, count);
        if (bytesCopied == 0) {
          break;
        }
        pos += bytesCopied;
      }
    }
    final long srcLen = srcFile.length();
    final long dstLen = destFile.length();
    if (srcLen != dstLen) {
      throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
          + "' Expected length: " + srcLen + " Actual: " + dstLen);
    }
    if (preserveFileDate && destFile.setLastModified(srcFile.lastModified())) {
      logger.warning(() -> String.format("Can not preserve file date for file %s!",
          destFile.getAbsolutePath()));
    }
  }

  public static void copyToFile(final InputStream source, final File destination)
      throws IOException {
    try (InputStream in = source; OutputStream out = new FileOutputStream(destination)) {
      StreamUtils.copy(in, out);
    }
  }

  public static File createTempDir(String prefix, String suffix) {
    try {
      File tempDir = File.createTempFile(prefix, suffix);
      boolean notExist = true;
      if (tempDir.exists()) {
        notExist = tempDir.delete();
      }
      if (notExist) {
        if (tempDir.mkdir()) {
          logger.info(() -> String.format("Created temp dir %s!", tempDir.getAbsolutePath()));
        } else {
          throw new CorantRuntimeException("Unable to create tempDir. java.io.tmpdir is set to %s"
              + System.getProperty("java.io.tmpdir"));
        }
      }
      tempDir.deleteOnExit();
      return tempDir;
    } catch (IOException ex) {
      throw new CorantRuntimeException(ex);
    }
  }

  public static void extractJarFile(Path src, Path dest, Predicate<JarEntry> filter)
      throws IOException {
    shouldBeTrue(src != null && dest != null, "Extract jar file the src and dest can not null");
    try (JarFile jar = new JarFile(shouldNotNull(src.toFile()))) {
      Predicate<JarEntry> useFilter = defaultObject(filter, e -> true);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry each = entries.nextElement();
        if (useFilter.test(each)) {
          Path eachPath = dest.resolve(each.getName().replace('/', File.separatorChar));
          if (each.isDirectory()) {
            if (!Files.exists(eachPath)) {
              Files.createDirectories(eachPath);
            }
          } else {
            Files.copy(jar.getInputStream(each), eachPath);
          }
        }
      }
    }
  }

  public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
    if (asStream(JARS).anyMatch(p -> ObjectUtils.isEquals(p, jarUrl.getProtocol()))) {
      String urlFile = jarUrl.getFile();
      int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
      if (separatorIndex != -1) {
        String jarFile = urlFile.substring(0, separatorIndex);
        try {
          return new URL(jarFile);
        } catch (MalformedURLException ex) {
          if (!jarFile.startsWith("/")) {
            jarFile = "/" + jarFile;
          }
          return new URL(FILE_URL_PREFIX + jarFile);
        }
      } else {
        return jarUrl;
      }
    } else {
      return null;
    }
  }

}
