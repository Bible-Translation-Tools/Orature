package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

/**
 * This data class is used to hold the data that is selected from within the widget.
 *
 * @author Caleb Benedick
 * @author Matthew Russell
 */
data class FilterableItem<T>(
    val item: T,
    val filterText : List<String>
)
