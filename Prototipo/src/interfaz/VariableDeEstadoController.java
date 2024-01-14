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
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosDiscretizacion;
import datatypes.DatosHidraulicoCorrida;
import datatypes.DatosVariableEstado;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.UtilStrings;

import java.util.ArrayList;

public class VariableDeEstadoController {

    @FXML private Separator lowerSeparator;
    @FXML private Label labelNombre;
    @FXML private JFXTextField inputNombre;
    @FXML private JFXTextField inputEstadoInicial;
    @FXML private JFXComboBox<String> inputEstadoInicialUnidad;
    @FXML private JFXButton inputEstadoInicialSwapCoVo;
    @FXML private Label labelEstadoInicial;
    @FXML private JFXCheckBox inputHayValorSuperior;
    @FXML private JFXCheckBox inputHayValorInferior;
    @FXML private JFXTextField inputValorRecursoSuperior;
    @FXML private JFXTextField inputValorRecursoInferior;
    @FXML private JFXCheckBox inputDiscreta;
    @FXML private JFXCheckBox inputOrdinal;
    @FXML private JFXCheckBox inputDiscretaIncremental;
    @FXML private JFXComboBox<String> inputDiscretizacionTipo;
    @FXML private JFXTextField inputDiscretizacionMin;
    @FXML private JFXTextField inputDiscretizacionMax;
    @FXML private JFXTextField inputDiscretizacionCantPuntos;

    private String label;
    private String nombre;
    private Integer padding;
    private String unidadEstadoInicial;

    private DatosVariableEstado datosVariableEstado;
    private boolean edicion;
    private EditorHidraulicoController editorHidraulicoController;
    private boolean esHidro = false;
    private boolean editoVariables;

    public VariableDeEstadoController(String label, String nombre, Integer padding, String unidadEstadoInicial){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        edicion = false;
        this.unidadEstadoInicial = unidadEstadoInicial;
    }

    public VariableDeEstadoController(String label, String nombre, Integer padding, DatosVariableEstado datosVariableEstado, String unidadEstadoInicial){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        System.out.println("-->"+datosVariableEstado);
        this.datosVariableEstado = datosVariableEstado;
        edicion = true;
        this.unidadEstadoInicial = unidadEstadoInicial;
    }

    public VariableDeEstadoController(String label, String nombre, Integer padding, String unidadEstadoInicial, EditorHidraulicoController editorHidraulicoController){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        edicion = false;
        this.unidadEstadoInicial = unidadEstadoInicial;
        this.editorHidraulicoController = editorHidraulicoController;
        esHidro = true;
    }

    public VariableDeEstadoController(String label, String nombre, Integer padding, DatosVariableEstado datosVariableEstado, String unidadEstadoInicial, EditorHidraulicoController editorHidraulicoController){
        this.label = label;
        this.nombre = nombre;
        this.padding = padding;
        System.out.println("-->"+datosVariableEstado);
        this.datosVariableEstado = datosVariableEstado;
        edicion = true;
        this.unidadEstadoInicial = unidadEstadoInicial;
        this.editorHidraulicoController = editorHidraulicoController;
        esHidro = true;
    }

