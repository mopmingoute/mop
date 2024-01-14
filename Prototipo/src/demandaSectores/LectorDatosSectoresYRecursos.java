/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorDatosSectoresYRecursos is part of MOP.
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

package demandaSectores;

import java.util.ArrayList;
import java.util.Hashtable;

import datatypesTiempo.DatosTiposDeDia;
import logica.CorridaHandler;
import persistencia.CargadorPEDemandaEscenarios;
import pizarron.PizarronRedis;
import procesosEstocasticos.ConjuntoDeSeries;
import procesosEstocasticos.MetodosSeries;
import procesosEstocasticos.Serie;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.LectorDireccionArchivoDirectorio;
import utilitarios.LeerDatosArchivo;
import utilitarios.VentanasEntrada;



/**
 * La clase tiene métodos que permiten leer datos para los métodos en las clases:
 * - EstimadorCostoNetoUTEMigracion
 * - CalculadorCertificados
 * 
 * 
 * @author ut469262
 *
 */
public class LectorDatosSectoresYRecursos {
	
	
	private String directorio;	
	private String directorioCorrida;
	private String nombreDemanda; // nombre de la demanda
	private ArrayList<String> nombresRecursos;
	private Hashtable<String, Double> recursos;
	private DatosGenSectores dgs;
	
	
	public LectorDatosSectoresYRecursos(String directorio) {
		super();
		this.directorio = directorio;
	}

	/**
	 * Lee en dirArchivo y carga en this los datos generales de sectores
	 * @param dirArchivo
	 */
	public DatosGenSectores leerDatosGenSectores() {
		String dirArchivo = directorio + "/datosGen.txt";		
		ArrayList<ArrayList<String>> texto = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		DatosGenSectores datGen = new DatosGenSectores();
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos(texto, dirArchivo);
		int i = 0;
		datGen.setAnioFac(ale.cargaEntero(i,"ANIO_FACTURACION"));
		i++;
		datGen.setAnioGen(ale.cargaEntero(i,"ANIO_GENERACION"));
		i++;		
		datGen.setCantTiposDia(ale.cargaEntero(i,"CANT_TIPOS_DIA"));
		i++;
		datGen.setTiposDiasSemana(utilitarios.UtilArrays.dameArrayI(ale.cargaListaEnteros(i, "TIPOS_DIAS_SEMANA")));
		i++;
		datGen.setCantHoras(ale.cargaEntero(i,"CANT_HORAS"));
		i++;
		datGen.setCantEstac(ale.cargaEntero(i,"CANT_ESTAC"));
		i++;
		datGen.setDefEstacMeses(utilitarios.UtilArrays.dameArrayI(ale.cargaListaEnteros(i, "DEF_ESTAC_MESES")));
		i++;
		datGen.setNombresEstac(utilitarios.UtilArrays.dameArrayS(ale.cargaLista(i, "NOMBRES_ESTAC")));
		i++;
		datGen.setCantSectores(ale.cargaEntero(i,"CANT_SECTORES"));
		i++;
		datGen.setNombresSectores(utilitarios.UtilArrays.dameArrayS(ale.cargaLista(i, "NOMBRES_SECTORES")));
		i++;
		datGen.setCantCronicas(ale.cargaEntero(i, "CANT_CRON"));
		dgs = datGen;
		return datGen;
		
	}
	
