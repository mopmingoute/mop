/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorDuctoController is part of MOP.
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
import datatypes.DatosDuctoCombCorrida;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EditorDuctoController {
	@FXML
	private JFXTextField inputNombre;
	@FXML
	private JFXComboBox<String> inputBarra1;
	@FXML
	private JFXComboBox<String> inputBarra2;
	@FXML
	private JFXTextField inputCapacidad12;
	@FXML
	private JFXButton inputCapacidad12EV;
	@FXML
	private JFXTextField inputCapacidad21;
	@FXML
	private JFXButton inputCapacidad21EV;
	@FXML
	private JFXTextField inputPerdidas12;
	@FXML
	private JFXButton inputPerdidas12EV;
	@FXML
	private JFXTextField inputPerdidas21;
	@FXML
	private JFXButton inputPerdidas21EV;
	@FXML
	private JFXTextField inputCantModInst;
	@FXML
	private JFXButton inputCantModInstEV;
	@FXML
	private JFXComboBox<String> inputDispModUnidad;
	@FXML
	private JFXTextField inputCantModIni;
	@FXML
	private JFXTextField inputDispMedia;
	@FXML
	private JFXButton inputDispMediaEV;
	@FXML
	private JFXTextField inputTMedioArreglo;
	@FXML
	private JFXButton inputTMedioArregloEV;
	@FXML
	private JFXComboBox<String> inputTMedioArregloUnidad;
	@FXML
	private JFXTextField inputMantProg;
	@FXML
	private JFXButton inputMantProgEV;
	@FXML
	private JFXTextField inputCostoFijo;
	@FXML
	private JFXButton inputCostoFijoEV;
	@FXML
	private JFXCheckBox inputSalidaDetallada;

	private HashMap<String, EVController> evsPorNombre = new HashMap<>();

	private ArrayList<String> listaBarras = new ArrayList<>();

	private DatosDuctoCombCorrida datosDuctoCombCorrida;
	private boolean edicion;
	private EditorCombustibleController parentController;
	private boolean editoVariables = false;

	public EditorDuctoController(EditorCombustibleController editorCombustibleController) {
		this.parentController = editorCombustibleController;
	}

	public EditorDuctoController(EditorCombustibleController editorCombustibleController,
			DatosDuctoCombCorrida datosDuctoCombCorrida) {
		this.datosDuctoCombCorrida = datosDuctoCombCorrida;
		edicion = true;
		this.parentController = editorCombustibleController;
	}

	public boolean isEditoVariables() { return editoVariables;  }

	public void setEditoVariables(boolean editoVariables) {  this.editoVariables = editoVariables;   }
	public void initialize() {

		listaBarras.addAll(parentController.getListaBarras());

		inputBarra1.getItems().addAll(listaBarras);
		inputBarra2.getItems().addAll(listaBarras);

		inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
		inputTMedioArregloUnidad.getSelectionModel().selectFirst();

		inputDispModUnidad.getItems().add(Text.TIPO_EXPONENCIAL);
		inputDispModUnidad.getSelectionModel().selectFirst();

		inputBarra1.setOnAction(actionEvent -> {
			ArrayList<String> elemsToAdd = new ArrayList<>(listaBarras);
			elemsToAdd.removeAll(inputBarra2.getItems());
			inputBarra2.getItems().addAll(elemsToAdd);
			inputBarra2.getItems().remove(inputBarra1.getValue());
		});
		inputBarra2.setOnAction(actionEvent -> {
			ArrayList<String> elemsToAdd = new ArrayList<>(listaBarras);
			elemsToAdd.removeAll(inputBarra1.getItems());
			inputBarra1.getItems().addAll(elemsToAdd);
			inputBarra1.getItems().remove(inputBarra2.getValue());
		});

		if (edicion) {
			unloadData();
		} else {
			evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
			evForField(null, "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);
			evForField(null, "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);
			evForField(null, "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);
			evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);
			evForField(null, "capacidad12", inputCapacidad12EV, Text.EV_NUM_DOUBLE, null, inputCapacidad12);
			evForField(null, "capacidad21", inputCapacidad21EV, Text.EV_NUM_DOUBLE, null, inputCapacidad21);
			evForField(null, "perdidas12", inputPerdidas12EV, Text.EV_NUM_DOUBLE, null, inputPerdidas12);
			evForField(null, "perdidas21", inputPerdidas21EV, Text.EV_NUM_DOUBLE, null, inputPerdidas21);
		}

		//Control de cambios:

		inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputBarra1.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputBarra2.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputCapacidad12.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputCapacidad21.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputPerdidas12.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputPerdidas21.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

		inputCantModIni.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputDispMedia.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputTMedioArreglo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputMantProg.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
		controlHayCambiosEnEvoluciones();

		editoVariables = false;
	}

	private void controlHayCambiosEnEvoluciones() {
		for (String clave : evsPorNombre.keySet()) {
			if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
				editoVariables = true;
			}
		}

	}

	public void updateBarras() {
		ArrayList<String> elemsToAdd = new ArrayList<>(parentController.getListaBarras());
		elemsToAdd.removeAll(listaBarras);

		ArrayList<String> elemsToRemove = new ArrayList<>(listaBarras);
		elemsToRemove.removeAll(parentController.getListaBarras());

		listaBarras = parentController.getListaBarras();

		inputBarra1.getItems().addAll(elemsToAdd);
		inputBarra2.getItems().addAll(elemsToAdd);

		if (elemsToRemove.contains(inputBarra1.getValue())) {
			inputBarra1.getSelectionModel().clearSelection();
		}
		inputBarra1.getItems().removeAll(elemsToRemove);
		if (elemsToRemove.contains(inputBarra2.getValue())) {
			inputBarra2.getSelectionModel().clearSelection();
		}
		inputBarra2.getItems().removeAll(elemsToRemove);
	}

	public DatosDuctoCombCorrida getDatosDuctoCombCorrida() {
		loadData();
		return datosDuctoCombCorrida;
	}

	private void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars,
			Node componenteAsociado) {
		ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
		if (ev != null) {
			listaConUnaEv.add(ev);
			EVController evController = new EVController(listaConUnaEv, parentController.datosCorrida, tipoData, vars,
					componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
			evController.setEVBtnAction(evBtn, evController);
			evsPorNombre.put(key, evController);

		} else {
			EVController evController = new EVController(parentController.datosCorrida, tipoData, vars,
					componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
			evController.setEVBtnAction(evBtn, evController);
			evsPorNombre.put(key, evController);
		}
	}

	/**
	 * Método para cargar los datos en el DatosDuctoCombCorrida
	 */
	private void loadData() {
		System.out.println("LOAD DATA-DUCTO");
		String nombre = inputNombre.getText();
		String barra1 = inputBarra1.getValue();
		String barra2 = inputBarra2.getValue();
		Evolucion<Double> capacidad12 = EVController.loadEVsegunTipo(inputCapacidad12, Text.EV_NUM_DOUBLE, evsPorNombre,
				"capacidad12");
		Evolucion<Double> capacidad21 = EVController.loadEVsegunTipo(inputCapacidad21, Text.EV_NUM_DOUBLE, evsPorNombre,
				"capacidad21");
		Evolucion<Double> perdidas12 = EVController.loadEVsegunTipo(inputPerdidas12, Text.EV_NUM_DOUBLE, evsPorNombre,
				"perdidas12");
		Evolucion<Double> perdidas21 = EVController.loadEVsegunTipo(inputPerdidas21, Text.EV_NUM_DOUBLE, evsPorNombre,
				"perdidas21");
		Evolucion<Integer> cantModInst = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT, evsPorNombre,
				"cantModInst");
		Integer cantModIni = 0;
		if (UtilStrings.esNumeroEntero(inputCantModIni.getText())) {
			cantModIni = Integer.parseInt(inputCantModIni.getText());
		}
		Evolucion<Double> dispMedia = EVController.loadEVsegunTipo(inputDispMedia, Text.EV_NUM_DOUBLE, evsPorNombre,
				"dispMedia");
		Evolucion<Double> tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo, Text.EV_NUM_DOUBLE,
				evsPorNombre, "tMedioArreglo");
		Evolucion<Integer> mantProgramado = EVController.loadEVsegunTipo(inputMantProg, Text.EV_NUM_INT, evsPorNombre,
				"mantProg");
		Evolucion<Double> costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE, evsPorNombre,
				"costoFijo");
		boolean salidaDetallada = inputSalidaDetallada.isSelected();

		if (edicion) {
			datosDuctoCombCorrida.setNombre(nombre);
			datosDuctoCombCorrida.setBarra1(barra1);
			datosDuctoCombCorrida.setBarra2(barra2);
			datosDuctoCombCorrida.setCapacidad12(capacidad12);
			datosDuctoCombCorrida.setCapacidad21(capacidad21);
			datosDuctoCombCorrida.setPerdidas12(perdidas12);
			datosDuctoCombCorrida.setPerdidas21(perdidas21);
			datosDuctoCombCorrida.setCantModInst(cantModInst);
			datosDuctoCombCorrida.setCantModIni(cantModIni);
			datosDuctoCombCorrida.setDispMedia(dispMedia);
			datosDuctoCombCorrida.settMedioArreglo(tMedioArreglo);
			datosDuctoCombCorrida.setMantProgramado(mantProgramado);
			datosDuctoCombCorrida.setCostoFijo(costoFijo);
			datosDuctoCombCorrida.setSalDetallada(salidaDetallada);
		} else {
			datosDuctoCombCorrida = new DatosDuctoCombCorrida(nombre, cantModInst, barra1, barra2, capacidad12,
					capacidad21, perdidas12, perdidas21, cantModIni, dispMedia, tMedioArreglo, salidaDetallada,
					mantProgramado, costoFijo);
		}
	}

	/**
	 * Método para obtener los datos del DatosDuctoCombCorrida y ponerlos en la
	 * interfaz (solo en modo edición)
	 */
	private void unloadData() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		System.out.println("UNLOAD DATA-DUCTO");
		inputNombre.setText(datosDuctoCombCorrida.getNombre());
		if (datosDuctoCombCorrida.getBarra1() != null) {
			inputBarra1.getSelectionModel().select(datosDuctoCombCorrida.getBarra1());
		}

		if (datosDuctoCombCorrida.getBarra2() != null) {
			inputBarra2.getSelectionModel().select(datosDuctoCombCorrida.getBarra2());
		}

		if (datosDuctoCombCorrida.getBarra1() != null) {
			inputBarra1.getSelectionModel().select(datosDuctoCombCorrida.getBarra1());
		}

		if (datosDuctoCombCorrida.getBarra2() != null) {
			inputBarra2.getSelectionModel().select(datosDuctoCombCorrida.getBarra2());
		}

		if (datosDuctoCombCorrida.getCapacidad12() != null) {
			inputCapacidad12.setText(datosDuctoCombCorrida.getCapacidad12().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.getCapacidad12(), "capacidad12", inputCapacidad12EV, Text.EV_NUM_DOUBLE, null,
				inputCapacidad12);

		if (datosDuctoCombCorrida.getCapacidad21() != null) {
			inputCapacidad21.setText(datosDuctoCombCorrida.getCapacidad21().getValor(instanteActual).toString());
			evForField(datosDuctoCombCorrida.getCapacidad21(), "capacidad21", inputCapacidad21EV, Text.EV_NUM_DOUBLE,
					null, inputCapacidad21);
		}

		if (datosDuctoCombCorrida.getPerdidas12() != null) {
			inputPerdidas12.setText(datosDuctoCombCorrida.getPerdidas12().getValor(instanteActual).toString());
			evForField(datosDuctoCombCorrida.getPerdidas12(), "perdidas12", inputPerdidas12EV, Text.EV_NUM_DOUBLE, null,
					inputPerdidas12);
		}

		if (datosDuctoCombCorrida.getPerdidas21() != null) {
			inputPerdidas21.setText(datosDuctoCombCorrida.getPerdidas21().getValor(instanteActual).toString());
			evForField(datosDuctoCombCorrida.getPerdidas21(), "perdidas21", inputPerdidas21EV, Text.EV_NUM_DOUBLE, null,
					inputPerdidas21);
		}

		if (datosDuctoCombCorrida.getCantModInst() != null) {
			inputCantModInst.setText(datosDuctoCombCorrida.getCantModInst().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null,
				inputCantModInst);

		if (datosDuctoCombCorrida.getCantModIni() != null) {
			inputCantModIni.setText(datosDuctoCombCorrida.getCantModIni().toString());
		}

		if (datosDuctoCombCorrida.getDispMedia() != null) {
			inputDispMedia.setText(datosDuctoCombCorrida.getDispMedia().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null,
				inputDispMedia);

		if (datosDuctoCombCorrida.gettMedioArreglo() != null) {
			inputTMedioArreglo.setText(datosDuctoCombCorrida.gettMedioArreglo().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE,
				null, inputTMedioArreglo);

		if (datosDuctoCombCorrida.getMantProgramado() != null) {
			inputMantProg.setText(datosDuctoCombCorrida.getMantProgramado().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null,
				inputMantProg);

		if (datosDuctoCombCorrida.getCostoFijo() != null) {
			inputCostoFijo.setText(datosDuctoCombCorrida.getCostoFijo().getValor(instanteActual).toString());
		}
		evForField(datosDuctoCombCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null,
				inputCostoFijo);

		inputSalidaDetallada.setSelected(datosDuctoCombCorrida.isSalDetallada());
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if (inputNombre.getText().trim().equals("")) {
			errores.add("Editor Ducto sin nombre.");
		}
		if (inputBarra1.getSelectionModel() == null) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " sin barra 1 seleccionada.");
		}
		if (inputBarra2.getSelectionModel() == null) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " sin barra 2 seleccionada.");
		}

		if (inputCapacidad12.getText().trim().equals("")) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 1 2 vacio.");
		}
		if (!UtilStrings.esNumeroDouble(inputCapacidad12.getText())) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 1 2 no es dato numérico.");
		}

		if (inputCapacidad21.getText().trim().equals("")) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 2 1 vacio.");
		}
		if (!UtilStrings.esNumeroDouble(inputCapacidad21.getText())) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 2 1 no es dato numérico.");
		}

		if (inputPerdidas12.getText().trim().equals("")) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Perdidad 1 2 vacio.");
		}
		if (!UtilStrings.esNumeroDouble(inputPerdidas12.getText())) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 1 2 no es dato numérico.");
		}

		if (inputPerdidas21.getText().trim().equals("")) {
			errores.add("DEditor ucto: " + inputNombre.getText() + " Perdidad 2 1 vacio.");
		}
		if (!UtilStrings.esNumeroDouble(inputPerdidas21.getText())) {
			errores.add("Editor Ducto: " + inputNombre.getText() + " Capacidad 2 1 no es dato numérico.");
		}

		if (inputCantModInst.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Cantidad módulos instalados vacío.");
		if (!UtilStrings.esNumeroEntero(inputCantModInst.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Cantidad módulos instalados no es entero.");

		if (inputCantModIni.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Cantidad módulos inicial vacío.");
		if (!UtilStrings.esNumeroEntero(inputCantModIni.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Cantidad módulos inicial no es entero.");

		if (inputTMedioArreglo.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Tiempo medio de arreglo vacío.");
		if (!UtilStrings.esNumeroDouble(inputTMedioArreglo.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Tiempo medio de arreglo no es double.");

		if (inputMantProg.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Mantenimiento programado vacío.");
		if (!UtilStrings.esNumeroEntero(inputMantProg.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Mantenimiento programado no es entero.");

		if (inputDispMedia.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Disponibilidad media vacío.");
		if (!UtilStrings.esNumeroDouble(inputDispMedia.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Disponibilidad media no es double.");

		if (inputCostoFijo.getText().trim().equalsIgnoreCase(""))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Costo fijo vacío.");
		if (!UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim()))
			errores.add("Editor Ducto: " + inputNombre.getText() + " Costo fijo no es double.");

		return errores;
	}
}
