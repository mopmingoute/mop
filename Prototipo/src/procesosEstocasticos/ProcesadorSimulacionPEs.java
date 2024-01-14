/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProcesadorSimulacionPEs is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.Hashtable;

import datatypesSalida.DatosPEsUnEscenario;
import matrices.Oper;
import tiempo.LineaTiempo;
import tiempo.PasoTiempo;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilArrays;

public class ProcesadorSimulacionPEs {
	
	private int cantEsc;
	
	/**
	 * ESC_UNICO procesa una único escenario larga obteniendo estadísticos por paso del año calculados
	 * en base a las observaciones de todos los años y considerando la variación a lo largo del tiempo.
	 * ESC_MULT procesa muchos escenarios con el mismo período de simulación, obteniendo estadísticos 
	 * para cada paso del período, considerando la variación entre los escenarios 
	 */
	private String tipoSimulPE; 
	
	private int cantPasos;
	
	private Hashtable<String, double[]> pondRachas; // clave nombre PE, valor los ponderadores para el cálculo de rachas
	
	private LineaTiempo lt;
	
	private ArrayList<ProcesoEstocastico> listaPEs; // lista de PEs de los que se saca la simulación
	
	private Hashtable<String, ProcesoEstocastico> tablaPEs; // tabla de los PEs, clave nombre PE
	
	/**
	 * Lista de combinaciones (nombre del PE + "-" + nombre variable aleatoria del PE)
	 */
	private ArrayList<String> nombresPEyVA;  
	
	private ArrayList<DatosPEsUnEscenario> resultados;  // resultados de un escenario de simulación
	
	private DatosPEsUnEscenario d1EscCorr; // el escenario que se está cargando
	
	
	/**
	 * Se usa si tipoSimulPE es ESC_UNICO
	 * clave: nombre del PE + "-" + nombre variable aleatoria del PE
	 * valor: la serie de la VA del PE
	 */
	private Hashtable<String, Serie> seriesSimul; 

	/**
	 * Se usa si tipoSimulPE es ESC_MULT
	 * clave: nombre del PE + "-" + nombre variable aleatoria del PE)
	 * valor: EL haz de escenarios de la VA del PE
	 */
	private Hashtable<String, HazEsc> hazEscSimul; // se usa si tipoSimulPE es ESC_MULT
	
	private ArrayList<Long> instantesDeMuestreoSimulacion; // la totalidad de los instantes de muestreo de la simulación
	
	
	public ProcesadorSimulacionPEs(LineaTiempo lt, ArrayList<ProcesoEstocastico> listaPEs, int cantPasos, 
			String tipoSimulPE, int cantEsc, Hashtable<String, double[]> pondRachas){
		this.lt = lt;
		this.cantPasos = cantPasos;
		this.cantEsc = cantEsc;
		this.pondRachas = pondRachas;
		this.tipoSimulPE = tipoSimulPE;
		resultados = new ArrayList<DatosPEsUnEscenario>();
		this.listaPEs = listaPEs;
		tablaPEs = new Hashtable<String, ProcesoEstocastico>();
		nombresPEyVA = new ArrayList<String>(); 
		instantesDeMuestreoSimulacion = new ArrayList<Long>();
		for(ProcesoEstocastico p: listaPEs){
			tablaPEs.put(p.getNombre(), p);
			for(VariableAleatoria va: p.getVariablesAleatorias()){
				nombresPEyVA.add(clavePEVA(p,va));
			}
		}		
	}
	
	
	
	public void inicializarEscenario(int i){
		d1EscCorr = new DatosPEsUnEscenario(i, cantPasos, nombresPEyVA);		
	}
	
	
	public void finalizarEscenario(){
		resultados.add(d1EscCorr);		
	}
	
	/**
	 * Construye la clave en el Hashtable a partir de los nombres
	 * del PE y la VA
	 */
	public String clavePEVA(ProcesoEstocastico p, VariableAleatoria va){
		return p.getNombre() + "-" + va.getNombre();
	}
	
