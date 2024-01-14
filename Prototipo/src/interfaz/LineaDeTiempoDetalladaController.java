/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaDeTiempoDetalladaController is part of MOP.
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
import datatypesTiempo.DatosLineaTiempo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utilitarios.DirectoriosYArchivos;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.awt.Desktop;
public class LineaDeTiempoDetalladaController extends GeneralidadesController {

    //INPUTS DATA LINEA DE TIEMPO
    @FXML private JFXDatePicker inputInicioCorrida;
    @FXML private JFXDatePicker inputFinCorrida;
    @FXML private JFXRadioButton inputPostizacionExterna;
    @FXML private JFXRadioButton inputPostizacionInterna;
    @FXML private JFXRadioButton inputValPostizacionExterna;
    @FXML private JFXRadioButton inputValPostizacionInterna;
    @FXML private JFXTextField inputPostizacionRuta;
    @FXML private JFXDatePicker inputInicioDeTiempo;
    @FXML private JFXToggleButton inputUsarPeriodoIntegracion;
    @FXML private JFXTextField inputPeriodoIntegracion;
    @FXML private JFXButton inputPostizacionRutaBtn;
    @FXML private JFXButton volverLineaTiempoButton;
    //@FXML private Label msjeValidacion;

    @FXML private Hyperlink erroresHyperlink;
    @FXML private GridPane gridPaneMain;

    @FXML private VBox vBoxBloques;
    @FXML private JFXButton nuevoBloqueButton;
    private HashMap<Integer, BloqueLineaDeTiempoController> bloquesCorrida = new HashMap<>();

    private DatosCorrida datosCorrida;

    private Manejador parentController;
    public Manejador getParentController() { return parentController; }
    public void setParentController(Manejador parentController) { this.parentController = parentController;   }


    public LineaDeTiempoDetalladaController(Manejador parentController, DatosCorrida datosCorrida){

        this.datosCorrida = datosCorrida;
        this.parentController = parentController;
    }

    @FXML
    public void initialize(){
        logicaPostizacion();
        addBloqueLineaDeTiempo();
        inputPostizacionRutaBtn.setOnAction(event -> rutaPostizacionFileChooser());
        inputInicioCorrida.setOnAction(actionEvent -> inputInicioDeTiempo.setValue(inputInicioCorrida.getValue()));
        nuevoBloqueButton.setOnAction(actionEvent -> addBloqueLineaDeTiempo());
        volverLineaTiempoButton.setOnAction(actionEvent -> volverNuevaLineaTiempo());
        popularLineaDeTiempo();
    }

