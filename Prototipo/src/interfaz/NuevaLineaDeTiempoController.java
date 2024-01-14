/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * NuevaLineaDeTiempoController is part of MOP.
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
import datatypesTiempo.DatosLineaTiempo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import logica.CorridaHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;


public class NuevaLineaDeTiempoController {

    @FXML
    private GridPane gridPaneAnios;
    @FXML
    private GridPane gridPaneMeses;
    @FXML
    private JFXDatePicker inputFechaInicio;
    @FXML
    private JFXDatePicker inputFechaFin;
    @FXML
    private JFXComboBox<String> inputPasoBase;
    @FXML
    private JFXTextField inputCantPostes;
    @FXML
    private Label anioLabel;

    //DETALLE-ANIO
    @FXML
    private GridPane gridPaneDetalleAnio;
    @FXML
    private Label anioDetalleLabel;
    @FXML
    private JFXComboBox<String> anioDetallePasoInput;
//    TODO: Repetir parece no ser de utilizad, reflotar en un futuro?
//    @FXML
//    private JFXCheckBox anioDetalleRepetirInput;
//    @FXML
//    private JFXTextField anioDetalleCadaXInput;
//    @FXML
//    private JFXComboBox<String> anioDetalleHastaInput;

    private HashMap<Integer, DetalleLTData> detalleLTDataAnios = new HashMap<>();

    //DETALLE-MES
    @FXML
    private GridPane gridPaneDetalleMes;
    @FXML
    private Label mesDetalleLabel;
    @FXML
    private JFXComboBox<String> mesDetallePasoInput;
//    TODO: Repetir parece no ser de utilizad, reflotar en un futuro?
//    @FXML
//    private JFXCheckBox mesDetalleRepetirInput;
//    @FXML
//    private JFXTextField mesDetalleCadaXInput;
//    @FXML
//    private JFXComboBox<String> mesDetalleHastaInput;


    @FXML
    private JFXButton btnDetallarBloques;

    private Manejador parentController;
    private DatosCorrida datosCorrida;
    private Integer selectedAnio;
    private Integer selectedMes;
    private String selectedPaso;
    private boolean reRenderMeses = true;
    private boolean cargaInicial = true;


    public NuevaLineaDeTiempoController(Manejador parentController, DatosCorrida datosCorrida) {
        this.parentController = parentController;
        this.datosCorrida = datosCorrida;
    }

    @FXML
    public void initialize() {

        btnDetallarBloques.setOnAction(action -> {
            loadData();
            parentController.setNuevaLineaDeTiempoActiva(false);
            //GeneradorDatosLineaDeTiempo.parsearCadenaBloques(2021,3021,Text.PASO_SEMANAL); //TODO: prueba, eliminar
        });

        inputPasoBase.getItems().addAll(Text.PASOS_BASE);
        inputPasoBase.getSelectionModel().select(Text.PASO_SEMANAL);
        inputPasoBase.setOnAction(action -> inputPasoBaseOnAction());

        inputPasoBase.fireEvent(new ActionEvent());

        inputFechaInicio.setOnAction(action -> {
            if (inputFechaInicio.getValue() != null && inputFechaFin.getValue() != null) {
                renderAnios();
                if (selectedAnio != null && selectedAnio < inputFechaInicio.getValue().getYear()) {
                    clearMeses();
                    gridPaneDetalleAnio.setVisible(false);
                    gridPaneDetalleMes.setVisible(false);
                } else {
                    for (Node node : gridPaneAnios.getChildren()) {
                        if (selectedAnio != null && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == selectedAnio - inputFechaInicio.getValue().getYear() && node instanceof JFXButton) {
                            selectedAnio = null;
                            reRenderMeses = false;
                            node.fireEvent(new ActionEvent());
                            for (Node nodeMes : gridPaneMeses.getChildren()) {
                                if (selectedMes != null && GridPane.getColumnIndex(nodeMes) != null && GridPane.getColumnIndex(nodeMes) == selectedMes - 1 && nodeMes instanceof JFXButton) {
                                    selectedMes = null;
                                    nodeMes.fireEvent(new ActionEvent());
                                }
                            }
                            reRenderMeses = true;
                        }
                    }
                }
            }

        });
        inputFechaFin.setOnAction(action -> {
            if (inputFechaInicio.getValue() != null && inputFechaFin.getValue() != null) {
                renderAnios();
                if (selectedAnio != null && selectedAnio > inputFechaFin.getValue().getYear()) {
                    clearMeses();
                    gridPaneDetalleAnio.setVisible(false);
                    gridPaneDetalleMes.setVisible(false);
                } else {
                    for (Node node : gridPaneAnios.getChildren()) {
                        if (selectedAnio != null && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == (selectedAnio - inputFechaInicio.getValue().getYear()) && node instanceof JFXButton) {
                            selectedAnio = null;
                            reRenderMeses = false;
                            node.fireEvent(new ActionEvent());
                            for (Node nodeMes : gridPaneMeses.getChildren()) {
                                if (selectedMes != null && GridPane.getColumnIndex(nodeMes) != null && GridPane.getColumnIndex(nodeMes) == selectedMes - 1 && nodeMes instanceof JFXButton) {
                                    selectedMes = null;
                                    nodeMes.fireEvent(new ActionEvent());
                                    System.out.println(((JFXButton) nodeMes).getOnAction());
                                }
                            }
                            reRenderMeses = true;
                        }
                    }
                }
            }
        });

        anioDetallePasoInput.setOnAction(action -> setAnioDetallePasoInputOnAction(true));
//        anioDetalleRepetirInput.setOnAction(action -> {
//            for (Node node : gridPaneDetalleAnio.getChildren()) {
//                if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == 2 && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == 1) {
//                    node.setDisable(!anioDetalleRepetirInput.isSelected());
//                }
//            }
//            detalleLTDataAnios.get(selectedAnio).setRepetir(anioDetalleRepetirInput.isSelected());
//        });
//        anioDetalleCadaXInput.setOnAction(action -> detalleLTDataAnios.get(selectedAnio).setRepetirCada(Integer.parseInt(anioDetalleCadaXInput.getText())));// TODO: 29/10/2020 pasar a combo
//        anioDetalleHastaInput.setOnAction(action -> detalleLTDataAnios.get(selectedAnio).setRepetirHasta(Integer.parseInt(anioDetalleHastaInput.getValue())));

        mesDetallePasoInput.setOnAction(action -> mesDetallePasoInputOnAction());
//        mesDetalleRepetirInput.setOnAction(action -> {
//            for (Node node : gridPaneDetalleMes.getChildren()) {
//                if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == 2 && GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == 1) {
//                    node.setDisable(!mesDetalleRepetirInput.isSelected());
//                }
//            }
//            detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).setRepetir(mesDetalleRepetirInput.isSelected());
//        });
//        mesDetalleCadaXInput.setOnAction(action -> detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).setRepetirCada(Integer.parseInt(mesDetalleCadaXInput.getText())));// TODO: 30/10/2020 pasar a combo ofreciendo anios dispinibles en la corrida
//        mesDetalleHastaInput.setOnAction(action -> detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).setRepetirHasta(Integer.parseInt(mesDetalleHastaInput.getValue())));
    }