    @FXML
    public void initialize(){
        labelNombre.setText(label);
        lowerSeparator.setPadding(new Insets(0,padding,0,0));

        inputDiscretizacionTipo.getItems().add(Text.TIPO_EQUIESPACIADA);
        inputDiscretizacionTipo.getSelectionModel().selectFirst();

        inputEstadoInicialUnidad.getItems().add(unidadEstadoInicial);
        inputEstadoInicialUnidad.getSelectionModel().selectFirst();

        if(esHidro){
            inputEstadoInicialSwapCoVo.setVisible(true);
            inputEstadoInicialSwapCoVo.setOnAction(actionEvent -> {
                editorHidraulicoController.swapCotaVolumen("estadoInicial", inputEstadoInicial, labelEstadoInicial, "Estado Inicial:", "Estado Inicial:", inputEstadoInicialUnidad);
            });
        }


        if(edicion){
            unloadData();
        }

        //Control de cambios:

        //inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputHayValorInferior.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });
        inputHayValorSuperior.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });
        inputDiscreta.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });
        inputOrdinal.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });
        inputDiscretaIncremental.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });

        inputEstadoInicial.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputValorRecursoSuperior.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputValorRecursoInferior.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDiscretizacionMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDiscretizacionMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDiscretizacionCantPuntos.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });


        editoVariables = false;

    }

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
        if(UtilStrings.esNumeroDouble(inputEstadoInicial.getText())) {
            if(esHidro && inputEstadoInicialUnidad.getValue().equals(Text.UNIDAD_M)){
                editorHidraulicoController.swapCotaVolumen("estadoInicial", inputEstadoInicial, labelEstadoInicial, "Estado Inicial:", "Estado Inicial:", inputEstadoInicialUnidad);
            }
            estadoInicial = Double.parseDouble(inputEstadoInicial.getText());
        }
        String estadoInicialUnidad = inputEstadoInicialUnidad.getValue();

        boolean hayValorInferior = inputHayValorInferior.isSelected();
        boolean hayValorSuperior = inputHayValorSuperior.isSelected();

        Evolucion<Double> valorRecursoInferior = new EvolucionConstante<>(null, null);
        Evolucion<Double> valorRecursoSuperior = new EvolucionConstante<>(null, null);
        if(hayValorInferior) {
            valorRecursoInferior = new EvolucionConstante<>(Double.parseDouble(inputValorRecursoInferior.getText()), new SentidoTiempo(1));
        }
        if(hayValorSuperior) {
            valorRecursoSuperior = new EvolucionConstante<>(Double.parseDouble(inputValorRecursoSuperior.getText()), new SentidoTiempo(1));
        }
        boolean discreta = inputDiscreta.isSelected();
        boolean ordinal = inputOrdinal.isSelected();
        boolean discretaIncremental = inputDiscretaIncremental.isSelected();

        //discretizacion
        double discretizacionMin = 0;
        if(UtilStrings.esNumeroDouble(inputDiscretizacionMin.getText())) { discretizacionMin = Double.parseDouble(inputDiscretizacionMin.getText()); }

        double discretizacionMax = 0;
        if(UtilStrings.esNumeroDouble(inputDiscretizacionMax.getText())) {  discretizacionMax = Double.parseDouble(inputDiscretizacionMax.getText()); }

        int discretizacionCantPuntos = 0;
        if (UtilStrings.esNumeroEntero(inputDiscretizacionCantPuntos.getText())) { discretizacionCantPuntos = Integer.parseInt(inputDiscretizacionCantPuntos.getText()); }

        double[] discretizacionParticion = new double[discretizacionCantPuntos];
        double salto = (discretizacionMax - discretizacionMin)/(discretizacionCantPuntos - 1);
        for (int i=0;i<discretizacionCantPuntos;i++) {
            discretizacionParticion[i] = discretizacionMin + i * salto;
        }
        DatosDiscretizacion datosDiscretizacion = new DatosDiscretizacion();
        datosDiscretizacion.setMinimo(discretizacionMin);
        datosDiscretizacion.setMaximo(discretizacionMax);
        datosDiscretizacion.setParticion(discretizacionParticion);
        EvolucionConstante<DatosDiscretizacion> discretizacion = new EvolucionConstante<>(datosDiscretizacion, new SentidoTiempo(1));

        if(edicion){
            datosVariableEstado.setNombre(nombre);
            datosVariableEstado.setEstadoInicial(estadoInicial);
            datosVariableEstado.setHayValorInferior(hayValorInferior);
            if(hayValorInferior) {
                datosVariableEstado.setValorRecursoInferior(valorRecursoInferior);
            }
            datosVariableEstado.setHayValorSuperior(hayValorSuperior);
            if(hayValorSuperior){
                datosVariableEstado.setValorRecursoSuperior(valorRecursoSuperior);
            }
            datosVariableEstado.setDiscreta(discreta);
            datosVariableEstado.setOrdinal(ordinal);
            datosVariableEstado.setDiscretaIncremental(discretaIncremental);
            datosVariableEstado.setDiscretizacion(discretizacion);
        }else {
            datosVariableEstado = new DatosVariableEstado(nombre, estadoInicial, estadoInicialUnidad, discretizacion, valorRecursoInferior,
                                                          valorRecursoSuperior, hayValorInferior, hayValorSuperior,
                                                          discreta, ordinal, discretaIncremental);
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
        inputHayValorInferior.setSelected(datosVariableEstado.isHayValorInferior());
        if(datosVariableEstado.isHayValorInferior()){
            inputValorRecursoInferior.setText(datosVariableEstado.getValorRecursoInferior().getValor(instanteActual).toString());
        }
        inputHayValorSuperior.setSelected(datosVariableEstado.isHayValorSuperior());
        if(datosVariableEstado.isHayValorSuperior()){
            inputValorRecursoSuperior.setText(datosVariableEstado.getValorRecursoSuperior().getValor(instanteActual).toString());
        }
        inputDiscreta.setSelected(datosVariableEstado.isDiscreta());
        inputOrdinal.setSelected(datosVariableEstado.isOrdinal());
        inputDiscretaIncremental.setSelected(datosVariableEstado.isDiscretaIncremental());
        inputDiscretizacionMin.setText(String.valueOf(datosVariableEstado.getDiscretizacion().getValor(instanteActual).getMinimo()));
        inputDiscretizacionMax.setText(String.valueOf(datosVariableEstado.getDiscretizacion().getValor(instanteActual).getMaximo()));
        inputDiscretizacionCantPuntos.setText(String.valueOf(datosVariableEstado.getDiscretizacion().getValor(instanteActual).getParticion().length));
    }


    public ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputEstadoInicial.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Estado Inicial vacío.");
        if( inputValorRecursoSuperior.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Valor recurso superior vacío.");
        //if( inputValorRecursoInferior.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Valor recurso inferior vacío.");
        if( inputDiscretizacionMin.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Minimo discretizacion vacío.");
        if( inputDiscretizacionMax.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Maximo discretizacion  vacío.");
        if( inputDiscretizacionCantPuntos.getText().trim().equalsIgnoreCase("") ) errores.add("VA: "+ nombre + " Canitdad puntos discretizacion vacío.");

        return errores;
    }
}