	public void guardarResultadoPasoPE(int numpaso, long[] instantesMuestreo){
		for(ProcesoEstocastico p: listaPEs){
			for(String nva: p.getNombresVarsAleatorias()){				
				VariableAleatoria va = p.devuelveVADeNombre(nva);
				// toma de d1EscCorr el objeto ya construído, para cargarle los valores simulados
				double[][] val1VA = d1EscCorr.getValores().get(clavePEVA(p,va));
				if(!p.isMuestreado()){
					double[] aux = {va.getValor()};
					val1VA[numpaso] = aux;
				}else{					
					val1VA[numpaso] = va.getUltimoMuestreo();
				}				
			}
		}				
	}
	
	
	
	public ArrayList<Long> getInstantesDeMuestreoSimulacion() {
		return instantesDeMuestreoSimulacion;
	}



	public void setInstantesDeMuestreoSimulacion(ArrayList<Long> instantesDeMuestreoSimulacion) {
		this.instantesDeMuestreoSimulacion = instantesDeMuestreoSimulacion;
	}



	public void finalizarSimulacionPEs(String dirSalidas){
		for(ProcesoEstocastico p: listaPEs){	
			escribeValores1PE(p, dirSalidas);			
			// Estadísticos del proceso
			calculaEstadisticos1PE(p, dirSalidas);
			
		}
		System.out.println("Termina escritura de archivos de resultados");	
		
	}
	
	
	public void calculaEstadisticos1PE(ProcesoEstocastico p, String dirSalidas){

		String[] nombres = new String[p.getVariablesAleatorias().size()];
		int is=0;
		for(VariableAleatoria va: p.getVariablesAleatorias()){						
			nombres[is]=clavePEVA(p,va);
			is++;
		}
		String dirPE = dirSalidas+"/"+p.getNombre();
		if(tipoSimulPE.equalsIgnoreCase(utilitarios.Constantes.ESC_UNICO)){
			MetodosSeries.imprimeEstadisticosSeries(nombres, seriesSimul, dirPE);	
			ArrayList<Serie> ls = new ArrayList<Serie>();
			ArrayList<Double> ld = new ArrayList<Double>();
			int iva = 0;
			for(VariableAleatoria va: p.getVariablesAleatorias()){
				ls.add(seriesSimul.get(clavePEVA(p, va)));
				ld.add(pondRachas.get(p.getNombre())[iva]);
				iva ++;
			}
			Serie srachas = MetodosSeries.combilin(ls, ld);	
			srachas.setNombre("SerieCombilinParaRachas");
			int hanios = 4; // máxima ventana móvil de años
			String texto = MetodosSeries.escribePromOSumaAyT(srachas, hanios, "S");
			String archProm = dirSalidas + "/promedios.xlt";
			DirectoriosYArchivos.siExisteElimina(archProm);
			DirectoriosYArchivos.agregaTexto(archProm, texto);
			
		}else if(tipoSimulPE.equalsIgnoreCase(utilitarios.Constantes.ESC_MULT)){
			// TODO
		}
			
	}

	/**
	 * Escribe las salidas de texto y crea las estructuras de datos de salida 
	 * para estimar estadísticos de un proceso estocástico simulado.
	 * @param dirSalidas
	 */
	public void escribeValores1PE(ProcesoEstocastico p, String dirSalidas){
		
		utilitarios.Sistema.muestraMemoria();
		
		String dirArchChan = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS/GENERA ESCENARIOS EOLOSOL/Salidas/observPorPaso.xlt";

		
		String nomDir = dirSalidas + "/" + p.getNombre();
		if(!DirectoriosYArchivos.existeDirectorio(nomDir)){
			DirectoriosYArchivos.creaDirectorio(dirSalidas, p.getNombre());
		}
		
		if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
			seriesSimul = new Hashtable<String,Serie>();
		}else if(tipoSimulPE.equals(utilitarios.Constantes.ESC_MULT)){
			hazEscSimul = new Hashtable<String, HazEsc>();
		}
				
