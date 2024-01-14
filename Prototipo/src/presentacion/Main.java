/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Main is part of MOP.
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

package presentacion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import datatypes.DatosCorrida;
import logica.CorridaHandler;
import logica.EstudioHandler;
import persistencia.PersistenciaHandler;
import problema.ProblemaHandler;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorDireccionArchivoDirectorio;
import utilitarios.LectorPropiedades;
import utilitarios.ProfilerBasicoTiempo;

/**
 * Programa principal
 * 
 * @author ut602614
 *
 */
public class Main {

	private JFrame frame;
	private PresentacionHandler ph;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		setPh(PresentacionHandler.getInstance());
		initialize();
		// pizarron = new PizarronRedis();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame(" MOP" + Constantes.VERSION_ET);
		frame.setBounds(100, 100, 550, 630);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		ImageIcon img = new ImageIcon(".\\resources\\logo.png");
		frame.setIconImage(img.getImage());

		JButton btnNewButton = new JButton("Cargar Corrida");

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String ruta_xml = null;
				LectorPropiedades lprop = new LectorPropiedades("./resources/mop.conf");
				try {
					ruta_xml = lprop.getProp("rutaEntradas");
				} catch (IOException e) {
					e.printStackTrace();
				}

				JFileChooser fileChooser = new JFileChooser(ruta_xml);
				fileChooser.setPreferredSize(new Dimension(1000, 430));
				fileChooser.setDialogTitle("ELIJA EL ARCHIVO .xml DE LA CORRIDA");
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String ruta = selectedFile.getAbsolutePath();

