package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import tornadofx.*

class SelectBook() : View() {
    val viewModel: ProjectCreationViewModel by inject()
    //val root = DataGrid<Project>()
//    var mappedList: ObservableList<Project>
    init {
//        mappedList = viewmodel.projectsProperty.value.map {
//            add
//        }
    }

    override val root =
            datagrid(listOf("string")) {
                cellCache {
                    vbox(10) {
                        alignment = Pos.CENTER
                        //label(it.book.title)
                        button()
                    }
            }
        }
    }