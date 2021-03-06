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
package org.corant.devops.maven.plugin.packaging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.stream.Collectors;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.logging.Log;
import org.corant.devops.maven.plugin.BuildStageException;
import org.corant.devops.maven.plugin.archive.Archive;
import org.corant.devops.maven.plugin.archive.Archive.Entry;
import org.corant.devops.maven.plugin.archive.ClassPathEntry;
import org.corant.devops.maven.plugin.archive.DefaultArchive;
import org.corant.devops.maven.plugin.archive.FileEntry;
import org.corant.devops.maven.plugin.archive.ManifestEntry;
import bin.JarLauncher;

/**
 * corant-devops-maven
 *
 * @author bingo 下午4:33:08
 *
 */
public class JarPackager implements Packager {

  public static final String JAR_LIB_DIR = "lib";
  public static final String JAR_APP_DIR = "app";
  public static final String JAR_CFG_DIR = "cfg";
  public static final String JAR_BIN_DIR = "bin";
  public static final String JAR_LAU_PATH =
      JarLauncher.class.getName().replaceAll("\\.", "/") + ".class";
  public static final String JAR_LAU_NME = JarLauncher.class.getSimpleName() + ".class";

  final PackageMojo mojo;
  final Log log;

  JarPackager(PackageMojo mojo) {
    if (!mojo.isJar()) {
      throw new BuildStageException("Only support jar mojo!");
    }
    this.mojo = mojo;
    log = mojo.getLog();
  }

  @Override
  public void pack() throws Exception {
    log.debug("(corant) start packaging process...");
    doPack(buildArchive(), mojo.getDestination());
    if (mojo.isAttach()) {
      doPackAttach();
    }
  }

  Archive buildArchive() {
    Archive root = DefaultArchive.root();
    DefaultArchive.of(JAR_LIB_DIR, root).addEntries(mojo.getProject().getArtifacts().stream()
        .map(Artifact::getFile).map(FileEntry::of).collect(Collectors.toList()));
    DefaultArchive.of(JAR_APP_DIR, root)
        .addEntry(FileEntry.of(mojo.getProject().getArtifact().getFile()));
    DefaultArchive.of(JAR_BIN_DIR, root).addEntry(ClassPathEntry.of(JAR_LAU_PATH, JAR_LAU_NME));
    DefaultArchive.of(META_INF_DIR, root).addEntry(ManifestEntry.of((attr) -> {
      attr.put(new Attributes.Name("runner-class"), mojo.getMainClass());
      attr.put(Attributes.Name.MAIN_CLASS, JarLauncher.class.getName());
    }));
    log.debug("(corant) built archive for packaging.");
    return root;
  }

  void doPack(Archive root, Path destPath) throws IOException {
    Files.createDirectories(destPath.getParent());
    log.debug(String.format("(corant) created destination dir %s for packaging.",
        destPath.getParent().toUri().getPath()));
    try (JarArchiveOutputStream jos =
        new JarArchiveOutputStream(new FileOutputStream(destPath.toFile()))) {
      // handle entries
      if (!root.getEntries(null).isEmpty()) {
        JarArchiveEntry jarDirEntry = new JarArchiveEntry(root.getPathName());
        jos.putArchiveEntry(jarDirEntry);
        jos.closeArchiveEntry();
        log.debug(String.format("(corant) created dir %s", jarDirEntry.getName()));
        for (Entry entry : root) {
          JarArchiveEntry jarFileEntry = new JarArchiveEntry(root.getPathName() + entry.getName());
          jos.putArchiveEntry(jarFileEntry);
          IOUtils.copy(entry.getInputStream(), jos);
          jos.closeArchiveEntry();
          log.debug(String.format("(corant) created entry %s", jarFileEntry.getName()));
        }
      }
      // handle child archives
      List<Archive> childrenArchives = new LinkedList<>(root.getChildren());
      while (!childrenArchives.isEmpty()) {
        Archive childArchive = childrenArchives.remove(0);
        if (!childArchive.getEntries(null).isEmpty()) {
          JarArchiveEntry childJarDirEntry = new JarArchiveEntry(childArchive.getPathName());
          jos.putArchiveEntry(childJarDirEntry);
          jos.closeArchiveEntry();
          log.debug(String.format("(corant) created dir %s", childJarDirEntry.getName()));
          for (Entry childEntry : childArchive) {
            JarArchiveEntry childJarFileEntry =
                new JarArchiveEntry(childArchive.getPathName() + childEntry.getName());
            jos.putArchiveEntry(childJarFileEntry);
            IOUtils.copy(childEntry.getInputStream(), jos);
            jos.closeArchiveEntry();
            log.debug(String.format("(corant) created entry %s", childJarFileEntry.getName()));
          }
        }
        childrenArchives.addAll(childArchive.getChildren());
      }
    }
  }

  void doPackAttach() {
    DefaultArtifact artifact =
        new DefaultArtifact(mojo.getProject().getGroupId(), mojo.getProject().getArtifactId(),
            mojo.getProject().getVersion(), mojo.getProject().getArtifact().getScope(), "jar",
            mojo.getClassifier(), new DefaultArtifactHandler("jar"));
    artifact.setFile(mojo.getDestination().toFile());
    mojo.getProject().addAttachedArtifact(artifact);
    log.debug("(corant) created attach!");
  }

}
