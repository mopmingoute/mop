/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaFallasController is part of MOP.
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
import datatypes.DatosFallaEscalonadaCorrida;
import datatypes.DatosFallasEscalonadasCorrida;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.*;


public class ListaFallasController extends ListaParticipante{
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> inputCompFalla;
    @FXML private JFXButton inputCompFallaEV;

    private DatosCorrida datosCorrida;
    private DatosFallasEscalonadasCorrida datosFallasEscalonadasCorrida;
    private Hashtable<String, DatosFallaEscalonadaCorrida> listaDatosFallaEscalonadaCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
    private Manejador parentController;
    private ListaFallasController thisController;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();
    private String fallaABorrar;
    private int indFallaABorrar;


    public ListaFallasController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida,parentController);
        this.datosCorrida = datosCorrida;
        this.datosFallasEscalonadasCorrida = datosCorrida.getFallas();
        this.listaDatosFallaEscalonadaCorrida = datosCorrida.getFallas().getFallas();
        this.parentController = parentController;
        thisController = this;
    }

    @FXML
    public void initialize(){

        inputCompFalla.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.FALLASINESTADO, Constantes.FALLA_CONESTADO_SINDUR, Constantes.FALLA_CONESTADO_CONDUR)));

        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }

    /**
     * Método para refrescar la lista de participantes
     */
    public void refresh(){
        parentController.refresh();
//        unloadData();
    }

    public void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars, Node componenteAsociado){
        ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
        if(ev != null){
            listaConUnaEv.add(ev);
            EVController evController = new EVController(listaConUnaEv, datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);

        }else{
            EVController evController = new EVController(datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);
        }
    }

    /**
     *  Método para cargar los datos en el DatosFallasEscalonadasCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA FALLAS");
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> compFalla = new EvolucionConstante<>(inputCompFalla.getValue(), new SentidoTiempo(1));
        valoresComportamiento.put(Constantes.COMPFALLA, compFalla);
        datosFallasEscalonadasCorrida.setValoresComportamiento(valoresComportamiento);
        datosFallasEscalonadasCorrida.setFallas(listaDatosFallaEscalonadaCorrida);
        datosCorrida.setFallas(datosFallasEscalonadasCorrida);
    }

    /**
     * Método para obtener los datos del DatosFallasEscalonadasCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA FALLAS");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosFallaEscalonadaCorrida datosFallaEscalonadaCorrida : listaDatosFallaEscalonadaCorrida.values()){
            Label nombre = new Label(datosFallaEscalonadaCorrida.getNombre());
            if(datosFallaEscalonadaCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosFallaEscalonadaCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                parentController.setVisibleParticipate(datosFallaEscalonadaCorrida.getNombre(), visible.isSelected());
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
            editButton.setOnAction(actionEvent -> parentController.openEditor("EditorFalla.fxml", "Editor Falla - "+ datosFallaEscalonadaCorrida.getNombre(), Text.FALLA_SIZE[0], Text.FALLA_SIZE[1], new EditorFallaController(datosCorrida, datosFallaEscalonadaCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosFallaEscalonadaCorrida clonado = new DatosFallaEscalonadaCorrida("Falla clonada", datosFallaEscalonadaCorrida.getCompFalla(),
                        datosFallaEscalonadaCorrida.getValsComps(), datosFallaEscalonadaCorrida.getDemanda(), datosFallaEscalonadaCorrida.getEscalones(),
                        datosFallaEscalonadaCorrida.getCantEscProgram(), null, datosFallaEscalonadaCorrida.getVarsEstado(),
                        datosFallaEscalonadaCorrida.getVarsControlDE(),datosFallaEscalonadaCorrida.isSalDetallada());

                parentController.openEditor("EditorFalla.fxml", "Editor Falla - " + clonado.getNombre(),  Text.FALLA_SIZE[0], Text.FALLA_SIZE[1], new EditorFallaController(datosCorrida, clonado, thisController, true));
                datosFallasEscalonadasCorrida.getOrdenCargaXML().add("Falla clonada");
                datosFallasEscalonadasCorrida.getListaUtilizados().add("Falla clonada");

            });
            deleteButton.setOnAction(actionEvent -> {

                fallaABorrar = datosFallaEscalonadaCorrida.getNombre();
                indFallaABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Falla " + fallaABorrar);
            });
        }
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        inputCompFalla.getSelectionModel().select(datosFallasEscalonadasCorrida.getValoresComportamiento().get(Constantes.COMPFALLA).getValor(instanteActual));

        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });

        evForField(datosFallasEscalonadasCorrida.getValoresComportamiento().get(Constantes.COMPFALLA), "compFalla", inputCompFallaEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.FALLASINESTADO, Constantes.FALLA_CONESTADO_SINDUR, Constantes.FALLA_CONESTADO_CONDUR)), inputCompFalla);
    }

    public void borrarFalla(String fallaABorrar){
        datosFallasEscalonadasCorrida.getOrdenCargaXML().remove(fallaABorrar);
        datosFallasEscalonadasCorrida.getListaUtilizados().remove(fallaABorrar);
        listaDatosFallaEscalonadaCorrida.remove(fallaABorrar);
        unloadData();
        refresh();
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){
            if(confirmacion) {
                borrarFalla(fallaABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}
