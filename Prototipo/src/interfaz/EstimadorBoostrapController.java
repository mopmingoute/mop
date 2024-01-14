/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorBoostrapController is part of MOP.
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import procesosEstocasticos.EstimadorBootstrapDiscreto;

import java.io.File;


public class EstimadorBoostrapController extends GeneralidadesController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXTextField inputDirEntradas;

    @FXML private JFXButton inputDirEntradasBtn;
    @FXML private JFXButton inputEstimar;

    @FXML private JFXButton infoNombre;
    @FXML private JFXButton infoNombrePEHist;
    @FXML private JFXButton infoDirOrigenDatos;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    private Manejador parentController;

    public EstimadorBoostrapController(Manejador parentController){
        this.parentController = parentController;
    }


    @FXML
    public void initialize(){
        parentController.asignarTooltips(this);

        inputDirEntradasBtn.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFile = directoryChooser.showDialog(inputDirEntradasBtn.getScene().getWindow());
            if(selectedFile != null){
                inputDirEntradas.setText(selectedFile.getPath());
            }
        });

        inputEstimar.setOnAction(actionEvent ->{
            if(EstimadorBootstrapDiscreto.estimar(inputDirEntradas.getText(), inputNombre.getText())){
                setLabelMessageTemporal("Terminó la estimación del proceso correctamente.", TipoInfo.FEEDBACK);
            }else {
                setLabelMessageTemporal("No se pudo estimar el proceso", TipoInfo.FEEDBACK);
            }
        });

        inputAceptar.setOnAction(actionEvent -> ((Stage)inputAceptar.getScene().getWindow()).close());
        inputCancelar.setOnAction(actionEvent -> ((Stage)inputCancelar.getScene().getWindow()).close());

    }

    /**
     *  Método para cargar los datos en el DatosEolicoCorrida
     */
    private void loadData(){
        System.out.println("LOAD DATA-ESTIMADOR");
        String nombre = inputNombre.getText();
        String direntradas = inputDirEntradas.getText();


    }

    /**
     * Método para obtener los datos del DatosEolicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-EOLICO");
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}
