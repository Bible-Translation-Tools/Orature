/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
