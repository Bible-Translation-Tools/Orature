package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty

class ChunkViewData(val number: Int, val completedProperty: BooleanProperty, val selectedChunkProperty: IntegerProperty)