					ph.cargarCorrida(ruta, false, true);
				}
			}
		};
		btnNewButton.addActionListener(al);

		btnNewButton.setBounds(45, 39, 144, 40);
		frame.getContentPane().add(btnNewButton);
		

		JButton btnNewButton_1 = new JButton("Simular");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (ph.hayResoptim()) {
					CorridaHandler ch = CorridaHandler.getInstance();
					ch.recargarSimulable();
					ph.simular();
				} else {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnValue = fileChooser.showOpenDialog(null);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();

						String ruta = selectedFile.getAbsolutePath();

						ph.simular(ruta);
					}
				}
				CorridaHandler.deleteInstance();
				EstudioHandler.deleteInstance();
				ProblemaHandler.deleteInstance();
				ConstructorReportes.deleteInstance();
				ProfilerBasicoTiempo.deleteInstance();
				PresentacionHandler.deleteInstance();
				PersistenciaHandler.deleteInstance();
				ph = PresentacionHandler.getInstance();
			}
		});
		btnNewButton_1.setBounds(45, 171, 144, 40);
		frame.getContentPane().add(btnNewButton_1);
		

		JButton btnNewButton_2 = new JButton("Optimizar");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ph.optimizar();
			}
		});
		btnNewButton_2.setBounds(45, 105, 144, 40);
		frame.getContentPane().add(btnNewButton_2);
		

		JButton btnNewButton_3 = new JButton("Simular Proc.Estocásticos");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ph.simularProcesosEstocasticos();
			}
		});
		btnNewButton_3.setBounds(18, 236, 194, 40);
		frame.getContentPane().add(btnNewButton_3);

		JButton btnNewButton_4 = new JButton("Sortear P.Est.de Optimización");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ph.sortearPEOptim();
			}
		});
		btnNewButton_4.setBounds(6, 300, 216, 40);
		frame.getContentPane().add(btnNewButton_4);

		JButton btnNewButton_5 = new JButton("Cargar Estudio");
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String ruta_xml = null;
				LectorPropiedades lprop = new LectorPropiedades("./resources/mop.conf");
				try {
					ruta_xml = lprop.getProp("rutaEntradas");
				} catch (IOException e) {
					e.printStackTrace();
				}

				JFileChooser fileChooser = new JFileChooser(ruta_xml);
				fileChooser.setPreferredSize(new Dimension(1000, 400));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String ruta = selectedFile.getAbsolutePath();

					ph.cargarEstudio(ruta);
				}
			}
		});
		btnNewButton_5.setBounds(45, 364, 144, 40);
		frame.getContentPane().add(btnNewButton_5);

		JButton btnNewButton_6 = new JButton("Ejecutar Estudio");
		btnNewButton_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ph.ejecutarEstudio();
			}
		});
		btnNewButton_6.setBounds(45, 428, 144, 40);
		frame.getContentPane().add(btnNewButton_6);

		JButton btnNewButton_7 = new JButton("Resultados por int.muestreo");
		ActionListener al_7 = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String rutaDirPoste = "";
				String rutaNumpos = "";

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Elija directorio de los archivos de salidas por poste");
				fileChooser.setPreferredSize(new Dimension(1000, 400));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					rutaDirPoste = selectedFile.getAbsolutePath();
				}

				JFileChooser fileChooser2 = new JFileChooser();
				fileChooser2.setDialogTitle("Elija archivo numpos");
				fileChooser2.setPreferredSize(new Dimension(1000, 400));
				int returnValue2 = fileChooser2.showOpenDialog(null);
				if (returnValue2 == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser2.getSelectedFile();
					rutaNumpos = selectedFile.getAbsolutePath();

				}
				System.out.println("Se calculan resultados por poste \n" + "ruta directorio de origen " + rutaDirPoste
						+ "\nruta numpos " + rutaNumpos);

				ph.resultadosPorIM(rutaDirPoste, rutaNumpos);

			}
		};
		btnNewButton_7.addActionListener(al_7);
		btnNewButton_7.setBounds(10, 490, 210, 40);
		frame.getContentPane().add(btnNewButton_7);
		
		
		
		JButton btnNewButton_8 = new JButton("Reprocesar escenarios corrida previa");
		btnNewButton_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String rutaDirEscPrevia = "";
				String rutaOtrosDatos = "";
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Elija directorio de escenarios serializados de la corrida previa");
				fileChooser.setPreferredSize(new Dimension(1000, 400));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					rutaDirEscPrevia = selectedFile.getAbsolutePath();
				}
				
				JFileChooser fileChooser2 = new JFileChooser();
				fileChooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser2.setDialogTitle("Elija directorio donde leer otros archivos de datos del reproceso");
				fileChooser2.setPreferredSize(new Dimension(1000, 400));
				returnValue = fileChooser2.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser2.getSelectedFile();
					rutaOtrosDatos = selectedFile.getAbsolutePath();
				}

				System.out.println("Se calculan reportes \n" + "ruta escenario serializados " + rutaDirEscPrevia);

				ph.reprocesarEscenariosCorrida(rutaDirEscPrevia, rutaOtrosDatos);

			}
		});
		btnNewButton_8.setBounds(250, 236, 264, 40);
		frame.getContentPane().add(btnNewButton_8);
		
		JButton btnNewButton_9 = new JButton("Reprocesar corrida previa por completo");
		btnNewButton_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String rutaDirEscPrevia = "";

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Elija directorio de escenarios serializados de la corrida previa");
				fileChooser.setPreferredSize(new Dimension(1000, 400));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					rutaDirEscPrevia = selectedFile.getAbsolutePath();
				}
				
				ph.simularConSerializados(rutaDirEscPrevia);

			}
		});
		btnNewButton_9.setBounds(250, 166, 264, 40);
		frame.getContentPane().add(btnNewButton_9);
        
        JButton btnNewButton_10 = new JButton("DESPACHO CORTO PLAZO");
        btnNewButton_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean soloDirectorio = true;
				String dirArchConf = "resources/MOP.conf";
				String nombreProp = "rutaCP";

				String titulo1 = "ELIJA EL DIRECTORIO DE TRABAJO, EN EL DEBE HABER SUBDIRECTORIOS entradas y salidas ";
				String dirTrabajo = LectorDireccionArchivoDirectorio.direccionLeida2(soloDirectorio, titulo1, dirArchConf,
						nombreProp);

				String dirEntradas = dirTrabajo + "/entradas";
				String dirSalidas = dirTrabajo + "/salidas";
				if(!DirectoriosYArchivos.existeDirectorio(dirSalidas)) {
					DirectoriosYArchivos.creaDirectorio(dirTrabajo, "salidas");
				}
				
				ph.despacharCP(dirEntradas, dirSalidas);
			}
		});
        btnNewButton_10.setBounds(250, 326, 264, 40);
		frame.getContentPane().add(btnNewButton_10);
		

	}

	public PresentacionHandler getPh() {
		return ph;
	}

	public void setPh(PresentacionHandler ph) {
		this.ph = ph;
	}

}