    private void updateMesDetalle() {
        mesDetallePasoInput.setOnAction(null);

        // Intento hallar el minimo paso que cubra todos los casos
        String paso = detalleLTDataAnios.get(selectedAnio).getPaso();
        if (paso == null) {
            // Es un anio con pasos mixtos, me fijo en los meses
            for (DetalleLTData detalleMes : detalleLTDataAnios.get(selectedAnio).subDetalleMap.values()) {
                if (compararPasos(paso, detalleMes.paso)) {
                    paso = detalleMes.paso;
                }
            }
        }

        if (paso.equalsIgnoreCase(Text.PASO_SEMANAL)) {
            mesDetallePasoInput.getItems().clear();
            mesDetallePasoInput.getItems().addAll(Text.PASOS_BASE);

        } else if (paso.equalsIgnoreCase(Text.PASO_DIARIO)) {
            mesDetallePasoInput.getItems().clear();
            mesDetallePasoInput.getItems().addAll(Arrays.asList(Text.PASO_DIARIO, Text.PASO_HORARIO));
        } else {
            mesDetallePasoInput.getItems().clear();
            mesDetallePasoInput.getItems().add(Text.PASO_HORARIO);

        }

        if (detalleLTDataAnios.get(selectedAnio).getPaso() != null) {
            for (DetalleLTData detalleMes : detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().values()) {
                detalleMes.setPaso(detalleLTDataAnios.get(selectedAnio).getPaso());
                mesDetallePasoInput.getSelectionModel().select(anioDetallePasoInput.getValue());
            }
        } else {
            mesDetallePasoInput.getSelectionModel().select(detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).paso);
        }


