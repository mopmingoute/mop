/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Cliente is part of MOP.
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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import datatypes.DatosCorrida;
import logica.CorridaHandler;
import paralelismo.IParalelismo;
import utilitarios.Constantes;
import utilitarios.LectorPropiedades;
import utilitarios.ProfilerBasicoTiempo;
import pizarron.PizarronRedis;

/**
 * Programa principal
 * 
 * @author ut602614
 *
 */
public class Cliente {

	private JFrame frame;
	private PresentacionHandler ph;
	private static boolean levantados = false;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Cliente window = new Cliente();
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
	public Cliente() {
		setPh(PresentacionHandler.getInstance());
		initialize();
		PizarronRedis pp = PizarronRedis.getInstance();
		pp.flushDB();

		levantarHash(pp);

		pp.matarServidores();

		pp.levantarServidores();
		levantados = true;
	}

	private void levantarHash(PizarronRedis pp) {
		HashMap<String, String> hash = new HashMap<>();

//		hash.put("ruta", "\\\\ntpal/grupos2/Plaimaedfrun/Ejecutables/MOP/Ver_1.72-PARALELO");
//		hash.put("nombre", "Servidor.jar");
//		hash.put("tipo", "JDK-11.0.11");
//		hash.put("WD", hash.get("ruta"));
//		hash.put("prioridad", "0");
//		hash.put("redireccionar", "1");
//		pp.getServ().hmset("LEVANTAR", hash);

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// Pizarron piz = Main.getPizarron();
		frame = new JFrame(" MOP PARALELO" + Constantes.VERSION_ET);
		frame.setBounds(100, 100, 250, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		ImageIcon img = new ImageIcon(".\\resources\\logo.png");
		frame.setIconImage(img.getImage());

		JButton btnNewButton = new JButton("Cargar Corrida");

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PizarronRedis pp = PizarronRedis.getInstance();
				if (!levantados) {
					pp.matarServidores();
					pp.levantarServidores();
					levantados = true;
				}

				String ruta_xml = null;
				LectorPropiedades lprop = new LectorPropiedades("./resources/mop.conf");
				try {
					ruta_xml = lprop.getProp("rutaEntradas");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JFileChooser fileChooser = new JFileChooser(ruta_xml);

				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					ruta_xml = fileChooser.getSelectedFile().getAbsolutePath();
					Runtime runTime = Runtime.getRuntime();
					Process process = null;
					try {
						process = runTime.exec("net use");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InputStream inStream = process.getInputStream();
					InputStreamReader inputStreamReader = new InputStreamReader(inStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String line = null;
					String[] components = null;
					try {
						while (null != (line = bufferedReader.readLine())) {
							components = line.split("\\s+");
							if ((components.length > 2) && (components[1].equals(ruta_xml.substring(0, 2)))) {
								ruta_xml = ruta_xml.replace(components[1], components[2]);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String ruta = ruta_xml;
					System.out.println(ruta);
					ph.cargarCorridaCliente(ruta);
					// ph.simularConSerializados();
					System.out.println("CARGUÉ CORRIDA CLIENTE");

				}
			}
		};
		btnNewButton.addActionListener(al);

		btnNewButton.setBounds(45, 39, 144, 23);
		frame.getContentPane().add(btnNewButton);

		JButton btnNewButton_1 = new JButton("Simular");
		btnNewButton_1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (ph.hayResoptim()) {
					CorridaHandler ch = CorridaHandler.getInstance();
					ch.recargarSimulable();
					ProfilerBasicoTiempo pbt = ProfilerBasicoTiempo.getInstance();
					pbt.reset();
					pbt.iniciarContador("TotalSimulacion");
					ph.simularCliente();
					pbt.pausarContador("TotalSimulacion");
					pbt.imprimirTiempos(ch.getCorridaActual().getRutaSals());
				}
				// else {
//					CorridaHandler ch = CorridaHandler.getInstance();
//					ch.simularClienteDesdeDirectorio(Constantes.RUTA_SALIDA_SIM_PARALELA);
//				}


			}
		});
		btnNewButton_1.setBounds(45, 171, 144, 23);
		frame.getContentPane().add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("Optimizar");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ph.optimizarCliente();
			}
		});
		btnNewButton_2.setBounds(45, 105, 144, 23);
		frame.getContentPane().add(btnNewButton_2);

		JButton btnNewButton_3 = new JButton("Cerrar Servidores");
		btnNewButton_3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				CorridaHandler ch = CorridaHandler.getInstance();
				ch.cerrarServidores();
				levantados = false;
			}
		});
		btnNewButton_3.setBounds(45, 237, 144, 23);
		frame.getContentPane().add(btnNewButton_3);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Está seguro que desea cerrar el MOP?", "Cerrar sistema",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					CorridaHandler ch = CorridaHandler.getInstance();
					ch.cerrarServidores();
					PizarronRedis pp = PizarronRedis.getInstance();
					
					pp.flushDB();
					pp.cerrarPool();
					levantados = false;
					System.exit(0);
				} else {

				}
			}
		});
	}

	public PresentacionHandler getPh() {
		return ph;
	}

	public void setPh(PresentacionHandler ph) {
		this.ph = ph;
	}

}
