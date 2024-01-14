/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MostrarMensajeController is part of MOP.
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
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MostrarMensajeController {

    @FXML private JFXButton inputAceptar;
    @FXML private Label mensaje;
    @FXML private GridPane gridPane;

    String  msje;
    Manejador padre;
    int codigoMensaje;

    public MostrarMensajeController(String msje, Manejador padre, int codigoMensaje) {
        this.msje= msje;
        this.padre = padre;
        this.codigoMensaje = codigoMensaje;
    }
    public MostrarMensajeController(String msje) {
        this.msje= msje;
    }
    @FXML
    public void initialize() {
        inputAceptar.setOnAction(actionEvent -> {
            ((Stage) inputAceptar.getScene().getWindow()).close();
            if(padre != null){
                padre.aceptoMensaje(codigoMensaje);
                padre.getMainPane().setEffect(null);
            }
            loadData();

        });

        mensaje.setText(msje);
    }

    public void loadData() {

    }

    public void agregarHiperlink(String texto, File file){

        Hyperlink erroresHyperlink = new Hyperlink("Ver errores");
        erroresHyperlink.setAlignment(Pos.CENTER_RIGHT);
        erroresHyperlink.setMaxWidth(150);

        gridPane.add(erroresHyperlink, 0, 3);
        erroresHyperlink.setOnAction(e -> {
            if(!Desktop.isDesktopSupported())//check if Desktop is supported by Platform or not
            {
                System.out.println("not supported");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if(file.exists())         //checks file exists or not
            {
                try {
                    desktop.open(file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


    }

}