        mesDetallePasoInput.setOnAction(event -> mesDetallePasoInputOnAction());
        detalleLTDataAnios.get(selectedAnio).btnSetText();


    }

    private void initialRenderAnios() {
        clearAnios();
        int cantAnios = inputFechaFin.getValue().getYear() - inputFechaInicio.getValue().getYear() + 1;
        for (int i = 0; i < cantAnios; i++) {
            int anioInt = inputFechaInicio.getValue().getYear() + i;
            JFXButton anioBtn = detalleLTDataAnios.get(anioInt).getBtn();
            // Seteo las propiedades del boton
            anioBtn.setTextFill(Paint.valueOf("#ffffff"));
            anioBtn.setPrefWidth(gridPaneAnios.getPrefWidth() / cantAnios);
            anioBtn.setPrefHeight(40);
            anioBtn.setStyle("-fx-background-color: #27468d; -fx-text-color: white;");
            String anioStr = String.valueOf(anioInt);
            anioBtn.setOnAction(action -> anioBtnLogic(anioBtn, anioInt, anioStr));
            Label anioTxt = new Label(anioStr);
            anioTxt.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

            gridPaneAnios.add(anioBtn, i, 0);
            gridPaneAnios.add(anioTxt, i, 1);
            gridPaneAnios.getColumnConstraints().add(new ColumnConstraints(gridPaneAnios.getPrefWidth() / cantAnios - 3));
            GridPane.setHalignment(anioTxt, HPos.CENTER);
        }
    }

    private void renderAnios() {
        clearAnios();
        detalleLTDataAnios.clear();

        int cantAnios = inputFechaFin.getValue().getYear() - inputFechaInicio.getValue().getYear() + 1;
        for (int i = 0; i < cantAnios; i++) {
            int anioInt = inputFechaInicio.getValue().getYear() + i;
            JFXButton anioBtn;
            if (!detalleLTDataAnios.containsKey(anioInt) || detalleLTDataAnios.get(anioInt).getBtn() == null) {
                anioBtn = new JFXButton();
            } else {
                anioBtn = detalleLTDataAnios.get(anioInt).getBtn();
            }
            anioBtn.setTextFill(Paint.valueOf("#ffffff"));
            anioBtn.setPrefWidth(gridPaneAnios.getPrefWidth() / cantAnios);
            anioBtn.setPrefHeight(40);
            anioBtn.setStyle("-fx-background-color: #27468d; -fx-text-color: white;");
            String anioStr = String.valueOf(anioInt);
            anioBtn.setOnAction(action -> anioBtnLogic(anioBtn, anioInt, anioStr));
            Label anioTxt = new Label(anioStr);
            anioTxt.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

            gridPaneAnios.add(anioBtn, i, 0);
            gridPaneAnios.add(anioTxt, i, 1);
            gridPaneAnios.getColumnConstraints().add(new ColumnConstraints(gridPaneAnios.getPrefWidth() / cantAnios - 3));
            GridPane.setHalignment(anioTxt, HPos.CENTER);

            if (!detalleLTDataAnios.containsKey(anioInt)) {
                DetalleLTData detalleAnio = new DetalleLTData();
                detalleAnio.setBtn(anioBtn);
                detalleAnio.setPaso(selectedPaso);
                for (int j = 0; j < 12; j++) {
                    if (esPosterior(i, j, 0, (inputFechaInicio.getValue().getMonthValue() - 1)) &&
                            esPosterior( cantAnios - 1, inputFechaFin.getValue().getMonthValue()-1 ,i, j)) {
                        DetalleLTData detalleMes = new DetalleLTData();
                        detalleMes.setPaso(selectedPaso);
                        detalleAnio.getSubDetalleMap().put(j + 1, detalleMes);
                    }
                }
                detalleLTDataAnios.put(anioInt, detalleAnio);
            } else if (detalleLTDataAnios.get(anioInt).getBtn() == null) {
                detalleLTDataAnios.get(anioInt).setBtn(anioBtn);
            }
        }
    }

    // Dado dos anios y dos meses devuelve true si y solo si la primer fecha es posterior a la segunda
    private boolean esPosterior(int anioA, int mesA, int anioB, int mesB){
        if(anioA < anioB){
            return false;
        }
        if(anioA > anioB){
            return true;
        }
        return mesA >= mesB;
    }

    private void anioBtnLogic(JFXButton anioBtn, int anioInt, String anioStr) {
        if (selectedAnio == null || selectedAnio != anioInt) {
            selectedAnio = anioInt;
//            anioDetallePasoInput.fireEvent(new ActionEvent());///////////////////////////
            selectedMes = null;
//            updateMesDetalle();
            if (reRenderMeses) {
                renderMeses(anioStr);
            }

            //manejo de estilos
            for (Node node : gridPaneAnios.getChildren()) {
                if (node instanceof JFXButton) {
                    node.getStyleClass().remove("time_line_button");
                    node.setStyle("-fx-background-color: #27468d80");
                }
            }
            anioBtn.getStyleClass().add("time_line_button");
            anioBtn.setStyle("-fx-background-color: #27468d");

            //manejo detalle anio/mes
            gridPaneDetalleAnio.setVisible(true);
            gridPaneDetalleMes.setVisible(false);
            anioDetalleLabel.setText(anioStr);
            anioDetallePasoInput.getSelectionModel().select(pasoMesesToPasoAnio());
//            anioDetalleRepetirInput.setSelected(detalleLTDataAnios.get(anioInt).isRepetir());
//            anioDetalleCadaXInput.setText(String.valueOf(detalleLTDataAnios.get(anioInt).getRepetirCada()));
//            anioDetalleHastaInput.getSelectionModel().select(detalleLTDataAnios.get(anioInt).getRepetirHasta());
//            anioDetalleRepetirInput.fireEvent(new ActionEvent());
        } else {
            selectedAnio = null;
            clearMeses();
            for (Node node : gridPaneAnios.getChildren()) {
                if (node instanceof JFXButton) {
                    node.setStyle("-fx-background-color: #27468d");
                }
            }
            anioBtn.getStyleClass().remove("time_line_button");
            gridPaneDetalleAnio.setVisible(false);
            gridPaneDetalleMes.setVisible(false);
        }
    }

    private void inputPasoBaseOnAction() {
        if (selectedPaso == null || !selectedPaso.equalsIgnoreCase(inputPasoBase.getValue())) {
            selectedPaso = inputPasoBase.getValue();


            if(CorridaHandler.getInstance().getCorridaActual() == null){
                //cantidad de postes por defecto
                inputCantPostes.setText(String.valueOf(Text.DEFAULT_CANT_POSTES.get(inputPasoBase.getValue())));
            }else{
                int cantPostes = CorridaHandler.getInstance().getCorridaActual().getLineaTiempo().getCantidadPostes();
                inputCantPostes.setText(String.valueOf(cantPostes));
            }


            anioDetallePasoInput.setOnAction(action -> setAnioDetallePasoInputOnAction(false));
            //paso intervalos detallados
            if (selectedPaso.equalsIgnoreCase(Text.PASO_SEMANAL)) {
                anioDetallePasoInput.getItems().setAll(Text.PASOS_BASE);
            } else if (selectedPaso.equalsIgnoreCase(Text.PASO_DIARIO)) {
                anioDetallePasoInput.getItems().setAll(Arrays.asList(Text.PASO_DIARIO, Text.PASO_HORARIO));
            } else {
                anioDetallePasoInput.getItems().setAll(Text.PASO_HORARIO);
            }

            anioDetallePasoInput.getSelectionModel().select(selectedPaso);


            Integer oldSelectedAnio = selectedAnio;
            if (cargaInicial) {
                anioDetallePasoInput.getSelectionModel().select(inputPasoBase.getValue());
            } else {
                for (Integer anio : detalleLTDataAnios.keySet()) {
                    detalleLTDataAnios.get(anio).setPaso(selectedPaso);
                    detalleLTDataAnios.get(anio).btnSetText();
                    for (Integer mes : detalleLTDataAnios.get(anio).getSubDetalleMap().keySet()) {
                        detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).setPaso(selectedPaso);
                    }
                    selectedAnio = anio;
                    updateMesDetalle();
                }
                if (oldSelectedAnio != null) {
                    selectedAnio = oldSelectedAnio;
                }

            }
            anioDetallePasoInput.setOnAction(action -> setAnioDetallePasoInputOnAction(true));
            cargaInicial = false;

        }
    }

    private void setAnioDetallePasoInputOnAction(boolean propagarCambiosHaciaPasoBase) {
        if (selectedAnio != null && anioDetallePasoInput.getValue() != null) {
            detalleLTDataAnios.get(selectedAnio).setPaso(anioDetallePasoInput.getValue());
            updateMesDetalle();
        }
        if (propagarCambiosHaciaPasoBase) {
            // Si todos los anios poseen el mismo paso entonces el paso base deberia de compatir dicho paso
            // En otro caso el paso base deberia de ser null
            String primerPaso = "";
            boolean todosLosPasosIguales = true;
            for (DetalleLTData detallesAnio : detalleLTDataAnios.values()) {
                if (primerPaso == "") {
                    primerPaso = detallesAnio.paso;
                } else {
                    if (primerPaso != detallesAnio.paso) {
                        todosLosPasosIguales = false;
                        break;
                    }
                }
            }
            if (todosLosPasosIguales && primerPaso != null) {
                inputPasoBase.getSelectionModel().select(primerPaso);
            }
        }
    }

    private void mesDetallePasoInputOnAction() {
        if (selectedMes != null) {
            detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).setPaso(mesDetallePasoInput.getValue());
            detalleLTDataAnios.get(selectedAnio).btnSetText();
            String nuevoPaso = pasoMesesToPasoAnio();
            detalleLTDataAnios.get(selectedAnio).setPaso(nuevoPaso);
            anioDetallePasoInput.getSelectionModel().select(nuevoPaso);
        }
    }

    // En caso de que todos los meses posean el mismo paso devuelve el paso
    // En caso contrario devuelve null
    private String pasoMesesToPasoAnio() {
        String primerPaso = "";
        boolean todosLosPasosIguales = true;
        for (DetalleLTData detalleMes : detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().values()) {
            if (primerPaso.equals("")) {
                primerPaso = detalleMes.paso;
            } else {
                if (primerPaso != detalleMes.paso) {
                    todosLosPasosIguales = false;
                    break;
                }
            }
        }
        if (todosLosPasosIguales) {
            return primerPaso;
        } else {
            return null;
        }
    }

    private void clearAnios() {
        gridPaneAnios.getChildren().clear();
        gridPaneAnios.getColumnConstraints().clear();
    }

    private void renderMeses(String anioStr) {
        clearMeses();
        anioLabel.setText(anioStr);
        for (int i = 0; i < 12; i++) {
            int mesInt = i + 1;
            if (detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(mesInt) != null) {
                JFXButton mesBtn = new JFXButton();
                mesBtn.setPrefWidth(gridPaneMeses.getPrefWidth() / 12);
                mesBtn.setPrefHeight(40);
                mesBtn.setStyle("-fx-background-color: #e87c2a");
                String mesStr = Text.MONTHS_BY_NUMBER.get(i + 1);
                mesBtn.setOnAction(action -> mesBtnLogic(mesBtn, mesInt, mesStr));
                Label mesTxt = new Label(mesStr);
                mesTxt.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

                gridPaneMeses.add(mesBtn, i, 0);
                gridPaneMeses.add(mesTxt, i, 1);
                gridPaneMeses.getColumnConstraints().add(new ColumnConstraints(gridPaneMeses.getPrefWidth() / 12 - 3));
                GridPane.setHalignment(mesTxt, HPos.CENTER);

                detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(mesInt).setBtn(mesBtn);
            }
        }
    }

    private void mesBtnLogic(JFXButton mesBtn, int mesInt, String mesStr) {
        if (selectedMes == null || selectedMes != mesInt) {
            selectedMes = mesInt;
            updateMesDetalle();

            //manejo de estilos
            for (Node node : gridPaneMeses.getChildren()) {
                if (node instanceof JFXButton) {
                    node.getStyleClass().remove("time_line_button");
                    node.setStyle("-fx-background-color: #e87c2a80");
                }
            }
            mesBtn.setStyle("-fx-background-color: #e87c2a");
            mesBtn.getStyleClass().add("time_line_button");

            //manejo detalle mes
            gridPaneDetalleMes.setVisible(true);
            mesDetalleLabel.setText(mesStr);
            mesDetallePasoInput.getSelectionModel().select(detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).getPaso());
//            mesDetalleRepetirInput.setSelected(detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).isRepetir());
//            mesDetalleCadaXInput.setText(String.valueOf(detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).getRepetirCada()));
//            mesDetalleHastaInput.getSelectionModel().select(detalleLTDataAnios.get(selectedAnio).getSubDetalleMap().get(selectedMes).getRepetirHasta());
//            mesDetallePasoInput.fireEvent(new ActionEvent());
        } else {
            selectedMes = null;

            //manejo de estilos
            for (Node node : gridPaneMeses.getChildren()) {
                if (node instanceof JFXButton) {
                    node.setStyle("-fx-background-color: #e87c2a");
                }
            }
            mesBtn.getStyleClass().remove("time_line_button");

            //manejo detalle mes
            gridPaneDetalleMes.setVisible(false);
        }
    }

    private void clearMeses() {
        selectedMes = null;
        anioLabel.setText("");
        gridPaneMeses.getChildren().clear();
        gridPaneMeses.getColumnConstraints().clear();
    }


    /***
     * Construye un string que va a contener un codigo para cada bloque
     * DIARIO:24(1[2022]-365[2023])
     * Nomeclatura:  PASO:cantMeses(diaInicio[añoInicio]-diaFin[añoFin])
     * toma los datos de la interfaz grafica
     * @return
     */
    private String getCadenaBloques() {
        String bloqueActual = null;
        Integer anioIni = -1;
        int nuevoBloqueIni = -1;
        int bloqueIni = -1;
        int bloqueFin = -1;
        int mesesBloqueActual = 0;
        StringBuilder cadenaBloques = new StringBuilder();

        //LocalDateTime fechaInicioCorrida = LocalDateTime.parse(datosCorrida.getInicioCorrida(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));
        int diaAñoIniciaCorrida = inputFechaInicio.getValue().getDayOfYear();

        for (Integer anio : detalleLTDataAnios.keySet()) {
            for (Integer mes : detalleLTDataAnios.get(anio).getSubDetalleMap().keySet()) {
                if (bloqueActual == null || bloqueActual.equalsIgnoreCase(detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso())) {
                    mesesBloqueActual++;
                    if (bloqueActual == null) {
                        bloqueIni = diaAñoIniciaCorrida;
                        anioIni = anio;
                    } else if (bloqueActual.equalsIgnoreCase(Text.PASO_SEMANAL) && mes == 12) {
                        bloqueFin = LocalDate.of(anio, 1, 1).lengthOfYear();
                        nuevoBloqueIni = 1;
                        cadenaBloques.append(bloqueToString(bloqueActual, mesesBloqueActual, bloqueIni, anio, bloqueFin,
                                anio, anio == inputFechaFin.getValue().getYear() && mes == inputFechaFin.getValue().getMonthValue()));
                        bloqueIni = nuevoBloqueIni;
                        mesesBloqueActual = 0;
                    }

                    bloqueActual = detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso();
                } else {
                    if (detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso() != null &&
                            detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso().equalsIgnoreCase(Text.PASO_SEMANAL)) {
                        LocalDate f = LocalDate.of(anio, mes, 1);
//                        nuevoBloqueIni = (f.getDayOfYear() / 7)*7;
//                        nuevoBloqueIni = nuevoBloqueIni == 0 ? 1 : nuevoBloqueIni;
//                        bloqueFin = nuevoBloqueIni == 1 ? LocalDate.of(anio-1,1,1).lengthOfYear() : nuevoBloqueIni - 1;
                        // Encuentra el proximo dia multiplo de 7 + 1 ej: 3 => 7,  71 => 77, 7 => 7
                        bloqueFin = ((f.getDayOfYear() - 1) / 7) * 7 + 7 * ((f.getDayOfYear() - 1) % 7 > 0 ? 1 : 0);
                        nuevoBloqueIni = bloqueFin + 1;
                    } else if (bloqueActual.equalsIgnoreCase(Text.PASO_SEMANAL)) {
                        if (mes == 1) {
                            bloqueFin = LocalDate.of(anio - 1, 1, 1).lengthOfYear();
                            nuevoBloqueIni = 1;
                        } else {
                            LocalDate f = LocalDate.of(anio, mes, 1);
//                            bloqueFin = (f.getDayOfYear() / 7) * 7 + 7 * (f.getDayOfYear() % 7 > 0 ? 1 : 0);
//                            nuevoBloqueIni = bloqueFin + 1;
                            nuevoBloqueIni = (f.getDayOfYear() / 7) * 7 + 1;
//                            nuevoBloqueIni = nuevoBloqueIni == 0 ? 1 : nuevoBloqueIni;
                            bloqueFin = nuevoBloqueIni == 1 ? LocalDate.of(anio - 1, 1, 1).lengthOfYear() : nuevoBloqueIni - 1;
                        }
                    } else {
                        LocalDate f = LocalDate.of(anio, mes, 1);
                        nuevoBloqueIni = f.getDayOfYear();
                        bloqueFin = nuevoBloqueIni == 1 ? LocalDate.of(anio - 1, 1, 1).lengthOfYear() : nuevoBloqueIni - 1;
                    }

                    if (mesesBloqueActual > 0) {

                        cadenaBloques.append(bloqueToString(bloqueActual, mesesBloqueActual, bloqueIni, anioIni, bloqueFin,
                                (bloqueFin == LocalDate.of(anio - 1, 1, 1).lengthOfYear() ? anio - 1
                                        : anio), false));
                    }

                    bloqueActual = detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso();
                    mesesBloqueActual = 1;

                    bloqueIni = nuevoBloqueIni;
                    anioIni = anio;
                }
            }
        }
        if (mesesBloqueActual > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); //Se contempla corrida que no finaliza 31 de diciembre
            String date = "01/01/" + inputFechaFin.getValue().getYear();
            LocalDate iniAnioFin = LocalDate.parse(date, formatter);
            bloqueFin = (int) DAYS.between(iniAnioFin, inputFechaFin.getValue()) + 1;
            cadenaBloques.append(bloqueToString(bloqueActual, mesesBloqueActual, bloqueIni, anioIni, bloqueFin,
                    inputFechaFin.getValue().getYear(), true));
        }
        return cadenaBloques.toString();
    }

    // Funcion que transforma los datos de un bloque en un string con formato PASO:cantMeses(diaInicio[añoInicio]-diaFin[añoFin])
    private String bloqueToString(String nombrePaso, int cantidadMeses, int diaInicio, int anioInicio, int diaFin, int anioFin, boolean ultimoBloque){
        return nombrePaso + ":" + cantidadMeses + "(" + diaInicio +  "[" + anioInicio + "]" + "-" + diaFin +
                "[" + anioFin + "]" + (ultimoBloque ?  ")" : "),");
    }

    public void setInputsFechas(LocalDate fechaIni, LocalDate fechaFin) {
        inputFechaInicio.setValue(fechaIni);
        inputFechaFin.setValue(fechaFin);
    }

    /***
     * Toma los datos de la interfaz, construye y devuelve un objeto DatosLineaTiempo
     * @return
     */
    private DatosLineaTiempo parseLineaDeTiempo() {
        DatosLineaTiempo datosLineaTiempo = new DatosLineaTiempo();

        datosLineaTiempo.setTiempoInicial(inputFechaInicio.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosLineaTiempo.setTiempoFinal(inputFechaFin.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59");

        String cadenaBloques = getCadenaBloques();

        System.out.println(cadenaBloques);
        String[] segmentos = cadenaBloques.split(",");
        int cantBloques = 0;

        for (String segmento : segmentos) {
            cantBloques++;
            Pattern pattern = Pattern.compile("(\\d+)\\[(\\d+)\\]-(\\d+)\\[(\\d+)\\]");
            Matcher matcher = pattern.matcher(segmento);
            matcher.find();
            Integer bloqueIni = Integer.valueOf(matcher.group(1));
            Integer anioIni = Integer.valueOf(matcher.group(2));
            Integer bloqueFin = Integer.valueOf(matcher.group(3));
            Integer anioFin = Integer.valueOf(matcher.group(4));

            if (segmento.contains(Text.PASO_HORARIO)) {
                // add bloque horario
                int cantPasos;
                if (anioIni.equals(anioFin)) {
                    cantPasos = bloqueFin - bloqueIni + 1;
                    cantPasos *= 24;
                } else {
                    cantPasos = 365 - bloqueIni + 1;
                    cantPasos += LocalDate.of(anioIni, 1, 1).isLeapYear() ? 1 : 0;

                    //for(int i=anioIni+1;i<=anioFin-1;i++){  2022/11/15 ponia solo un año cuando eran 2
                    for (int i = anioIni; i < anioFin; i++) {
                        cantPasos += LocalDate.of(i, 1, 1).isLeapYear() ? 366 : 365;
                    }
                    cantPasos *= 24;
                }
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(3600), String.valueOf(3600), String.valueOf(1),
                        new ArrayList<>(Arrays.asList(3600)),
                        String.valueOf(0), String.valueOf(true));
            } else if (segmento.contains(Text.PASO_DIARIO)) {
                // add bloque diario 24p - 1hr -cron T
                int cantPasos = 0;
                if (anioIni.equals(anioFin)) {
                    cantPasos = bloqueFin - bloqueIni + 1;
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    if (cantBloques == 1 && !corridaIniciaPrimeroAnio()) {  //Se contempla corrida que no inicia 1 de enero
                        String date = "31/12/" + anioIni;
                        LocalDate finAnioIni = LocalDate.parse(date, formatter);
                        cantPasos += DAYS.between(inputFechaInicio.getValue(), finAnioIni) + 1;//
                    } else {
                        cantPasos = 365 - bloqueIni + 1;
                        cantPasos += LocalDate.of(anioIni, 1, 1).isLeapYear() ? 1 : 0;

                    }
                    for (int i = anioIni + 1; i <= anioFin - 1; i++) {

                        cantPasos += LocalDate.of(i, 1, 1).isLeapYear() ? 366 : 365;
                    }
                    String date = "01/01/" + anioFin;
                    LocalDate inicioAnioFin = LocalDate.parse(date, formatter);
                    cantPasos += DAYS.between(inicioAnioFin, inputFechaFin.getValue()) + 1;
                }
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(3600 * 24), String.valueOf(3600), String.valueOf(24),
                        new ArrayList<>(Arrays.asList(3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600)),
                        String.valueOf(0), String.valueOf(true));
            } else if (segmento.contains(Text.PASO_SEMANAL)) {
                // add bloque(s) semanal(es) 4p -cron F
                int cantPasos;
//                if((bloqueFin - bloqueIni + 1) % 7 == 0){
                cantPasos = (bloqueFin - bloqueIni + 1) / 7;
                if((bloqueFin - bloqueIni + 1 ) > 364) {
                    cantPasos -= ((bloqueFin - bloqueIni + 1) % 7 == 0) ? 0 : 1; // Años completos: la ultima semana se agrega aparte
                }
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(604800), String.valueOf(3600), String.valueOf(4),
                        new ArrayList<>(Arrays.asList(18000, 108000, 327600, 151200)),
                        String.valueOf(0), String.valueOf(false));
//                }else{
                if((bloqueFin - bloqueIni + 1 ) > 364) {

                    if ((bloqueFin - bloqueIni + 1) % 7 == 1 ) {
                        cantPasos = 1;
                        // semana larga - no bisiesto
                        datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(691200), String.valueOf(3600), String.valueOf(4),
                                new ArrayList<>(Arrays.asList(21600, 122400, 374400, 172800)),
                                String.valueOf(0), String.valueOf(false));
                    } else if ((bloqueFin - bloqueIni + 1) % 7 == 2 ) {
                        cantPasos = 1;
                        // semana larga - bisiesto
                        datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(777600), String.valueOf(3600), String.valueOf(4),
                                new ArrayList<>(Arrays.asList(21600, 140400, 421200, 194400)),
                                String.valueOf(0), String.valueOf(false));
                    }
                }
