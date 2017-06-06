
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

/**
 * Created by kasra on 5/26/17.
 *
 * TODO_LIST
 * - TODO send exit command to shell when terminal receives Ctrl+D
 * - TODO have exit command close terminal as well
 */

fun main(args: Array<String>) {
  Application.launch(Kerm::class.java, *args)
}


class Kerm: App(TermView::class, KermStyle::class) {
  override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 800.0, 600.0)
}


class KermStyle : Stylesheet() {

  companion object {
    val borderpane by cssclass()
    val warning by cssclass()
  }

  init {
    warning {
      textFill = Color.RED
    }

    borderpane {
      minWidth = 800.px
      prefWidth = 800.px
    }

    listView {
      backgroundColor += Color.SLATEGREY
      fillWidth = true
    }

    listCell {
      backgroundColor += Color.SLATEGREY
    }

    titledPane {
      baseColor = Color.SLATEGREY
      //TODO find out how to color this one
    }
  }
}


class TermView : View() {
  val historyLog: HistoryLog by inject()

  override val root = borderpane {
    center = HistoryLogView().root
    bottom = textfield {
      font = Font.font("Monospaced")
      action {
        historyLog.handle(text)
        text = ""
      }
      Platform.runLater({
        requestFocus()
      })
    }

    shortcut("Esc") {
      Platform.exit()
    }
  }
}


class HistoryLogView : View() {
  val historyLog: HistoryLog by inject()

  override val root = listview(historyLog.history) {
    cellFragment(EntryFragment::class)
  }
}


class HistoryLog : Controller() {
  val shell = Shell()
  val history = FXCollections.observableArrayList<Entry>()

  init {
    history.add(Entry("Welcome", "Welcome to Kerm!", error = true))
  }

  fun handle(command: String) {
    val output = shell.execute(command)
    if (null == output) {
      handleError(command, shell.error)
    } else {
      handleCommand(command, output)
    }
  }

  private fun handleCommand(command: String, output: Pair<String, String>) {
    val entry = Entry(command, output.first +"\n"+ output.second, error = false)
    history.add(entry)
  }

  private fun handleError(command: String, output: String) {
    val entry = Entry(command, output, error = true)
    history.add(entry)
  }
}


class Entry(command: String, output: String, error: Boolean) {
  val commandProperty = SimpleStringProperty(command)
  val command by commandProperty

  val outputProperty = SimpleStringProperty(output)
  val output by outputProperty

  val errorProperty = SimpleBooleanProperty(error)
  val error by errorProperty
}


class EntryModel(property: ObjectProperty<Entry>) : ItemViewModel<Entry>(itemProperty = property) {
  val command: StringProperty  = bind(autocommit = true) { item?.commandProperty }
  val output : StringProperty  = bind(autocommit = true) { item?.outputProperty  }
  val error  : BooleanProperty = bind(autocommit = true) { item?.errorProperty   }
}


class EntryFragment : ListCellFragment<Entry>() {
  val entryModel: EntryModel = EntryModel(itemProperty)

  override val root = titledpane(entryModel.command) {
    label(entryModel.output) {
      toggleClass(KermStyle.warning, entryModel.error)
    }
  }
}



