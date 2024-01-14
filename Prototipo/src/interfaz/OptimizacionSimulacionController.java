/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * OptimizacionSimulacionController is part of MOP.
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
import datatypes.DatosCorrida;
import datatypes.DatosIteracionesCorrida;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;
import logica.CorridaHandler;
import org.controlsfx.control.PopOver;
import persistencia.CargadorXML;
import presentacion.PresentacionHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import javax.swing.*;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class OptimizacionSimulacionController extends GeneralidadesController{

    @FXML private JFXProgressBar progressBarOpt;
    @FXML private Label labelProgressOpt;
    @FXML private JFXProgressBar progressBarSim;
    @FXML private Label labelProgressSim;
    @FXML private JFXButton btnOptimizar;
    @FXML private JFXButton btnSimular;
    @FXML private JFXButton btnOptimizarYSimular;
    @FXML private JFXButton btnCancelar;
    @FXML private JFXCheckBox inputParalelo;


    //INPUTS DATA OPT/SIM
    @FXML private TextArea inputDescripcionCorrida;
    @FXML private JFXTextField inputTasaAnual;
    @FXML private JFXTextField inputCantEscenarios;
    //@FXML private  JFXCheckBox inputEscenariosSerializados;
    @FXML private JFXTextField inputSemilla;
    @FXML private JFXTextField inputCantSorteosMonteCarlo;
    @FXML private JFXTextField inputRutaSalidas;
    @FXML private JFXTextField inputNombreCorrida;
    @FXML private JFXRadioButton inputTipoSimEnHaz;
    @FXML private JFXRadioButton inputTipoSimEnCadena;
    @FXML private JFXRadioButton inputTipoResolucionValBellmanIncrementos;
    @FXML private JFXRadioButton inputTipoResolucionValBellmanHiperplanos;
    @FXML private JFXRadioButton inputTipoDemandaTotal;
    @FXML private JFXRadioButton inputTipoDemandaResidual;
    @FXML private JFXButton inputRutaSalidasBtn;
    //INPUTS DATA ITERACIONES
    @FXML private JFXTextField inputNumIteraciones;
    @FXML private JFXTextField inputMaxIteraciones;
    @FXML private JFXComboBox<String> inputCriterioParada;

    @FXML private JFXButton XMLbtn;
    @FXML private JFXButton btnEVOPtSim;

    @FXML private JFXTextField inputTopeSpot;

    @FXML private  JFXCheckBox inputDespSinExp;
    @FXML private JFXTextField inputIteracionSinExp;
    @FXML private JFXTextField inputPaisesACortar;

    @FXML private JFXCheckBox inputValorAplicaEnOpt;

    boolean avisoEscritura = false;


    public DatosCorrida datosCorrida = new DatosCorrida();
    public PresentacionHandler ph;
    public Manejador padre;

    private String rutaXMLGenerado;

    public OptimizacionSimulacionController(Manejador manejador, DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
        this.padre = manejador;
    }


    private void cambiarVisibilidadBotonesLanzarCorrida(Boolean estaCorriendo){

        btnOptimizar.setDisable(estaCorriendo);
        btnSimular.setDisable(estaCorriendo);
        btnOptimizarYSimular.setDisable(estaCorriendo);

    }

    private void cambiarVisibilidadProgressBar(int codigoCorrida){

        if(codigoCorrida == 1) {
            labelProgressOpt.setVisible(false);
            progressBarOpt.setVisible(false);
        }else if(codigoCorrida == 2) {
            labelProgressSim.setVisible(false);
            progressBarSim.setVisible(false);
        }
    }

    private boolean generarXML(){
        boolean guardo = false;
        try {
            String nombreCorrida = datosCorrida.getNombre();
            rutaXMLGenerado="XML\\" + nombreCorrida + ".xml";
            guardo = padre.generateXML(rutaXMLGenerado);
            System.out.println("Guardo xml: " + guardo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return guardo;

    }

    private void lanzarOptimizacion() {
        ArrayList<String> errores = padre.controlDatosCompletos();
        if(errores.size() > 1 ){
            padre.mostrarMensajeDatosIncompletos(errores, "No se puede optimizar. Hay datos incompletos o erroneos");
        } else {
            long time_start;
            time_start = System.currentTimeMillis();

            boolean guardo = generarXML();
            if (!guardo) {
                mostrarMensaje("No se pudo guardar la corrida.");
            } else {
                cambiarVisibilidadBotonesLanzarCorrida(true);
                btnCancelar.setDisable(false);
                if (Text.MOSTRAR_MENSAJE_AL_LANZAR_CORRIDA) {
                    labelProgressOpt.textProperty().unbind();
                    progressBarOpt.progressProperty().unbind();
                    updateProgressOptSim(false, 0);
                    progressBarOpt.setVisible(true);

                    Task<Void> optTask = new Task<>() {
                        @Override
                        public Void call() {
                            updateProgress(0, 1);
                            ph.cargarCorrida(rutaXMLGenerado, false, true);
                            ph.optProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress(newProgress.doubleValue(), 1));
                            ph.optimizar();
                            return null;
                        }
                    };

                    labelProgressOpt.textProperty().bind(Bindings.concat("Optimizando:   ", optTask.progressProperty().multiply(100).asString("%.0f"), "%"));
                    progressBarOpt.progressProperty().bind(optTask.progressProperty());

                    optTask.setOnSucceeded(event -> {

                        cambiarVisibilidadBotonesLanzarCorrida(false);
                        long time_end = System.currentTimeMillis();
                        time_end = (time_end - time_start) / (1000);
                        mostrarMensajeFinCorrida(time_end, "Finalizó la optimización. ");
                        cambiarVisibilidadProgressBar( 1);
                        btnCancelar.setDisable(true);

                    });

                    Thread thread = new Thread(optTask);
                    thread.setDaemon(true);
                    thread.start();
                } else {

                    //Se realiza la optimizacion en el mismo hilo para ver errores en la consola
                    ph.cargarCorrida(rutaXMLGenerado, false, true);
                    ph.optimizar();
                    cambiarVisibilidadBotonesLanzarCorrida(false);
                    btnCancelar.setDisable(true);

                }
            }
        }
    }
    private void lanzarSimulacion(String ruta) {
        ArrayList<String> errores = padre.controlDatosCompletos();
        if(errores.size() > 1 ){
            padre.mostrarMensajeDatosIncompletos(errores, "No se puede simular. Hay datos incompletos o erroneos");
        } else {
            long time_start;
            time_start = System.currentTimeMillis();

            boolean guardo = generarXML();
            if (!guardo) {
                mostrarMensaje("No se pudo guardar la corrida.");
            } else {
                cambiarVisibilidadBotonesLanzarCorrida(true);
                btnCancelar.setDisable(false);
                if (Text.MOSTRAR_MENSAJE_AL_LANZAR_CORRIDA) {
                    progressBarSim.setVisible(true);
                    labelProgressSim.setVisible(true);
                    Task<Void> simTask;
                    if (ruta.equals("")) {
                        simTask = new Task<>() {
                            @Override
                            public Void call() {
                                avisoEscritura = false;
                                updateProgress(0, 1);
                                ph.simProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress(newProgress.doubleValue(), 1));
                                ph.escritorProgressProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                        // Ejecuta el código del Alert en el UI thread
                                        Platform.runLater(() -> {
                                            if (newValue && !avisoEscritura) {
                                                mostrarMensaje("Espere mientras se escriben los resultados en: " + datosCorrida.getRutaSals());
                                                avisoEscritura = true;
                                            }
                                        });
                                    }
                                });
                                CorridaHandler ch = CorridaHandler.getInstance();
                                ch.recargarSimulable();
                                ph.simular();
                                return null;
                            }
                        };
                    } else {
                        simTask = new Task<>() {
                            @Override
                            public Void call() {
                                updateProgress(0, 1);
                                ph.simProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress(newProgress.doubleValue(), 1));
                                ph.simular(ruta);
                                return null;
                            }
                        };
                    }

                    labelProgressSim.textProperty().bind(Bindings.concat("Simulando:   ", simTask.progressProperty().multiply(100).asString("%.0f"), "%"));
                    progressBarSim.progressProperty().bind(simTask.progressProperty());

                    simTask.setOnSucceeded(event -> {
                        cambiarVisibilidadBotonesLanzarCorrida(false);
                        padre.actualizarRenderizarReportes();
                        long time_end = System.currentTimeMillis();
                        time_end = (time_end - time_start) / (1000);
                        mostrarMensajeFinCorrida(time_end, "Finalizó la simulación.");
                        cambiarVisibilidadProgressBar(2);
                        btnCancelar.setDisable(true);
                    });

                    Thread thread = new Thread(simTask);
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    //Se realiza la simulacion en el mismo hilo para ver errores en la consola
                    if (ruta.equals("")) {
                        CorridaHandler ch = CorridaHandler.getInstance();
                        ch.recargarSimulable();
                        ph.simular();
                    } else {
                        ph.simular(ruta);
                    }
                    cambiarVisibilidadBotonesLanzarCorrida(false);
                    padre.actualizarRenderizarReportes();
                }
            }
        }
    }

    private void lanzarOptimizacionSimulacion(){
        ArrayList<String> errores = padre.controlDatosCompletos();
        if(errores.size() > 1 ){
            padre.mostrarMensajeDatosIncompletos(errores, "No se puede simular. Hay datos incompletos o erroneos");
        } else {
            boolean guardo = generarXML();
            if (!guardo) {
                mostrarMensaje("No se pudo guardar la corrida.");
            } else {
                cambiarVisibilidadBotonesLanzarCorrida(true);
                btnCancelar.setDisable(false);
                if (Text.MOSTRAR_MENSAJE_AL_LANZAR_CORRIDA) {
                    long time_start;
                    time_start = System.currentTimeMillis();

                    progressBarOpt.setVisible(true);

                    labelProgressOpt.textProperty().unbind();
                    progressBarOpt.progressProperty().unbind();
                    updateProgressOptSim(false, 0);
                    Task<Void> optTask = new Task<>() {
                        @Override
                        public Void call() {
                            updateProgress(0, 1);
                            ph.cargarCorrida(rutaXMLGenerado, false, true);
                            ph.optProgressProperty().addListener((obs, oldProgress, newProgress) -> updateProgress(newProgress.doubleValue(), 1));
                            ph.optimizar();
                            return null;
                        }
                    };

                    labelProgressOpt.textProperty().bind(Bindings.concat("Optimizando:   ", optTask.progressProperty().multiply(100).asString("%.0f"), "%"));
                    progressBarOpt.progressProperty().bind(optTask.progressProperty());

                    optTask.setOnSucceeded(event -> {
                        if(!btnCancelar.isDisable()){
                            cambiarVisibilidadBotonesLanzarCorrida(false);
                            btnCancelar.setDisable(true);
                            btnSimular.fire();
                            long time_end = System.currentTimeMillis();
                            time_end = (time_end - time_start) / (1000);
                            mostrarMensajeFinCorrida(time_end, "Finalizó la optimización.");
                            cambiarVisibilidadProgressBar(1);
                        }

                    });

                    Thread thread = new Thread(optTask);
                    thread.setDaemon(true);
                    thread.start();
                } else {

                    ph.cargarCorrida(rutaXMLGenerado, false, true);
                    ph.optimizar();
                    CorridaHandler ch = CorridaHandler.getInstance();
                    ch.recargarSimulable();
                    ph.simular();
                    cambiarVisibilidadBotonesLanzarCorrida(false);

                }
            }
        }
    }


    @FXML
    public void initialize() {

        inputRutaSalidasBtn.setOnAction(actionEvent -> rutaSalidasFileChooser());
        ph = PresentacionHandler.getInstance();

        //OPTIMIZAR
        btnOptimizar.setOnAction(actionEvent -> {

            try {
                lanzarOptimizacion();
            }catch (Exception e ){
                e.printStackTrace();
                cambiarVisibilidadBotonesLanzarCorrida(false);
            }
        });


        //SIMULAR
        btnSimular.setOnAction(actionEvent -> {

            try {
                if (ph.hayResoptim()) {
                    lanzarSimulacion("");
                }else{
                    JFileChooser fileChooser = new JFileChooser("d:\\salidasModeloOp");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        String ruta = selectedFile.getAbsolutePath();
                        lanzarSimulacion(ruta);
                    }
                }
            }catch (Exception e ){
                e.printStackTrace();
                cambiarVisibilidadBotonesLanzarCorrida(false);
            }



        });


        //OPTIMIZAR Y SIMULAR
        btnOptimizarYSimular.setOnAction(actionEvent -> {

            try {
                lanzarOptimizacionSimulacion();
            }catch (Exception e ){
                e.printStackTrace();
                cambiarVisibilidadBotonesLanzarCorrida(false);
            }


        });

        btnCancelar.setOnAction(actionEvent -> {
            ph.cancelarOptimizacion();
            ph.cancelarSimulacion();
            cambiarVisibilidadBotonesLanzarCorrida(false);
            btnCancelar.setDisable(true);

        });
        btnCancelar.setDisable(true);

        //POPULAR LISTAS
        inputCriterioParada.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.PORNUMEROITERACIONES, Constantes.PORUNANIMIDADPARTICIPANTES)));
        inputCriterioParada.getSelectionModel().selectFirst();//default

        XMLbtn.setOnAction(actionEvent -> {
            try {
                String nombreCorrida = datosCorrida.getNombre();
                rutaXMLGenerado="XML\\" + nombreCorrida + ".xml";
                padre.generateXML(rutaXMLGenerado);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        //EV en opt/sim
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EV.fxml"));
            AnchorPane newLoadedPane = loader.load();
            PopOver popOver = new PopOver(newLoadedPane);
            popOver.setTitle("EV");

            btnEVOPtSim.setOnAction(actionEvent -> {
//                loadData();
//                popOver.show(btnEVOPtSim);
                // TODO: 04/08/2021 para probar, sacar luego
//                try {
//                    System.out.println(MongoDBHandler.getInstance().authUser("admin", "admin"));
//                    System.out.println(MongoDBHandler.getInstance().getBibliotecas(user));
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    getParticipanteDelPortaPapeles();
//                } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
//                    e.printStackTrace();
//                }



            });
        }catch(Exception e) {
            e.printStackTrace();
        }


    }

    private void mostrarMensajeFinCorrida(long demora, String msje) {
        String textoMsje = "";
        if(demora > 60*60){
            textoMsje =  " Tiempo: " + demora/(60*60) + " horas, " +  (demora%(60*60)-demora%60)  +" minutos, " + demora%60 + " segundos.";
        }else{
            textoMsje  = msje + " Tiempo: " + demora/60 +" minutos, " + demora%60 + " segundos.";
        }
        mostrarMensaje(textoMsje);

    }

    private void mostrarMensaje(String msje) {
        System.out.println("FIN DE LA CORRIDA");
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MostrarMensaje.fxml"));

            MostrarMensajeController ventanaMsje = new MostrarMensajeController(msje , padre, 0);
            loader.setController(ventanaMsje);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            padre.getMainPane().setEffect(bb);

            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unloadData() {

        if(datosCorrida.getNombre() !=null) {
        	long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
            inputDescripcionCorrida.setText(datosCorrida.getDescripcion());
            inputTasaAnual.setText(String.valueOf(datosCorrida.getTasa()));
            inputCantEscenarios.setText(String.valueOf(datosCorrida.getCantEscenarios()));
            //inputEscenariosSerializados.setSelected(datosCorrida.getEscenariosSerializados());
            inputSemilla.setText(String.valueOf(datosCorrida.getSemilla()));
            inputCantSorteosMonteCarlo.setText(String.valueOf(datosCorrida.getCantSorteosMont().getValor(instanteActual)));
            inputTipoSimEnHaz.setSelected(datosCorrida.getTipoSimulacion().equalsIgnoreCase("enHaz"));
            inputTipoSimEnCadena.setSelected(datosCorrida.getTipoSimulacion().equalsIgnoreCase("enCadena"));
            inputRutaSalidas.setText(datosCorrida.getRutaSals());
            inputNombreCorrida.setText(datosCorrida.getNombre());
            inputNumIteraciones.setText(String.valueOf(datosCorrida.getDatosIteraciones().getNumIteraciones()));
            inputMaxIteraciones.setText(String.valueOf(datosCorrida.getDatosIteraciones().getMaximoIteraciones()));
            inputCriterioParada.getSelectionModel().select(datosCorrida.getDatosIteraciones().getCriterioParada());//TODO: probar
            inputTipoResolucionValBellmanIncrementos.setSelected(datosCorrida.getValoresComportamientoGlobal().get(Constantes.COMPVALORESBELLMAN).getValor(instanteActual).equalsIgnoreCase(Constantes.PROBINCREMENTOS));
            inputTipoResolucionValBellmanHiperplanos.setSelected(datosCorrida.getValoresComportamientoGlobal().get(Constantes.COMPVALORESBELLMAN).getValor(instanteActual).equalsIgnoreCase(Constantes.PROBHIPERPLANOS));
            inputTipoDemandaTotal.setSelected(datosCorrida.getValoresComportamientoGlobal().get(Constantes.COMPDEMANDA).getValor(instanteActual).equalsIgnoreCase(Constantes.DEMTOTAL));
            inputTipoDemandaResidual.setSelected(datosCorrida.getValoresComportamientoGlobal().get(Constantes.COMPDEMANDA).getValor(instanteActual).equalsIgnoreCase(Constantes.DEMRESIDUAL));

            inputTopeSpot.setText(String.valueOf(datosCorrida.getTopeSpot()));
            inputDespSinExp.setSelected(datosCorrida.isDespSinExp());
            inputIteracionSinExp.setText(String.valueOf(datosCorrida.getIteracionSinExp()));
            inputPaisesACortar.setText(UtilStrings.arrayStringAtexto(datosCorrida.getPaisesACortar(), ","));


        }
    }

    public void loadData() {

        datosCorrida.setDescripcion(inputDescripcionCorrida.getText());
        datosCorrida.setTasa(Double.valueOf(inputTasaAnual.getText()));
        datosCorrida.setCantEscenarios(Integer.valueOf(inputCantEscenarios.getText()));
        datosCorrida.setSemilla(Double.valueOf(inputSemilla.getText()));
        datosCorrida.setCantSorteosMont(new EvolucionConstante<>(Integer.parseInt(inputCantSorteosMonteCarlo.getText()), new SentidoTiempo(1)));

        datosCorrida.setTipoSimulacion(inputTipoSimEnHaz.isSelected() ? "enHaz" : "enCadena");
        datosCorrida.setRutaSals(inputRutaSalidas.getText());
        datosCorrida.setNombre(inputNombreCorrida.getText());
        Hashtable<String, Evolucion<String>> valoresComportamientoGlobal = new Hashtable<>();
        Evolucion<String> tipoResolucionValBellman = new EvolucionConstante<>(inputTipoResolucionValBellmanIncrementos.isSelected() ? Constantes.PROBINCREMENTOS : Constantes.PROBHIPERPLANOS, new SentidoTiempo(1));
        Evolucion<String> tipoDemanda = new EvolucionConstante<>(inputTipoDemandaTotal.isSelected() ? Constantes.DEMTOTAL : Constantes.DEMRESIDUAL, new SentidoTiempo(1));
        valoresComportamientoGlobal.put(Constantes.COMPVALORESBELLMAN, tipoResolucionValBellman);
        valoresComportamientoGlobal.put(Constantes.COMPDEMANDA, tipoDemanda);
        datosCorrida.setValoresComportamientoGlobal(valoresComportamientoGlobal);

        //DATA ITERACIONES CORRIDA
        DatosIteracionesCorrida datosIteracionesCorrida = new DatosIteracionesCorrida();
        datosIteracionesCorrida.setNumIteraciones(Integer.parseInt(inputNumIteraciones.getText()));
        datosIteracionesCorrida.setMaximoIteraciones(Integer.parseInt(inputMaxIteraciones.getText()));
        datosIteracionesCorrida.setCriterioParada(inputCriterioParada.getValue());
        datosCorrida.setDatosIteraciones(datosIteracionesCorrida);

        //TODO: controlar tipo de dato sea double o int
        datosCorrida.setTopeSpot( Double.parseDouble(inputTopeSpot.getText()));
        datosCorrida.setDespSinExp(inputDespSinExp.isSelected());
        datosCorrida.setIteracionSinExp(Integer.parseInt(inputIteracionSinExp.getText()));

        CargadorXML cargadorXML = new CargadorXML();
        ArrayList<String> paisesACortar = cargadorXML.generarListaStringConSeparador(inputPaisesACortar.getText(), ",");
        datosCorrida.setPaisesACortar(paisesACortar);


    }

    public void clean(){
        inputDescripcionCorrida.clear();
        inputTasaAnual.clear();
        inputCantEscenarios.clear();
        //inputEscenariosSerializados.setSelected(false);
        inputSemilla.clear();
        inputCantSorteosMonteCarlo.clear();
        inputRutaSalidas.clear();
        inputNombreCorrida.clear();
        inputTipoSimEnHaz.setSelected(true);
        inputTipoSimEnCadena.setSelected(false);
        inputTipoResolucionValBellmanIncrementos.setSelected(true);
        inputTipoResolucionValBellmanHiperplanos.setSelected(false);
        inputTipoDemandaTotal.setSelected(true);
        inputTipoDemandaResidual.setSelected(false);
        inputNumIteraciones.clear();
        inputMaxIteraciones.clear();
        inputCriterioParada.getSelectionModel().clearSelection();

    }

    public void updateProgressOptSim(boolean isSim, int progress) {

        if(isSim){
            labelProgressSim.setVisible(true);
            labelProgressSim.setText("Simulando:   " + progress + "%");
            progressBarSim.setProgress(progress/100.0);
        }else{
            labelProgressOpt.setVisible(true);
            labelProgressOpt.setText("Optimizando:   " + progress + "%");
            progressBarOpt.setProgress(progress/100.0);
        }
    }

    private void rutaSalidasFileChooser(){
        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        JFXTabPane mainTabPane = padre.getMainPane();
        File selectedFile = fileChooser.showOpenDialog(mainTabPane.getScene().getWindow());
        if(selectedFile != null) {
            inputRutaSalidas.setText(selectedFile.getPath());//TODO: Pasar a relativa??
        }
    }

    public void setDatosCorrida(DatosCorrida datosCorrida) {
        this.datosCorrida = datosCorrida;
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}
