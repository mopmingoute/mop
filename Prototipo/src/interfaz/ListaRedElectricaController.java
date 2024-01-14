/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaRedElectricaController is part of MOP.
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
import datatypes.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.util.*;


public class ListaRedElectricaController {



    private DatosCorrida datosCorrida;
    private DatosRedElectricaCorrida datosRedElectricaCorrida;
    private Hashtable<String, DatosRedElectricaCorrida> listaDatosRedElectricaCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
    private Manejador parentController;
    private String redABorrar;
    private int indRedABorrar;

    public ListaRedElectricaController(DatosCorrida datosCorrida, Manejador parentController) {
        this.datosCorrida = datosCorrida;
        this.datosRedElectricaCorrida = datosCorrida.getRed();
        this.parentController = parentController;

    }

    @FXML
    public void initialize(){

    }

    public void loadData() {

    }
}
