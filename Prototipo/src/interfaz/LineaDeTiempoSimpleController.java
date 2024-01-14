/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaDeTiempoSimpleController is part of MOP.
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tiempo.BloqueTiempo;
import utilitarios.Constantes;
import utilitarios.UtilArrays;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;


public class LineaDeTiempoSimpleController extends GeneralidadesController {

    @FXML
    private GridPane gridPaneAnios;
    @FXML
    private GridPane gridPanePostesAnio;
    @FXML
    private JFXDatePicker inputFechaInicio;
    @FXML
    private JFXDatePicker inputFechaFin;
    @FXML
    private JFXComboBox<String> inputPasoBase;
    @FXML
    private JFXTextField inputCantPostes;

    @FXML
    private JFXTextField inputIntervaloMuestreo;


    @FXML
    private JFXTextField poste1, poste2, poste3, poste4,poste5,poste6,poste7,poste8, poste9,poste10,poste11,poste12,
            poste13,poste14,poste15,poste16,poste17,poste18, poste19,poste20,poste21,poste22,poste23,poste24;

    private ArrayList<JFXTextField> posteList;

    @FXML
    private Label labelP1,labelP2,labelP3,labelP4,labelP5,labelP6,labelP7,labelP8,labelP9,labelP10,labelP11, labelP12,
            labelP13,labelP14,labelP15,labelP16,labelP17,labelP18,labelP19,labelP20,labelP21,labelP22,labelP23,labelP24;

    private ArrayList<Label> lbPosteList;

    private BloqueTiempo bloqueBase;
    @FXML
    private JFXTextField inputCantPasos;
    @FXML
    private JFXTextField inputHorasPorPaso;
    @FXML private JFXCheckBox inputBloquesCronologicos;
    @FXML
    private JFXButton btnFinalizarDisenio;
    @FXML
    private Label anioLabel;
    @FXML
    private Label anioDetalleLabel;
    private HashMap<Integer, DetalleLTData> detalleLTDataAnios = new HashMap<>();
    @FXML
    private JFXButton btnDetallarBloques;

    private Manejador parentController;
    private DatosCorrida datosCorrida;
    private Integer selectedMes;
    private String selectedPaso;
    private boolean reRenderMeses = true;
    private boolean cargaInicial = true;

    private LocalDate fechaSugeridaInicio;
    private LocalDate fechaSugeridaFin;
    private ArrayList<String> anios;

    public LineaDeTiempoSimpleController(Manejador parentController, DatosCorrida datosCorrida) {
        this.parentController = parentController;
        this.datosCorrida = datosCorrida;
        this.anios = new ArrayList<>();
    }

