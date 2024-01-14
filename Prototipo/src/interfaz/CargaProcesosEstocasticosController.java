/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargaProcesosEstocasticosController is part of MOP.
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
import datatypes.DatosPronostico;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import tiempo.Evolucion;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorPropiedades;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CargaProcesosEstocasticosController extends GeneralidadesController{


    //INPUTS DATA PE

    @FXML private JFXComboBox<String> inputTipoPE;
    @FXML private JFXComboBox<String> inputNombreProceso;
    @FXML private JFXCheckBox inputDiscretoExhaustivoPE;
    @FXML private JFXCheckBox inputMuestradoPE;
    @FXML private JFXButton inputCargarPE;

    @FXML private TableView tableViewPE;

    @FXML private JFXButton infoTipoProceso;

    @FXML private JFXButton infoProceso;

    @FXML private VBox vBoxPronosticos;

    String pathResources;

    private HashMap<String, BloquePronosticoPE> bloquesControllers = new HashMap<>();



    private Manejador parentController;

    public CargaProcesosEstocasticosController(Manejador parentController) {
        this.parentController = parentController;

    }


    @FXML
    public void initialize() {
        cargarDirectorioResources();
        parentController.asignarTooltips(this);

        //PE table view
        TableColumn<String, ProcesoEstocastico> column1 = new TableColumn<>("Nombre");
        column1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        column1.setPrefWidth(280);

        TableColumn<String, ProcesoEstocastico> column2 = new TableColumn<>("Tipo");
        column2.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        column2.setPrefWidth(180);

        tableViewPE.getColumns().add(column1);
        tableViewPE.getColumns().add(column2);

        TableColumn<String, ProcesoEstocastico> buttonColumn = new TableColumn<>("  ");
        buttonColumn.setCellFactory(param -> new ButtonCell(parentController));
        buttonColumn.setSortable(false);
        buttonColumn.setPrefWidth(50);


        tableViewPE.getColumns().add(buttonColumn);
        //inputRutaPEBtn.setOnAction(event -> rutaPEFileChooser());
        inputCargarPE.setOnAction(actionEvent -> loadPE());
        inputTipoPE.getItems().addAll(new ArrayList<>(Arrays.asList(Text.TIPO_PE_HISTORICO, Text.TIPO_PE_MARKOV, Text.TIPO_PE_POR_ESCENARIOS,
                    Text.TIPO_PE_POR_CRONICAS, Text.TIPO_PE_BOOTSTRAP_DISCRETO, Text.TIPO_PE_DEMANDA_ANIO_BASE)));
        inputTipoPE.setPromptText("Elija...");

        inputNombreProceso.setOnAction(actionEvent -> cargarCamposPronosticos());

        popularPEs();

    }

    private void cargarCamposPronosticos() {

        vBoxPronosticos.getChildren().clear();
        bloquesControllers.clear();

        if(inputTipoPE.getValue() != null && inputNombreProceso.getValue()!= null && !inputTipoPE.getValue().trim().equals("")
                && !inputNombreProceso.getValue().trim().equals("")){

            ArrayList<ArrayList<String>>  info = DirectoriosYArchivos.leePrimerasNLineas(pathResources +"\\"+ inputNombreProceso.getValue() + "\\datosGenerales.txt", 6);
            for (ArrayList<String> lista: info) {
                if(lista.get(0).trim().equals("NOMBRES_VARIABLES")){
                    for (int i = 1; i<lista.size(); i++){
                        agregarVariablePronostico(lista.get(i));
                    }

                }

            }

        }

    }

    private void agregarVariablePronostico(String nombreVariable) {
        try {

            BloquePronosticoPE bloque = new BloquePronosticoPE(parentController.datosCorrida, nombreVariable);
            bloquesControllers.put(nombreVariable, bloque);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("BloquePronosticoPE.fxml"));
            loader.setController(bloque);

            AnchorPane newLoadedPane = loader.load();

            vBoxPronosticos.getChildren().add(newLoadedPane);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }


    public void cargarDirectorioResources(){
        LectorPropiedades lprop = new LectorPropiedades(".\\resources\\mop.conf");
        try {
            pathResources= lprop.getProp("rutaEntradas") + "\\resources";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> cargarCarpetasDisponibles(){

        ArrayList<String> nombreCarpetas = new ArrayList<>();
        if(pathResources != null && !pathResources.trim().equals("")){
            File directorio = new File(pathResources);
            if (directorio.exists() && directorio.isDirectory()) {
                File[] archivosYDirectorios = directorio.listFiles();
                List<File> carpetas = new ArrayList<>();
                for (File archivoODirectorio : archivosYDirectorios) {
                    if (archivoODirectorio.isDirectory()) {
                        carpetas.add(archivoODirectorio);
                    }
                }
                for (File carpeta : carpetas) {
                    String nc = carpeta.getName();
                    if(!parentController.datosCorrida.getProcesosEstocasticos().keySet().contains(nc)){
                        nombreCarpetas.add(carpeta.getName()); }
                }
            }
            inputNombreProceso.getItems().addAll(nombreCarpetas);
        }
        return nombreCarpetas;
    }
    private void loadPE(){
        if(!(parentController.datosCorrida.getNombre() == null || parentController.datosCorrida.getNombre().trim().equals(""))){
            if(inputTipoPE.getValue() != null && inputNombreProceso.getValue()!= null && !inputTipoPE.getValue().trim().equals("")
                    && !inputNombreProceso.getValue().trim().equals("")) {
                if(bloquesControllers.size() == 0 || controlPronosticosCompletos()) {
                    String nombre = inputNombreProceso.getValue();
                    String tipo = inputTipoPE.getValue();
                    Boolean discretoExhaustivo = inputDiscretoExhaustivoPE.isSelected();
                    String tipoSoporte = "Archivo";
                    String ruta = "./resources/" + nombre;
                    Boolean muestreado = inputMuestradoPE.isSelected();
                    Hashtable<String, DatosPronostico> pronosticos = cargarPronosticos();
                    try {
                        DatosProcesoEstocastico datosProcesoEstocastico = new DatosProcesoEstocastico(nombre, tipo, tipoSoporte, ruta, discretoExhaustivo, muestreado, null, null);
                        parentController.datosCorrida.getProcesosEstocasticos().put(nombre, datosProcesoEstocastico);
                        tableViewPE.getItems().clear();
                        popularPEs();
                        mostrarMensajeAviso("El Proceso: " + nombre + " se cargó correctamente", parentController);
                    } catch (Exception e) {
                        setLabelMessageTemporal("No se puedo cargar el Proceso: " + nombre + ".", TipoInfo.ERROR);
                        System.out.println("Error al cargar proceso estocastico: " + nombre);
                        e.printStackTrace();
                    }
                } else {
                    setLabelMessageTemporal("No se cargaron los pronosticos.", TipoInfo.FEEDBACK);
                }
            }else {
                setLabelMessageTemporal("No se seleccionó Tipo o Proceso.", TipoInfo.FEEDBACK);
            }
        }else {
            setLabelMessageTemporal("Debe cargar o crear una corrida antes de cargar el proceso.", TipoInfo.FEEDBACK);
        }



    }

    private boolean controlPronosticosCompletos() {
        boolean ret = true;
        Iterator<Map.Entry<String, BloquePronosticoPE>> it = bloquesControllers.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, BloquePronosticoPE> entry = it.next();
            if(entry.getValue().getValor() == null){   return false; }
            if(entry.getValue().getPeso().controlDatosCompletos().size()>0 || entry.getValue().getValor().controlDatosCompletos().size()>0 ){
                return false;
            }

        }
        return ret;
    }

    private Hashtable<String, DatosPronostico> cargarPronosticos() {
        Hashtable<String, DatosPronostico> retorno = new Hashtable<>();
        Iterator<Map.Entry<String, BloquePronosticoPE>> it = bloquesControllers.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, BloquePronosticoPE> entry = it.next();
            String nombreVA =  entry.getKey();
            Evolucion<Double> peso = entry.getValue().getPeso();
            Evolucion<Double> valores = entry.getValue().getValor();
            DatosPronostico p = new DatosPronostico(nombreVA, peso, valores );
            retorno.put(nombreVA, p);
        }



        return retorno;
    }

    public void popularPEs(){
        for(DatosProcesoEstocastico datosProcesoEstocastico : parentController.datosCorrida.getProcesosEstocasticos().values()){
            tableViewPE.getItems().add(new ProcesoEstocastico(datosProcesoEstocastico.getNombre(), datosProcesoEstocastico.getTipo()));
        }
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }

    public class ProcesoEstocastico {
        public String nombre;
        public String tipo;

        public ProcesoEstocastico(String nombre, String tipo){
            this.nombre = nombre;
            this.tipo = tipo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTipo() {
            return tipo;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }

    public class ButtonCell extends TableCell<String, ProcesoEstocastico> {
        private final JFXButton button;
        private Manejador parentController;

        public ButtonCell(Manejador parentController) {
            this.parentController = parentController;
            MaterialIconView closeIcon = new MaterialIconView(MaterialIcon.CLOSE);
            closeIcon.getStyleClass().add("glyph-icon-delete");
            this.button = new JFXButton("", closeIcon);
            this.button.getStyleClass().add("btn_del_part");
            this.button.setAlignment(Pos.CENTER);



            this.button.setOnAction(event -> {
                Object proceso = getTableView().getItems().get(getIndex());
                ProcesoEstocastico p = (ProcesoEstocastico)proceso;
                if(parentController.controlProcesosEstocasticosEnUso().contains(p.nombre )){
                    parentController.mostrarMensajeAviso("El proceso: " + p.nombre+ " no se puede borrar, esta en uso.", parentController);
                }else {
                    parentController.borrarProcesoEstocastico(p.nombre);
                    tableViewPE.getItems().clear();
                    popularPEs();
                    parentController.mostrarMensajeAviso("El proceso: "+ p.nombre+" se elimino correctamente.", parentController);

                }

            });
        }

        @Override
        protected void updateItem(ProcesoEstocastico item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                button.setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(button);
            }
        }
    }

    public void clean(){
        tableViewPE.getItems().clear();
        cleanPEFields();

    }

    private void cleanPEFields(){
        //inputNombrePE.clear();
        inputTipoPE.getSelectionModel().clearSelection();
        //inputRutaPE.clear();
        inputDiscretoExhaustivoPE.setSelected(false);
        inputMuestradoPE.setSelected(false);
    }
    private void unloadData() {}



}