    private void volverNuevaLineaTiempo() {

        String msje = "";
        ArrayList<String> bloquesConError = controlDatosCompletos();
        if(bloquesConError.size() == 0 || bloquesConError == null) {
            if(loadData()){
                if (parentController.puedoGenerarNuevaLineaDeTiempo()) {
                    parentController.popularLineaDeTiempo();
                } else {
                    msje = "Detalles de la linea no permiten volver a vista simple";
                }
            }
        }else{
            for(String s : bloquesConError)
            {   msje += s + " ";   }
        }
        if(!msje.equalsIgnoreCase("")){

            //Se muestra mensaje temporal de error y link a abrir los errores
            setLabelMessageTemporal(msje, TipoInfo.FEEDBACK);

            String direc = datosCorrida.getRutaSals() + "/listaErrores.txt";
            File file = new File(direc);
            DirectoriosYArchivos.grabaTexto(direc, bloquesConError.toString() );

            erroresHyperlink = new Hyperlink("Ver errores");
            gridPaneMain.add(erroresHyperlink,2,10);
            erroresHyperlink.setOnAction(e -> {
                if(!Desktop.isDesktopSupported())//check if Desktop is supported by Platform or not
                {
                    System.out.println("not supported");
                    return;
                }
                Desktop desktop = Desktop.getDesktop();
                if(file.exists())         //checks file exists or not
                {
                    try {
                        desktop.open(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            //Se borra link temporal a los errores
            Timer timer = new Timer();
            TimerTask borrarMsje = new TimerTask() {
                @Override
                public void run() {
                    erroresHyperlink.setVisible(false);
                }
            };
            timer.schedule(borrarMsje, 7 * 1000);


        }
    }

    private void popularLineaDeTiempo() {

        inputInicioCorrida.setValue(LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());
        inputFinCorrida.setValue(LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoFinal(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());
        inputInicioDeTiempo.setValue(LocalDateTime.parse(datosCorrida.getLineaTiempo().getTiempoInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());
//        inputInicioDeTiempo.setValue(LocalDateTime.parse(datosCorrida.getInicioCorrida(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());//igual a inicio corrida
        datosCorrida.getLineaTiempo().setTiempoInicial(inputInicioDeTiempo.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        inputPostizacionExterna.setSelected(datosCorrida.getTipoPostizacion().equalsIgnoreCase("externa"));
        inputPostizacionInterna.setSelected(datosCorrida.getTipoPostizacion().equalsIgnoreCase("interna"));
        inputValPostizacionExterna.setSelected(datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_EXTERNA));
        inputValPostizacionInterna.setSelected(datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA));
//        inputValPostizacionRuta.setText(datosCorrida.getValPostizacion());
        inputPostizacionRuta.setText(datosCorrida.getPostizacion());
        // TODO: 09/11/2020 se oculta período de integración de la interfaz
//        inputUsarPeriodoIntegracion.setSelected(datosCorrida.getLineaTiempo().isUsarPeriodoIntegracion());
//        inputPeriodoIntegracion.setText(String.valueOf(datosCorrida.getLineaTiempo().getPeriodoIntegracion()));

        //bloques
        for (int i = 1; i <= datosCorrida.getLineaTiempo().getCantBloques(); i++) {
            if (i > 1) {
                addBloqueLineaDeTiempo();
            }
            bloquesCorrida.get(i).setCantPasos(String.valueOf(datosCorrida.getLineaTiempo().getPasosPorBloque().get(i-1)));
            bloquesCorrida.get(i).setDuracionPaso(String.valueOf(datosCorrida.getLineaTiempo().getDuracionPasoPorBloque().get(i-1) / 3600));
            bloquesCorrida.get(i).setIntevaloMuestreo(String.valueOf(datosCorrida.getLineaTiempo().getIntMuestreoPorBloque().get(i-1) / 3600));
            bloquesCorrida.get(i).setPeriodoBloque(String.valueOf(datosCorrida.getLineaTiempo().getPeriodoPasoPorBloque().get(i-1)));
            bloquesCorrida.get(i).setCronologico(datosCorrida.getLineaTiempo().getCronologicos().get(i-1));
            bloquesCorrida.get(i).setDuracionPostes(datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(i-1));
        }
    }

    private void rutaPostizacionFileChooser(){
        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(vBoxBloques.getScene().getWindow());
        if(selectedFile != null) {
            inputPostizacionRuta.setText(selectedFile.getPath());//TODO: Pasar a relativa??
        }
    }


    public void addBloqueLineaDeTiempo(){

        try {
            int numeroBloque = bloquesCorrida.size() +1;
            BloqueLineaDeTiempoController bloqueController = new BloqueLineaDeTiempoController( this, vBoxBloques.getChildren().size() > 0, numeroBloque);
            bloquesCorrida.put(numeroBloque, bloqueController);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueLineaDeTiempo.fxml"));
            loader.setController(bloqueController);
            TitledPane newLoadedPane = loader.load();
            newLoadedPane.setText("Bloque " + numeroBloque);
            vBoxBloques.getChildren().add(newLoadedPane);
        }catch(Exception e) {
            e.printStackTrace();
        }

    }


    private void logicaPostizacion(){
        inputPostizacionInterna.setOnAction(actionEvent -> {
            inputValPostizacionInterna.setSelected(true);
            inputValPostizacionInterna.setDisable(true);
            inputValPostizacionExterna.setDisable(true);
            inputPostizacionRuta.setDisable(true);
        });
        inputPostizacionExterna.setOnAction(actionEvent -> {
            inputValPostizacionInterna.setDisable(false);
            inputValPostizacionExterna.setDisable(false);
            inputPostizacionRuta.setDisable(false);
        });
    }


    private void cleanLineaDeTiempoFields(){
//        inputInicioCorrida.getEditor().clear();
//        inputFinCorrida.getEditor().clear();
        inputPostizacionExterna.setSelected(true);
        inputPostizacionInterna.setSelected(false);
        inputValPostizacionExterna.setSelected(true);
        inputValPostizacionInterna.setSelected(false);
        inputPostizacionRuta.clear();
        inputUsarPeriodoIntegracion.setSelected(false);
        inputPeriodoIntegracion.clear();
    }

    public void clear(){
        vBoxBloques.getChildren().clear();
        bloquesCorrida.clear();
        addBloqueLineaDeTiempo();
        cleanLineaDeTiempoFields();
    }

    public void removeBloqueLineaDeTiempo(int id) {
        bloquesCorrida.remove(id);
    }
    /**
     *  Método para cargar los datos en el DatosLineaTiempo
     */
    public boolean loadData() {
        boolean ret = true;
        datosCorrida.setInicioCorrida(inputInicioCorrida.getValue().toString());
        datosCorrida.setInicioCorrida(inputInicioCorrida.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosCorrida.setFinCorrida(inputFinCorrida.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59");
        datosCorrida.setTipoPostizacion(inputPostizacionExterna.isSelected() ? "externa" : "interna");
        datosCorrida.setTipoValpostizacion(inputValPostizacionExterna.isSelected() ? "externa" : "interna");
        datosCorrida.setPostizacion(inputPostizacionRuta.getText());

        DatosLineaTiempo datosLineaTiempo = new DatosLineaTiempo();
        datosLineaTiempo.setTiempoInicial(inputInicioDeTiempo.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosLineaTiempo.setTiempoInicialEvoluciones(datosCorrida.getLineaTiempo().getTiempoInicialEvoluciones());
        datosLineaTiempo.setTiempoFinal(inputFinCorrida.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59");
        LocalDate fechaFinSegunBloques = inputInicioCorrida.getValue();

        Iterator it = bloquesCorrida.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, BloqueLineaDeTiempoController> entry = (Map.Entry)it.next();
            BloqueLineaDeTiempoController bloque = entry.getValue();

            int diasBloque = bloque.duracionBloqueEnSegundos()/(24*3600);
            fechaFinSegunBloques = fechaFinSegunBloques.plusDays(diasBloque);
            try {
                datosLineaTiempo.agregarBloque(bloque.getCantPasos(), bloque.getDuracionPaso(), bloque.getIntervaloMuestreo(), bloque.getCantPostes(),
                        bloque.getDuracionPostes(), bloque.getPeriodoBloque(), bloque.getCronologico());
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
                ret = false;
            }
        }
        fechaFinSegunBloques = fechaFinSegunBloques.plusDays(-1);
        if(!fechaFinSegunBloques.equals(inputFinCorrida.getValue())){
            setLabelMessageTemporal("El tiempo entre la fecha de inicio y fin de la corrida no coincide con la duración de los bloques", TipoInfo.ERROR);
            ret = false;
        }

        datosCorrida.setLineaTiempo(datosLineaTiempo);
        return ret;
    }

    /**
     * Método para obtener los datos del DatosLineaTiempo y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> ret = new ArrayList<>();

        for(BloqueLineaDeTiempoController b : bloquesCorrida.values()){
            ArrayList<String> retBloque = b.controlDatosCompletos();
            if(retBloque.size() > 0){
                ret.addAll(retBloque);
            }
        }

        return ret;
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}
