/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ReportesInputPanelController is part of MOP.
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

import com.jfoenix.controls.*;
import datatypes.DatosCorrida;
import datatypes.DatosEspecificacionReporte;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import presentacion.PresentacionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;


public class ReportesInputPanelController {

    @FXML private JFXButton btnActualizar;

    @FXML private JFXComboBox<Integer> anioInicioInput;
    @FXML private JFXComboBox<Integer> anioFinInput;
    @FXML private Label porPostesLabel;
    @FXML private JFXCheckBox porPostesInput;
    @FXML private Label porTecnologiaLabel;
    @FXML private JFXCheckBox porTecnologiaInput;
    @FXML private Label fuenteLabel;
    @FXML private JFXComboBox fuenteInput;
    @FXML private Label tipoSimplificacionLabel;
    @FXML private JFXRadioButton promedioInput;
    @FXML private JFXRadioButton percentilesInput;
    @FXML private JFXComboBox percentilInput;
    @FXML private JFXRadioButton porCronicaInput;
    @FXML private JFXComboBox cronicaInput;
    @FXML private Label criterioLabel;
    @FXML private JFXTextField criterioInput;

    private ReportesContentController reportesContentController;
    private PresentacionHandler ph = PresentacionHandler.getInstance();
    private Integer tipoReporte;

    public ReportesInputPanelController(ReportesContentController reportesContentController){
        this.reportesContentController = reportesContentController;
    }

    @FXML
    public void initialize(){
        btnActualizar.setOnAction(action -> update());

        percentilesInput.setOnAction(action -> {
            percentilInput.setDisable(false);
            cronicaInput.setDisable(true);
        });

        porCronicaInput.setOnAction(action -> {
           cronicaInput.setDisable(false);
           percentilInput.setDisable(true);
        });

        promedioInput.setOnAction(action -> {
            percentilInput.setDisable(true);
            cronicaInput.setDisable(true);
        });

        porTecnologiaInput.setOnAction(action -> {
            fuenteInput.setDisable(porTecnologiaInput.isSelected());
        });
    }

    private void update() {
        // get inputs
        Integer tipoReporte = reportesContentController.datosReporteGUI.getTipoReporte();
        Integer anioIni = anioInicioInput.getValue();
        Integer anioFin = anioFinInput.getValue();
        Boolean porTecnologia = porTecnologiaInput.isSelected();
        Integer simplificacion = promedioInput.isSelected() ? 0 : percentilesInput.isSelected() ? 1 : 2;
        Boolean porPoste = porPostesInput.isSelected();
//        Double criterio = Double.parseDouble(criterioInput.getText());// TODO: 17/09/2020 criterio
//        Integer tipoFuente = tipoReporte;// TODO: 15/09/2020 tipoFuente ??

        DatosEspecificacionReporte datosEspecificacionReporte = new DatosEspecificacionReporte("", tipoReporte, anioIni, anioFin,
        porTecnologia, simplificacion, porPoste, null, tipoReporte, null, null, null, null);
        reportesContentController.actualizar(ph.devolverReporte(datosEspecificacionReporte));
    }

    public void render(DatosCorrida datosCorrida){
        int anioIni = Integer.parseInt(datosCorrida.getInicioCorrida().split(" ")[2]);
        int anioFin = Integer.parseInt(datosCorrida.getFinCorrida().split(" ")[2]);
        ArrayList<Integer> rangoAnios = new ArrayList<>();
        for(int i=anioIni;i<=anioFin;i++){
            rangoAnios.add(i);
        }
        anioInicioInput.getItems().addAll(rangoAnios);
        anioInicioInput.getSelectionModel().selectFirst();
        anioFinInput.getItems().addAll(rangoAnios);
        anioFinInput.getSelectionModel().selectLast();

        if(reportesContentController.datosReporteGUI.getTipoReporte().equals(DatosEspecificacionReporte.REP_RES_ENER)){
            ArrayList<Node> nodesToHide = new ArrayList<>(Arrays.asList(porTecnologiaLabel, porTecnologiaInput, fuenteLabel, fuenteInput, porPostesLabel,
                    porPostesInput, tipoSimplificacionLabel, promedioInput, percentilesInput, percentilInput, porCronicaInput, cronicaInput, criterioLabel, criterioInput));
//            hideNodeList(nodesToHide);
//            GridPane.setRowIndex(btnActualizar, 4);
        }else if(reportesContentController.datosReporteGUI.getTipoReporte().equals(DatosEspecificacionReporte.REP_RES_COSTO)){

        }
    }

    private void hideNodeList(ArrayList<Node> nodes){
        for(Node node : nodes){
            node.setVisible(false);
        }
    }

}