	public Hashtable<String, CoefSectorFact> leerCoefYEnerMes(DatosGenSectores dat) {
		
		
		// Crea la tabla de datos de coeficientes
		Hashtable<String, CoefSectorFact> sectores = new Hashtable<String, CoefSectorFact>();
		for(String ns: dat.getNombresSectores()) {
			CoefSectorFact csc = new CoefSectorFact(dat);
			sectores.put(ns, csc);
		}
		
		// Lee y carga coeficientes horarios
		String dirArchivo = directorio + "/coefHor.txt";		
		ArrayList<ArrayList<String>> texto = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos(texto, dirArchivo);
		int i=0;
		int iest=0;
		for(String sEst: dat.getNombresEstac()) {
			if(!(ale.cargaPalabra(i, "ESTACION").equalsIgnoreCase(sEst))){
				System.out.println("Error en lectura de coefs. horarios de estación " + sEst);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
			i++;
			for(int isec=1; isec<=dat.getCantSectores(); isec++) {
				for(int itip=0; itip<dgs.getCantTiposDia(); itip++) {
					String nomSec = texto.get(i).get(0);
					double[] aux = utilitarios.UtilArrays.dameArrayD(ale.cargaListaRealesColJ(i, 2, nomSec));				
					sectores.get(nomSec).getCoefHora()[iest][itip]=aux;
					i++;
				}
			}
			iest++;
		}


		// Lee y carga coeficientes diarios
		dirArchivo = directorio + "/coefDia.txt";		
		texto = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		ale = new AsistenteLectorEscritorTextos(texto, dirArchivo);
		for(int isec=0; isec<dat.getCantSectores(); isec++) {
			String nomSec = dat.getNombresSectores()[isec];
			sectores.get(nomSec).setCoefDia(new double[dat.getCantEstac()][dat.getCantTiposDia()]);		
		}
		
		i=0;
		iest=0;
		for(String sEst: dat.getNombresEstac()) {
			if(!(ale.cargaPalabra(i, "ESTACION").equalsIgnoreCase(sEst))){
				System.out.println("Error en lectura de coefs. diarios de estación " + sEst);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
			i++;
			for(int isec=1; isec<=dat.getCantSectores(); isec++) {
				String nomSec = texto.get(i).get(0);
				for(int itip=0; itip<dgs.getCantTiposDia(); itip++) {
					double[] aux = utilitarios.UtilArrays.dameArrayD(ale.cargaListaReales(i, nomSec));				
					sectores.get(nomSec).getCoefDia()[iest]=aux;
					i++;
				}
			}
			iest++;
		}
		
		// Lee y carga energías mensuales
		int anio = dat.getAnioFac();
		dirArchivo = directorio + "/enerMes.txt";		
		ConjuntoDeSeries series = MetodosSeries.leeConjuntoDeSeries(dirArchivo);
		for(String ns: dat.getNombresSectores()) {
			Serie s = series.getSeries().get(ns);
			int t=0;
			while(s.getAnio()[t]<anio) {
				t++;
			}
			double[] auxm = new double[12];
			for(int im=1; im<=12; im++) {
				auxm[im-1] = s.getDatos()[t];
				if(s.getPaso()[t]!=im) {
					System.out.println("Error en datos de mes " + im + " serie " + ns);
					if (CorridaHandler.getInstance().isParalelo()){
						//PizarronRedis pp = new PizarronRedis();
						//pp.matarServidores();
					}
					System.exit(1);
				}
				t++;				
			}
			sectores.get(ns).setEnerMesFacMWh(auxm);
		}		
		return sectores;	
	}	
		
	
	public DatosTiposDeDia cargaTiposDeDia(String dirEntradas, int anioInicial, int anioFinal) {		
		return persistencia.CargadorTiposDeDia.cargaTiposDeDia(dirEntradas, anioInicial, anioFinal);
	}
		
	public Hashtable<String, Double> leerRecursosYDemanda(){		
		recursos = new Hashtable<String, Double>();		
		// Lee datos de recursos y demanda
		String dirArchivo = directorio + "/recursos.txt";		
		ArrayList<ArrayList<String>> texto = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos(texto, dirArchivo);	
		int i=0;
		directorioCorrida = ale.cargaPalabra(i, "DIRECTORIO_CORRIDA");
		i++;
		nombreDemanda = ale.cargaPalabra(i, "DEMANDA");
		i=1+2;
		nombresRecursos = new ArrayList<String>();
		while(i<texto.size()) {
			String nomrec = texto.get(i).get(0);
			nombresRecursos.add(nomrec);
			double valor = Double.parseDouble(texto.get(i).get(1));
			recursos.put(nomrec, valor);
			i++;
		}		
		return recursos;		
	}
	
	
	
	


	public String getDirectorio() {
		return directorio;
	}

	public void setDirectorio(String directorio) {
		this.directorio = directorio;
	}
	
	

	public String getDirectorioCorrida() {
		return directorioCorrida;
	}

	public void setDirectorioCorrida(String directorioCorrida) {
		this.directorioCorrida = directorioCorrida;
	}

	public String getNombreDemanda() {
		return nombreDemanda;
	}

	public void setNombreDemanda(String nombreDemanda) {
		this.nombreDemanda = nombreDemanda;
	}
		

	public ArrayList<String> getNombresRecursos() {
		return nombresRecursos;
	}

	public void setNombresRecursos(ArrayList<String> nombresRecursos) {
		this.nombresRecursos = nombresRecursos;
	}

	public Hashtable<String, Double> getRecursos() {
		return recursos;
	}

	public void setRecursos(Hashtable<String, Double> recursos) {
		this.recursos = recursos;
	}

	public static void main(String[] args) {
		String directorio = LectorDireccionArchivoDirectorio.direccionLeida(true, "G:\\PLA\\Pla_datos\\Archivos\\ModeloOp\\CURVAS DE CARGA Y SECTORES\\Pruebas", "ENTRAR DIRECTORIO DE DATOS GENERALES DE SECTORES");
		
		LectorDatosSectoresYRecursos lec = new LectorDatosSectoresYRecursos(directorio);
		
		DatosGenSectores datGS = lec.leerDatosGenSectores();
		
		System.out.println("Termina lectura");
	}
	
	

}
