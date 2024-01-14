/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ElegirAniosController is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package interfaz;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logica.CorridaHandler;
import org.controlsfx.control.CheckListView;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilStrings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ElegirAniosController {



    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    @FXML private JFXButton  inputSeleccionarTodos;
    @FXML private GridPane gridPane;

    @FXML private VBox vboxAnios;

    private CheckListView<String> checkListView;

    ArrayList<String> anios;


    private LineaDeTiempoSimpleController padre;

    public ElegirAniosController(LineaDeTiempoSimpleController pPadre, ArrayList<String> anios)  {

        this.padre = pPadre;
        this.anios = anios;

    }


    @FXML
    public void initialize() {
        inputAceptar.setOnAction(actionEvent -> {
            if(padre!= null) {
            ((Stage) inputAceptar.getScene().getWindow()).close();
            ObservableList<String> selectedItems = checkListView.getCheckModel().getCheckedItems();
            padre.aplicarDisenio(new ArrayList<>(selectedItems));
            }


        });
        inputCancelar.setOnAction(actionEvent -> {
            ((Stage) inputCancelar.getScene().getWindow()).close();

        });

        inputSeleccionarTodos.setOnAction(actionEvent -> {
            checkListView.getCheckModel().checkAll();

        });

        ObservableList<String> years = FXCollections.observableArrayList(anios);
        checkListView = new CheckListView<>(years);
        vboxAnios.getChildren().add(checkListView);
        VBox.setVgrow(checkListView, Priority.ALWAYS);





    }

}