		for(VariableAleatoria va: p.getVariablesAleatorias()){
			ArrayList<Integer> anioT = new ArrayList<Integer>();
			ArrayList<Integer> pasoT = new ArrayList<Integer>();
			ArrayList<double[]> datosT = new ArrayList<double[]>();
			if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
				Serie s1 = new Serie(p.getNombrePaso());
			}else{
				HazEsc s1 = new HazEsc(p.getNombrePaso());
			}			
			String nva = va.getNombre();
			String nomArch = nomDir+"/"+nva+".xlt";
			if(DirectoriosYArchivos.existeArchivo(nomArch)){
				DirectoriosYArchivos.eliminaArchivo(nomArch);
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SIMULACION DEL PROCESO " + p.getNombre() + "\nVARIABLE "+va.getNombre()+"\nEstimación " + p.getNombreEstimacion());
			sb.append("\n");
			sb.append("numpaso \tfecha_inicial_paso\tpaso_del_anio\tint.muestreo\t");
			sb.append("PROMEDIO\t");
			for(int ie=0; ie<cantEsc; ie++){
				sb.append("ESC-"+ie+"\t");
			}
			
			sb.append("\n");

			lt.reiniciar();
			PasoTiempo paso = lt.devuelvePasoActual();
			int numpaso = 0;
			int t=0;
			int ims =0; // índice de instantes de muestreo de la simulación desde su inicio.
			StringBuilder sb2 = new StringBuilder(nva + "\n"); 
			while (paso != null){
				if(numpaso%1000==0)System.out.println(numpaso);
				long instante = paso.getInstanteInicial();
				int cantIM = paso.getDuracionPaso()/paso.getIntervaloMuestreo();
//				int cantEsc = 1;   // cantEsc es la cantidad de valores que se escriben por paso
				int salto = cantIM;
				if(p.isMuestreado()){
//					cantEsc = cantIM;
//					salto = 1;
				}
				for(int im=0; im<cantIM; im++) {
					sb.append(numpaso+"\t");
					sb.append(lt.fechaYHoraDeInstante(paso.getInstanteInicial()) +"\t");
					sb.append(p.pasoDelAnio(instantesDeMuestreoSimulacion.get(ims))+1);
					sb.append("\t");
					sb.append(im + "\t");
					double prom = 0.0;
					ArrayList<Double> valores = new ArrayList<Double>();
					int pasoAnio = p.pasoDelAnio(instantesDeMuestreoSimulacion.get(ims))+1;// en las series el paso del año empieza en 1
					pasoT.add(pasoAnio); 
					anioT.add(p.anioDeInstante(instante));
					double[] auxT = new double[cantEsc];					
					for(int ie=0; ie<cantEsc; ie++){
						double val = resultados.get(ie).getValores().get(clavePEVA(p,va))[numpaso][im];
						valores.add(val);
						prom += val/cantEsc;
						auxT[ie] = val;
					}
					datosT.add(auxT);
					sb.append(prom+"\t");
					for(int ie=0; ie<cantEsc; ie++){
						sb.append(valores.get(ie)+"\t");
					}
					sb.append("\n");
					ims=ims+1;
				}
				lt.avanzarPaso();
				paso = lt.devuelvePasoActual();				
				numpaso++;		
			}	
			DirectoriosYArchivos.agregaTexto(nomArch, sb.toString());
			int[] anioA = utilitarios.UtilArrays.dameArrayI(anioT);
			int[] pasoA = utilitarios.UtilArrays.dameArrayI(pasoT);
			double[][] datosA = UtilArrays.dameArrayDobleD(datosT);
			
			DirectoriosYArchivos.agregaTexto(dirArchChan, sb2.toString());
			
			String clave = clavePEVA(p, va);
			if(tipoSimulPE.equals(utilitarios.Constantes.ESC_UNICO)){
				Serie s1 = new Serie(va.getNombre(), p.getNombrePaso(), 
						Oper.devuelveCol(datosA, 0), anioA, pasoA);
				seriesSimul.put(clave, s1);				
			}else if(tipoSimulPE.equals(utilitarios.Constantes.ESC_MULT)){
				datosA = UtilArrays.dameArrayDobleD(datosT);
				HazEsc h1 = new HazEsc(va.getNombre(), p.getNombrePaso(), datosA, anioA, pasoA);
				hazEscSimul.put(clave, h1);
			}	
			
			System.out.println("FIN VA");
			utilitarios.Sistema.muestraMemoria();
		}	
	}		
		
		
	
	
}
