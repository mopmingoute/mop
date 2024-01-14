/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DespachadorCortoPlazo is part of MOP.
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

package cp_despacho;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

import datatypesProblema.DatosSalidaProblemaLineal;
import logica.CorridaHandler;
import logica.EstudioHandler;
import optimizacion.ResOptim;
import parque.Corrida;
import persistencia.PersistenciaHandler;
import presentacion.ConstructorReportes;
import presentacion.PresentacionHandler;
import problema.ProblemaHandler;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import utilitarios.LectorDireccionArchivoDirectorio;
import utilitarios.ManejaObjetosEnDisco;
import utilitarios.ProfilerBasicoTiempo;

public class DespachadorCortoPlazo<T extends DespachableCP> {

	private Corrida corrida;
	private LineaTiempo ltiempo;
	private long instanteInicialCP;
	private long instanteFinalCP;
	private ResOptim resoptim;
	private String dirEntradas;
	private String dirSalidas;
	private DespachableCP despachableCP;
	private boolean optimizar; //bandera que controla si hay que optimizar y generar salida o solo generar salida 
	
	
	
	public JFrame frame;
	
	
	public DespachadorCortoPlazo(T despachableCP, Corrida corrida, String dirEntradas, String dirSalidas) {
		super();
		this.corrida = corrida;
		this.dirEntradas = dirEntradas;
		this.dirSalidas = dirSalidas;
		this.ltiempo = corrida.getLineaTiempo();
		this.resoptim = corrida.getResoptim();
		this.despachableCP = despachableCP;
	}

	
	
	
//	public DespachadorCortoPlazo(T despachableCP, Corrida corrida, String dirEntradas, String dirSalidas) {
//		super();
//		this.corrida = corrida;
//		this.dirEntradas = dirEntradas;
//		this.dirSalidas = dirSalidas;
//		this.ltiempo = corrida.getLineaTiempo();
//		this.resoptim = corrida.getResoptim();
//		this.despachableCP = despachableCP;
//		
//		frame = new JFrame(" MOP PARALELO" + Constantes.VERSION_ET);
//		frame.setBounds(100, 100, 300, 387);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.getContentPane().setLayout(null);
//		//frame.setIconImage(img.getImage());
//
//		JButton btnNewButton_1 = new JButton("Aceptar");
//		btnNewButton_1.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				if(optimizar) {
//					optimizarYObtenerSalidas();
//				} else {
//					soloObtenerSalidas();
//				}
//				frame.setVisible(false);
//			}
//		}
//		);
//		
//		JRadioButton rdbtnNewRadioButton = new JRadioButton("Optimizar y generar salidas");
//		rdbtnNewRadioButton.setBounds(55, 30, 280, 40);
//		frame.getContentPane().add(rdbtnNewRadioButton);
//
//		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Solo generar salidas");
//		rdbtnNewRadioButton_1.setBounds(55, 130, 280, 40);
//		frame.getContentPane().add(rdbtnNewRadioButton_1);
//		
//		btnNewButton_1.setBounds(55, 230, 180, 40);
//		frame.getContentPane().add(btnNewButton_1);
//		
//		ButtonGroup G = new ButtonGroup();
//		
//		G.add(rdbtnNewRadioButton);
//		G.add(rdbtnNewRadioButton_1);
//		
//		
//		rdbtnNewRadioButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					optimizar=true;
//				}
//			}
//		);
//		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					optimizar=false;				
//				}
//			}
//		);
//	}

	public void despacharCP() {		
		System.out.println("COMIENZA A OPTIMIZAR Y LUEGO GENERA SALIDAS DE LA OPTIMIZACIÓN");
		
		despachableCP.setCorrida(corrida);
		
		despachableCP.setDirEntrada(dirEntradas);
		
		despachableCP.setDirSalida(dirSalidas);
		
		despachableCP.leerdatosGenerales();

		despachableCP.leerDatosParticipantesCP();
		
		despachableCP.leerProcEstocasticos();
		
		despachableCP.construirComportamientosCP();
		
		despachableCP.despacharCP();
		
		despachableCP.producirSalidasCP();		

	}
	
	

	public LineaTiempo getLtiempo() {
		return ltiempo;
	}

	public void setLtiempo(LineaTiempo ltiempo) {
		this.ltiempo = ltiempo;
	}

	public ResOptim getResoptim() {
		return resoptim;
	}

	public void setResoptim(ResOptim resoptim) {
		this.resoptim = resoptim;
	}

	public String getDirSalidas() {
		return dirSalidas;
	}

	public void setDirSalidas(String dirSalidas) {
		this.dirSalidas = dirSalidas;
	}

	public Corrida getCorrida() {
		return corrida;
	}

	public void setCorrida(Corrida corrida) {
		this.corrida = corrida;
	}

	
	
}
