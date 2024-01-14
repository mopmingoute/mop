/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BloqueLineaDeTiempoController is part of MOP.
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
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BloqueLineaDeTiempoController extends GeneralidadesController{

    @FXML private TitledPane titledPane;
    @FXML private JFXButton closeButton;
    @FXML private JFXTextField inputCantPostes;
    @FXML private AnchorPane anchorPaneBloque;
    @FXML private GridPane gridPaneBloque;

    @FXML private JFXTextField inputCantPasos;
    @FXML private JFXTextField inputDuracionPaso;
    @FXML private JFXComboBox<String> inputDuracionPasoUnidad;
    @FXML private JFXTextField inputIntervaloMuestreo;
    @FXML private JFXComboBox<String> inputIntervaloMuestreoUnidad;
    @FXML private JFXTextField inputPeriodoBloque;
    @FXML private JFXRadioButton inputEsMonotono;
    @FXML private JFXRadioButton inputEsCronologico;
//    @FXML private JFXRadioButton inputPeriodoBloque;
    @FXML private JFXComboBox<String> inputDuracionPostesUnidad;


    private int cantPostes = 0;
    private Map<Integer, JFXTextField> durPostes = new HashMap<>();
    private boolean closable = true;
    private LineaDeTiempoDetalladaController padre;
    private ArrayList<Integer> localDuracionPostes;



    private int id;


    public BloqueLineaDeTiempoController(LineaDeTiempoDetalladaController padre, boolean closable, int id){
        this.closable = closable;
        this.padre = padre;
        this.id = id;
    }

    @FXML
    public void initialize() {

        inputDuracionPasoUnidad.getItems().add(Text.UNIDAD_HORAS);
        inputDuracionPasoUnidad.getSelectionModel().selectFirst();

        inputIntervaloMuestreoUnidad.getItems().add(Text.UNIDAD_HORAS);
        inputIntervaloMuestreoUnidad.getSelectionModel().selectFirst();

        inputDuracionPostesUnidad.getItems().add(Text.UNIDAD_HORAS);
        inputDuracionPostesUnidad.getSelectionModel().selectFirst();

        if(closable) {
            closeButton.setOnAction(actionEvent -> {
                ((Pane) titledPane.getParent()).getChildren().remove(titledPane);
                padre.removeBloqueLineaDeTiempo(id);
            });

        }else{
            closeButton.setVisible(false);
        }

        postesDinamicos();
    }

    private void postesDinamicos(){
        inputCantPostes.setOnAction(actionEvent -> {
            actualizarCanidadDePostes();
        });
        inputCantPostes.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if( UtilStrings.esNumeroEntero(inputCantPostes.getText())) {
                    actualizarCanidadDePostes();
                }
            }
        });

    }

    private void actualizarCanidadDePostes() {

        if(UtilStrings.esNumeroEntero(inputCantPostes.getText())) {
            Integer nuevaCantPostes = Integer.valueOf(inputCantPostes.getText());
            double cantRows = Math.ceil(nuevaCantPostes / 7.0);

            anchorPaneBloque.setPrefHeight(288 + 40 * (cantRows - 1));
            anchorPaneBloque.setMinHeight(288 + 40 * (cantRows - 1));

            //aumenta cantidad postes
            for (int i = cantPostes; i < nuevaCantPostes; i++) {

                int col = (i % 7) + 1;
                int row = (i / 7) + 7;

                JFXTextField newField = new JFXTextField();
                newField.setMaxWidth(65);
                newField.setPrefWidth(65);
                durPostes.put(10 * col + row, newField);
                gridPaneBloque.add(newField, col, row);
                GridPane.setMargin(newField, new Insets(5, 10, 5, 10));

                //Si habia valores de postes que puso y sacó, los vuelvo a poner
                if(localDuracionPostes != null && nuevaCantPostes <= localDuracionPostes.size()){
                    newField.setText(localDuracionPostes.get(i).toString());
                }
            }

            if(localDuracionPostes != null){
                for (int i = 0 ; i < cantPostes; i++) {
                    int col = (i % 7) + 1;
                    int row = (i / 7) + 7;

                    int valor = 0;
                    if(UtilStrings.esNumeroEntero(durPostes.get(10 * col + row).getText())){
                        valor = Integer.parseInt(durPostes.get(10 * col + row).getText());
                    }

                    if(i >= nuevaCantPostes){   //reduce cantidad postes
                        gridPaneBloque.getChildren().remove(durPostes.get(10 * col + row));
                    }

                    if( localDuracionPostes.size() <= i){
                        localDuracionPostes.add(valor);
                    }else  {
                        localDuracionPostes.set(i, valor);
                    }
                }
            }

            cantPostes = nuevaCantPostes;
            if (cantPostes < 7) {
                GridPane.setColumnIndex(inputDuracionPostesUnidad, cantPostes + 1);
            } else {
                GridPane.setColumnIndex(inputDuracionPostesUnidad, 8);
            }
        }else {
            setLabelMessageTemporal("La cantidad de postes debe ser dato numérico", TipoInfo.FEEDBACK);
        }


    }

    public String getCantPasos() {
        return inputCantPasos.getText();
    }

    public void setCantPasos(String cantPasos){
        inputCantPasos.setText(cantPasos);
    }

    public String getDuracionPaso(){
        //TODO: chequear input unidad y convertir acorde   (la unidad en el xml es segundos y en la interfaz es hora con combobox que tiene este unico valor)
        return String.valueOf(Integer.parseInt(inputDuracionPaso.getText())*3600);
    }

    public void setDuracionPaso(String durPaso){
        //TODO: chequear input unidad y convertir acorde
        inputDuracionPaso.setText(durPaso);
    }

    public String getIntervaloMuestreo(){
        //TODO: chequear input unidad y convertir acorde
        return String.valueOf(Integer.parseInt(inputIntervaloMuestreo.getText())*3600);
    }

    public void setIntevaloMuestreo(String intervaloMuestreo){
        //TODO: chequear input unidad y convertir acorde
        inputIntervaloMuestreo.setText(intervaloMuestreo);
    }

    public String getPeriodoBloque(){
        return inputPeriodoBloque.getText();
    }

    public void setPeriodoBloque(String periodoBloque) {
        inputPeriodoBloque.setText(periodoBloque);
    }

    public String getCronologico(){
        return String.valueOf(inputEsCronologico.isPressed());
    }

    public void setCronologico(boolean cronologico){
        inputEsCronologico.setSelected(cronologico);
        inputEsMonotono.setSelected(!cronologico);
    }

    public String getCantPostes(){
        return inputCantPostes.getText();
    }

    public void setDuracionPostes(ArrayList<Integer> duracionPostes){
        ArrayList<Integer> dur = new ArrayList<>();
        inputCantPostes.setText(String.valueOf(duracionPostes.size()));
        inputCantPostes.fireEvent(new ActionEvent());
        for(int i=0;i<duracionPostes.size();i++){
            int col = (i % 7) + 1;
            int row = (i / 7) + 7;
            durPostes.get(10*col+row).setText(String.valueOf(duracionPostes.get(i)/3600));
            dur.add(duracionPostes.get(i)/3600);

        }
        localDuracionPostes = dur;
    }

    public ArrayList<Integer> getDuracionPostes(){
        int cantidadPostes = Integer.parseInt(inputCantPostes.getText());
        ArrayList<Integer> res = new ArrayList<>();

        for(int i=0;i<cantidadPostes ;i++){
            int col = (i % 7) + 1;
            int row = (i / 7) + 7;
            res.add((Integer.parseInt(durPostes.get(10*col+row).getText()))* 3600);

        }

        return res;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private ArrayList<String> controlDuracionPostes(){
        ArrayList<String> bloquesControlErroneo = new ArrayList<>();

        int suma = 0;
        try {
            int duracionPaso = Integer.parseInt(inputDuracionPaso.getText());
            String bloque = "";
            int intervaloMuestreo = Integer.parseInt(inputIntervaloMuestreo.getText());
            int duracionPoste = 0;
            if(cantPostes > 0 ) {
                for(int i=0;i<cantPostes ;i++){
                    int col = (i % 7) + 1;
                    int row = (i / 7) + 7;
                    duracionPoste = (Integer.parseInt(durPostes.get(10*col+row).getText()));
                    if (duracionPoste % intervaloMuestreo > 0) {
                        bloquesControlErroneo.add( "Poste no es multiplo de I. M. en bloque: " + id );
                        break;
                    }
                    suma += duracionPoste;
                }
            }else {
                bloquesControlErroneo.add("Cantidad Postes  bloque: "+ id +"no debe ser 0");
            }
            if(suma != duracionPaso){
                bloquesControlErroneo.add("Error: duracionPaso diferente a suma postes");
            }

        }catch (Exception e){
            //e.printStackTrace();
            bloquesControlErroneo.add("Error en bloque: " + id );
        }


       return bloquesControlErroneo;
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> bloquesControlErroneo = controlDuracionPostes();
        if( inputCantPasos.getText().trim().equalsIgnoreCase("") )bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Cantidad de Pasos vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantPasos.getText().trim()))bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Cantidad de Pasos no es entero.");
        if( inputPeriodoBloque.getText().trim().equalsIgnoreCase("") )bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Período Bloque vacío.");
        if( !UtilStrings.esNumeroEntero(inputPeriodoBloque.getText().trim()))bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Período Bloque no es entero.");
        if( inputCantPostes.getText().trim().equalsIgnoreCase("") )bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Cantidad de Postes vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantPostes.getText().trim()))bloquesControlErroneo.add("Linea Tiempo. Bloque: " + id + ". Cantidad de Postes no es entero.");
        return bloquesControlErroneo;
    }
    public int duracionBloqueEnSegundos(){
        int ret = -1;
        if(UtilStrings.esNumeroEntero(getCantPasos())){
            int cantPasos = Integer.parseInt(getCantPasos());
            if(UtilStrings.esNumeroEntero(getDuracionPaso())){
                int durPaso =  Integer.parseInt(getDuracionPaso());
                ret = durPaso * cantPasos;
            }else {
                setLabelMessageTemporal("La duracion del paso debe ser dato numérico.", TipoInfo.ERROR);
            }
        } else {
            setLabelMessageTemporal("La cantidad de pasos debe ser dato numérico.", TipoInfo.ERROR);
        }
        return ret;
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}
