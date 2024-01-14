/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ClienteHandler is part of MOP.
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

package pizarron;

import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import futuro.ClaveDiscreta;
import logica.CorridaHandler;
import optimizacion.Optimizable;
import optimizacion.Optimizador;
import optimizacion.OptimizadorPaso;
import simulacion.Simulable;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;


public class ClienteHandler implements ICliente {
	private static ClienteHandler instance;
	private Pizarron pizarron;
	private ArrayList<Paquete> paquetes;
	private ArrayList<Paquete> paquetesResueltos;
	private ArrayList<Paquete> paquetesEnResolucion;
	private ArrayList<Paquete> paquetesAResolver;
	
	private ArrayList<PaqueteEscenarios> paquetesEscenarios;
	private ArrayList<PaqueteEscenarios> paquetesEscenariosResueltos;

	private int CR;
	private int CREscenario;
	private int paso;
	
	
	private HashMap<String, ArrayList<Long>> tiempoPorPaqueteResuelto;		//mapa clave=nombrePC, valor=arrayList con tiempos de cada estado (o paquete) resuelto 
	private HashMap<String, Double> tiempoMedioHastaElMomentoCliente;				//mapa clave=nombrePC, valor=tiempo estimado
	private HashMap<String, Long> tiempoMedioHastaElMomentoServidor;
	private HashMap<String, ArrayList<Integer>> cantEstadosPorPaquete;
	private HashMap<String, Integer> cantEstadosHastaElMomento;
//	private HashMap<String, Integer> cantEstadosTotalPaso;
	private HashMap<String, Integer> cantEstadosParaMaquinas;
	public static double RO;
	public static double HOLGURA;
	private double tMedEsc;
	private int cantEscRes;
	private Optimizable optimizable;
	private Simulable simulable;
	
	public static ClienteHandler getInstance() {
		if (instance == null)
			instance = new ClienteHandler();

		return instance;
	}
	public static void deleteInstance() {
		instance = null;			
	}
	public ClienteHandler() {
		super();
		cantEscRes = 1;
		tMedEsc = Constantes.TDESCENARIOS;
		RO = 0.98;
		HOLGURA = 5;
		pizarron = PizarronRedis.getInstance();
		paquetes = new ArrayList<Paquete>();
		paquetesResueltos = new ArrayList<Paquete>();
		paquetesEscenarios = new ArrayList<PaqueteEscenarios>();
		paquetesEscenariosResueltos = new ArrayList<PaqueteEscenarios>();
		
		tiempoPorPaqueteResuelto = new HashMap<String, ArrayList<Long>>();
		tiempoMedioHastaElMomentoCliente = new HashMap<String, Double>();
		tiempoMedioHastaElMomentoServidor = new HashMap<String, Long>();
		cantEstadosPorPaquete = new HashMap<String,  ArrayList<Integer>>();
		cantEstadosHastaElMomento = new HashMap<String, Integer>();
//		cantEstadosTotalPaso = new HashMap<String, Integer>();	//Cantidad de estados total en el paso
		cantEstadosParaMaquinas = new HashMap<String, Integer>();

		CorridaHandler ch = CorridaHandler.getInstance();
		boolean paralelo = ch.isParalelo();
		if (paralelo) {
			for (String nombreServ : pizarron.obtenerServidores()) {
				nombreServ = nombreServ.split(":")[1];
				tiempoPorPaqueteResuelto.put(nombreServ, new ArrayList<Long>());
				tiempoMedioHastaElMomentoCliente.put(nombreServ, (double) Constantes.TD);
				cantEstadosPorPaquete.put(nombreServ, new ArrayList<Integer>());
				cantEstadosHastaElMomento.put(nombreServ, 0);
//			cantEstadosTotalPaso.put(nombreServ, 0);	//Cantidad de estados total en el paso
				cantEstadosParaMaquinas.put(nombreServ, 0);
			}
		}

		
	}

	@Override
	public void cargarCorrida(String corrida, String ruta) {
		String rutaParalelo = "\\\\" + ruta.substring(2).replace("\\", "/");
		System.out.println(rutaParalelo);
		pizarron.cargarCorrida(corrida, ruta);
	}

	@Override
	public void optimizar() {
		pizarron.optimizar();
	}

	@Override
	public void escribirPasoActual(int paso) {
		this.paso = paso;
		pizarron.escribirPasoActual(paso);
	}

