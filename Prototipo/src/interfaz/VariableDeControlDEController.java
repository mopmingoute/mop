/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableDeControlDEController is part of MOP.
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
import com.jfoenix.controls.JFXTextField;
import datatypes.DatosVariableControlDE;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class VariableDeControlDEController {
    @FXML private Label labelNombre;
    @FXML private JFXTextField inputNombre;
    @FXML private JFXTextField inputPeriodo;
    @FXML private JFXComboBox<String> inputPeriodoUnidad;
    private HashMap<Integer, JFXTextField> inputsCostoDeControl = new HashMap<>();
    @FXML private JFXComboBox<String> inputCostoControlUnidad;
    @FXML private GridPane gridPane;
    @FXML private JFXButton inputCostoDeControlEV;

    private String label;
    private String nombre;
    private Integer cantEscProgram;
    private Integer cantCostoDeControlInputs = 0;

    private DatosVariableControlDE datosVariableControlDE;
    private boolean edicion;



    private boolean editoVariables;

    public VariableDeControlDEController(String label, String nombre, Integer cantEscProgram) {
        this.label = label;
        this.nombre = nombre;
        this.cantEscProgram = cantEscProgram;
    }

    public VariableDeControlDEController(String label, String nombre, DatosVariableControlDE datosVariableControlDE, Integer cantEscProgram) {
        this.label = label;
        this.nombre = nombre;
        this.datosVariableControlDE = datosVariableControlDE;
        edicion = true;
        this.cantEscProgram = cantEscProgram;
    }



    public void initialize(){
        labelNombre.setText(label);

        inputPeriodoUnidad.getItems().add(Text.UNIDAD_HORAS);
        inputPeriodoUnidad.getSelectionModel().selectFirst();

        inputCostoControlUnidad.getItems().add(Text.UNIDAD_USD);
        inputCostoControlUnidad.getSelectionModel().selectFirst();

        renderCostoDeControlInputs(cantEscProgram);
        if(edicion){
            unloadData();
        }

        editoVariables = false;
    }

    public DatosVariableControlDE getDatosVariableControlDE(){
        loadData();
        return datosVariableControlDE;
    }

     public boolean isEditoVariables() {
        //todo:  FALTA AGREGAR LOS BOTONES DE LAS EVOLUCIONES, ESTAN EN LA INTERFAZ PERO NO TIENEN COMPORTAMIETNO, SE DEJAN ??
        /*for (String clave : evsPorNombre.keySet()) {
            if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }*/
        return editoVariables;
    }
    public void setEditoVariables(boolean editoVariables) {     this.editoVariables = editoVariables;    }

    public void renderCostoDeControlInputs(Integer newCantInputs){
        // validación para que no sean más que 5, se puede sacar después de agregar la validación al campo
        if(newCantInputs > 5){
            newCantInputs = 5;
        }
        for(int i=cantCostoDeControlInputs;i<newCantInputs;i++){ 
            JFXTextField input = new JFXTextField();
            input.textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            input.setMinWidth(85);
            input.setPrefWidth(85);
            input.setMaxWidth(85);
            gridPane.add(input, 1+i, 3);
            inputsCostoDeControl.put(1+i, input);
        }
        for(int i=cantCostoDeControlInputs;i>newCantInputs;i--){
            System.out.println("i: "+i);
            gridPane.getChildren().remove(inputsCostoDeControl.get(i));
            inputsCostoDeControl.remove(i);
        }
        GridPane.setColumnIndex(inputCostoControlUnidad, newCantInputs+1);
        GridPane.setColumnIndex(inputCostoDeControlEV, newCantInputs+2);
        cantCostoDeControlInputs = newCantInputs;
    }

    /**
     *  Método para cargar los datos en el DatosVariableControlDE
     */
    private void loadData(){
        System.out.println("LOAD DATA-VCDE");
//        String nombre = inputNombre.getText();
        int periodo = 0;
        if(UtilStrings.esNumeroEntero(inputPeriodo.getText())) { Integer.parseInt(inputPeriodo.getText()); }
//        inputPeriodoUnidad
        Double[] costoDeControlValores = new Double[inputsCostoDeControl.size()];
        for(int i=0;i<inputsCostoDeControl.size();i++){
            if(UtilStrings.esNumeroDouble(inputsCostoDeControl.get(i+1).getText())){
                costoDeControlValores[i] = Double.parseDouble(inputsCostoDeControl.get(i+1).getText());
            }

        }
        Evolucion<Double[]> costoDeControl = new EvolucionConstante<>(costoDeControlValores, new SentidoTiempo(1));
//        costo de control unidad

        if(!edicion){
            datosVariableControlDE = new DatosVariableControlDE();
        }
        datosVariableControlDE.setNombre(nombre);
        datosVariableControlDE.setPeriodo(periodo);
        datosVariableControlDE.setCostoDeControl(costoDeControl);
    }

    /**
     * Método para obtener los datos del DatosVariableControlDE y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-VCDE");
//        inputNombre.setText(datosVariableControlDE.getNombre());
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        inputPeriodo.setText(String.valueOf(datosVariableControlDE.getPeriodo()));
        System.out.println("%%%"+datosVariableControlDE.getCostoDeControl().getValor(instanteActual));
//        for(Double x : datosVariableControlDE.getCostoDeControl().getValor()){
//            System.out.println(x);
//        }
//        System.out.println("/////"+datosVariableControlDE.getCostoDeControl().getValor().getClass());
        for(int i=0;i<datosVariableControlDE.getCostoDeControl().getValor(instanteActual).length;i++){
            if(datosVariableControlDE.getCostoDeControl() != null) {
                inputsCostoDeControl.get(i + 1).setText(String.valueOf(datosVariableControlDE.getCostoDeControl().getValor(instanteActual)[i]));
            }
        }
    }

    public ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        if(inputPeriodo.getText().trim().equals("")) { errores.add( "Editor Variable de Control:  Período vacío"); }
        if(!UtilStrings.esNumeroEntero(inputPeriodo.getText())) { errores.add("Editor Variable de Control:  Período no es dato numérico.") ; }

        for(int i=0;i<inputsCostoDeControl.size();i++){
            if(! UtilStrings.esNumeroDouble(inputsCostoDeControl.get(i+1).getText())) { errores.add("Editor Variable de Control: Costo de control no es dato numérico.") ;}
        }

        return errores;

    }
}
