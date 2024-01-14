/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorVARenVNormController is part of MOP.
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
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import procesosEstocasticos.EstimadorVARenVNormalizadas;

import java.io.File;


public class EstimadorVARenVNormController extends GeneralidadesController{
    @FXML private GridPane gridPane;

    @FXML private JFXTextField inputNombrePESim;
    @FXML private JFXTextField inputNombrePEOptim;
    @FXML private JFXTextField inputNombrePEHist;
    @FXML private JFXTextField inputDirEntradas;
    @FXML private JFXTextField inputDirPVA;
    @FXML private JFXButton inputDirEntradasBtn;

    @FXML private JFXButton inputDirPVABtn;
    @FXML private JFXButton inputEstimar;
    @FXML private JFXCheckBox inputOpcionPVA;

    //Tooltips
    @FXML private JFXButton infoNombrePESim;
    @FXML private JFXButton infoNombrePEOptim;
    @FXML private JFXButton infoNombrePEHist;
    @FXML private JFXButton infoDirOrigenDatos;
    @FXML private JFXButton infoDirPVA;
    @FXML private JFXButton infoOpcionPVA;

    private Manejador parentController;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    public EstimadorVARenVNormController(Manejador parentController){
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
/*        inputDirResultadosBtn.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFile = directoryChooser.showDialog(inputDirResultadosBtn.getScene().getWindow());
            if(selectedFile != null){
                inputDirResultados.setText(selectedFile.getPath());
            }
        });*/
        inputDirPVABtn.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFile = directoryChooser.showDialog(inputDirPVABtn.getScene().getWindow());
            if(selectedFile != null){
                inputDirPVA.setText(selectedFile.getPath());
            }
        });


        inputOpcionPVA.setOnAction(actionEvent -> {
            for(Node node : gridPane.getChildren()){
                if((GridPane.getRowIndex(node) != null) && (GridPane.getRowIndex(node) == 10)){
                    node.setDisable(!inputOpcionPVA.isSelected());
                }
            }
        });

        inputEstimar.setOnAction(actionEvent ->{
            if(EstimadorVARenVNormalizadas.estimar(inputDirEntradas.getText(), inputDirPVA.getText(),
                    inputNombrePESim.getText(), inputNombrePEOptim.getText(), inputNombrePEHist.getText())){
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
//        String nombre = inputNombre.getText();
        String direntradas = inputDirEntradas.getText();


        // TODO: 27/07/2021 crear el data type
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
