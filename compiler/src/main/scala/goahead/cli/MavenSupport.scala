package goahead.cli

import java.nio.file.{Path, Paths}
import java.util.zip.ZipFile

import goahead.Logger
import goahead.compile.ClassPath
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.artifact.{Artifact, DefaultArtifact}
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.{LocalRepository, RemoteRepository}
import org.eclipse.aether.resolution.{ArtifactRequest, DependencyRequest}
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.util.version.GenericVersionScheme
import org.eclipse.aether.{RepositorySystem, RepositorySystemSession}

trait MavenSupport {
  def classPathEntries(): Seq[ClassPath.Entry]
  def classEntriesByCompileToDir(
    confClasses: Seq[CompileConfig.ConfCls],
    classPath: ClassPath
  ): Seq[(Path, Seq[ClassPath.ClassDetails])]
}

object MavenSupport {

  val invalidDirNameChars = "/!#$%&'()*,:;<=>?[\\]^`{|})".toSet

  val empty = new MavenSupport {
    override def classPathEntries() = Nil
    override def classEntriesByCompileToDir(confClasses: Seq[CompileConfig.ConfCls], classPath: ClassPath) = Nil
  }

  def apply(conf: CompileConfig.Maven): MavenSupport = new Aether(conf)

  class Aether(conf: CompileConfig.Maven) extends MavenSupport with Logger {
    require(conf.dependencyScopes.nonEmpty)

    lazy val repoSys: RepositorySystem = {
      MavenRepositorySystemUtils.newServiceLocator().
        addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory]).
        addService(classOf[TransporterFactory], classOf[FileTransporterFactory]).
        addService(classOf[TransporterFactory], classOf[HttpTransporterFactory]).
        getService(classOf[RepositorySystem])
    }

    lazy val sess: RepositorySystemSession = {
      val session = MavenRepositorySystemUtils.newSession()
      session.setLocalRepositoryManager(repoSys.newLocalRepositoryManager(session, new LocalRepository(conf.localRepo)))
      session
    }

    lazy val repos: Seq[RemoteRepository] = {
      Seq(
        new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build()
      )
    }

    lazy val artifacts: Map[(String, String), Artifact] = {
      import scala.collection.JavaConverters._
      val arts = repoSys.resolveArtifacts(sess, conf.artifacts.map({ str =>
        new ArtifactRequest().setRepositories(repos.asJava).setArtifact(new DefaultArtifact(str))
      }).asJava).asScala.map(_.getArtifact).distinct
      // Make sure there are no ambiguities
      arts.groupBy(a => a.getGroupId -> a.getArtifactId).mapValues {
        case Seq(singleArt) =>
          logger.debug(s"Resolved artifact: $singleArt")
          singleArt
        case multi => sys.error(s"Conflicting artifacts found: ${multi.mkString(", ")}")
      }
    }

    lazy val artifactDependencies: Seq[Artifact] = {
      import scala.collection.JavaConverters._
      val req = new DependencyRequest().
        setCollectRequest(new CollectRequest().
          setDependencies(artifacts.values.toSeq.map(a => new Dependency(a, JavaScopes.COMPILE)).asJava).
          setRepositories(repos.asJava)
        ).
        setFilter(DependencyFilterUtils.classpathFilter(conf.dependencyScopes:_*))
      val deps = repoSys.resolveDependencies(sess, req).getArtifactResults.asScala.map(_.getArtifact).distinct
      logger.debug("Resolved dependencies:\n  " + deps.sortBy(a => a.getGroupId -> a.getArtifactId).mkString("\n  "))
      // We need to exclude any ones in the main list and get the highest version of remaining
      val versScheme = new GenericVersionScheme
      deps.groupBy(a => a.getGroupId -> a.getArtifactId).flatMap({ case (key, arts) =>
        // Sort to make head the best
        val artsWithVersion = arts.map(a => versScheme.parseVersion(a.getVersion) -> a).sortBy(_._1)
        artifacts.get(key) match {
          case Some(other) =>
            if (versScheme.parseVersion(other.getVersion).compareTo(artsWithVersion.head._1) < 0) {
              logger.warn(s"Using explicitly defined maven artifact $other instead of " +
                s"newer transitive dependency ${artsWithVersion.head._2}")
            }
            None
          case None =>
            if (artsWithVersion.length > 1) {
              logger.warn(s"Using artifact ${artsWithVersion.head} and evicting older " +
                s"dependencies: ${artsWithVersion.tail.map(_._2).mkString(", ")}")
            }
            Some(artsWithVersion.head._2)
        }
      }).toSeq
    }

    override def classPathEntries() = {
      // All artifacts and dependencies are on the class path
      val importDirBase =
        if (conf.importDirBase.isEmpty || conf.importDirBase.endsWith("/"))conf.importDirBase
        else conf.importDirBase + "/"
      // We have to make sure there aren't multiple with the same import path
      val artsByImportPaths = (artifacts.values ++ artifactDependencies).groupBy { art =>
        s"$importDirBase${importName(art.getGroupId)}/${importName(art.getArtifactId)}"
      }
      artsByImportPaths.toSeq.map { case (importPath, arts) =>
        if (arts.size > 1) sys.error(s"Multiple artifacts with import path $importPath: ${arts.mkString(", ")}")
        new AetherClassPathEntry(arts.head, importPath)
      }
    }

    override def classEntriesByCompileToDir(confClasses: Seq[CompileConfig.ConfCls], classPath: ClassPath) = {
      var toCheck = artifacts.values.toSet
      if (conf.compileDependencies) toCheck ++= artifactDependencies
      val basePath = Paths.get(conf.outDirBase)
      // Go over all entries in the class path and take em from there
      // We don't load the class until it matches a name
      classPath.entries.collect({ case e: AetherClassPathEntry if toCheck.contains(e.artifact) => e }).flatMap { ent =>
        val classes = ent.allClassNames().flatMap { className =>
          val nameMatches = confClasses.filter(_.classNameMatches(className))
          if (nameMatches.isEmpty) None else {
            val clsDet = ent.findClass(className).get
            if (!nameMatches.exists(_.classMatches(clsDet.cls))) None else Some(clsDet)
          }
        }
        if (classes.isEmpty) None else Some {
          val path = basePath.resolve(importName(ent.artifact.getGroupId)).
            resolve(importName(ent.artifact.getArtifactId))
          logger.info(s"Compiling some classes from maven artifact ${ent.artifact} " +
            s"at ${ent.artifact.getFile} to path $path")
          path -> classes.toSeq
        }
      }
    }

    def importName(str: String): String = str.filterNot(invalidDirNameChars.contains)
  }

  class AetherClassPathEntry(
    val artifact: Artifact,
    relativeCompiledDir: String
  ) extends ClassPath.Entry.SingleDirJarEntry(new ZipFile(artifact.getFile), relativeCompiledDir)
}
