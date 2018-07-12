package view

import tornadofx.*;

class RecordView1: Fragment() {
    override val root = hbox {
        label("1");
    }
}

class RecordView2: View() {
    override val root = hbox {
        label("2");
    }
}

class RecordView3: View() {
    override val root = hbox {
        label("3");
    }
}
