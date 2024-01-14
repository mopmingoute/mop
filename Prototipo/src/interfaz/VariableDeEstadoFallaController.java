/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VariableDeEstadoController is part of MOP.
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
import datatypes.DatosDiscretizacion;
import datatypes.DatosVariableEstado;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.HashMap;

public class VariableDeEstadoFallaController {

    @FXML private Separator lowerSeparator;
    @FXML private Label labelNombre;
    @FXML private JFXTextField inputNombre;
    @FXML private JFXTextField inputEstadoInicial;
    @FXML private JFXTextField inputFrecuenciaControlPerMinDesp;
    @FXML private Label labelEstadoInicial;

    @FXML private JFXTextField inputCantEscProgram;


    private String label;
    private String nombre;
    private Integer padding;
    private String unidadEstadoInicial;

    private int cantEscProgram = 0;
    @FXML private GridPane gridPane;

    private DatosVariableEstado datosVariableEstado;
    private boolean edicion;
    private boolean editoVariables;



    private int frecuenciaControlPerminDesp;



    public VariableDeEstadoFallaController(String label, String nombre, Integer padding, String unidadEstadoInicial){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        edicion = false;
        this.unidadEstadoInicial = unidadEstadoInicial;
    }

    public VariableDeEstadoFallaController(String label, String nombre, Integer padding, DatosVariableEstado datosVariableEstado, int cantEscProgram, int frecuenciaConrolPerMinDesp, String unidadEstadoInicial){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        System.out.println("-->"+datosVariableEstado);
        this.datosVariableEstado = datosVariableEstado;
        edicion = true;
        this.unidadEstadoInicial = unidadEstadoInicial;
        this.cantEscProgram = cantEscProgram;
        frecuenciaControlPerminDesp = frecuenciaConrolPerMinDesp;
    }

    public int getCantEscProgram() {
        return cantEscProgram;
    }

    public void setCantEscProgram(int cantEscProgram) {
        this.cantEscProgram = cantEscProgram;
    }

    public int getFrecuenciaControlPerminDesp() {   return frecuenciaControlPerminDesp;   }

    public void setFrecuenciaControlPerminDesp(int frecuenciaControlPerminDesp) {      this.frecuenciaControlPerminDesp = frecuenciaControlPerminDesp; }


    @FXML
    public void initialize(){
        labelNombre.setText(label);
        lowerSeparator.setPadding(new Insets(0,padding,0,0));

        /*inputCantEscProgram.setOnAction(actionEvent -> {
            durMinForzamientosRenderInputs(cantEscProgram, Integer.parseInt(inputCantEscProgram.getText()));

        });*/

        inputCantEscProgram.setText(String.valueOf(cantEscProgram));

        if(edicion){
            unloadData();
        }




        //Control de cambios:


        inputEstadoInicial.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantEscProgram.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });



        editoVariables = false;

    }
   /* public int[] obtenerDurMinForzSeg() {
        int [] retorno = new int[cantEscProgram];

        for(int i=1;i<=cantEscProgram;i++){
            int row = (i / 6) + 5;
            int col = (i % 6) + row-4;
            if(UtilStrings.esNumeroEntero((inputsDurMinForzamientos.get(col*10+row)).getText())){
                retorno[i-1] = Integer.parseInt((inputsDurMinForzamientos.get(col*10+row)).getText());
//            durMinForzDia[i] = Integer.parseInt(((JFXTextField)inputsDurMinForzamientos.get(i)).getText());
            }

        }
        return retorno;
    }*/

