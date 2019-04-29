package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view.CardGrid
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.TakeManagementView
import tornadofx.*

class MainViewViewModel : ViewModel() {
    val selectedProjectProperty = SimpleObjectProperty<Collection>()
    val selectedProjectName = SimpleStringProperty()
    val selectedProjectLanguage = SimpleStringProperty()

    val selectedCollectionProperty = SimpleObjectProperty<Collection>()
    val selectedCollectionTitle = SimpleStringProperty()
    val selectedCollectionBody = SimpleStringProperty()

    val selectedContentProperty = SimpleObjectProperty<Content>()
    val selectedContentTitle = SimpleStringProperty()
    val selectedContentBody = SimpleStringProperty()

    val takesPageDocked = SimpleBooleanProperty(false)

    init {
        selectedProjectProperty.onChange {
            if (it != null) {
                projectSelected(it)
            }
        }

        selectedCollectionProperty.onChange {
            if (it != null) {
                collectionSelected(it)
            }
        }

        selectedContentProperty.onChange {
            if (it != null) {
                contentSelected(it)
            }
            else { // the take manager was undocked
                takesPageDocked.set(false)
            }
        }
    }

    private fun projectSelected(selectedProject: Collection) {
        setActiveProjectText(selectedProject)

        find<MainScreenView>().activeFragment.dock<CardGrid>()
        CardGrid().apply {
            activeProject.bindBidirectional(selectedProjectProperty)
            activeCollection.bindBidirectional(selectedCollectionProperty)
            activeContent.bindBidirectional(selectedContentProperty)
        }
    }

    private fun collectionSelected(collection: Collection) {
        setActiveCollectionText(collection)
    }

    private fun contentSelected(content: Content) {
        setActiveContentText(content)

        if(takesPageDocked.value == false) {
            find<MainScreenView>().activeFragment.dock<TakeManagementView>()
            TakeManagementView().apply {
                activeProject.bindBidirectional(selectedProjectProperty)
                activeCollection.bindBidirectional(selectedCollectionProperty)
                activeContent.bindBidirectional(selectedContentProperty)
            }
        }
        takesPageDocked.set(true)
    }

    private fun setActiveContentText(content: Content) {
        selectedContentTitle.set(content.labelKey.toUpperCase())
        selectedContentBody.set(content.start.toString())
    }

    private fun setActiveCollectionText(collection: Collection) {
        selectedCollectionTitle.set(collection.labelKey.toUpperCase())
        selectedCollectionBody.set(collection.titleKey)
    }

    private fun setActiveProjectText(activeProject: Collection) {
        selectedProjectName.set(activeProject.titleKey)
        selectedProjectLanguage.set(activeProject.resourceContainer?.language?.name)
    }
}