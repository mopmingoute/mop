/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaTermicosController is part of MOP.
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
import datatypes.DatosCorrida;
import datatypes.DatosTermicoCorrida;
import datatypes.DatosTermicosCorrida;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.*;


public class ListaTermicosController extends ListaParticipante{
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> inputCompMinTecnicos;
    @FXML private JFXButton inputCompMinTecnicosEV;

    private DatosTermicosCorrida datosTermicosCorrida;
    private Hashtable<String, DatosTermicoCorrida> listaDatosTermicoCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
    private ListaTermicosController thisController;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private String termicoABorrar;
    private int indTermicoABorrar;

    public ListaTermicosController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida,parentController);
        this.datosTermicosCorrida = datosCorrida.getTermicos();
        this.listaDatosTermicoCorrida = datosCorrida.getTermicos().getTermicos();
        thisController = this;
    }

    @FXML
    public void initialize(){

        getParentController().asignarTooltips(thisController);
        inputCompMinTecnicos.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.TERSINMINTEC, Constantes.TERMINTECFORZADO, Constantes.TERVARENTERAS, Constantes.TERVARENTERASYVARESTADO, Constantes.TERDOSPASOS)));
        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);

        unloadData();
    }





    /**
     *  Método para cargar los datos en el DatosTermicosCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA TÉRMICOS");
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> compMinTecnicos = new EvolucionConstante<>(inputCompMinTecnicos.getValue(), new SentidoTiempo(1));
        valoresComportamiento.put(Constantes.COMPMINTEC, compMinTecnicos);
        datosTermicosCorrida.setValoresComportamiento(valoresComportamiento);
        datosTermicosCorrida.setTermicos(listaDatosTermicoCorrida);
        getDatosCorrida().setTermicos(datosTermicosCorrida);
    }

    /**
     * Método para obtener los datos del DatosTermicosCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA TÉRMICOS");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosTermicoCorrida datosTermicoCorrida : listaDatosTermicoCorrida.values()){        	
            Label nombre = new Label(datosTermicoCorrida.getNombre());
            if(datosTermicoCorrida.controlDatosCompletos(false, "").size() > 0){
                nombre.setText(datosTermicoCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosTermicoCorrida.getNombre()+"(T)", visible.isSelected());
                if(!visible.isSelected()){
                    todosVisiblesInput.setSelected(false);
                }else{
                    boolean allVisible = true;
                    for(JFXCheckBox checkBox : checkBoxes){
                        allVisible &= checkBox.isSelected();
                    }
                    if(!todosVisiblesInput.isSelected()){
                        todosVisiblesInput.setSelected(allVisible);
                    }
                }
            });
            checkBoxes.add(visible);
            GridPane.setMargin(visible, new Insets(0,0, 0,16));
            MaterialIconView editIcon = new MaterialIconView(MaterialIcon.EDIT);
            editIcon.getStyleClass().add("glyph-icon-edit");
            JFXButton editButton = new JFXButton("", editIcon);
            editButton.getStyleClass().add("btn_edit_part");
            editButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            editButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            MaterialIconView cloneIcon = new MaterialIconView(MaterialIcon.CONTENT_COPY);
            cloneIcon.getStyleClass().add("glyph-icon-clone");
            JFXButton cloneButton = new JFXButton("", cloneIcon);
            cloneButton.getStyleClass().add("btn_clone_part");
            cloneButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            cloneButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            MaterialIconView closeIcon = new MaterialIconView(MaterialIcon.CLOSE);
            closeIcon.getStyleClass().add("glyph-icon-delete");
            JFXButton deleteButton = new JFXButton("", closeIcon);
            deleteButton.getStyleClass().add("btn_del_part");
            deleteButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            deleteButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            GridPane.setMargin(nombre, new Insets(4, 0, 0, 0));
            GridPane.setMargin(visible, new Insets(4, 0, 0, 16));
            GridPane.setMargin(editButton, new Insets(4, 0, 0, 0));
            GridPane.setMargin(cloneButton, new Insets(4, 0, 0, 0));
            GridPane.setMargin(deleteButton, new Insets(4, 0, 0, 0));
            gridPane.add(nombre, 0, nodesByRow.size()+3);
            gridPane.add(visible, 1, nodesByRow.size()+3);
            gridPane.add(editButton, 2, nodesByRow.size()+3);
            gridPane.add(cloneButton, 3, nodesByRow.size()+3);
            gridPane.add(deleteButton, 4, nodesByRow.size()+3);
            Integer index = nodesByRow.size() > 0 ? Collections.max(nodesByRow.keySet())+1 : 0;
            nodesByRow.put(index, new ArrayList<>(Arrays.asList(nombre, visible, editButton, cloneButton, deleteButton)));
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorTermico.fxml", "Editor Térmico - " + datosTermicoCorrida.getNombre(), Text.TERMICO_SIZE[0], Text.TERMICO_SIZE[1], new EditorTermicoController(getDatosCorrida(), datosTermicoCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {

                DatosTermicoCorrida clonado = new DatosTermicoCorrida("Termico clonado", "UTE",datosTermicoCorrida.getBarra(), datosTermicoCorrida.getCantModInst(),datosTermicoCorrida.getListaCombustibles(),
                        datosTermicoCorrida.getCombustiblesBarras(), datosTermicoCorrida.getPotMin(), datosTermicoCorrida.getPotMax(), datosTermicoCorrida.getRendimientosPotMax(),
                        datosTermicoCorrida.getRendimientosPotMin(), datosTermicoCorrida.getFlexibilidadMin(), datosTermicoCorrida.getCantModIni(), datosTermicoCorrida.getDispMedia(),
                        datosTermicoCorrida.gettMedioArreglo(), datosTermicoCorrida.isSalDetallada(), datosTermicoCorrida.getMantProgramado(), datosTermicoCorrida.getCostoFijo(),
                        datosTermicoCorrida.getCostoVariable());
                clonado.getValoresComportamientos().put(Constantes.COMPMINTEC, datosTermicoCorrida.getValoresComportamientos().get(Constantes.COMPMINTEC));


                getParentController().openEditor("EditorTermico.fxml", "Editor Térmico + "+ clonado.getNombre(), Text.TERMICO_SIZE[0], Text.TERMICO_SIZE[1], new EditorTermicoController(getDatosCorrida(), clonado, thisController, true));
                datosTermicosCorrida.getOrdenCargaXML().add("Termico clonado");
                datosTermicosCorrida.getListaUtilizados().add("Termico clonado");
            });
            deleteButton.setOnAction(actionEvent -> {
                termicoABorrar = datosTermicoCorrida.getNombre();
                indTermicoABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Térmico " + termicoABorrar);
            });
        }
        inputCompMinTecnicos.getSelectionModel().select(datosTermicosCorrida.getValoresComportamiento().get(Constantes.COMPMINTEC).getValor(CorridaHandler.getInstance().dameInstanteActual()));
        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });

        evForField(datosTermicosCorrida.getValoresComportamiento().get(Constantes.COMPMINTEC), "compMinTecnicos", inputCompMinTecnicosEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.TERSINMINTEC, Constantes.TERMINTECFORZADO, Constantes.TERVARENTERAS, Constantes.TERVARENTERASYVARESTADO, Constantes.TERDOSPASOS)), inputCompMinTecnicos);

    }

    public void borrarTermico(String termicoABorrar) {
        datosTermicosCorrida.getOrdenCargaXML().remove(termicoABorrar);
        datosTermicosCorrida.getListaUtilizados().remove(termicoABorrar);
        listaDatosTermicoCorrida.remove(termicoABorrar);
        unloadData();
        refresh();
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){

            if(confirmacion) {
                borrarTermico(termicoABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }


}
