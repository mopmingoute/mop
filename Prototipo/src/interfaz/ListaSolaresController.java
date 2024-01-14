/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaSolaresController is part of MOP.
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
import datatypes.DatosCorrida;
import datatypes.DatosFotovoltaicoCorrida;
import datatypes.DatosFotovoltaicosCorrida;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import java.util.*;


public class ListaSolaresController extends ListaParticipante {
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;

    private DatosFotovoltaicosCorrida datosFotovoltaicosCorrida;
    private Hashtable<String, DatosFotovoltaicoCorrida> listaDatosFotovoltaicoCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
    private ListaSolaresController thisController;

    private String solarABorrar;
    private int indSolarABorrar;

    public ListaSolaresController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida, parentController);
        this.datosFotovoltaicosCorrida = datosCorrida.getFotovoltaicos();
        this.listaDatosFotovoltaicoCorrida = datosCorrida.getFotovoltaicos().getFotovoltaicos();
        thisController = this;
    }

    @FXML
    public void initialize(){
        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }



    /**
     *  Método para cargar los datos en el DatosFotovoltaicosCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA SOLARES");
        datosFotovoltaicosCorrida.setFotovoltaicos(listaDatosFotovoltaicoCorrida);
        getDatosCorrida().setFotovoltaicos(datosFotovoltaicosCorrida);
    }

    /**
     * Método para obtener los datos del DatosFotovoltaicosCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA SOLARES");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosFotovoltaicoCorrida datosFotovoltaicoCorrida : listaDatosFotovoltaicoCorrida.values()){
            Label nombre = new Label(datosFotovoltaicoCorrida.getNombre());
            if(datosFotovoltaicoCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosFotovoltaicoCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosFotovoltaicoCorrida.getNombre()+"(F)", visible.isSelected());
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
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorSolar.fxml", "Editor Solar - " + datosFotovoltaicoCorrida.getNombre(),  Text.SOLAR_SIZE[0], Text.SOLAR_SIZE[1], new EditorSolarController(getDatosCorrida(), datosFotovoltaicoCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosFotovoltaicoCorrida clonado = new DatosFotovoltaicoCorrida("Fotovoltaico clonado", "UTE",datosFotovoltaicoCorrida.getBarra(), datosFotovoltaicoCorrida.getCantModInst(),
                        datosFotovoltaicoCorrida.getPotMin(), datosFotovoltaicoCorrida.getPotMax(), datosFotovoltaicoCorrida.getFactor(), datosFotovoltaicoCorrida.getCantModIni(),
                        datosFotovoltaicoCorrida.getDispMedia(), datosFotovoltaicoCorrida.gettMedioArreglo(), datosFotovoltaicoCorrida.isSalDetallada(), datosFotovoltaicoCorrida.getMantProgramado(),
                        datosFotovoltaicoCorrida.getCostoFijo(), datosFotovoltaicoCorrida.getCostoVariable());

                getParentController().openEditor("EditorSolar.fxml", "Editor Solar " + clonado.getNombre(), Text.SOLAR_SIZE[0], Text.SOLAR_SIZE[1], new EditorSolarController(getDatosCorrida(),clonado, thisController, true));
                datosFotovoltaicosCorrida.getOrdenCargaXML().add("Fotovoltaico clonado");
                datosFotovoltaicosCorrida.getListaUtilizados().add("Fotovoltaico clonado");
            });
            deleteButton.setOnAction(actionEvent -> {
                solarABorrar = datosFotovoltaicoCorrida.getNombre();
                indSolarABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Solar " + solarABorrar);
            });
        }

        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });
    }



    public void borrarSolar(String solarABorrar) {
        datosFotovoltaicosCorrida.getOrdenCargaXML().remove(solarABorrar);
        datosFotovoltaicosCorrida.getListaUtilizados().remove(solarABorrar);
        listaDatosFotovoltaicoCorrida.remove(solarABorrar);
        unloadData();
        refresh();

    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){
            if(confirmacion) {
                borrarSolar(solarABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}
