/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("FunctionNaming")
package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

class InitializeProjects @Inject constructor(
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val takeRepo: ITakeRepository,
    private val directoryProvider: IDirectoryProvider,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val workbookRepository: IWorkbookRepository,
    private val rcImporterFactory: RCImporterFactory
) : Installable {
    override val name = "PROJECTS"
    override val version = 2

    private val log = LoggerFactory.getLogger(InitializeProjects::class.java)
    private lateinit var callback: ProjectImporterCallback

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        return Completable.fromCallable {
            var installedVersion = installedEntityRepo.getInstalledVersion(this)
            if (installedVersion != version) {
                log.info("Initializing $name version: $version...")
                progressEmitter.onNext(ProgressStatus(titleKey = "initializingProjects"))

                migrate()

                installedEntityRepo.install(this)
                log.info("$name version: $version installed!")
            } else {
                log.info("$name up to date with version: $version")
            }

            if (fetchProjects().isEmpty()) {
                log.info("Importing projects...")
                callback = setupImportCallback(progressEmitter)

                val dir = directoryProvider.getUserDataDirectory("/")
                importProjects(dir)
            }
        }
    }

    private fun migrate() {
        `migrate to version 1`()
        `migrate sources to version 2`()
    }

    private fun `migrate to version 1`() {
        `migrate takes to version 1`()

        val projects = fetchProjects()
        `migrate projects to version 1`(projects)
    }

    private fun `migrate projects to version 1`(workbooks: List<Workbook>) {
        workbooks.forEach { workbook ->
            // Migrate main rc
            `migrate project to version 1`(workbook.target.resourceMetadata, workbook)

            // Migrate linked resources
            workbook.target.linkedResources.forEach { targetRc ->
                `migrate project to version 1`(targetRc, workbook)
            }
        }
    }

    private fun `migrate project to version 1`(targetMetadata: ResourceMetadata, workbook: Workbook) {
        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            targetMetadata,
            workbook.target.toCollection()
        )
        val linkedResource = workbook.source.linkedResources
            .firstOrNull { it.identifier == targetMetadata.identifier }

        val projectIsBook = targetMetadata.identifier == workbook.target.resourceMetadata.identifier

        projectFilesAccessor.initializeResourceContainerInDir()
        projectFilesAccessor.copySourceFiles(linkedResource)
        projectFilesAccessor.writeSelectedTakesFile(workbook, projectIsBook)
    }

    private fun `migrate sources to version 2`() {
        resourceMetadataRepo.getAllSources().blockingGet()
            .forEach { resourceMetadata ->
                if (resourceMetadata.path.isFile) {
                    val sourceFile = resourceMetadata.path
                    val dirName = "${resourceMetadata.language.slug}_${resourceMetadata.identifier}-source"
                    val targetDir = sourceFile.parentFile.resolve(dirName)
                    if (targetDir.exists() && targetDir.list()?.any() == true) {
                        targetDir.deleteRecursively()
                    }

                    directoryProvider.newFileReader(sourceFile).use { reader ->
                        val entries = reader.list(".").toList()
                        when {
                            entries.size == 1 -> {
                                // root is a directory, copy its content to avoid nested dirs
                                reader.copyDirectory(entries.first(), targetDir)
                            }

                            else -> {
                                reader.copyDirectory("/", targetDir)
                            }
                        }
                    }

                    // Delete old resource container
                    resourceMetadata.path.delete()

                    val updatedRc = resourceMetadata.copy(path = targetDir)
                    resourceMetadataRepo.update(updatedRc).blockingGet()
                }
            }
    }

    private fun `migrate takes to version 1`() {
        takeRepo.getAll().blockingGet()
            .forEach { take ->
                val projectDir = take.path.parentFile.parentFile

                if (projectDir.toString().contains(ProjectFilesAccessor.getTakesDirPath())) {
                    // Perhaps already migrated. Skipping...
                    return@forEach
                }

                val takesDir = projectDir.resolve(ProjectFilesAccessor.getTakesDirPath())
                val chapterDir = takesDir.resolve(take.path.parentFile.name)

                chapterDir.mkdirs()

                val destFile = chapterDir.resolve(take.path.name)
                take.path.renameTo(destFile)

                val updatedTake = take.copy(path = destFile)
                takeRepo.update(updatedTake).blockingGet()

                // Delete empty dir
                take.path.parentFile.delete()
            }
    }

    private fun importProjects(dir: File) {
        if (dir.isFile) return

        dir.listFiles()?.forEach {
            // Find resource containers to import
            val manifest = Path.of("manifest.yaml")
            if (it.isFile && it.toPath().contains(manifest)) {
                importProject(it.parentFile)
            }
            importProjects(it)
        }
    }

    private fun importProject(project: File) {
        rcImporterFactory.makeImporter()
            .import(project, callback).toObservable()
            .doOnError { e ->
                log.error("Error importing ${project.name}.", e)
            }
            .blockingSubscribe {
                log.info("${project.name} imported!")
            }
    }

    private fun fetchProjects(): List<Workbook> {
        return workbookRepository.getProjects()
            .doOnError { e ->
                log.error("Error in loading projects", e)
            }
            .blockingGet()
    }

    private fun createTempFile(name: String, extension: String): File {
        val tempDir = Files.createTempDirectory("orature_temp")
        val tempPath = tempDir.resolve("$name.$extension")
        val tempFile = tempPath.toFile()
        tempFile.deleteOnExit()
        return tempFile
    }
}