	@Override
	public void cargarPaquetesAResolver(int paso) {
		//long startTime = System.currentTimeMillis();

		cargarPaquetesAResolver(paso, paquetes.size());

	//	long estimatedTime = System.currentTimeMillis() - startTime;

//		DirectoriosYArchivos.agregaTexto(
//				Constantes.ruta_log_paralelismo + "logCliente" + ManagementFactory.getRuntimeMXBean().getName()
//						+ ".txt",
//				"DEMORÉ: " + estimatedTime + "PARA MANDAR A RESOLVER " + paquetes.size() + " PAQUETES");
	}

	@Override
	public ArrayList<Paquete> obtenerPaquetesResueltos(int paso) {		//TODO ESTO NO SE USA?????	
		return pizarron.obtenerPaquetesResueltos(paso);
	}

	@Override
	public void finalizarOptimizacion() {
		pizarron.finalizarOptimizacion();
	}

	@Override
	public void simular() {
		pizarron.simular();
	}

	@Override
	public ArrayList<PaqueteEscenarios> obtenerPaquetesEscenariosResueltos() {
		return pizarron.obtenerPaquetesEscenariosResueltos();
	}

	@Override
	public void finalizarSimulacion() {
		pizarron.finalizarSimulacion();
	}

	@Override
	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit) {

		return pizarron.devuelveCodigoControlesDEOpt(corrida, paso, sInit);
	}

	@Override
	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt) {
		pizarron.cargaCodigoControlesDEOpt(corrida, paso, sInit, codigoOpt);
	}

	@Override
	/**
	 * Crea la lista paquetes con todos los paquetes del paso con estado enEspera
	 */
	public void cargarPaquetes(int totalEstados, int paso, int estadosPorPaquete) {
		this.paquetes = new ArrayList<Paquete>();
		int i = 0;
		int estIni = 0;
		int estFin = estadosPorPaquete - 1;

		while (estIni < totalEstados) {
			estFin = estIni + estadosPorPaquete;
			estFin = Math.min(estFin, totalEstados);
			paquetes.add(new Paquete(i, estIni, estFin, paso));
			estIni = estIni + estadosPorPaquete;
			i++;
		}

	}
	