//                }


            }
        }

        datosLineaTiempo.print();// TODO: 12/11/2020 debug

        return datosLineaTiempo;
    }


    /***
     Construye el hashMap:  detalleLTDataAnios que son todos los botones que representan años y meses
     toma los datos de datosLineaTiempo del datosCorrida
     Devuelve true si la linea del tiempo puede ser representada por NuevaLineaDeTiempoController
     * @param datosLineaTiempo
     * @return
     */
    public boolean generarDatosLT(DatosLineaTiempo datosLineaTiempo) {
        LocalDateTime fechaIni = LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));
        LocalDateTime fechaFin = LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoFinal(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));

        boolean corridaIniciaDiaUno = (fechaIni.getDayOfMonth() == 1);

//        System.out.println("=>"+LocalDateTime.of(2000,1,1,0,0,0).plusSeconds(604800).getDayOfYear());

        Integer restoSemanaIni = (fechaIni.toLocalDate().lengthOfYear() - fechaIni.getDayOfYear() + 1) % 7;
        Integer restoSemanaFin = fechaFin.getDayOfYear() % 7;

        StringBuilder cadenaBloques = new StringBuilder();

        HashMap<Integer, HashMap<Integer, Integer>> daysOfYear = new HashMap<>();

        detalleLTDataAnios.clear();
        int totalDias = 0;
        // Creo el arbol de botones
        for (int i = fechaIni.getYear(); i <= fechaFin.getYear(); i++) {
            int diasAnio = LocalDate.of(i, 1, 1).isLeapYear() ? 366 : 365;
            totalDias += diasAnio;
            HashMap<Integer, Integer> currYearMap = new HashMap<>(); // Mapa que contiene la cantidad de dias de cada mes del anio

            // Creo un anio con su correspondiente boton
            DetalleLTData detalleAnio = new DetalleLTData();
            detalleAnio.setBtn(new JFXButton());
            detalleLTDataAnios.put(i, detalleAnio);

            HashMap<Integer, DetalleLTData> subDetalleMap = new HashMap<>(); // Mapa de meses

            // Para cada uno de los meses
            for (int j = 1; j <= 12; j++) {
                if (esPosterior(i, j,fechaIni.getYear(), fechaIni.getMonthValue() ) &&
                        esPosterior(fechaFin.getYear(), fechaFin.getMonthValue(), i, j)){ // El mes es posterior al inicio y anterior al fin
                    currYearMap.put(j, LocalDate.of(i, j, 1).lengthOfMonth());
                    // Creo el mes con su respectivo boton
                    DetalleLTData detalleMes = new DetalleLTData();
                    detalleMes.setBtn(new JFXButton());
                    subDetalleMap.put(j, detalleMes);
                }
            }
            detalleLTDataAnios.get(i).setSubDetalleMap(subDetalleMap);
            daysOfYear.put(i, currYearMap);
        }


        int diasProcesados = 0;
        int anioActual = fechaIni.getYear();
        int mesActual = fechaIni.getMonthValue();
        String pasoAnio = null;
        String pasoGeneral = null;
        int diasRestantes = 0;

        for (int i = 0; i < datosLineaTiempo.getCantBloques(); i++) {
            Integer durPaso = datosLineaTiempo.getDuracionPasoPorBloque().get(i);
            Integer cantPasos = datosLineaTiempo.getPasosPorBloque().get(i);
            if(datosLineaTiempo.getDurPostesPorBloque().get(0).size()!=24  && datosLineaTiempo.getDurPostesPorBloque().get(0).size()!=4){
                return false;
            }
            String pasoMes;
            switch (durPaso) {
                case 3600:
                    pasoMes = Text.PASO_HORARIO;
                    if (pasoAnio == null) {
                        pasoAnio = pasoMes;
                    }
                    break;
                case 3600 * 24:
                    pasoMes = Text.PASO_DIARIO;
                    if (pasoAnio == null || pasoAnio.equalsIgnoreCase(Text.PASO_HORARIO)) {
                        pasoAnio = pasoMes;
                    }
                    break;
                case 604800:
                    pasoMes = Text.PASO_SEMANAL;
                    pasoAnio = pasoMes;
                    break;
                case 777600:
                    if (mesActual != 1 || !LocalDate.of(anioActual - 1, 1, 1).isLeapYear()) {
                        return false;
                    } else {
//                        mesActual = 1;
//                        anioActual++;
                        continue;
                    }
//                    pasoMes = Text.PASO_SEMANAL;
//                    pasoAnio = pasoMes;
//                    break;
                case 691200:
                    if (mesActual != 1 || LocalDate.of(anioActual - 1, 1, 1).isLeapYear()) {
                        return false;
                    } else {
//                        mesActual = 1;
//                        anioActual++;
                        continue;
                    }
//                    pasoMes = Text.PASO_SEMANAL;
//                    pasoAnio = pasoMes;
//                    break;
                default:
                    return false;
            }

            if (compararPasos(pasoGeneral, pasoAnio)) {
                pasoGeneral = pasoAnio;
            }

            int diasBloque = durPaso * cantPasos / 86400;
            diasBloque += diasRestantes;

            diasProcesados += diasBloque;

            if (pasoMes.equalsIgnoreCase(Text.PASO_HORARIO) || pasoMes.equalsIgnoreCase(Text.PASO_DIARIO)) {
                while (diasBloque > 0 && (
                        (anioActual == fechaFin.getYear() && mesActual == fechaFin.getMonthValue()) || // Es el ultimo mes del ultimo anio
                                // TODO: Se contemplan bloques que no terminan a fin de mes?
                                daysOfYear.get(anioActual).get(mesActual) != null && anioActual <= fechaFin.getYear() &&
                                        diasBloque >= daysOfYear.get(anioActual).get(mesActual))) {


                    diasBloque -= daysOfYear.get(anioActual).get(mesActual);
                    detalleLTDataAnios.get(anioActual).setPaso(pasoAnio);
                    detalleLTDataAnios.get(anioActual).getSubDetalleMap().get(mesActual).setPaso(pasoMes);
                    mesActual++;
                    if (mesActual == 13) {
                        mesActual = 1;
                        pasoAnio = pasoMes;
                        anioActual++;
                    }
                }
                diasRestantes = diasBloque;
            } else {
                do {
                    diasBloque -= daysOfYear.get(anioActual).get(mesActual);
                    if(diasBloque < 0){
                        diasBloque = 0;
                    }
                    detalleLTDataAnios.get(anioActual).getSubDetalleMap().get(mesActual).setPaso(pasoMes);
                    detalleLTDataAnios.get(anioActual).setPaso(pasoAnio);
                    mesActual++;
                } while (daysOfYear.get(anioActual).get(mesActual) != null && diasBloque >= daysOfYear.get(anioActual).get(mesActual));
                if (diasBloque > 0) {
                    detalleLTDataAnios.get(anioActual).getSubDetalleMap().get(mesActual).setPaso(pasoMes);
                    detalleLTDataAnios.get(anioActual).setPaso(pasoAnio);
                    mesActual++;
                    if (mesActual == 13) {
                        mesActual = 1;
                        pasoAnio = null;
                        anioActual++;
                    }
                }
            }

            cadenaBloques.append(durPaso * cantPasos / 86400).append("(").append(durPaso / 86400).append(")").append("|");
        }
        // seteo el paso del anio
        for (int i = fechaIni.getYear(); i <= fechaFin.getYear(); i++) {
            String paso = null;
            for (DetalleLTData detalleaMes : detalleLTDataAnios.get(i).subDetalleMap.values()) {
                if(paso == null) {
                    paso = detalleaMes.paso;
                }else {
                    if(paso != detalleaMes.paso){
                        paso = null;
                        break;
                    }
                }
            }
            detalleLTDataAnios.get(i).setPaso(paso);
        }
        inputPasoBase.getSelectionModel().select(pasoGeneral);
        inputPasoBase.fireEvent(new ActionEvent());
        selectedPaso = pasoGeneral;
        return true;
    }

    /***
     * Devuelve true si el pasoAnio es mayor al pasoGeneral. Si son iguales o el pasoAnio es menor devuelve false (no hay que actualizar)
     * El pasoGeneral quedara con la mayor granularidad mayor de todos los bloques evaluados
     */
    private boolean compararPasos(String pasoGeneral, String pasoAnio) {
        boolean ret = false;
        if (pasoGeneral == null && pasoAnio != null) {
            ret = true;
        } else if (pasoGeneral != null) {
            switch (pasoGeneral) {
                case Text.PASO_HORARIO:
                    if (pasoAnio.equalsIgnoreCase(Text.PASO_SEMANAL) || pasoAnio.equalsIgnoreCase(Text.PASO_DIARIO)) {
                        ret = true;
                    }
                    break;
                case Text.PASO_DIARIO:
                    if (pasoAnio.equalsIgnoreCase(Text.PASO_SEMANAL)) {
                        ret = true;
                    }
                    break;
            }
        }
        return ret;
    }

    public void clean() {
    } // TODO: 02/02/2022

    public void setDatosCorrida(DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
    }

    /**
     * Método para cargar los datos en el DatosLineaTiempo
     */
    public void loadData() {
        if (datosCorrida.getNombre() != null) {
            DatosLineaTiempo lt = parseLineaDeTiempo();
            datosCorrida.setLineaTiempo(lt);
            datosCorrida.setInicioCorrida(lt.getTiempoInicial());
            datosCorrida.setFinCorrida(lt.getTiempoFinal());
        }
    }

    /**
     * Método para obtener los datos del DatosLineaTiempo y ponerlos en la interfaz (solo en modo edición)
     * Si la complejidad de la linea de tiempo lo permite se muestra con NuevaLineaDeTiempoController sino se usa la LineaDeTiempoDetallada
     */
    public void unloadData() {
        if (datosCorrida.getInicioCorrida() != null) {
            inputFechaInicio.setValue(LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());
            inputFechaFin.setValue(LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoFinal(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());

            if (generarDatosLT(datosCorrida.getLineaTiempo())) {
                initialRenderAnios(); // renderAnios();
            } else {
                parentController.setNuevaLineaDeTiempoActiva(false);
            }
        }
    }

    private static class DetalleLTData {
        private String paso;
        private boolean repetir;
        private int repetirCada;
        private int repetirHasta;
        private JFXButton btn;
        private HashMap<Integer, DetalleLTData> subDetalleMap = new HashMap<>();

        private DetalleLTData() {

        }

        public String getPaso() {
            return paso;
        }

        public void setPaso(String paso) {
            this.paso = paso;
            if (this.btn != null) {
                btnSetText();
            }
        }

        public boolean isRepetir() {
            return repetir;
        }

        public void setRepetir(boolean repetir) {
            this.repetir = repetir;
        }

        public int getRepetirCada() {
            return repetirCada;
        }

        public void setRepetirCada(int repetirCada) {
            this.repetirCada = repetirCada;
        }

        public int getRepetirHasta() {
            return repetirHasta;
        }

        public void setRepetirHasta(int repetirHasta) {
            this.repetirHasta = repetirHasta;
        }

        public JFXButton getBtn() {
            return btn;
        }

        public void setBtn(JFXButton btn) {
            this.btn = btn;
            btnSetText();
        }

        public HashMap<Integer, DetalleLTData> getSubDetalleMap() {
            return subDetalleMap;
        }

        public void setSubDetalleMap(HashMap<Integer, DetalleLTData> subDetalleMap) {
            this.subDetalleMap = subDetalleMap;
        }

        private void btnSetText() {
            if (subDetalleMap.size() == 0) {
                if (paso != null && btn != null) {
                    switch (paso) {
                        case Text.PASO_SEMANAL:
                            btn.setText("S");
                            break;
                        case Text.PASO_DIARIO:
                            btn.setText("D");
                            break;
                        case Text.PASO_HORARIO:
                            btn.setText("H");
                            break;
                    }
                }
            } else {
                String txt = "";
                boolean containsSemanal = false;
                boolean containsDiario = false;
                boolean containsHorario = false;
                for (DetalleLTData subDetalle : subDetalleMap.values()) {
                    if (subDetalle.getPaso() != null) {
                        if (!containsSemanal && subDetalle.getPaso().equalsIgnoreCase(Text.PASO_SEMANAL)) {
                            containsSemanal = true;
                        }
                        if (!containsDiario && subDetalle.getPaso().equalsIgnoreCase(Text.PASO_DIARIO)) {
                            containsDiario = true;
                        }
                        if (!containsHorario && subDetalle.getPaso().equalsIgnoreCase(Text.PASO_HORARIO)) {
                            containsHorario = true;
                        }
                    }
                }
                txt += containsSemanal ? "S" : "";
                txt += containsDiario ? txt.length() > 0 ? "/D" : "D" : "";
                txt += containsHorario ? txt.length() > 0 ? "/H" : "H" : "";
                btn.setText(txt);
            }
        }
    }

    private boolean corridaIniciaPrimeroAnio() {
        boolean ret = false;
        if (inputFechaInicio.getValue().getMonthValue() == 1 && inputFechaInicio.getValue().getDayOfMonth() == 1) {
            ret = true;
        }
        return ret;
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> ret = new ArrayList<>();
        // TODO: Revisar con Mónica
        // Validacion directamente sobre los inputs
        if(inputFechaInicio.getValue() == null || !inputFechaInicio.validate()){
            ret.add("Debe ingresarse una fecha de inicio valida para la linea de tiempo");
        }
        if(inputFechaFin.getValue() == null || !inputFechaFin.validate()){
            ret.add("Debe ingresarse una fecha de fin valida para la linea de tiempo");
        }
        if(ret.size() == 0 && inputFechaInicio.getValue().isAfter(inputFechaFin.getValue())){
            ret.add(("La fecha de fin no puede ser aterior a la de inicio"));
        }
        try {
            int cantPostes = Integer.valueOf(inputCantPostes.getText());
            if(cantPostes < 1){
                throw new NumberFormatException();
            }
        }catch(NumberFormatException exception){
               ret.add("La cantidad de postes debe ser un número entero positivo");
        }
        DatosLineaTiempo datosLineaTiempo = parseLineaDeTiempo();
        ArrayList<String> erroresLT = datosLineaTiempo.controlDatosCompletos();
        ret.addAll(erroresLT);
        if(ret.size() == 0) {
            if(datosLineaTiempo.getCantBloques() != datosLineaTiempo.getDurPostesPorBloque().size()){
                ret.add("No coincide la cantidad de bloques");
            }
        }
        return ret;
    }


}
