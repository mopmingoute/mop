/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ReportesMenuController is part of MOP.
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
import datatypes.DatosEspecificacionReporte;
import datatypes.DatosReporteGUI;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import presentacion.PresentacionHandler;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportesMenuController {

    @FXML private GridPane gridPane;

    private PresentacionHandler ph = PresentacionHandler.getInstance();

    private ArrayList<DatosReporteGUI> datosReportesGUI;
    private ReportesContentController reportesContentController;

    public ReportesMenuController(){}

    public ReportesMenuController(ReportesContentController reportesContentController){
        this.reportesContentController = reportesContentController;
    }

    @FXML
    public void initialize(){
        populateMenu();
    }

    private void populateMenu(){

        // TODO: 21/07/2020 a futuro: pasar a dinámico
        HashMap<Integer,String> listaTipoReportes = new HashMap<>();
        listaTipoReportes.put(DatosEspecificacionReporte.REP_RES_ENER      , "Resumen Energías"          );
        listaTipoReportes.put(DatosEspecificacionReporte.REP_RES_COSTO     , "Resumen Costos"            );
        listaTipoReportes.put(DatosEspecificacionReporte.REP_COSTOS_MARG   , "Costos Marginales"         );
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_HIDROS        , "Hidráulicos"               );
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_TERS          , "Térmicos"                  );
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_FALLAS        , "Fallas"                    );
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_VAL_RENOVABLES, "Valorización de renovables");
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_AMBIENTAL     , "Ambiental"                 );
//        listaTipoReportes.put(DatosEspecificacionReporte.REP_CONTRATOS     , "Contratos"                 );

        for(Integer tipoReporte : listaTipoReportes.keySet()){
            JFXButton reporteBtn = new JFXButton();
            reporteBtn.setMinWidth(100);
            reporteBtn.getStyleClass().add("btn_reporte");
            reporteBtn.setOnAction(event -> {});
            reporteBtn.setText(listaTipoReportes.get(tipoReporte));
            gridPane.add(reporteBtn, 0, tipoReporte);
            gridPane.getRowConstraints().add(new RowConstraints(40,40,40));

            reporteBtn.setOnAction(action -> {
                reportesContentController.actualizar(ph.devolverReporte(new DatosEspecificacionReporte(tipoReporte, true)));
            });
        }
    }
}
