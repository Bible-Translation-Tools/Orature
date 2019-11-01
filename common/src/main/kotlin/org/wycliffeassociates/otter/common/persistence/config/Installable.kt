package org.wycliffeassociates.otter.common.persistence.config

/**
 * An entity that is installed to the application, or a configuration task that is only meant to execute on the first
 * time use of the application.
 *
 * @property name the name of the Installable. This is treated as the Installable's primary key and needs to be unique.
 *
 * @property version the version of the Installable. As the implementing use case is updated this value should be
 * increased, and the exec method's implementation should check this against the value of what is currently installed
 * to determine if a migration should be done.
 */
interface Installable : Initializable {
    val name: String
    val version: Int
}