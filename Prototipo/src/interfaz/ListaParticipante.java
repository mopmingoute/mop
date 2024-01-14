/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaParticipante is part of MOP.
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
import datatypes.DatosCorrida;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tiempo.Evolucion;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ListaParticipante extends GeneralidadesController {


    private Manejador parentController;
    private DatosCorrida datosCorrida;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    ListaParticipante(DatosCorrida datosCorrida, Manejador parentController) {
        this.parentController = parentController;
        this.datosCorrida = datosCorrida;
    }


    public Manejador getParentController() {
        return parentController;
    }

    public void setParentController(Manejador parentController) {
        this.parentController = parentController;
    }

    public DatosCorrida getDatosCorrida() {
        return datosCorrida;
    }

    public void setDatosCorrida(DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
    }

    public void actualizarLineaDeEntrada() {
        parentController.actualizarLineaEntrada();
    }

    /**
     * Método para refrescar la lista de participantes
     */
    public void refresh() {
        parentController.refresh();
//        unloadData(); // TODO: 01/02/2022 orden 52-53 check, uncomment check
    }


    public void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars, Node componenteAsociado) {
        ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
        if (ev != null) {
            listaConUnaEv.add(ev);
            EVController evController = new EVController(listaConUnaEv, datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);

        } else {
            EVController evController = new EVController(datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);
        }
    }

    protected void mostrarMensajeConfirmacionBorrado(String nomParticipante) {
         mostrarMensajeConfirmacion(Text.MSG_CONF_ELIMINAR_PARTICIPANTE + nomParticipante + " ?", 2);

    }



}