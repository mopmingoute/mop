/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MensajeConfirmacionController is part of MOP.
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
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logica.CorridaHandler;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilStrings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MensajeConfirmacionController {


    @FXML private Label mensajeLabel;
    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    @FXML private Hyperlink erroresHyperlink;
    @FXML private GridPane gridPane;

    String mensaje;
    int codigoMensaje;
    ArrayList<String> listaErrores;


    private GeneralidadesController padre;

    public MensajeConfirmacionController(GeneralidadesController pPadre, String pMensaje, int codigo, ArrayList<String> errores)  {

        padre = pPadre;
        mensaje = pMensaje;
        codigoMensaje = codigo;
        listaErrores = errores;

    }

    public void soloBtnAceptar(){
        inputCancelar.setVisible(false);
    }

    @FXML
    public void initialize() {
        mensajeLabel.setText(mensaje);
        inputAceptar.setOnAction(actionEvent -> {
            if(padre!= null) {
            ((Stage) inputAceptar.getScene().getWindow()).close();
            padre.confirmoMensaje(true, codigoMensaje);}



        });
        inputCancelar.setOnAction(actionEvent -> {
            padre.confirmoMensaje(false, codigoMensaje);
            ((Stage) inputCancelar.getScene().getWindow()).close();

        });

        if(listaErrores != null) {
            erroresHyperlink = new Hyperlink("Ver errores");
            erroresHyperlink.setAlignment(Pos.CENTER_RIGHT);
            erroresHyperlink.setMaxWidth(150);
            erroresHyperlink.setOnAction(e -> {

                if (!Desktop.isDesktopSupported())//check if Desktop is supported by Platform or not
                {
                    System.out.println("not supported");
                    return;
                }
                String direc = CorridaHandler.getInstance().getCorridaActual().getRutaSals() + "/listaErrores.txt";
                File file = new File(direc);
                String listadoErrores = UtilStrings.arrayStringAtexto(listaErrores, "\n");
                DirectoriosYArchivos.grabaTexto(direc, listadoErrores);
                Desktop desktop = Desktop.getDesktop();

                if (file.exists())         //checks file exists or not
                {
                    try {
                        desktop.open(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            gridPane.add(erroresHyperlink, 0, 3);
        }


    }






}
