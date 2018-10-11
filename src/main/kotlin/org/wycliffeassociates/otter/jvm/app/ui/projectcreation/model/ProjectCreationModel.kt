package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import tornadofx.*

class ProjectCreationModel {
    var sourceLanguageProperty: Language by property()

    var targetLanguageProperty: Language by property()

    var resourceSelected : Collection by property()
//    var resource by resourceProperty
   // var resourceProperty = getProperty(ProjectCreationModel::resourceSelected)

    private var project: ObservableList<Collection> by property(
            FXCollections.observableList(ProjectList().projectList
            )
    )

    var projectProperty = getProperty(ProjectCreationModel::project)

    /*
    TODO adding Resources, Filtering and Book Selection to model
     */

}