//	@Override
//	/**
//	 * Crea la lista paquetes con todos los paquetes del paso con estado enEspera
//	 */
//	public void cargarPaquetes(int totalEstados, int paso) {
//		this.paquetes = new ArrayList<Paquete>();
//		int i = 0;
//		int estIni = 0;
//		int estFin = estadosPorPaquete - 1;
//
//		m = maquinas.pop();
//		sumaInvTmedios = for .estFin..estFin.
//		coefMul = totalestados/ sumaInvTmedios;
//		while (estIni < totalEstados) {
//			
//			estadosPorPaquete = coefMul/tiempoMedio(m);
//			estFin = estIni + estadosPorPaquete;
//			estFin = Math.min(estFin, totalEstados);
//			paquetes.add(new Paquete(i,m, estIni, estFin, paso));
//			m = maquinas.pop();
//			estIni = estIni + estadosPorPaquete;
//			i++;
//		}
//		
//	}

	/**
	 * Carga CP paquetes a resolver del paso tc en la lista paquetesAResolver Setea
	 * en la lista paquetes instantes de tiempo de envóo y estado enResolucion Envóa
	 * al pizarrón los paquetes a resolver
	 * 
	 * @param paso
	 * @param cantidad
	 */
	private void cargarPaquetesAResolver(int paso, int cantidad) {
		ArrayList<Paquete> paraMandarAResolver = new ArrayList<Paquete>();
		int k = 0;
		int i = 0;
		while (k < cantidad && i < paquetes.size()) {
			Paquete p = paquetes.get(i);
			if (p.getEstado() == Constantes.ENESPERA) {
				p.setEstado(Constantes.ENRESOLUCION);
				p.setInstanteTiempoEnvio(System.currentTimeMillis());
				paraMandarAResolver.add(p);
				k++;
			}
			i++;
		}
//		DirectoriosYArchivos.agregaTexto(Constantes.ruta_log_paralelismo + "logCliente"
//				+ ManagementFactory.getRuntimeMXBean().getName() + ".txt",
//				"SE MANDAN " + cantidad + " A RESOLVER DEL PASO: " + paso);
		pizarron.cargarPaquetesAResolver(paso, paraMandarAResolver);
	}

	@Override
	public void cargarPaquetesEscenarios(int totalEscenarios, int escenariosPorPaquete) {
		this.paquetesEscenarios = new ArrayList<PaqueteEscenarios>();
		int i = 0;
		int estIni = 0;
		int estFin = escenariosPorPaquete - 1;

		while (estIni < totalEscenarios) {
			estFin = estIni + escenariosPorPaquete;
			estFin = Math.min(estFin, totalEscenarios);
			paquetesEscenarios.add(new PaqueteEscenarios(i, estIni + 1, estFin + 1));
			estIni = estIni + escenariosPorPaquete;
			i++;
		}
	}

	private void cargarPaquetesEscenariosAResolver(int paso, int cantidad) {
		ArrayList<PaqueteEscenarios> paraMandarAResolver = new ArrayList<PaqueteEscenarios>();
		int k = 0;
		int i = 0;
		while (k < cantidad && i < paquetesEscenarios.size()) {
			PaqueteEscenarios p = paquetesEscenarios.get(i);
			if (p.getEstado() == Constantes.ENESPERA) {
				p.setEstado(Constantes.ENRESOLUCION);
				p.setInstanteTiempoEnvio(System.currentTimeMillis());
				paraMandarAResolver.add(p);
				k++;
			}
			i++;
		}
		System.out.println("");
		pizarron.cargarPaquetesEscenarioAResolver(paraMandarAResolver);
	}

	/**
	 * Mientras existan paquetes en estado diferente a Terminado cada TRESU obtengo
	 * listaResueltos del pizarrón y vacóo la lista CR = cantidad resueltos de la
	 * lista anterior actualizo lista paquetes con estado Terminado en los paquetes
	 * resueltos recorre lista de paquetes si esta EnResolucion desde hace mós de TD
	 * se pone el estado del paquete enEspera se toman de la lista de paquetes CR +
	 * CE que esten en estado enEspera y se envian a paquetesAResolver en el
	 * pizarron
	 */
	public void resolucionPaquetes() {
		Paquete pa = paquetes.get(0);
		int cantEstadosPaquete = pa.getEstadoFin() - pa.getEstadoIni();
		String nm;
		String nmaq;
		double tMedAcumC;
		int cEstAcum;
		int cantEPaquete;
		long tiempo;
		double tmed;
		double tAct;
		double tComp;
		int penalizador;
		
		boolean termine = false;
		try {
			while (!termine) {
				try {
					Thread.sleep(50);
					paquetesResueltos = pizarron.obtenerPaquetesResueltos(paso);
					
					paquetesEnResolucion = pizarron.obtenerPaquetesEnResolucion(paso);
					
					tiempo = System.currentTimeMillis();
					//paquetesAResolver = pizarron.obtenerPaquetesAResolver(paso);	//El paquete se pierde
	
					
					//aca recorrer los paquetes y sacar la info para actualizar los tiempos medios por estado de cada máquina
					for (Paquete p : paquetesResueltos) {
					//	System.out.println("El paquete con clave " + p.getClave() + " está terminado. Paso actual: " + paso + ". Paso del paquete: " + p.getPaso());
						if (this.paquetes.get(p.getClave()).getEstado() != Constantes.TERMINADO) {		//Esta condición puede que no sea necesaria
							
							p.setTiempoResolucionCliente(tiempo - p.getInstanteTiempoEnvio());
							nm = p.getNroMaquina();	
							tMedAcumC = tiempoMedioHastaElMomentoCliente.get(nm);
							cEstAcum = cantEstadosHastaElMomento.get(nm);
							cantEPaquete = p.getEstadoFin()-p.getEstadoIni();
							tiempoPorPaqueteResuelto.get(nm).add(p.getTiempoResolucionCliente());
							tiempoMedioHastaElMomentoCliente.put(nm, (RO* tMedAcumC * cEstAcum + p.getTiempoResolucionCliente() ) / ( RO*cEstAcum + cantEPaquete ));
							cantEstadosPorPaquete.get(nm).add(cantEPaquete);
							cantEstadosHastaElMomento.put(nm, cEstAcum + cantEPaquete);
						}
						
						this.paquetes.get(p.getClave()).setEstado(Constantes.TERMINADO);
						p.setEstado(Constantes.TERMINADO);
					}
					tiempo = System.currentTimeMillis();
					termine = true;
					for (Paquete p : paquetesEnResolucion) {
						nmaq = p.getNroMaquina();
							
						if (this.paquetes.get(p.getClave()).getEstado() != Constantes.TERMINADO) {			//TODO el if puede que sea innecesario a partir de los cambios en el código
							cantEstadosPaquete = p.getEstadoFin() - p.getEstadoIni();
							tmed = tiempoMedioHastaElMomentoCliente.get(nmaq);
							tAct = tiempo - p.getInstanteTiempoEnvio();
							penalizador = p.getPenalizadorTiempo();
							tComp = tmed * cantEstadosPaquete	* HOLGURA * Math.pow(2, penalizador) ;
							if (tAct > tComp
									&& p.getEstado() == Constantes.ENRESOLUCION) {
								penalizador++;
								p.setPenalizadorTiempo(penalizador);
								System.out.println("Se vuelve a enviar un paquete con factor penalización: " + penalizador + ". Lo tenía la maquina: " + p.getNroMaquina());
								cargarPaquetesAResolver(paso, p);
							} else {
								pizarron.pasarPaqueteAEnResolucion(p);
							}
							
						}
					}
					
					for(Paquete p : paquetes) {
						
						termine = termine && (p.getEstado() == Constantes.TERMINADO);
						
					}
	
	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		
//			String dirSal = CorridaHandler.getInstance().getDirSals();
//			DirectoriosYArchivos.creaDirectorio(dirSal, "Paralelismo");

			String dirSO = optimizable.getDirSalidas();
			
			
			FileWriter myFile2 = new FileWriter( dirSO + "\\Paralelismo\\tiemposMediosOpt.txt",	true);
//			FileWriter myFile2 = new FileWriter( "\\\\ntpal\\grupos2\\Plaimaedfrun\\PruebaParalelismoAndres\\tiemposMediosOpt.txt",	
//					true);
			
			Iterator<HashMap.Entry<String, Double>> ent = tiempoMedioHastaElMomentoCliente.entrySet().iterator();
			Iterator<HashMap.Entry<String, Integer>> cant = cantEstadosHastaElMomento.entrySet().iterator();
			while(ent != null && ent.hasNext()){
				HashMap.Entry<String, Double> e= ent.next();
				HashMap.Entry<String, Integer> c= cant.next();
				myFile2.write("Paso: " + this.getPaso()+ " " +e.getKey() + " TMed: " + e.getValue() + " Cant. estados acumulados: " + c.getValue() +"\n");
			}
			myFile2.close();	
			
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}

	private void cargarPaquetesAResolver(int paso, Paquete p) {

		ArrayList<Paquete> paraMandarAResolver = new ArrayList<Paquete>();
		p.setEstado(Constantes.ENRESOLUCION);
		p.setInstanteTiempoEnvio(System.currentTimeMillis());
		paraMandarAResolver.add(p);

		pizarron.cargarPaquetesAResolver(paso, paraMandarAResolver);

	}

	@Override
	public void resolucionPaquetesEscenarios() {

		PaqueteEscenarios pa = paquetesEscenarios.get(0);
		int cantEscPaquete = pa.getEscenarioFin() - pa.getEscenarioIni();
		boolean termine = false;
		long tiempo;
		double tMedEscANT =2000;
//		int cant_resueltos = 0;
//		int cant_resueltos_nuevo = 0;
		System.out.println("COMIENZA RESOLUCIÓN PAQUETES ESCENARIOS");
		try {
			
			String dirSS = simulable.dameDirSalidas();
			
			
			FileWriter myFile2 = new FileWriter( dirSS + "\\Paralelismo\\tiemposMediosSim.txt",	true);
			
//			FileWriter myFile2 = new FileWriter( "\\\\ntpal\\grupos2\\Plaimaedfrun\\PruebaParalelismoAndres\\tiemposMediosSim.txt",	
//					true);
			
			while (!termine) {
				try {
					Thread.sleep(50);
					paquetesEscenariosResueltos = pizarron.obtenerPaquetesEscenariosResueltos();
					tiempo = System.currentTimeMillis();
					for (PaqueteEscenarios p : paquetesEscenariosResueltos) {
						if (this.paquetesEscenarios.get(p.getClave()).getEstado() != Constantes.TERMINADO) {
							this.paquetesEscenarios.get(p.getClave()).setEstado(Constantes.TERMINADO);
							cantEscPaquete = p.getEscenarioFin() - p.getEscenarioIni();
							tMedEsc = (tMedEsc * cantEscRes + (tiempo - p.getInstanteTiempoEnvio())) / (cantEscRes + cantEscPaquete);
							System.out.println("El tiempo medio por escenario es: " + tMedEsc);
							cantEscRes = cantEscRes + cantEscPaquete;
							System.out.println("SE TERMINÓ EL ESCENARIO: " + p.getEscenarioIni());
						}
						
					}
					tiempo = System.currentTimeMillis();
					termine = true;
					for (PaqueteEscenarios p : paquetesEscenarios) {
						termine = termine && (p.getEstado() == Constantes.TERMINADO);
	
						if (p.getEstado() != Constantes.TERMINADO) {
							cantEscPaquete = p.getEscenarioFin() - p.getEscenarioIni();
							if (tiempo - p.getInstanteTiempoEnvio() > tMedEsc * cantEscPaquete * HOLGURA
									&& p.getEstado() == Constantes.ENRESOLUCION) {
								System.out.println("Se vuelve a enviar un paquete con: " + cantEscPaquete);
								cargarPaquetesEscenariosAResolver(p);
							}
						}
					}	
					if(tMedEsc != tMedEscANT) {	
						myFile2.write("TMedEsc: " + tMedEsc +"\n");	
						tMedEscANT = tMedEsc;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			myFile2.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		System.out.println("Paquetes Escenarios Todos Terminados");  
	}

	private void cargarPaquetesEscenariosAResolver(PaqueteEscenarios p) {
		ArrayList<PaqueteEscenarios> paraMandarAResolver = new ArrayList<PaqueteEscenarios>();

		p.setEstado(Constantes.ENRESOLUCION);
		p.setInstanteTiempoEnvio(System.currentTimeMillis());
		paraMandarAResolver.add(p);

		pizarron.cargarPaquetesEscenarioAResolver(paraMandarAResolver);
	}

	@Override
	public void cargarPaquetesEscenariosAResolver() {
		cargarPaquetesEscenariosAResolver(paso, paquetesEscenarios.size());
	}

	public Pizarron getPizarron() {
		return pizarron;
	}

	public void setPizarron(Pizarron pizarron) {
		this.pizarron = pizarron;
	}

	public ArrayList<Paquete> getPaquetes() {
		return paquetes;
	}

	public void setPaquetes(ArrayList<Paquete> paquetes) {
		this.paquetes = paquetes;
	}

	public int getPaso() {
		return paso;
	}

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public int getCR() {
		return CR;
	}

	public void setCR(int cR) {
		CR = cR;
	}

	public void cargarOperacion(int oper) {
		if (oper == Constantes.OPTIMIZAR) {
			pizarron.optimizar();
		} else if (oper == Constantes.SIMULAR) {
			pizarron.simular();
		}

	}

	public int getCREscenario() {
		return CREscenario;
	}

	public void setCREscenario(int cREscenario) {
		CREscenario = cREscenario;
	}

	public ArrayList<PaqueteEscenarios> getPaquetesEscenarios() {
		return paquetesEscenarios;
	}

	public void setPaquetesEscenarios(ArrayList<PaqueteEscenarios> paquetesEscenarios) {
		this.paquetesEscenarios = paquetesEscenarios;
	}

	public ArrayList<PaqueteEscenarios> getPaquetesResueltosEscenarios() {
		return paquetesEscenariosResueltos;
	}

	public void setPaquetesResueltosEscenarios(ArrayList<PaqueteEscenarios> paquetesResueltosEscenarios) {
		this.paquetesEscenariosResueltos = paquetesResueltosEscenarios;
	}

	public void imprimirListaPaquetes() {
		for (Paquete p : paquetes) {
			p.imprimirPaquete();
		}
	}

	@Override
	public int obtenercantServidores() {
		return pizarron.obtenercantServidores();
	}
	public Optimizable getOptimizable() {
		return optimizable;
	}
	public void setOptimizable(Optimizable optimizable) {
		this.optimizable = optimizable;
	}
	public Simulable getSimulable() {
		return simulable;
	}
	public void setSimulable(Simulable simulable) {
		this.simulable = simulable;
	}

}