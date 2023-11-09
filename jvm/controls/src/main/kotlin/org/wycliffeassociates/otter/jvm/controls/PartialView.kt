package org.wycliffeassociates.otter.jvm.controls

import tornadofx.*

/**
 * An alternative component for Fragment with the capability of
 * freeing up resources when detached from the scene and mitigating
 * memory leak issues.
 *
 * It is highly recommended to use this class instead of Fragment.
 */
abstract class PartialView : View()