/*    private void durMinForzamientosRenderInputs(int cantEscProg, int cantInputs){
        // validación para que no sean más que 10, se puede sacar después de agregar la validación al campo
        if(cantInputs > 10){
            cantInputs = 10;
        }
        for(int i=cantEscProg+1;i<=cantInputs;i++){
            JFXTextField input = new JFXTextField();
            input.setMinWidth(85);
            input.setPrefWidth(85);
            input.setMaxWidth(85);
            int row = (i / 6) + 5;
            int col = (i % 6) + row-4;
            gridPane.add(input, col, row);
            GridPane.setMargin(input, new Insets(0,0,0,5));
            inputsDurMinForzamientos.put(col*10+row, input);
        }
        for(int i=cantEscProg;i>cantInputs;i--){
            int row = (i / 6) + 5;
            int col = (i % 6) + row-4;
            gridPane.getChildren().remove(inputsDurMinForzamientos.get(col*10+row));
            inputsDurMinForzamientos.remove(col*10+row);
        }
        int colUnidad = 6;
        if(cantInputs < 5){
            colUnidad = cantInputs + 1;
        }
        //GridPane.setColumnIndex(inputDurMinForzamientosUnidad, colUnidad);
        cantEscProgram = cantInputs;
    }*/
    public DatosVariableEstado getDatosVariableEstado(){
        loadData();
        return datosVariableEstado;
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

    public void setEditoVariables(boolean editoVariables) {
        this.editoVariables = editoVariables;
    }

    /**
     *  Método para cargar los datos en el DatosVariableEstado
     */
    private void loadData(){
        System.out.println("LOAD DATA-VE");
//        String nombre = inputNombre.getText();
        double estadoInicial = 0;
        if(UtilStrings.esNumeroDouble(inputEstadoInicial.getText())) { estadoInicial = Double.parseDouble(inputEstadoInicial.getText()); }

        Evolucion<Double> valorRecursoInferior = new EvolucionConstante<>(null, null);
        Evolucion<Double> valorRecursoSuperior = new EvolucionConstante<>(null, null);

        if(UtilStrings.esNumeroEntero(inputCantEscProgram.getText())) { cantEscProgram =  Integer.parseInt(inputCantEscProgram.getText()); }

        int cantEscProgram = 0;
        int[] durMinForzDia = new int[cantEscProgram];
//        for(int i=0;i<cantEscProgram;i++){
        for(int i=1;i<=cantEscProgram;i++){
            int row = (i / 6) + 5;
            int col = (i % 6) + row-4;
            /*if(UtilStrings.esNumeroEntero((inputsDurMinForzamientos.get(col*10+row)).getText())){
                durMinForzDia[i-1] = Integer.parseInt((inputsDurMinForzamientos.get(col*10+row)).getText());
//            durMinForzDia[i] = Integer.parseInt(((JFXTextField)inputsDurMinForzamientos.get(i)).getText());
            }*/

        }
        int[] durMinForzSeg = new int[cantEscProgram];
        for(int i =0; i<durMinForzDia.length; i++){
            durMinForzSeg[i] = durMinForzDia[i] * Constantes.SEGUNDOSXDIA;
        }
        if(edicion){
            datosVariableEstado.setNombre(nombre);
            datosVariableEstado.setEstadoInicial(estadoInicial);

        }else {
            datosVariableEstado = new DatosVariableEstado(nombre, estadoInicial, Text.UNIDAD_PERIODOS, null, valorRecursoInferior,
                                                          valorRecursoSuperior, false, false,
                    false, false, false);
        }
    }

    /**
     * Método para obtener los datos del DatosVariableEstado y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        System.out.println("UNLOAD DATA-VE");
        System.out.println(datosVariableEstado);
        System.out.println("nombreVA->"+datosVariableEstado.getNombre());
//        inputNombre.setText(datosVariableEstado.getNombre());
        inputEstadoInicial.setText(datosVariableEstado.getEstadoInicial().toString());

       inputCantEscProgram.setText(String.valueOf(cantEscProgram));
       inputFrecuenciaControlPerMinDesp.setText(String.valueOf(frecuenciaControlPerminDesp));
//        for(int i=0;i<datosFallaCorrida.getCantEscProgram();i++){
        /*for(int i=1;i<=cantEscProgram;i++){
            int row = (i / 6) + 5;
            int col = (i % 6) + row-4;
            inputsDurMinForzamientos.get(col*10+row).setText(String.valueOf(durMinForzamientos[i-1]/Constantes.SEGUNDOSXDIA));
//            inputsDurMinForzamientos.get(i).setText(String.valueOf(datosFallaCorrida.getDurMinForzSeg()[i]/Constantes.SEGUNDOSXDIA));
            inputsDurMinForzamientos.get(col*10+row).textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        }*/

    }


    public ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputEstadoInicial.getText().trim().equalsIgnoreCase("") ) { errores.add("VA: "+ nombre + " Estado Inicial vacío."); }
        if( !UtilStrings.esNumeroDouble(inputEstadoInicial.getText()) ) { errores.add("VA: "+ nombre + " no es dato numérico."); }
        else if(Double.parseDouble( inputEstadoInicial.getText()) > Integer.parseInt( inputCantEscProgram.getText())){
            errores.add("VA: "+ nombre + "El Estado inicial debe ser menor o igual a la cantidad de escalones programables.");
        }
        if(inputCantEscProgram.getText().trim().equals("")) { errores.add( "Editor Falla.  "+ inputNombre.getText() +" Cantidad escalones programados vacío"); }
        if(!UtilStrings.esNumeroEntero(inputCantEscProgram.getText())) errores.add("Editor Falla - TG: " + inputNombre.getText() + "  Cantidad escalones programados no es dato numérico.") ;


        return errores;
    }
}
