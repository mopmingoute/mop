/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PostesAnioController is part of MOP.
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
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tiempo.BloqueTiempo;
import utilitarios.UtilStrings;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class PostesAnioController extends GeneralidadesController {


    LineaDeTiempoSimpleController padre;

    BloqueTiempo bloqueTiempo;

    int anioBloque;

    @FXML
    private JFXComboBox<String> inputPasoAnio;
    @FXML
    private JFXTextField inputCantPostes;

    @FXML
    private JFXTextField inputHorasPorPaso;

    @FXML
    private JFXButton btnGuardar;
    @FXML
    private JFXButton btnCancelar;

    @FXML
    private JFXCheckBox inputBloquesCronologicos;

    @FXML
    private Label lbAnio;

    private Boolean seModifico;


    @FXML
    private JFXTextField poste1, poste2, poste3, poste4,poste5,poste6,poste7,poste8, poste9,poste10,poste11,poste12,
            poste13,poste14,poste15,poste16,poste17,poste18, poste19,poste20,poste21,poste22,poste23,poste24;



    private ArrayList<JFXTextField> posteList;

    @FXML
    private Label labelP1,labelP2,labelP3,labelP4,labelP5,labelP6,labelP7,labelP8,labelP9,labelP10,labelP11, labelP12,
            labelP13,labelP14,labelP15,labelP16,labelP17,labelP18,labelP19,labelP20,labelP21,labelP22,labelP23,labelP24;

    private ArrayList<Label> lbPosteList;

    @FXML
    private JFXTextField inputIntervaloMuestreo;

    public PostesAnioController (LineaDeTiempoSimpleController padre, BloqueTiempo bloqueTiempo, int anio) {
        this.padre = padre;
        this.bloqueTiempo = bloqueTiempo;
        this.anioBloque = anio;
    }

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

        btnGuardar.setOnAction( action -> {
            if(loadData()) {
                padre.actualizarLabelPasoAnio(anioBloque, inputPasoAnio.getValue());
                padre.verPosteAnio(false, anioBloque);
            } else {
                mostrarMensajeConfirmacion("Duración de postes no válida, ¿desea cerrar sin guardar?", 1);
            }
        });
        btnCancelar.setOnAction( action -> {
            padre.verPosteAnio(false, anioBloque);
        });

        inputPasoAnio.getItems().addAll(Text.PASOS_BASE);
        inputPasoAnio.getSelectionModel().select(Text.PASO_NONE);
        inputPasoAnio.fireEvent(new ActionEvent());

        inputCantPostes.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if( UtilStrings.esNumeroEntero(inputCantPostes.getText().trim())) {
                    int c = Integer.parseInt(inputCantPostes.getText().trim());
                    cambiarVisibilidadPostesBase(c);
                    horasPorPaso();
                }
                else {
                    setLabelMessageTemporal("La cantidad de postes no es dato numérico.", TipoInfo.ERROR);
                }
            }
        });
        lbAnio.setText(String.valueOf(anioBloque));
        horasPorPaso();
    }
    private void cambiarVisibilidadPostesBase(int cantPostes) {
        cantPostes--;
        for (int ind =0; ind< 24; ind ++){
            if(ind <= cantPostes){
                posteList.get(ind).setVisible(true);
                lbPosteList.get(ind).setVisible(true);
            }else {
                posteList.get(ind).setVisible(false);
                lbPosteList.get(ind).setVisible(false);
            }
        }
        horasPorPaso();


    }


    private int duracionPaso(String paso){
        int ret = 0;
        switch (paso){
            case Text.PASO_SEMANAL:
                ret =  24*7*3600;
                break;
            case Text.PASO_DIARIO:
                ret = 24*3600;
                break;
        }
        return ret;
    }
    private int horasPorPaso (){
        int sum = 0;
        if(UtilStrings.esNumeroEntero(inputCantPostes.getText().trim())) {
            int cantidadPostes = Integer.parseInt(inputCantPostes.getText().trim());
            for (int ind = 0; ind < cantidadPostes; ind++) {
                String texto = posteList.get(ind).getText();
                if (UtilStrings.esNumeroEntero(texto)) {
                    sum += Integer.parseInt(texto);
                }
            }
            inputHorasPorPaso.setText(String.valueOf(sum));
        }
        return sum;
    }

    private String seleccionarPaso(int durPaso){
        String pasoAnio = "";
        switch (durPaso) {
            case 3600:
                pasoAnio = Text.PASO_HORARIO;
                break;
            case 3600 * 24:
                pasoAnio = Text.PASO_DIARIO;
                break;
            case 604800:   ///   7 Dias
                pasoAnio = Text.PASO_SEMANAL;
                break;
            case 777600:   ///   9 Dias
                pasoAnio = Text.PASO_SEMANAL;
                break;
            case 691200:   ///   8  Dias
                pasoAnio = Text.PASO_SEMANAL;
                break;
            default:
                return pasoAnio;
        }
        return pasoAnio;
    }

    private int[] duracionPostes(int cantP) {
        int[] ret = new int[cantP];
        int sum = 0;
        for (int ind =0; ind< cantP; ind ++){
            if(UtilStrings.esNumeroEntero(posteList.get(ind).getText().trim())) {
                ret[ind] = Integer.parseInt(posteList.get(ind).getText().trim()) * 3600;
                sum +=  ret[ind];
            }else {
                setLabelMessageTemporal("Hay postes que no tienen como duración un dato numérico", TipoInfo.ERROR);
                ret = null;
            }


        }
        if(sum != padre.getPasoSegunString(inputPasoAnio.getValue())){
            setLabelMessageTemporal("La duración de los postes no coincide con la duración del paso", TipoInfo.ERROR);
            return null;
        }else {
            inputHorasPorPaso.setText(String.valueOf(sum/3600));
        }
        return ret;
    }
    public boolean loadData() {
        boolean ret = false;
        BloqueTiempo b = new BloqueTiempo();

        int durPaso = duracionPaso(inputPasoAnio.getValue());
        b.setDuracionPaso(durPaso);
        b.setCronologico(inputBloquesCronologicos.isSelected());
        if(!UtilStrings.esNumeroEntero(inputIntervaloMuestreo.getText().trim())){
            setLabelMessageTemporal("El intervalo de muestreo no es Dato numérico", TipoInfo.ERROR);
            ret = false;
        } else if(UtilStrings.esNumeroEntero(inputCantPostes.getText().trim())){
            int cantPostes = Integer.parseInt(inputCantPostes.getText().trim());
            int intMuest = Integer.parseInt(inputIntervaloMuestreo.getText().trim()) * 3600;
            b.setCantPostes(cantPostes);
            int [] durPos = duracionPostes(cantPostes);
            if(durPos != null) {
                if(!padre.controlDurPostIntervaloMuestreo(durPos, intMuest)){
                    setLabelMessageTemporal("La duración de algún poste no es múltiplo del intervalo de muestreo", TipoInfo.ERROR);
                } else {
                    b.setDuracionPostes(durPos);
                    b.setIntervaloMuestreo(intMuest);
                    padre.actualizarBloqueAnio(b, anioBloque);
                    int cantidadPasos = padre.calcularPasosBloqueAnio( durPaso, anioBloque);
                    b.setCantidadPasos(cantidadPasos);
                    ret = true;
                }


            }
        }else {
            setLabelMessageTemporal("La cantidad de postes no es Dato numérico", TipoInfo.ERROR);
        }
        return ret;
    }



    public void unloadData(){
        if(bloqueTiempo != null){
            inputPasoAnio.getSelectionModel().select(seleccionarPaso(bloqueTiempo.getDuracionPaso()));  // Falta pasar a la etiqueta
            inputCantPostes.setText(String.valueOf(bloqueTiempo.getCantPostes()));
            inputBloquesCronologicos.setSelected(bloqueTiempo.isCronologico());
            inputIntervaloMuestreo.setText(String.valueOf(bloqueTiempo.getIntervaloMuestreo()/3600));
            for (int i = 0; i < bloqueTiempo.getCantPostes(); i++) {
                posteList.get(i).setText(String.valueOf(bloqueTiempo.getDuracionPostes()[i]/3600));
                posteList.get(i).focusedProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue) {
                        horasPorPaso();
                    }
                });
            }
            cambiarVisibilidadPostesBase(bloqueTiempo.getCantPostes());

        }else {
            cambiarVisibilidadPostesBase(0);
        }
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 1){
            if(confirmacion) {
                padre.verPosteAnio(false, anioBloque);
            }
        }
    }


    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> ret = new ArrayList<>();

        if(inputPasoAnio.getValue().equalsIgnoreCase(Text.PASO_NONE)){
            ret.add("No eligió paso");
        }

        if(UtilStrings.esNumeroEntero(inputIntervaloMuestreo.getText().trim())){
            ret.add("Intervalo muestreo erronea");

        }
        if(UtilStrings.esNumeroEntero(inputCantPostes.getText().trim())){
            ret.add("Cantidad de postes erronea");

        }else {
            if(posteList != null){
                int cantP = Integer.parseInt(inputCantPostes.getText().trim());
                for(int i = 0; i < cantP; i++){
                    if(UtilStrings.esNumeroEntero(posteList.get(i).getText())) {
                        ret.add("Duración postes erronea");
                    }
                }
            }
        }

        return ret;
    }
}