    @FXML
    public void initialize() {

        posteList = new ArrayList<>();
        lbPosteList = new ArrayList<>();

        {
            posteList.add(poste1);
            posteList.add(poste2);
            posteList.add(poste3);
            posteList.add(poste4);
            posteList.add(poste5);
            posteList.add(poste6);
            posteList.add(poste7);
            posteList.add(poste8);
            posteList.add(poste9);
            posteList.add(poste10);
            posteList.add(poste11);
            posteList.add(poste12);
            posteList.add(poste13);
            posteList.add(poste14);
            posteList.add(poste15);
            posteList.add(poste16);
            posteList.add(poste17);
            posteList.add(poste18);
            posteList.add(poste19);
            posteList.add(poste20);
            posteList.add(poste21);
            posteList.add(poste22);
            posteList.add(poste23);
            posteList.add(poste24);
        }

        {

            lbPosteList.add(labelP1);
            lbPosteList.add(labelP2);
            lbPosteList.add(labelP3);
            lbPosteList.add(labelP4);
            lbPosteList.add(labelP5);
            lbPosteList.add(labelP6);
            lbPosteList.add(labelP7);
            lbPosteList.add(labelP8);
            lbPosteList.add(labelP9);
            lbPosteList.add(labelP10);
            lbPosteList.add(labelP11);
            lbPosteList.add(labelP12);
            lbPosteList.add(labelP13);
            lbPosteList.add(labelP14);
            lbPosteList.add(labelP15);
            lbPosteList.add(labelP16);
            lbPosteList.add(labelP17);
            lbPosteList.add(labelP18);
            lbPosteList.add(labelP19);
            lbPosteList.add(labelP20);
            lbPosteList.add(labelP21);
            lbPosteList.add(labelP22);
            lbPosteList.add(labelP23);
            lbPosteList.add(labelP24);
        }

        inputCantPostes.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {

                if( UtilStrings.esNumeroEntero(inputCantPostes.getText())) {
                    int c = Integer.parseInt(inputCantPostes.getText());

                    cambiarVisibilidadPostesBase(c);
                    horasPorPaso();
                }
            }
        });
        btnDetallarBloques.setOnAction(action -> {
            if (datosCorrida.getNombre() != null) {
                if(loadData()){
                    parentController.setNuevaLineaDeTiempoActiva(false);
                }else {
                    setLabelMessageTemporal("Hay años con bloques incompletos",TipoInfo.FEEDBACK);
                }

            }else {

            }

        });

        btnFinalizarDisenio.setOnAction( action -> finalizarDisenio());

        inputPasoBase.getItems().addAll(Text.PASOS_BASE);
        inputPasoBase.getSelectionModel().select(Text.PASO_NONE);
        inputPasoBase.fireEvent(new ActionEvent());
        inputPasoBase.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                inputCantPasos.setText(calcularCantidadPasos());
                if(inputPasoBase.getValue().equalsIgnoreCase(Text.PASO_SEMANAL)){
                    verificarInicioCorrida();
                    verificarFinCorrida();
                }
            }
        });

        inputFechaInicio.setOnAction(action -> {

            verificarInicioCorrida();
            if (inputFechaInicio.getValue() != null && inputFechaFin.getValue() != null) {
                renderAnios();
            }

        });
        inputFechaFin.setOnAction(action -> {
            verificarFinCorrida();
            if (inputFechaFin.getValue() != null && inputFechaFin.getValue() != null) {
                renderAnios();
            }
        });

        inputCantPostes.setText("0");
        eventoCalcularDuracionPostesBase();
        cambiarVisibilidadPostesBase(0);

    }

    private void finalizarDisenio() {

        if(datosCorrida.getNombre() != null){
            BloqueTiempo b = cargarBloqueBase();
            if( b != null) {
                try {
                    //Elegir anios a los que asignar el bloque
                    ElegirAniosController elegiAniosController = new ElegirAniosController(this, anios);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ElegirAnios.fxml"));
                    loader.setController(elegiAniosController);

                    Parent root1 = loader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initStyle(StageStyle.UNDECORATED);
                    Scene firstScene = new Scene(root1, 546, 550);

                    firstScene.getRoot().requestFocus();
                    stage.setScene(firstScene);
                    stage.show();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            setLabelMessageTemporal("Debe crear o cargar una corrida",TipoInfo.FEEDBACK);
        }
    }

    public void aplicarDisenio(ArrayList<String> aniosSeleccionados){
        for (String a : aniosSeleccionados) {
            BloqueTiempo clon = cargarBloqueBase();
            detalleLTDataAnios.get(Integer.parseInt(a)).setBloqueTiempo(copiarBloque(clon, Integer.parseInt(a)));
            detalleLTDataAnios.get(Integer.parseInt(a)).setPaso(getPasoSegunDuracion(clon.getDuracionPaso()));
        }
    }

    private BloqueTiempo copiarBloque(BloqueTiempo bloqueBase, int anio) {
        BloqueTiempo ret = new BloqueTiempo();
        ret.setCronologico( bloqueBase.isCronologico());
        ret.setCantPostes(bloqueBase.getCantPostes());
        ret.setDuracionPaso(bloqueBase.getDuracionPaso());
        int[] durPos = copiarDuracionPostes(bloqueBase.getDuracionPostes());
        ret.setDuracionPostes(durPos);
        ret.setCantidadPasos(calcularPasosBloqueAnio( bloqueBase.getDuracionPaso(), anio));
        ret.setIntervaloMuestreo(bloqueBase.getIntervaloMuestreo());
        return ret;
    }

    private int[] copiarDuracionPostes(int[] duracionPostes) {
        int[] ret = new int[duracionPostes.length];
        for (int i = 0; i < duracionPostes.length; i++){
            ret[i] = duracionPostes[i];
        }
        return ret;
    }

    private BloqueTiempo cargarBloqueBase() {
        BloqueTiempo base = null;

        if(inputPasoBase.getValue().equals(Text.PASO_NONE)){ setLabelMessageTemporal("Debe elegir el Paso Base", TipoInfo.ERROR);}
        else if(inputFechaInicio.getValue() == null){ setLabelMessageTemporal("Debe elegir fecha de inicio", TipoInfo.ERROR);}
        else if(inputFechaFin.getValue() == null){ setLabelMessageTemporal("Debe elegir fecha de finalizacion", TipoInfo.ERROR);}
        else if(!UtilStrings.esNumeroEntero(inputCantPostes.getText())){
            setLabelMessageTemporal("La cantidad de postes debe ser número entero", TipoInfo.ERROR);
        } else if(!UtilStrings.esNumeroEntero(inputIntervaloMuestreo.getText())){
            setLabelMessageTemporal("El intervalo muestreo debe ser número entero", TipoInfo.ERROR);
        }
        else if (horasPorPaso() == 0){      setLabelMessageTemporal("La de la duracion de los postes no es correcta", TipoInfo.ERROR);         }
        else{
            int segundosPaso = getPasoSegunString(inputPasoBase.getSelectionModel().getSelectedItem());
            int hpp = horasPorPaso();
            if(hpp != (segundosPaso/3600)){
                setLabelMessageTemporal("La de la duracion de los postes debe ser igual a la duracion del paso", TipoInfo.ERROR);
            } else{
                int cantPostes = Integer.parseInt(inputCantPostes.getText());
                int[] durPos = cargoDuracionPostes(cantPostes);
                if(durPos != null){
                    int intMuest =Integer.parseInt(inputIntervaloMuestreo.getText())*3600;
                    if(!controlDurPostIntervaloMuestreo(durPos, intMuest)){
                        setLabelMessageTemporal("La duración de algún poste no es múltiplo del intervalo de muestreo", TipoInfo.ERROR);
                    }
                        else{
                        base = new BloqueTiempo();

                        base.setCronologico( inputBloquesCronologicos.isSelected());
                        base.setCantPostes(Integer.parseInt(inputCantPostes.getText()));
                        base.setDuracionPaso(getPasoSegunString(inputPasoBase.getValue()));
                        base.setDuracionPostes(copiarDuracionPostes(durPos));
                        base.setIntervaloMuestreo(intMuest);

                    }


                    return base;
                }
            }
        }

        return base;

    }

    /***
     * Devuelve true si pasa el control correctamente
     * @param durPos
     * @param intMuest
     * @return
     */
    public boolean controlDurPostIntervaloMuestreo(int[] durPos, int intMuest) {
        boolean ret = true;
        for(int i = 0; i<durPos.length; i++){
            if(durPos[i]%intMuest != 0){
                return false;
            }
        }
        return ret;
    }

    private int[] cargoDuracionPostes(int cantPostes) {
        int[] ret = new int[cantPostes];
        for (int ind =0; ind< cantPostes; ind ++){
            if(UtilStrings.esNumeroEntero(posteList.get(ind).getText())){
               ret[ind] = (Integer.parseInt(posteList.get(ind).getText()))*3600;
            } else {
                return null;
            }
        }
        return ret;
    }

    public LocalDate getInputInicioCorrida(){
        return inputFechaInicio.getValue();
    }

    public LocalDate getInputFinCorrida(){
        return inputFechaFin.getValue();
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
        int cantAnios = inputFechaFin.getValue().getYear() - inputFechaInicio.getValue().getYear() + 1;
        anios.clear();
        HashMap<Integer, DetalleLTData> detalleLTDataAnios2 = new HashMap<>();
        for (int i = 0; i < cantAnios; i++) {
            int anioInt = inputFechaInicio.getValue().getYear() + i;
            anios.add(String.valueOf(anioInt));
            JFXButton anioBtn;
            if (!detalleLTDataAnios.containsKey(anioInt) || detalleLTDataAnios.get(anioInt).getBtn() == null) {
                anioBtn = new JFXButton();
            } else {
                anioBtn = detalleLTDataAnios.get(anioInt).getBtn();
            }
            anioBtn.setTextFill(Paint.valueOf("#ffffff"));
            anioBtn.setPrefWidth(gridPaneAnios.getPrefWidth() / cantAnios);
            anioBtn.setPrefHeight(40);
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
                detalleAnio.setPaso(Text.PASO_NONE);
                detalleAnio.setBloqueTiempo(new BloqueTiempo());
                detalleLTDataAnios.put(anioInt, detalleAnio);

            } else if (detalleLTDataAnios.get(anioInt).getBtn() == null) {
                detalleLTDataAnios.get(anioInt).setBtn(anioBtn);
            }
            if(i == 0){
                detalleLTDataAnios.get(anioInt).bloqueTiempo.setCantidadPasos(calcularCantidadPasosAnioIni());
            }
            detalleLTDataAnios2.put(anioInt, detalleLTDataAnios.get(anioInt));

        }
        detalleLTDataAnios = detalleLTDataAnios2;
        inputCantPasos.setText(calcularCantidadPasos());
    }


    private void anioBtnLogic(JFXButton anioBtn, int anioInt, String anioStr) {
        // Crear un efecto DropShadow para resaltar el botón
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(javafx.scene.paint.Color.BLUE); // Color de la sombra
        dropShadow.setRadius(10); // Tamaño de la sombra
        anioBtn.setEffect(dropShadow);

       try {
            BloqueTiempo bloqueSeleccionado = detalleLTDataAnios.get(anioInt).bloqueTiempo;
            //Se muestra el bloque para el anio seleccionado
            PostesAnioController postesAnioController = new PostesAnioController( this, bloqueSeleccionado, anioInt);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PostesAnio.fxml"));
            loader.setController(postesAnioController);
            AnchorPane newLoadedPane = loader.load();
            gridPanePostesAnio.getChildren().clear();
            gridPanePostesAnio.getChildren().add(newLoadedPane);
            postesAnioController.unloadData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        verPosteAnio(true, Integer.parseInt(anioStr));

    }

    private void clearAnios() {
        gridPaneAnios.getChildren().clear();
        gridPaneAnios.getColumnConstraints().clear();
    }

    public void verPosteAnio(Boolean ver, int anio) {
        if(!ver) { detalleLTDataAnios.get(anio).getBtn().setEffect(null); }
        this.gridPanePostesAnio.setVisible(ver);
    }


    private void clearMeses() {
        selectedMes = null;
        anioLabel.setText("");
        gridPanePostesAnio.getChildren().clear();
        gridPanePostesAnio.getColumnConstraints().clear();
    }

    private DatosLineaTiempo obtenerLineaTiempoDeInterfaz(){
        DatosLineaTiempo datosLineaTiempo = new DatosLineaTiempo();

        datosLineaTiempo.setTiempoInicial(inputFechaInicio.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosLineaTiempo.setTiempoInicialEvoluciones(datosCorrida.getLineaTiempo().getTiempoInicialEvoluciones());
        datosLineaTiempo.setTiempoFinal(inputFechaFin.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59");

        BloqueTiempo bloqueAnterior = null;
        BloqueTiempo bloqueActual = null;
        int cantidadPasos = 0;

        List<Integer> clavesOrdenadas = new ArrayList<>(detalleLTDataAnios.keySet());
        Collections.sort(clavesOrdenadas);

        for (Integer anio : clavesOrdenadas) {

            bloqueActual = detalleLTDataAnios.get(anio).bloqueTiempo;
            if(bloqueActual == null){  return null; }
            if(bloqueActual.controlDatosCompletos().size() > 0)  {  return null; }
            int durp = bloqueActual.getDuracionPaso();
            int cantPasosBloque = calcularPasosBloqueAnio(durp,anio);
            boolean esSemanal = bloqueEsSemanal(bloqueActual);

            if(esSemanal ){
                if(bloqueAnterior != null){
                    cargarBloqueDiario(bloqueAnterior, datosLineaTiempo, cantidadPasos);
                }
                cargarBloquesSemanales(bloqueActual, datosLineaTiempo, anio);
                cantidadPasos = 0;
                bloqueAnterior = null;
            }
            else if( bloqueAnterior != null && !bloquesSonIguales(bloqueActual, bloqueAnterior)){
                cargarBloqueDiario(bloqueAnterior, datosLineaTiempo, cantidadPasos);
                cantidadPasos = cantPasosBloque;
                bloqueAnterior = bloqueActual;
            }else{
                bloqueAnterior = bloqueActual;
                cantidadPasos += cantPasosBloque;
            }
        }
        // cargo el ultimo bloque de la iteracion diaria
        if(!bloqueEsSemanal(bloqueActual)) {
            cargarBloqueDiario(bloqueActual, datosLineaTiempo, cantidadPasos);
        }
        return datosLineaTiempo;
    }

    private void cargarBloqueDiario(BloqueTiempo bloqueActual, DatosLineaTiempo datosLineaTiempo, Integer cantidadPasos) {
        datosLineaTiempo.agregarBloqueDatosInt(cantidadPasos, bloqueActual.getDuracionPaso(),
                3600, bloqueActual.getCantPostes(),  UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0,bloqueActual.isCronologico() );

    }

    private void cargarBloquesSemanales(BloqueTiempo bloqueActual, DatosLineaTiempo datosLineaTiempo, int anio) {
        int anioInicio = inputFechaInicio.getValue().getYear();
        int anioFin = inputFechaFin.getValue().getYear();

        boolean esBisiesto = (anio%4 == 0);
        int durPasoSemanaComplemento = (esBisiesto) ? 777600 : 691200;   // 9 y 8 dias
        int diasSemanaComplemento = (esBisiesto) ? 9 : 8;   // 9 y 8 dias
        double facorNoBiciesto = 8.0/7;
        double facorBiciesto = 9.0/7;

        double factorProrrateo = (esBisiesto) ? facorBiciesto: facorNoBiciesto;

        LocalDate inicioCorrida = getInputInicioCorrida();
        LocalDate finCorrida = getInputFinCorrida();
        ArrayList<Integer> duracionPostesUltimaSemana = prorratearDuracionPostes(bloqueActual.getDuracionPostes(), factorProrrateo, durPasoSemanaComplemento, bloqueActual.getIntervaloMuestreo());
        //   216 horas o 192 horas)  en segundos equivale a 777600 : 691200;

        //Si es semanal se puede necesitar agregar dos bloques, el del anio hasta semana 51 mas el compemento (semana 52)
        if(anio == anioInicio && anio == anioFin){
            LocalDate ultimoDiaDelAnio = inicioCorrida.withDayOfYear(365).plusDays(inicioCorrida.isLeapYear() ? 2 : 1) ;

            if(finCorrida.getDayOfYear() == 365 || finCorrida.getDayOfYear() == 366){
                int diasBloqueGde = (int) ChronoUnit.DAYS.between(inicioCorrida, ultimoDiaDelAnio) - diasSemanaComplemento;
                datosLineaTiempo.agregarBloqueDatosInt(diasBloqueGde/7, 604800,  3600,
                        bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );
                datosLineaTiempo.agregarBloqueDatosInt(1, durPasoSemanaComplemento, 3600,
                        bloqueActual.getCantPostes(), duracionPostesUltimaSemana, 0,bloqueActual.isCronologico() );
            }else{
                int diasBloqueGde = (int) ChronoUnit.DAYS.between(inicioCorrida, finCorrida);
                diasBloqueGde++;
                datosLineaTiempo.agregarBloqueDatosInt(diasBloqueGde/7, 604800,  3600,
                        bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );
            }
        }
        else if (anio == anioInicio) {
            LocalDate ultimoDiaDelAnio = inicioCorrida.withDayOfYear(365).plusDays(inicioCorrida.isLeapYear() ? 2 : 1) ;
            int diasBloqueGde = (int) ChronoUnit.DAYS.between(inicioCorrida, ultimoDiaDelAnio) - diasSemanaComplemento;

            datosLineaTiempo.agregarBloqueDatosInt(diasBloqueGde/7, 604800,  3600,
                    bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );
            datosLineaTiempo.agregarBloqueDatosInt(1, durPasoSemanaComplemento, 3600,
                    bloqueActual.getCantPostes(), duracionPostesUltimaSemana, 0,bloqueActual.isCronologico() );

        }else if (anio == anioFin){

            LocalDate primerDiaDelAnio = LocalDate.of(finCorrida.getYear(), 1, 1);
            int diasBloque = (int) ChronoUnit.DAYS.between(primerDiaDelAnio, finCorrida) + 1;
            if(diasBloque > 357){
                diasBloque = diasBloque - diasSemanaComplemento;
                datosLineaTiempo.agregarBloqueDatosInt(diasBloque/7, 604800,  3600,
                        bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );
                datosLineaTiempo.agregarBloqueDatosInt(1, durPasoSemanaComplemento, 3600,
                        bloqueActual.getCantPostes(), duracionPostesUltimaSemana, 0,bloqueActual.isCronologico() );
            }else{
                datosLineaTiempo.agregarBloqueDatosInt(diasBloque/7, 604800,  3600,
                        bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );
            }



        } else {
            datosLineaTiempo.agregarBloqueDatosInt(51, 604800,  3600,
                    bloqueActual.getCantPostes(),UtilArrays.dameAListI(bloqueActual.getDuracionPostes()), 0, bloqueActual.isCronologico() );

            datosLineaTiempo.agregarBloqueDatosInt(1, durPasoSemanaComplemento, 3600,
                    bloqueActual.getCantPostes(), duracionPostesUltimaSemana, 0,bloqueActual.isCronologico() );
        }


    }

    private ArrayList<Integer> prorratearDuracionPostes(int[] duracionPostes, double fatorProrrateo, int duracionPasoFinal,int intervaloMuestreo) {
        double[] res = UtilArrays.arrayIntProdNumero( duracionPostes,fatorProrrateo);
        int[] truncado = UtilArrays.truncarArray(res);
        for(int i = 0; i<truncado.length; i++){
            int valor = truncado[i];
            if(valor % intervaloMuestreo != 0){
                truncado[i] = valor / intervaloMuestreo * intervaloMuestreo;
            }
        }
        int duracionPaso = UtilArrays.sumaArrayInt(truncado);
        int dif = duracionPasoFinal - duracionPaso;
        int ajuste = truncado[truncado.length-1] + dif;
        truncado[truncado.length-1] = ajuste;
        ArrayList<Integer> ret = UtilArrays.dameAListI(truncado);
        return ret;
    }

    private int buscarMultiploMasCercano(int valor, int intervaloMuestreo) {
        int intervalosCompletos = valor / intervaloMuestreo;

        return  intervalosCompletos * intervaloMuestreo;

    }

    private boolean bloquesSonIguales(BloqueTiempo bloqueTiempo1, BloqueTiempo bloqueTiempo2) {
        boolean ret = true;
        ret = (bloqueTiempo1.getDuracionPaso() == bloqueTiempo2.getDuracionPaso())  ? ret: false;
        ret = (bloqueTiempo1.getCantPostes() == bloqueTiempo2.getCantPostes())  ? ret: false;
        ret = (bloqueTiempo1.isCronologico() == bloqueTiempo2.isCronologico())  ? ret: false;
        ret = (UtilArrays.sonIguales(bloqueTiempo1.getDuracionPostes(), bloqueTiempo2.getDuracionPostes()))  ? ret: false;

        return ret;
    }

    private boolean bloqueEsSemanal(BloqueTiempo bloqueTiempo) {
        boolean ret = false;
        if (UtilArrays.estaIntEnArray(Constantes.SEG_PASOS_SEMANALES, bloqueTiempo.getDuracionPaso())) {
            ret = true;
        }
        return ret;
    }


    /***
     Construye el hashMap:  detalleLTDataAnios que son todos los botones que representan años
     Construye el bloquesCorridaPorAnio  que son los bloques asignados a cada anio para describir la corrida
     Devuelve true si la linea del tiempo puede ser representada por NuevaLineaDeTiempoController
     DIARIO:24(1[2022]-365[2023])
     * @param datosLineaTiempo
     * @return
     */
    public boolean generarDatosLT(DatosLineaTiempo datosLineaTiempo) {

        LocalDateTime fechaIni = LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));
        LocalDateTime fechaFin = LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoFinal(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"));

        // Este hash es usado en forma local para iterar sobre la duracion del bloque
        HashMap<Integer,  Integer> daysOfYear = new HashMap<>();

        detalleLTDataAnios.clear();
        int totalDias = 0;
        // Creo el arbol de botones
        for (int i = fechaIni.getYear(); i <= fechaFin.getYear(); i++) {
            int diasAnio = LocalDate.of(i, 1, 1).isLeapYear() ? 366 : 365;
            totalDias += diasAnio;
            DetalleLTData detalleAnio = new DetalleLTData();
            detalleAnio.setBtn(new JFXButton());
            detalleAnio.setBloqueTiempo(new BloqueTiempo());
            detalleLTDataAnios.put(i, detalleAnio);
            daysOfYear.put(i, diasAnio);
            anios.add(String.valueOf(i));
        }


        int diasProcesados = 0;
        int anioActual = fechaIni.getYear();
        String pasoAnio = null;
        String pasoGeneral = null;
        int diasRestantes = 0;

        LocalDateTime fechaInicioBloque = fechaIni;
        LocalDateTime fechaFinBloque = null;

        // variables para definir luego el bloque Base (mayoritario de la corrida)
        int aniosConBloquesSemanales = 0;
        int aniosConBloquesDiarios = 0;
        BloqueTiempo diario = null;
        BloqueTiempo semanal = null;

        // Iteracion de los bloques de la corrida
        for (int i = 0; i < datosLineaTiempo.getCantBloques(); i++) {

            // Preparo el "bloqueAnio" que se va a asignar a cada anio  (va esto en lugar del detalle de los meses)
            Integer durPaso = datosLineaTiempo.getDuracionPasoPorBloque().get(i);
            Integer cantPasos = datosLineaTiempo.getPasosPorBloque().get(i);
            int diasBloque = durPaso * cantPasos / 86400;
            fechaFinBloque = fechaInicioBloque.plusDays(diasBloque-1);

            boolean esBloqueSemanal = UtilArrays.estaIntEnArray(Constantes.SEG_PASOS_SEMANALES, durPaso);
            // Controles Corrida simple o detallada
            // 1-Control si es bloque inicial
            if(i ==0 && esBloqueSemanal){
                    int diaAñoIni = fechaInicioBloque.getDayOfYear();
                    if (diaAñoIni % 7 != 1) {
                        parentController.setLineaDeTiempoDetalladaControllerSugerenciaCorreccion("Se sugiere cambiar fecha inicio para que coincida con el inicio de la semana.");
                        return false;     }
            }
            // 2-Control si es bloque final
            else if(i == datosLineaTiempo.getCantBloques()-1 && esBloqueSemanal){
                int diaAñoFin = fechaFinBloque.getDayOfYear();
                int año = fechaFinBloque.getYear();
                if(diaAñoFin > 357 ){
                    if(fechaFinBloque.getMonthValue() != 12 || fechaFinBloque.getDayOfMonth() != 31) {
                        parentController.setLineaDeTiempoDetalladaControllerSugerenciaCorreccion("Se sugiere cambiar fecha finalización para que coincida con el fin de la semana.");
                        return  false;   }
                } else {
                    if (diaAñoFin % 7 != 0) { return  false;   }
                }
            // 3-Control bloques intermedios
            } else if (i > 0 && i < datosLineaTiempo.getCantBloques()-1){
                if(esBloqueInermedioCorridaDetallada(fechaInicioBloque, fechaFinBloque, durPaso)){
                    return false;  }
            }

            pasoAnio = getPasoSegunDuracion(durPaso);

            //Para cada anio del bloque de la corrida le seteo este bloque
            for (int a = fechaInicioBloque.getYear(); a <= fechaFinBloque.getYear(); a++) {
                /////
                // if(detalleLTDataAnios.get(a).getBloqueTiempo() != null){ }  // Esto es para los bloques semanales si cargo el primero (51 sem) ya no cargue el segundo (1 sem)
                /////
                int pasosAnio = calcularPasosBloqueAnio(durPaso, a);

                BloqueTiempo b = new BloqueTiempo();
                b.setCantidadPasos(pasosAnio);
                b.setDuracionPaso(durPaso);
                b.setDuracionPostes(UtilArrays.dameArrayI(datosLineaTiempo.getDurPostesPorBloque().get(i)));
                b.setCantPostes(datosLineaTiempo.getDurPostesPorBloque().get(i).size());
                b.setCronologico(datosLineaTiempo.getCronologicos().get(i));
                b.setIntervaloMuestreo(datosLineaTiempo.getIntMuestreoPorBloque().get(i));

                if (pasoAnio.equalsIgnoreCase(Text.PASO_SEMANAL)) {
                    aniosConBloquesSemanales++;
                    semanal = (semanal == null) ? b: semanal;
                    b.setCantidadPasos(pasosAnio);
                } else if (pasoAnio.equalsIgnoreCase(Text.PASO_DIARIO)) {
                    aniosConBloquesDiarios++;
                    diario = (diario == null) ? b: diario;
                    b.setCantidadPasos(pasosAnio);
                }
                if(detalleLTDataAnios.get(a).getBloqueTiempo().getDuracionPostes()== null){
                    detalleLTDataAnios.get(a).setBloqueTiempo(b);
                    detalleLTDataAnios.get(a).setPaso(getPasoSegunDuracion(b.getDuracionPaso()));
                }
            }

            diasBloque += diasRestantes;
            diasProcesados += diasBloque;

            if (pasoAnio.equalsIgnoreCase(Text.PASO_HORARIO) || pasoAnio.equalsIgnoreCase(Text.PASO_DIARIO)) {
                while (diasBloque > daysOfYear.get(anioActual)){
                    diasBloque -= daysOfYear.get(anioActual);
                    anioActual++;
                }
                diasRestantes = diasBloque;
            } else {
                do {
                    diasBloque -= daysOfYear.get(anioActual);
                    if(diasBloque < 0){
                        diasBloque = 0;
                    }
                } while (daysOfYear.get(anioActual) != null && diasBloque >= daysOfYear.get(anioActual));
                if (diasBloque > 0) {
                    anioActual++;
                }
            }
            fechaInicioBloque = fechaFinBloque.plusDays(1);
        }

        //Asigno como bloque base el que es mayoria en la corrida
        if (aniosConBloquesSemanales > aniosConBloquesDiarios){

            bloqueBase = semanal;
        }else {
            bloqueBase = diario;
        }
        return true;
    }

    public int calcularPasosBloqueAnio(int pasoAnio, int anioBloque) {

        int retorno = 0;
        if(anioBloque == getInputInicioCorrida().getYear()){
            LocalDate inicioCorrida = getInputInicioCorrida();
            LocalDate finCorrida = getInputFinCorrida();
            LocalDate ultimoDiaDelAnio = inicioCorrida.withDayOfYear(365).plusDays(inicioCorrida.isLeapYear() ? 2 : 1);
            if(finCorrida.isBefore(ultimoDiaDelAnio)){
                retorno = (int) ChronoUnit.DAYS.between(inicioCorrida, finCorrida);
                retorno++; //agrego uno porque es hasta el dia fin inclusive
            }
            else{
                retorno = (int) ChronoUnit.DAYS.between(inicioCorrida, ultimoDiaDelAnio);
            }


        } else if (anioBloque == getInputFinCorrida().getYear()){
            LocalDate finCorrida = getInputFinCorrida();
            LocalDate primerDiaDelAnio = LocalDate.of(finCorrida.getYear(), 1, 1);
            retorno = (int) ChronoUnit.DAYS.between(primerDiaDelAnio, finCorrida) + 1;

        } else {
            retorno = (anioBloque % 4 == 0) ? 366 : 365;
        }

        if(pasoAnio == 604800 || pasoAnio == 691200 || pasoAnio == 777600){
            retorno = retorno/7;
        }
        return retorno;

    }


    /***
     * Devuelve en int el valor del paso (Diario o Semanal)
     */
    public int getPasoSegunString(String pasoGeneral) {
        int ret = 0;
        switch (pasoGeneral) {
            case Text.PASO_HORARIO:
                ret = 3600;
                break;
            case Text.PASO_DIARIO:
                ret = 86400;
                break;
            case Text.PASO_SEMANAL:
                ret = 604800;
                break;
            }
        return ret;
    }
    public void setDatosCorrida(DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
    }



    private void mostrarDatosBloqueBase(){

        if(bloqueBase != null) {
            for (int i = 0; i < bloqueBase.getCantPostes(); i++) {
                posteList.get(i).setText(String.valueOf(bloqueBase.getDuracionPostes()[i]/3600));
            }
            cambiarVisibilidadPostesBase(bloqueBase.getCantPostes());
            inputBloquesCronologicos.setSelected(datosCorrida.getLineaTiempo().getCronologicos().get(0));
            inputCantPostes.setText(String.valueOf(bloqueBase.getCantPostes()));
            inputIntervaloMuestreo.setText(String.valueOf(bloqueBase.getIntervaloMuestreo()/3600));
        }
        else {
            cambiarVisibilidadPostesBase(0);
        }
    }

    /***
     * Usando el Array de bloques de la corrida defino el bloque base según sea la mayoria (semanales o diarios)
     * @return
     */


    private void cambiarVisibilidadPostesBase(int cantPostes) {
        cantPostes--;
        for (int ind =0; ind< lbPosteList.size(); ind ++){
            if(ind <= cantPostes){
                posteList.get(ind).setVisible(true);
                lbPosteList.get(ind).setVisible(true);
            }else {
                posteList.get(ind).setVisible(false);
                lbPosteList.get(ind).setVisible(false);
            }
            posteList.get(ind).focusedProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue) {
                    horasPorPaso();
                }
            });
        }



    }

    private void eventoCalcularDuracionPostesBase() {
        for (int ind =0; ind< posteList.size(); ind ++){
            posteList.get(ind).focusedProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue) {
                    horasPorPaso();
                }
            });
        }
    }


    /***
     * Se usa solo para los bloque intermedios, para los bloques inicial y final no se deberia llamar a este metodo
     * @param fechaInicioBloque
     * @param fechaFinBloque
     * @param durPaso
     * @return
     */
    private boolean esBloqueInermedioCorridaDetallada(LocalDateTime fechaInicioBloque, LocalDateTime fechaFinBloque, int durPaso) {
        boolean ret = false;
        switch (durPaso) {
            case 86400:   /////   1 Dias
                int diaFin = (fechaFinBloque.getYear()%4 == 0 ) ? 366 : 365;
                if(!(fechaInicioBloque.getDayOfYear()==1 || fechaFinBloque.getDayOfYear()==diaFin)) {
                    ret = true;
                }
                break;case 604800:   /////   7 Dias
                if(!(fechaInicioBloque.getDayOfYear()==1 || fechaFinBloque.getDayOfYear()==356)) {
                    ret = true;
                }
                break;
            case 777600:   /////   9 Dias
                if(!(fechaFinBloque.getDayOfYear()==357 || fechaFinBloque.getDayOfYear()==366)) {
                    ret = true;
                }
                break;
            case 691200:   /////   8  Dias
                if(!(fechaFinBloque.getDayOfYear()==357 || fechaFinBloque.getDayOfYear()==365)) {
                    ret = true;
                }
                break;
        }
        return ret;
    }
    private int horasPorPaso(){
        int sum = 0;
        if(UtilStrings.esNumeroEntero(inputCantPostes.getText())) {
            int cantP = Integer.valueOf(inputCantPostes.getText());

            for (int ind = 0; ind < cantP; ind++) {
                String texto = posteList.get(ind).getText();
                if (UtilStrings.esNumeroEntero(texto)) {
                    sum += Integer.parseInt(texto);
                } else {
                    return 0;
                }
            }
            inputHorasPorPaso.setText(Integer.toString(sum));


        }else {
            setLabelMessageTemporal("La cantidad de Postes debe ser número entero", TipoInfo.ERROR);
        }
        return sum;
    }

    /**
     * Devuelve true si el inicio de corrida es correcto (no es necesario sugerir cambio cuando es semanal para linea simple)
     * @return
     */
    private  Boolean verificarInicioCorrida(){
        if(inputPasoBase.getValue().equals(Text.PASO_SEMANAL)) {
            LocalDate ini = inputFechaInicio.getValue();
            int diaAñoIni = ini.getDayOfYear();
            if (diaAñoIni % 7 != 1) {
                    //Sugerir fecha
                    LocalDate sugeridaIni = ini.minusDays((diaAñoIni % 7) - 1);
                    fechaSugeridaInicio = sugeridaIni;
                    mostrarMensajeConfirmacion("La fecha de inicio no inicia en la semana exacta. ¿Cambia por: " + sugeridaIni + " ?", 1);
            }

        }
        return true;
    }
    /**
     * Devuelve true si el fin de corrida es correcto (no es necesario sugerir cambio cuando es semanal para linea simple)
     * @return
     */
    private  Boolean verificarFinCorrida(){
        if(inputPasoBase.getValue().equals(Text.PASO_SEMANAL)) {
            LocalDate fin = inputFechaFin.getValue();
            int diaAñoFin = fin.getDayOfYear();
            int año = inputFechaFin.getValue().getYear();

            if(diaAñoFin > 357 ){
                if(!inputFechaFin.getValue().isEqual( LocalDate.of(año, 12, 31))) {
                    fechaSugeridaFin = LocalDate.of(año, 12, 31);
                    mostrarMensajeConfirmacion("La fecha fin no cierra en la semana exacta. ¿Cambia por: " + fechaSugeridaFin + " ?", 2);
                }

            } else {
                if (diaAñoFin % 7 != 0) {
                    fechaSugeridaFin = fin.plusDays((7 - (diaAñoFin % 7)));
                    mostrarMensajeConfirmacion("La fecha fin no cierra en la semana exacta. ¿Cambia por: " + fechaSugeridaFin + " ?", 2);
                }
            }
        }
        return true;
    }

    private int calcularHorasPasoBase(){
        int ret = 0;

        if(bloqueBase != null) {
            for (int i = 0; i < bloqueBase.getCantPostes(); i++) {
                int[] durPos = bloqueBase.getDuracionPostes();
                ret += durPos[i];
            }
            inputHorasPorPaso.setText(Integer.toString(ret/3600));
        }

        return ret;
    }

    private String calcularCantidadPasos(){
        int ret = 0;
        if(inputFechaInicio.getValue() != null && inputFechaFin.getValue() != null ) {
            LocalDate ini = inputFechaInicio.getValue();
            LocalDate fin = inputFechaFin.getValue();

            int diasTranscurridos = (int) DAYS.between(ini, fin) + 1;

            String paso = inputPasoBase.getValue();
            switch (paso) {
                case Text.PASO_SEMANAL:
                    ret = diasTranscurridos / 7;
                    break;
                case Text.PASO_DIARIO:
                    ret = diasTranscurridos;
                    break;
            }
        }

        //inputCantPasos.setText(calcularCantidadPasos());
        return Integer.toString(ret);
    }

    private int calcularCantidadPasosAnioIni(){

        int ret = 0;
        if(inputFechaInicio.getValue().getYear() == inputFechaFin.getValue().getYear()){
            ret = Integer.parseInt(calcularCantidadPasos());
        }
        else{
            if(inputFechaInicio.getValue() != null) {
                LocalDate ini = inputFechaInicio.getValue();
                LocalDate fin = ini.withDayOfYear(365).plusDays(ini.isLeapYear() ? 2 : 1) ;

                int diasTranscurridos = (int) DAYS.between(ini, fin) + 1;

                String paso = inputPasoBase.getValue();
                switch (paso) {
                    case Text.PASO_SEMANAL:
                        ret = diasTranscurridos / 7;
                        break;
                    case Text.PASO_DIARIO:
                        ret = diasTranscurridos;
                        break;
                }
            }
        }
        return ret;
    }

    public void actualizarBloqueAnio(BloqueTiempo b, int anio) {
        detalleLTDataAnios.get(anio).setBloqueTiempo(b);
    }

    public void actualizarLabelPasoAnio(int anio, String value) {
        detalleLTDataAnios.get(anio).setPaso(value);


    }

    /**
     * Método para cargar los datos en el DatosLineaTiempo
     */
    public boolean loadData() {
        boolean ret = false;
        if (datosCorrida.getNombre() != null) {
            DatosLineaTiempo lt = obtenerLineaTiempoDeInterfaz();
            if(lt != null) {
                datosCorrida.setLineaTiempo(lt);
                datosCorrida.setInicioCorrida(lt.getTiempoInicial());
                datosCorrida.setFinCorrida(lt.getTiempoFinal());
                ret = true;
            }
        }
        return ret;
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
                inputPasoBase.getSelectionModel().select(getPasoSegunDuracion(bloqueBase.getDuracionPaso()));
                initialRenderAnios();
            } else {
                parentController.setNuevaLineaDeTiempoActiva(false);
            }
            calcularHorasPasoBase();
            mostrarDatosBloqueBase();
            inputCantPasos.setText(calcularCantidadPasos());
        }
    }

    private String getPasoSegunDuracion(int durPaso) {
        String paso = "";
        switch (durPaso) {
            case 3600:
                paso = Text.PASO_HORARIO;
                break;
            case 3600 * 24: ///  1 Dia
                paso = Text.PASO_DIARIO;
                break;
            case 604800:   ///   7 Dias
                paso = Text.PASO_SEMANAL;
                break;
            case 777600:   ///   9 Dias
                paso = Text.PASO_SEMANAL;
                break;
            case 691200:   ///   8  Dias
                paso = Text.PASO_SEMANAL;
                break;
            default:
                break;
        }
        return paso;
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if (codigoMensaje==1){
            if(confirmacion){
                inputFechaInicio.setValue(fechaSugeridaInicio);
            }
        } else  if (codigoMensaje==2){
            if(confirmacion){
                inputFechaFin.setValue(fechaSugeridaFin);
            }
        }
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
        /*DatosLineaTiempo datosLineaTiempo = parseLineaDeTiempo();
        ArrayList<String> erroresLT = datosLineaTiempo.controlDatosCompletos();
        ret.addAll(erroresLT);
        if(ret.size() == 0) {
            if(datosLineaTiempo.getCantBloques() != datosLineaTiempo.getDurPostesPorBloque().size()){
                ret.add("No coincide la cantidad de bloques");
            }
        }*/
        return ret;
    }

    public void clean() {
    }

    private static class DetalleLTData {
        private String paso;
        private BloqueTiempo bloqueTiempo;
        private JFXButton btn;
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

        public BloqueTiempo getBloqueTiempo() { return bloqueTiempo; }

        public void setBloqueTiempo(BloqueTiempo bloqueTiempo) {
            this.bloqueTiempo = bloqueTiempo;
        }

        public JFXButton getBtn() {
            return btn;
        }

        public void setBtn(JFXButton btn) {
            this.btn = btn;
            btnSetText();
        }

        private void btnSetText() {

            if (paso != null && btn != null) {
                switch (paso) {
                    case Text.PASO_NONE:
                        btn.setText("?");
                        btn.setStyle("-fx-background-color: red; -fx-text-color: white;");
                        break;
                    case Text.PASO_SEMANAL:
                        btn.setText("S");
                        btn.setStyle("-fx-background-color: #27468d; -fx-text-color: white;");
                        break;
                    case Text.PASO_DIARIO:
                        btn.setText("D");
                        btn.setStyle("-fx-background-color: #e87c2a; -fx-text-color: white;");
                        break;
                    case Text.PASO_HORARIO:
                        btn.setText("H");
                        btn.setStyle("-fx-background-color: yellow; -fx-text-color: white;");
                        break;
                }
            }
        }
    }





}
