/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CrearNuevaCorridaController is part of MOP.
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
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class CrearNuevaCorridaController extends GeneralidadesController{

    @FXML private JFXTextField inputNombreCorrida;
//    @FXML private JFXDatePicker inputFechaInicio;
//    @FXML private JFXDatePicker inputFechaFin;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    private Manejador manejador;

    public CrearNuevaCorridaController(Manejador manejador) {
        this.manejador = manejador;
    }

    @FXML
    public void initialize() {
        inputAceptar.setOnAction(actionEvent -> {
            if(inputNombreCorrida.getText().trim().equals("")) {
                setLabelMessageTemporal("Falta el nombre", TipoInfo.FEEDBACK);
            }else {
                ((Stage) inputAceptar.getScene().getWindow()).close();
                loadData();
                manejador.clearBlur();
            }
        });
        inputCancelar.setOnAction(actionEvent -> {
            ((Stage) inputCancelar.getScene().getWindow()).close();
            manejador.clearBlur();
        });
    }

    public void loadData() {
        String nombre = inputNombreCorrida.getText();
//        String fechaIni = inputFechaInicio.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00";
//        String fechaFin = inputFechaFin.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59";

        manejador.crearNuevaCorrida(nombre);       
    }


    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        //No aplica
    }
}
