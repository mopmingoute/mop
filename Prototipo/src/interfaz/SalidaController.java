/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * SalidaController is part of MOP.
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
import com.jfoenix.controls.JFXToggleButton;
import datatypes.*;
import datatypesSalida.DatosParamSalida;
import datatypesSalida.DatosParamSalidaOpt;
import datatypesSalida.DatosParamSalidaSim;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import presentacion.PresentacionHandler;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class SalidaController {

    //INPUTS DATA SALIDA SIM
    @FXML
    private JFXToggleButton inputSalidaSim;
    @FXML private JFXTextField inputPasoIni;
    @FXML private JFXTextField inputPasoFin;
    @FXML private JFXTextField inputEscenarioIni;
    @FXML private JFXTextField inputEscenarioFin;

    //INPUTS DATA SALIDA OPT
    @FXML private JFXToggleButton inputSalidaOpt;
    @FXML private JFXTextField inputPasoIniOpt;
    @FXML private JFXTextField inputPasoFinOpt;
    @FXML private JFXTextField inputSorteoIniOpt;
    @FXML private JFXTextField inputSorteoFinOpt;
    @FXML private JFXTextField inputEstadoIni_1;
    @FXML private JFXTextField inputEstadoIni_2;
    @FXML private JFXTextField inputEstadoIni_3;
    @FXML private JFXTextField inputEstadoIni_4;
    @FXML private JFXTextField inputEstadoFin_1;
    @FXML private JFXTextField inputEstadoFin_2;
    @FXML private JFXTextField inputEstadoFin_3;
    @FXML private JFXTextField inputEstadoFin_4;

    //INPUTS DATA PARAM SALIDA
    @FXML private JFXCheckBox inputEnerResumen;
    @FXML private JFXCheckBox inputEnerCron;
    @FXML private JFXCheckBox inputPotPoste;
    @FXML private JFXCheckBox inputCostoResumen;
    @FXML private JFXCheckBox inputCostoCron;
    @FXML private JFXCheckBox inputCostoPoste;
    @FXML private JFXCheckBox inputCosmarResumen;
    @FXML private JFXCheckBox inputCosmarCron;
    @FXML private JFXCheckBox inputCantMod;
    @FXML private JFXCheckBox inputSalidaDetalladaPaso;
    @FXML private JFXCheckBox inputCostoPasoCron;

    //INPUTS PARA PARTICIPANTES SALIDA
    @FXML private GridPane participantesSalidaGridPane;

    //INPUTS PARA ATRIBUTOS SALIDA
    @FXML private JFXCheckBox inputAtribEolicoPotencias;
    @FXML private JFXCheckBox inputAtribEolicoEnergias;
    @FXML private JFXCheckBox inputAtribEolicoCostos;

    @FXML private JFXCheckBox inputAtribSolarPotencias;
    @FXML private JFXCheckBox inputAtribSolarEnergias;
    @FXML private JFXCheckBox inputAtribSolarCostos;

    @FXML private JFXCheckBox inputAtribTermicoPotencias;
    @FXML private JFXCheckBox inputAtribTermicoEnergias;
    @FXML private JFXCheckBox inputAtribTermicoCostos;
    @FXML private JFXCheckBox inputAtribTermicoEnercom1;
    @FXML private JFXCheckBox inputAtribTermicoEnercom2;
    @FXML private JFXCheckBox inputAtribTermicoVolcom1;
    @FXML private JFXCheckBox inputAtribTermicoVolcom2;


    @FXML private JFXCheckBox inputAtribCicloCombinadoPotencias;
    @FXML private JFXCheckBox inputAtribCicloCombinadoCostos;
    @FXML private JFXCheckBox inputAtribCicloCombinadoEnergias;
    @FXML private JFXCheckBox inputAtribCicloCombinadoVolcom1;
    @FXML private JFXCheckBox inputAtribCicloCombinadoVolcom2;
    @FXML private JFXCheckBox inputAtribCicloCombinadoEnercom1;
    @FXML private JFXCheckBox inputAtribCicloCombinadoEnercom2;
    @FXML private JFXCheckBox inputAtribCicloCombinadoPotsTG;
    @FXML private JFXCheckBox inputAtribCicloCombinadoPotsCV;
    @FXML private JFXCheckBox inputAtribCicloCombinadoPotsAB;
    @FXML private JFXCheckBox inputAtribCicloCombinadoPotsCOMB;

    @FXML private JFXCheckBox inputAtribHidraulicoPotencias;
    @FXML private JFXCheckBox inputAtribHidraulicoEnergias;
    @FXML private JFXCheckBox inputAtribHidraulicoCostos;
    @FXML private JFXCheckBox inputAtribHidraulicoTurbinados;
    @FXML private JFXCheckBox inputAtribHidraulicoVertido;
    @FXML private JFXCheckBox inputAtribHidraulicoCotaAguasArriba;
    @FXML private JFXCheckBox inputAtribHidraulicoCoefEnergetico;
    @FXML private JFXCheckBox inputAtribHidraulicoTurbinadoPaso;
    @FXML private JFXCheckBox inputAtribHidraulicoVertidoPaso;
    @FXML private JFXCheckBox inputAtribHidraulicoAporte;
    @FXML private JFXCheckBox inputAtribHidraulicoValorDelAgua;

    @FXML private JFXCheckBox inputAtribHidraulicoCotaPenalizadaInf;
    @FXML private JFXCheckBox inputAtribHidraulicoCotaPenalizadaSup;
    @FXML private JFXCheckBox inputAtribHidraulicoVolumenPenalizadoInf;
    @FXML private JFXCheckBox inputAtribHidraulicoVolumenPenalizadoSup;
    @FXML private JFXCheckBox inputAtribHidraulicoCostosPenCotaInf;
    @FXML private JFXCheckBox inputAtribHidraulicoCostosPenCotaSup;

    @FXML private JFXCheckBox inputAtribCentralAcumulacionPotIny;
    @FXML private JFXCheckBox inputAtribCentralAcumulacionPotAlmac;
    @FXML private JFXCheckBox inputAtribCentralAcumulacionEnerIny;
    @FXML private JFXCheckBox inputAtribCentralAcumulacionEnerAlmac;
    @FXML private JFXCheckBox inputAtribCentralAcumulacionCostos;
    @FXML private JFXCheckBox inputAtribDemandaPotencias;
    @FXML private JFXCheckBox inputAtribDemandaEnergias;
    @FXML private JFXCheckBox inputAtribDemandaCostos;
    @FXML private JFXCheckBox inputAtribFallaPotencias;
    @FXML private JFXCheckBox inputAtribFallaEnergias;
    @FXML private JFXCheckBox inputAtribFallaCostos;
    @FXML private JFXCheckBox inputAtribImpoExpoPotencias;
    @FXML private JFXCheckBox inputAtribImpoExpoEnergias;
    @FXML private JFXCheckBox inputAtribImpoExpoCostos;
    @FXML private JFXCheckBox inputAtribProveedoresPotencias;
    @FXML private JFXCheckBox inputAtribProveedoresEnergias;
    @FXML private JFXCheckBox inputAtribProveedoresCostos;


    public DatosCorrida datosCorrida;
    public PresentacionHandler ph;
    public Manejador padre;

    public SalidaController(Manejador manejador, DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
        this.padre = manejador;
    }

    @FXML
    public void initialize() {


    }


    private void popularListaParticipantesSalida(){
        int cantParticipantes = 0;
        int participantesAntes;
        int participantesDespues;

        //EOLICOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosEolicoCorrida> entry : datosCorrida.getEolicos().getEolicos().entrySet()){
            JFXButton etiqueta = new JFXButton("E");
            etiqueta.getStyleClass().add("label_eolico");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));// el 50 es el margen del btn celeste en el caso de Hidraulico
            Label nombre = new Label(entry.getKey());
            nombre.setPrefHeight(40);
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));// el 50 es el margen del Nombre al lado del btn celeste
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesEolicos = participantesDespues - participantesAntes;
        if(cantParticipantesEolicos > 0){
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //SOLARES
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosFotovoltaicoCorrida> entry : datosCorrida.getFotovoltaicos().getFotovoltaicos().entrySet()){
            JFXButton etiqueta = new JFXButton("S");
            etiqueta.getStyleClass().add("label_solar");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesSolares = participantesDespues - participantesAntes;
        if(cantParticipantesSolares > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //TERMICOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosTermicoCorrida> entry : datosCorrida.getTermicos().getTermicos().entrySet()){
            JFXButton etiqueta = new JFXButton("T");
            etiqueta.getStyleClass().add("label_termico");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesTermicos = participantesDespues - participantesAntes;
        if(cantParticipantesTermicos > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }


        //CICLOS COMBINADOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosCicloCombinadoCorrida> entry : datosCorrida.getCcombinados().getCcombinados().entrySet()){
            JFXButton etiqueta = new JFXButton("CC");
            etiqueta.getStyleClass().add("label_ciclo_combinado");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesCiclosCombinados = participantesDespues - participantesAntes;
        if(cantParticipantesCiclosCombinados > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //HIDRAULICOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosHidraulicoCorrida> entry : datosCorrida.getHidraulicos().getHidraulicos().entrySet()){
            JFXButton etiqueta = new JFXButton("H");
            etiqueta.getStyleClass().add("label_hidraulico");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesHidraulicos = participantesDespues - participantesAntes;
        if(cantParticipantesHidraulicos > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //CENTRALES ACUMULACION
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosAcumuladorCorrida> entry : datosCorrida.getAcumuladores().getAcumuladores().entrySet()){
            JFXButton etiqueta = new JFXButton("A");
            etiqueta.getStyleClass().add("label_central_acumulacion");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesCentralAcumulacion = participantesDespues - participantesAntes;
        if(cantParticipantesCentralAcumulacion > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //DEMANDAS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosDemandaCorrida> entry : datosCorrida.getDemandas().getDemandas().entrySet()){
            JFXButton etiqueta = new JFXButton("D");
            etiqueta.getStyleClass().add("label_demanda");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesDemandas = participantesDespues - participantesAntes;
        if(cantParticipantesDemandas > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //FALLAS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosFallaEscalonadaCorrida> entry : datosCorrida.getFallas().getFallas().entrySet()){
            JFXButton etiqueta = new JFXButton("F");
            etiqueta.getStyleClass().add("label_falla");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesFallas = participantesDespues - participantesAntes;
        if(cantParticipantesFallas > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //IMPO/EXPOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosImpoExpoCorrida> entry : datosCorrida.getImpoExpos().getImpoExpos().entrySet()){
            JFXButton etiqueta = new JFXButton("IE");
            etiqueta.getStyleClass().add("label_impo_expo");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesImpoExpo = participantesDespues - participantesAntes;
        if(cantParticipantesImpoExpo > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //COMBUSTIBLE
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosCombustibleCorrida> entry : datosCorrida.getCombustibles().getCombustibles().entrySet()){
            JFXButton etiqueta = new JFXButton("C");
            etiqueta.getStyleClass().add("label_combustible");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesRedCombustible = participantesDespues - participantesAntes;
        if(cantParticipantesRedCombustible > 0) {
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //RED ELECTRICA

        //IMPACTOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosImpactoCorrida> entry : datosCorrida.getImpactos().getImpactos().entrySet()){
            JFXButton etiqueta = new JFXButton("I");
            etiqueta.getStyleClass().add("label_impacto");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setPrefHeight(40);
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesImpactos = participantesDespues - participantesAntes;
        if(cantParticipantesImpactos > 0){
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }

        //CONTRATOS
        participantesAntes = cantParticipantes;
        for(Map.Entry<String, DatosContratoEnergiaCorrida> entry : datosCorrida.getContratosEnergia().getContratosEnergia().entrySet()){
            JFXButton etiqueta = new JFXButton("CE");
            etiqueta.getStyleClass().add("label_contrato");
            GridPane.setMargin(etiqueta, new Insets(0,0,0,50));
            Label nombre = new Label(entry.getKey());
            nombre.setPrefHeight(40);
            nombre.setStyle("-fx-font-size: 15px; -fx-font-weight: BOLD;");
            GridPane.setMargin(nombre, new Insets(0,0,0,60));
            JFXCheckBox detalle = new JFXCheckBox();
            GridPane.setMargin(detalle, new Insets(0,0,0,20));
            detalle.setSelected(entry.getValue().isSalDetallada());
            detalle.setOnAction(actionEvent -> entry.getValue().setSalDetallada(detalle.isSelected()));
            participantesSalidaGridPane.add(etiqueta, 0, cantParticipantes+2);
            participantesSalidaGridPane.add(nombre, 1, cantParticipantes+2);
            participantesSalidaGridPane.add(detalle, 2, cantParticipantes+2);
            cantParticipantes++;
        }
        participantesDespues = cantParticipantes;
        int cantParticipantesContratos = participantesDespues - participantesAntes;
        if(cantParticipantesContratos > 0){
            Separator sep = new Separator();
            participantesSalidaGridPane.add(sep, 0, cantParticipantes+2, GridPane.REMAINING, 1);
            cantParticipantes++;
        }


        for(int i=0;i<cantParticipantes;i++){
            participantesSalidaGridPane.getRowConstraints().add(new RowConstraints(40));
        }
    }

    private void popularAtributosSalida(){
        //EOLICOS
        inputAtribEolicoPotencias.setSelected(datosCorrida.getEolicos().getAtributosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribEolicoEnergias.setSelected(datosCorrida.getEolicos().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribEolicoCostos.setSelected(datosCorrida.getEolicos().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribEolicoPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getEolicos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribEolicoEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getEolicos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribEolicoCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getEolicos().getAtributosDetallados());
        //SOLARES
        inputAtribSolarPotencias.setSelected(datosCorrida.getFotovoltaicos().getAtributosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribSolarEnergias.setSelected(datosCorrida.getFotovoltaicos().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribSolarCostos.setSelected(datosCorrida.getFotovoltaicos().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribSolarPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getFotovoltaicos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribSolarEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getFotovoltaicos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribSolarCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getFotovoltaicos().getAtributosDetallados());
        //TERMICOS
        inputAtribTermicoPotencias.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribTermicoEnergias.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribTermicoCostos.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COSTOS));

        inputAtribTermicoVolcom1.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VOLUMEN_COMBUSTIBLE1));
        inputAtribTermicoVolcom2.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VOLUMEN_COMBUSTIBLE2));
        inputAtribTermicoEnercom1.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_ENERGIA_COMBUSTIBLE1));
        inputAtribTermicoEnercom2.setSelected(datosCorrida.getTermicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_ENERGIA_COMBUSTIBLE2));

        funcionalidadAtributo(inputAtribTermicoPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getTermicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribTermicoEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getTermicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribTermicoCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getTermicos().getAtribtosDetallados());

        funcionalidadAtributo(inputAtribTermicoVolcom1, Text.ATRIBUTOS_VOLUMEN_COMBUSTIBLE1, datosCorrida.getTermicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribTermicoVolcom2, Text.ATRIBUTOS_VOLUMEN_COMBUSTIBLE2, datosCorrida.getTermicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribTermicoEnercom1, Text.ATRIBUTOS_ENERGIA_COMBUSTIBLE1, datosCorrida.getTermicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribTermicoEnercom2, Text.ATRIBUTOS_ENERGIA_COMBUSTIBLE2, datosCorrida.getTermicos().getAtribtosDetallados());

        //CICLOS COMBINADOS

        inputAtribCicloCombinadoPotencias.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribCicloCombinadoCostos.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribCicloCombinadoEnergias.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        inputAtribCicloCombinadoVolcom1.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.VOLCOM1));
        inputAtribCicloCombinadoVolcom2.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.VOLCOM2));
        inputAtribCicloCombinadoEnercom1.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.ENERCOM1));
        inputAtribCicloCombinadoEnercom2.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.ENERCOM2));
        inputAtribCicloCombinadoPotsTG.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.POTSTG));
        inputAtribCicloCombinadoPotsCV.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.POTSCV));
        inputAtribCicloCombinadoPotsAB.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.POTSAB));
        inputAtribCicloCombinadoPotsCOMB.setSelected(datosCorrida.getCcombinados().getAtribtosDetallados().contains(Constantes.POTSCOMB));


        funcionalidadAtributo(inputAtribCicloCombinadoPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoCostos, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoEnergias, Text.ATRIBUTOS_COSTOS, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoVolcom1, Constantes.VOLCOM1, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoVolcom2, Constantes.VOLCOM2, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoEnercom1, Constantes.ENERCOM1, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoEnercom2, Constantes.ENERCOM2, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoPotsTG, Constantes.POTSTG, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoPotsCV, Constantes.POTSCV, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoPotsAB, Constantes.POTSAB, datosCorrida.getCcombinados().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribCicloCombinadoPotsCOMB, Constantes.POTSCOMB, datosCorrida.getCcombinados().getAtribtosDetallados());

        //HIDRAULICOS
        inputAtribHidraulicoPotencias.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribHidraulicoEnergias.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribHidraulicoCostos.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        inputAtribHidraulicoTurbinados.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_TURBINADOS));
        inputAtribHidraulicoVertido.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VERTIDO));
        inputAtribHidraulicoCotaAguasArriba.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COTA_AGUAS_ARRIBA));
        inputAtribHidraulicoCoefEnergetico.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COEF_ENERGETICO));
        inputAtribHidraulicoTurbinadoPaso.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_TURBINADO_PASO));
        inputAtribHidraulicoVertidoPaso.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VERTIDO_PASO));
        inputAtribHidraulicoAporte.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_APORTE));
        inputAtribHidraulicoValorDelAgua.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VALLOR_AGUA));

        inputAtribHidraulicoCotaPenalizadaInf.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COTA_PENALIZADA_INFERIOR));
        inputAtribHidraulicoCotaPenalizadaSup.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COTA_PENALIZADA_SUPERIOR));
        inputAtribHidraulicoVolumenPenalizadoInf.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VOLUMEN_PENALIZADO_INFERIOR));
        inputAtribHidraulicoVolumenPenalizadoSup.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_VOLUMEN_PENALIZADO_SUPERIOR));
        inputAtribHidraulicoCostosPenCotaInf.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COSTOS_PEN_COTA_INF));
        inputAtribHidraulicoCostosPenCotaSup.setSelected(datosCorrida.getHidraulicos().getAtribtosDetallados().contains(Text.ATRIBUTOS_COSTOS_PEN_COTA_SUP));



        funcionalidadAtributo(inputAtribHidraulicoPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoTurbinados, Text.ATRIBUTOS_TURBINADOS, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoVertido, Text.ATRIBUTOS_VERTIDO, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCotaAguasArriba, Text.ATRIBUTOS_COTA_AGUAS_ARRIBA, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCoefEnergetico, Text.ATRIBUTOS_COEF_ENERGETICO, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoTurbinadoPaso, Text.ATRIBUTOS_TURBINADO_PASO, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoVertidoPaso, Text.ATRIBUTOS_VERTIDO_PASO, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoAporte, Text.ATRIBUTOS_APORTE, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoValorDelAgua, Text.ATRIBUTOS_VALLOR_AGUA, datosCorrida.getHidraulicos().getAtribtosDetallados());

        funcionalidadAtributo(inputAtribHidraulicoCotaPenalizadaInf, Text.ATRIBUTOS_COTA_PENALIZADA_INFERIOR, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCotaPenalizadaSup, Text.ATRIBUTOS_COTA_PENALIZADA_SUPERIOR, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoVolumenPenalizadoInf, Text.ATRIBUTOS_VOLUMEN_PENALIZADO_INFERIOR, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoVolumenPenalizadoSup, Text.ATRIBUTOS_VOLUMEN_PENALIZADO_SUPERIOR, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCostosPenCotaInf, Text.ATRIBUTOS_COSTOS_PEN_COTA_INF, datosCorrida.getHidraulicos().getAtribtosDetallados());
        funcionalidadAtributo(inputAtribHidraulicoCostosPenCotaSup, Text.ATRIBUTOS_COSTOS_PEN_COTA_SUP, datosCorrida.getHidraulicos().getAtribtosDetallados());


        //CENTRALES ACUMULACION
        inputAtribCentralAcumulacionPotIny.setSelected(datosCorrida.getAcumuladores().getAtributosDetallados().contains(Text.ATRIBUTOS_POT_INYEC));
        inputAtribCentralAcumulacionPotAlmac.setSelected(datosCorrida.getAcumuladores().getAtributosDetallados().contains(Text.ATRIBUTOS_POT_ALMAC));
        inputAtribCentralAcumulacionEnerIny.setSelected(datosCorrida.getAcumuladores().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERG_INYEC));
        inputAtribCentralAcumulacionEnerAlmac.setSelected(datosCorrida.getAcumuladores().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERG_ALMAC));
        inputAtribCentralAcumulacionCostos.setSelected(datosCorrida.getAcumuladores().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribCentralAcumulacionPotIny, Text.ATRIBUTOS_POT_INYEC, datosCorrida.getAcumuladores().getAtributosDetallados());
        funcionalidadAtributo(inputAtribCentralAcumulacionPotAlmac, Text.ATRIBUTOS_POT_ALMAC, datosCorrida.getAcumuladores().getAtributosDetallados());
        funcionalidadAtributo(inputAtribCentralAcumulacionEnerIny, Text.ATRIBUTOS_ENERG_INYEC, datosCorrida.getAcumuladores().getAtributosDetallados());
        funcionalidadAtributo(inputAtribCentralAcumulacionEnerAlmac, Text.ATRIBUTOS_ENERG_ALMAC, datosCorrida.getAcumuladores().getAtributosDetallados());
        funcionalidadAtributo(inputAtribCentralAcumulacionCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getAcumuladores().getAtributosDetallados());
        //DEMANDAS
        inputAtribDemandaPotencias.setSelected(datosCorrida.getDemandas().getAtributosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribDemandaEnergias.setSelected(datosCorrida.getDemandas().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribDemandaCostos.setSelected(datosCorrida.getDemandas().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribDemandaPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getDemandas().getAtributosDetallados());
        funcionalidadAtributo(inputAtribDemandaEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getDemandas().getAtributosDetallados());
        funcionalidadAtributo(inputAtribDemandaCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getDemandas().getAtributosDetallados());
        //FALLAS
        inputAtribFallaPotencias.setSelected(datosCorrida.getFallas().getAtributosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribFallaEnergias.setSelected(datosCorrida.getFallas().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribFallaCostos.setSelected(datosCorrida.getFallas().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribFallaPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getFallas().getAtributosDetallados());
        funcionalidadAtributo(inputAtribFallaEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getFallas().getAtributosDetallados());
        funcionalidadAtributo(inputAtribFallaCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getFallas().getAtributosDetallados());
        //IMPO/EXPO
        inputAtribImpoExpoPotencias.setSelected(datosCorrida.getImpoExpos().getAtributosDetallados().contains(Text.ATRIBUTOS_POTENCIAS));
        inputAtribImpoExpoEnergias.setSelected(datosCorrida.getImpoExpos().getAtributosDetallados().contains(Text.ATRIBUTOS_ENERGIAS));
        inputAtribImpoExpoCostos.setSelected(datosCorrida.getImpoExpos().getAtributosDetallados().contains(Text.ATRIBUTOS_COSTOS));
        funcionalidadAtributo(inputAtribImpoExpoPotencias, Text.ATRIBUTOS_POTENCIAS, datosCorrida.getImpoExpos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribImpoExpoEnergias, Text.ATRIBUTOS_ENERGIAS, datosCorrida.getImpoExpos().getAtributosDetallados());
        funcionalidadAtributo(inputAtribImpoExpoCostos, Text.ATRIBUTOS_COSTOS, datosCorrida.getImpoExpos().getAtributosDetallados());
     
    }

    private void funcionalidadAtributo(JFXCheckBox checkbox, String atributo, ArrayList<String> listaAtributos){
        checkbox.setOnAction(actionEvent -> {
            if(checkbox.isSelected()){
                if(!listaAtributos.contains(atributo)){
                    listaAtributos.add(atributo);
                }
            }else{
                listaAtributos.remove(atributo);
            }
        });
    }

    private void toggleAtributos(boolean toggle){
        //EOLICOS
        inputAtribEolicoPotencias.setDisable(toggle);
        inputAtribEolicoEnergias.setDisable(toggle);
        inputAtribEolicoCostos.setDisable(toggle);
        //SOLARES
        inputAtribSolarPotencias.setDisable(toggle);
        inputAtribSolarEnergias.setDisable(toggle);
        inputAtribSolarCostos.setDisable(toggle);
        //TERMICOS
        inputAtribTermicoPotencias.setDisable(toggle);
        inputAtribTermicoEnergias.setDisable(toggle);
        inputAtribTermicoCostos.setDisable(toggle);
        inputAtribTermicoVolcom1.setDisable(toggle);
        inputAtribTermicoVolcom2.setDisable(toggle);
        inputAtribTermicoEnercom1.setDisable(toggle);
        inputAtribTermicoEnercom2.setDisable(toggle);
        //HIDRAULICOS
        inputAtribHidraulicoPotencias.setDisable(toggle);
        inputAtribHidraulicoEnergias.setDisable(toggle);
        inputAtribHidraulicoCostos.setDisable(toggle);
        inputAtribHidraulicoTurbinados.setDisable(toggle);
        inputAtribHidraulicoVertido.setDisable(toggle);
        inputAtribHidraulicoCotaAguasArriba.setDisable(toggle);
        inputAtribHidraulicoCoefEnergetico.setDisable(toggle);
        inputAtribHidraulicoTurbinadoPaso.setDisable(toggle);
        inputAtribHidraulicoVertidoPaso.setDisable(toggle);
        inputAtribHidraulicoAporte.setDisable(toggle);
        inputAtribHidraulicoValorDelAgua.setDisable(toggle);
        inputAtribHidraulicoCotaPenalizadaInf.setDisable(toggle);
        inputAtribHidraulicoCotaPenalizadaSup.setDisable(toggle);
        inputAtribHidraulicoVolumenPenalizadoInf.setDisable(toggle);
        inputAtribHidraulicoVolumenPenalizadoSup.setDisable(toggle);
        inputAtribHidraulicoCostosPenCotaInf.setDisable(toggle);
        inputAtribHidraulicoCostosPenCotaSup.setDisable(toggle);


        //CENTRALES ACUMULACION
        inputAtribCentralAcumulacionPotIny.setDisable(toggle);
        inputAtribCentralAcumulacionPotAlmac.setDisable(toggle);
        inputAtribCentralAcumulacionEnerIny.setDisable(toggle);
        inputAtribCentralAcumulacionEnerAlmac.setDisable(toggle);
        inputAtribCentralAcumulacionCostos.setDisable(toggle);
        //DEMANDAS
        inputAtribDemandaPotencias.setDisable(toggle);
        inputAtribDemandaEnergias.setDisable(toggle);
        inputAtribDemandaCostos.setDisable(toggle);
        //FALLAS
        inputAtribFallaPotencias.setDisable(toggle);
        inputAtribFallaEnergias.setDisable(toggle);
        inputAtribFallaCostos.setDisable(toggle);
        //IMPO/EXPO
        inputAtribImpoExpoPotencias.setDisable(toggle);
        inputAtribImpoExpoEnergias.setDisable(toggle);
        inputAtribImpoExpoCostos.setDisable(toggle);
        //PROVEEDORES
        inputAtribProveedoresPotencias.setDisable(toggle);
        inputAtribProveedoresEnergias.setDisable(toggle);
        inputAtribProveedoresCostos.setDisable(toggle);
    }

    private void popularParametrosSalida(){
        DatosParamSalidaSim datosParamSalidaSim = datosCorrida.getDatosParamSalidaSim();
        inputSalidaSim.setSelected(datosParamSalidaSim.isSalSim());
        inputPasoIni.setText(String.valueOf(datosParamSalidaSim.getPasoIni()));
        inputPasoFin.setText(String.valueOf(datosParamSalidaSim.getPasoFin()));
        inputEscenarioIni.setText(String.valueOf(datosParamSalidaSim.getEscIni()));
        inputEscenarioFin.setText(String.valueOf(datosParamSalidaSim.getEscFin()));

        DatosParamSalidaOpt datosParamSalidaOpt = datosCorrida.getDatosParamSalidaOpt();
        inputSalidaOpt.setSelected(datosParamSalidaOpt.isSalOpt());
        inputPasoIniOpt.setText(String.valueOf(datosParamSalidaOpt.getPasoIni()));
        inputPasoFinOpt.setText(String.valueOf(datosParamSalidaOpt.getPasoFin()));
        inputSorteoIniOpt.setText(String.valueOf(datosParamSalidaOpt.getSortIni()));
        inputSorteoFinOpt.setText(String.valueOf(datosParamSalidaOpt.getSortFin()));
        inputEstadoIni_1.setText(String.valueOf(datosParamSalidaOpt.getEstadoIni()[0]));
        inputEstadoIni_2.setText(String.valueOf(datosParamSalidaOpt.getEstadoIni()[1]));
        inputEstadoIni_3.setText(String.valueOf(datosParamSalidaOpt.getEstadoIni()[2]));
        if(datosParamSalidaOpt.getEstadoIni().length > 3){  inputEstadoIni_4.setText(String.valueOf(datosParamSalidaOpt.getEstadoIni()[3])); }

        inputEstadoFin_1.setText(String.valueOf(datosParamSalidaOpt.getEstadoFin()[0]));
        inputEstadoFin_2.setText(String.valueOf(datosParamSalidaOpt.getEstadoFin()[1]));
        inputEstadoFin_3.setText(String.valueOf(datosParamSalidaOpt.getEstadoFin()[2]));
        if(datosParamSalidaOpt.getEstadoFin().length > 3){ inputEstadoFin_4.setText(String.valueOf(datosParamSalidaOpt.getEstadoFin()[3])); }

        DatosParamSalida datosParamSalida = datosCorrida.getDatosParamSalida();
        inputEnerResumen.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_RESUMEN][0] > 0);
        inputEnerCron.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_ENERCRON][0] > 0);
        inputPotPoste.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_POT][0] > 0);
        inputCostoResumen.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSTO_RESUMEN][0] > 0);
        inputCostoCron.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSTO_CRON][0] > 0);
        inputCostoPoste.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSTO_POSTE][0] > 0);
        inputCosmarResumen.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSMAR_RESUMEN][0] > 0);
        inputCosmarCron.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSMAR_CRON][0] > 0);
        inputCantMod.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_CANTMOD][0] > 0);
        inputSalidaDetalladaPaso.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_SALIDA_DET_PASO][0] > 0);
        inputCostoPasoCron.setSelected(datosParamSalida.getParam()[Constantes.PARAMSAL_COSTO_PASO_CRON][0] > 0);

    }

    public void unloadData() {
        popularAtributosSalida();
        popularParametrosSalida();
        popularListaParticipantesSalida();

    }

    public void loadData() {
        //DATOS PARAM SALIDA SIM
        DatosParamSalidaSim datosParamSalidaSim = new DatosParamSalidaSim();
        datosParamSalidaSim.setSalSim(inputSalidaSim.isSelected());
        datosParamSalidaSim.setPasoIni(Integer.parseInt(inputPasoIni.getText()));
        datosParamSalidaSim.setPasoFin(Integer.parseInt(inputPasoFin.getText()));
        datosParamSalidaSim.setEscIni(Integer.parseInt(inputEscenarioIni.getText()));
        datosParamSalidaSim.setEscFin(Integer.parseInt(inputEscenarioFin.getText()));
        datosCorrida.setDatosParamSalidaSim(datosParamSalidaSim);

        //DATOS PARAM SALIDA OPT
        DatosParamSalidaOpt datosParamSalidaOpt = new DatosParamSalidaOpt();
        datosParamSalidaOpt.setSalOpt(inputSalidaOpt.isSelected());
        datosParamSalidaOpt.setPasoIni(Integer.parseInt(inputPasoIniOpt.getText()));
        datosParamSalidaOpt.setPasoFin(Integer.parseInt(inputPasoFinOpt.getText()));
        datosParamSalidaOpt.setSortIni(Integer.parseInt(inputSorteoIniOpt.getText()));
        datosParamSalidaOpt.setSortFin(Integer.parseInt(inputSorteoFinOpt.getText()));
        int[] estadoIni = new int[4];
        estadoIni[0] = Integer.parseInt(inputEstadoIni_1.getText());
        estadoIni[1] = Integer.parseInt(inputEstadoIni_2.getText());
        estadoIni[2] = Integer.parseInt(inputEstadoIni_3.getText());
        if(UtilStrings.esNumeroEntero(inputEstadoIni_4.getText())) { estadoIni[3] = Integer.parseInt(inputEstadoIni_4.getText()); }
        datosParamSalidaOpt.setEstadoIni(estadoIni);
        int[] estadoFin = new int[4];
        estadoFin[0] = Integer.parseInt(inputEstadoFin_1.getText());
        estadoFin[1] = Integer.parseInt(inputEstadoFin_2.getText());
        estadoFin[2] = Integer.parseInt(inputEstadoFin_3.getText());
        if(UtilStrings.esNumeroEntero(inputEstadoFin_4.getText())) { estadoFin[3] = Integer.parseInt(inputEstadoFin_4.getText()); }
        datosParamSalidaOpt.setEstadoFin(estadoFin);
        datosCorrida.setDatosParamSalidaOpt(datosParamSalidaOpt);

        //DATOS PARAM SALIDA
        DatosParamSalida datosParamSalida = new DatosParamSalida();
        int[][] param = new int[Constantes.PARAMSAL_CANT_PARAM][];
        int[] vEnerResumen = { inputEnerResumen.isSelected() ? 1 : 0 };
        int[] vecEnercron = { inputEnerCron.isSelected() ? 1 : 0 };
        int[] vpotPoste = { inputPotPoste.isSelected() ? 1 : 0 };
//		int[] vindPot = { inputIndPot.isSelected() ? 1 : 0 };
        int[] vcostoResumen = { inputCostoResumen.isSelected() ? 1 : 0 };
        int[] vcostocron = { inputCostoCron.isSelected() ? 1 : 0 };
        int[] vcostoPoste = { inputCostoPoste.isSelected() ? 1 : 0 };
        int[] vcosmarResumen = { inputCosmarResumen.isSelected() ? 1 : 0 };
        int[] vcosmarCron = { inputCosmarCron.isSelected() ? 1 : 0 };
        int[] vcantMod = { inputCantMod.isSelected() ? 1 : 0 };
        int[] vsalidaDetalladaPaso = { inputSalidaDetalladaPaso.isSelected() ? 1 : 0 };
        int[] vcostoPasoCron = { inputCostoPasoCron.isSelected() ? 1 : 0 };

        param[Constantes.PARAMSAL_RESUMEN] = vEnerResumen;
        param[Constantes.PARAMSAL_ENERCRON] = vecEnercron;
        param[Constantes.PARAMSAL_POT] = vpotPoste;
//		param[Constantes.PARAMSAL_IND_POT] = vindPot;
        param[Constantes.PARAMSAL_COSTO_RESUMEN] = vcostoResumen;
        param[Constantes.PARAMSAL_COSTO_CRON] = vcostocron;
        param[Constantes.PARAMSAL_COSTO_POSTE] = vcostoPoste;
//		param[Constantes.PARAMSAL_IND_COSTO_POSTE] = vindcostoPoste;
        param[Constantes.PARAMSAL_COSMAR_RESUMEN] = vcosmarResumen;
        param[Constantes.PARAMSAL_COSMAR_CRON] = vcosmarCron;
//		param[Constantes.PARAMSAL_IND_COSMAR_CRON] = vindCosmarCron;
        param[Constantes.PARAMSAL_CANTMOD] = vcantMod;
//		param[Constantes.PARAMSAL_IND_ATR_DET] = vindAtriDetallado;
        param[Constantes.PARAMSAL_SALIDA_DET_PASO] = vsalidaDetalladaPaso;
        param[Constantes.PARAMSAL_COSTO_PASO_CRON] = vcostoPasoCron;

        datosParamSalida.setParam(param);
        datosCorrida.setDatosParamSalida(datosParamSalida);


    }

    public void clean(){
        participantesSalidaGridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 1);
        int amountToRemove = participantesSalidaGridPane.getRowConstraints().size();
        for(int i=2;i<amountToRemove;i++){
            participantesSalidaGridPane.getRowConstraints().remove(participantesSalidaGridPane.getRowConstraints().size()-1);
        }

        //INPUTS DATA SALIDA SIM
        inputSalidaSim.setSelected(false);
        inputPasoIni.clear();
        inputPasoFin.clear();
        inputEscenarioIni.clear();
        inputEscenarioFin.clear();

        //INPUTS DATA SALIDA OPT
        inputSalidaOpt.setSelected(false);
        inputPasoIniOpt.clear();
        inputPasoFinOpt.clear();
        inputSorteoIniOpt.clear();
        inputSorteoFinOpt.clear();
        inputEstadoIni_1.clear();
        inputEstadoIni_2.clear();
        inputEstadoIni_3.clear();
        inputEstadoIni_4.clear();
        inputEstadoFin_1.clear();
        inputEstadoFin_2.clear();
        inputEstadoFin_3.clear();
        inputEstadoFin_4.clear();

        //INPUTS DATA PARAM SALIDA
        inputEnerResumen.setSelected(false);
        inputEnerCron.setSelected(false);
        inputPotPoste.setSelected(false);
        inputCostoResumen.setSelected(false);
        inputCostoCron.setSelected(false);
        inputCostoPoste.setSelected(false);
        inputCosmarResumen.setSelected(false);
        inputCosmarCron.setSelected(false);
        inputCantMod.setSelected(false);
        inputSalidaDetalladaPaso.setSelected(false);
        inputCostoPasoCron.setSelected(false);

        //INPUTS PARA ATRIBUTOS SALIDA
        inputAtribEolicoPotencias.setSelected(false);
        inputAtribEolicoEnergias.setSelected(false);
        inputAtribEolicoCostos.setSelected(false);
        inputAtribSolarPotencias.setSelected(false);
        inputAtribSolarEnergias.setSelected(false);
        inputAtribSolarCostos.setSelected(false);
        inputAtribTermicoPotencias.setSelected(false);
        inputAtribTermicoEnergias.setSelected(false);
        inputAtribTermicoCostos.setSelected(false);
        inputAtribTermicoVolcom1.setSelected(false);
        inputAtribTermicoVolcom2.setSelected(false);
        inputAtribTermicoEnercom1.setSelected(false);
        inputAtribTermicoEnercom2.setSelected(false);


        inputAtribHidraulicoPotencias.setSelected(false);
        inputAtribHidraulicoEnergias.setSelected(false);
        inputAtribHidraulicoCostos.setSelected(false);
        inputAtribHidraulicoTurbinados.setSelected(false);
        inputAtribHidraulicoVertido.setSelected(false);
        inputAtribHidraulicoCotaAguasArriba.setSelected(false);
        inputAtribHidraulicoCoefEnergetico.setSelected(false);
        inputAtribHidraulicoTurbinadoPaso.setSelected(false);
        inputAtribHidraulicoVertidoPaso.setSelected(false);
        inputAtribHidraulicoAporte.setSelected(false);
        inputAtribHidraulicoValorDelAgua.setSelected(false);
        inputAtribHidraulicoCotaPenalizadaInf.setSelected(false);
        inputAtribHidraulicoCotaPenalizadaSup.setSelected(false);


        inputAtribCentralAcumulacionPotIny.setSelected(false);
        inputAtribCentralAcumulacionPotAlmac.setSelected(false);
        inputAtribCentralAcumulacionEnerIny.setSelected(false);
        inputAtribCentralAcumulacionEnerAlmac.setSelected(false);
        inputAtribCentralAcumulacionCostos.setSelected(false);
        inputAtribDemandaPotencias.setSelected(false);
        inputAtribDemandaEnergias.setSelected(false);
        inputAtribDemandaCostos.setSelected(false);
        inputAtribFallaPotencias.setSelected(false);
        inputAtribFallaEnergias.setSelected(false);
        inputAtribFallaCostos.setSelected(false);
        inputAtribImpoExpoPotencias.setSelected(false);
        inputAtribImpoExpoEnergias.setSelected(false);
        inputAtribImpoExpoCostos.setSelected(false);
        inputAtribProveedoresPotencias.setSelected(false);
        inputAtribProveedoresEnergias.setSelected(false);
        inputAtribProveedoresCostos.setSelected(false);
    }

}
