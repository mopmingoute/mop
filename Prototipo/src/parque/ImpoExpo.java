/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ImpoExpo is part of MOP.
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

package parque;

import java.util.ArrayList;
import java.util.Hashtable; 

import compdespacho.EolicoCompDesp;
import compdespacho.ImpoExpoCompDesp;
import compgeneral.EolicoComp;
import compgeneral.ImpoExpoComp;
import compsimulacion.EolicoCompSim;
import compsimulacion.ImpoExpoCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.EolicoCompDespPE;
import cp_compdespProgEst.ImpoExpoCompDespPE;
import datatypes.DatosEolicoCorrida;
import datatypes.DatosImpoExpoCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosImpoExpoSP;
import datatypesSalida.DatosProveedorElecSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.PEDispSimple;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.Polinomio;

public class ImpoExpo extends Recurso{
	
	private String pais;  // pais de la interconexion
	private String tipoImpoExpo;
	
	/**
	 * Nombres de las variables aleatorias de este participante
	 */
	private ArrayList<String> nombresVAPrecio;  
	private ArrayList<String> nombresVAPotencia;
	private String nombreVACMg;
	
	private boolean hayMinTec;
	private Evolucion<Double> minTec;
	
	/**
	 * Se supone que la cantidad de modulos instalados de este tipo de recurso es idónticamente uno
	 */
	
	

	/**
	 * Tipos de ImpoExpo
	 * 
	 * 	"IEEVOL"    La potencia y el precio estón dados por Evoluciones. LA disponibilidad la da un PEDispSimple
	 * 
	 *  "IEALEATFORMUL" La potencia y el precio son Polinomios que se aplican a una variable aleatoria que
	 *  normalmente es el costo marginal del paós con el que se comercia.
	 *  
	 *  "IEALEATPRPOT" La potencia y el precio son variables aleatorias diferentes 
	 *  
	 */	
		
	private Barra barra;	
	private String operacionCompraVenta;
	private int cantBloques; // cantidad de bloques de ofertas o demandas de potencia
	private ImpoExpoCompDesp compD;
	private ImpoExpoCompSim compS;
	private ImpoExpoComp compG;	
	
	/**
	 * Atributos si tipoImpoExpo = IEEVOL
	 * primer índice bloque
	 * segundo índice poste; solo hay mós de un valor si hay valpostización externa
	 * La estacionalidad estó resuelta por la Evolucion
	 * Este tipo cubre las funciones del tipo ESTAC del EDF
	 */
	private ArrayList<Evolucion<ArrayList<Double>>>  potEvol;   // evoluciones de potencias
	private ArrayList<Evolucion<ArrayList<Double>>>  preEvol;   // evoluciones de precios
	private ArrayList<Evolucion<ArrayList<Double>>>  dispEvol;   // evoluciones de precios
	// La disponibilidad es el producto de la diponibilidad del recurso por la dispEvol
	
	/** 
	 * Atributos si tipoImpoExpo = IEALEATPRPOT
	 * Se definen la potencia y el precio mediante variables aleatorias. La disponibilidad queda tenida en cuenta en la potencia.
	 *	El óndice de los ArrayList es en los bloques.
	 *	Para cada una de estas variables se hace el mismo tratamiento respecto a la postización interna o externa que para la demanda o el eólico y solar.
	 *  Si la postización es interna, se toma la realización de la VA en los instantes de muestreo.
	 *  Si la postización es externa, se espera que con el nombre de la respectiva VA y el sufijo nómero de poste, existan variables aleatorias en el proceso proc, una para cada poste.
	 * Con esto se puede hacer lo que hace el tipo FIJO del EDF.	
	 */ 	
	private ArrayList<VariableAleatoria> vaPrecio;
	private ArrayList<VariableAleatoria> vaPotencia;
		
	/**
	 * Atributos si tipoImpoExpo = IEALEATFORMUL
	 * Hay una variable aleatoria vACMg.
	 * Hay Polinomios que a partir del valor de la VA devuelven la potencia, el precio, y la disponibilidad por cada bloque.
	 * En principio los Polinomios no cambian con los postes 
	 * 	 
	 * Si la postización es externa para cada bloque y cada poste externo hay una variable aleatoria
	 * con cuyo nombre es nombreVA + i, donde i es el poste externo a partir de cero, igual que en la postización
	 * externa de la demanda o el eólico y solar. 
	 */
	
	private VariableAleatoria vaCMg;   // normalmente un costo marginal de un paós con el que se comercia o el precio.
	private Evolucion<Double> factorEscalamiento; // multiplica el valor de vaCMg
	private VariableAleatoria vaUniforme;
	
	private ArrayList< Polinomio> poliPot;
	private ArrayList< Polinomio> poliPre;
	private ArrayList< Polinomio> poliDisp;
	// La disponibilidad es el producto de la diponibilidad del recurso por la poliDisp	
	

	/**Constructor del ImpoExpo a partir de sus datos*/
	public ImpoExpo(DatosImpoExpoCorrida dat) {
		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida actual = ch.getCorridaActual();
		this.setPropietario(dat.getPropietario());
		this.setNombre(dat.getNombre());
		this.setPais(dat.getPais());
		this.setBarra(CorridaHandler.getInstance().getBarra(dat.getBarra()));
		this.setTipoImpoExpo(dat.getTipoImpoExpo());
		this.setCantBloques(dat.getCantBloques());
		this.setOperacionCompraVenta(dat.getOperacionCompraVenta());
		this.setCostoFijo(dat.getCostoFijo());
// 		TODO: OJO NO ENTIENDO LA SENTENCIA SIGUIENTE
//		this.setCompDesp(new ImpoExpoCompDesp(this));
		this.hayMinTec = dat.isHayMinTec();
		this.minTec = dat.getMinTec();
		this.setCantModInst(dat.getCantModInst());
		compD = new ImpoExpoCompDesp(this);
		// OJO NO ENTENDEMOS EL CONSTRUCTOR PORQUE compS es null
		compG = new ImpoExpoComp(this,compD,compS);
		compS = new ImpoExpoCompSim(compD, this,  compG);	
		
		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
		compG.setParticipante(this);
		
		nombresVAPrecio = new ArrayList<String>(); 
		nombresVAPotencia = new ArrayList<String>();
		vaPrecio = new ArrayList<VariableAleatoria>();
		vaPotencia = new ArrayList<VariableAleatoria>();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEEVOL)){
			
			this.setPotEvol(dat.getPotEvol());
			this.setPreEvol(dat.getPreEvol());
			this.setDispEvol(dat.getDispEvol());
			
						
		}else if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATFORMUL)){		
			poliPre = new ArrayList<Polinomio>();
			poliPot = new ArrayList<Polinomio>();
			poliDisp = new ArrayList<Polinomio>();
			for(int ib=0; ib<cantBloques; ib++){
				poliPre.add(new Polinomio(dat.getPoliPre().get(ib).getValor(instanteActual)));
				poliPot.add(new Polinomio(dat.getPoliPot().get(ib).getValor(instanteActual)));
				poliDisp.add(new Polinomio(dat.getPoliDisp().get(ib).getValor(instanteActual)));
			}
			/**
			 * VARIABLES ALEATORIAS
			 */
			ProcesoEstocastico proc = actual.dameProcesoEstocastico(dat.getDatCMg().getProcSimulacion());
			ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(dat.getDatCMg().getProcOptimizacion());								
			nombreVACMg = dat.getDatCMg().getNombre();
			// VER: este código se hace porque es necesario acceder a la prioridad de la VA CMG y puede haber postización paso
			this.vaCMg = proc.devuelveVADeNombre(nombreVACMg);
			if(this.vaCMg==null) this.vaCMg = proc.devuelveVADeNombre(nombreVACMg+"1");			
			this.agregarProcesoAParticipante(nombreVACMg, Constantes.FASE_OPT, procOptim);
			this.agregarProcesoAParticipante(nombreVACMg, Constantes.FASE_SIM, proc);
			this.chequearProcesosConAsociadoEnOptim(proc, procOptim);	
			
			this.factorEscalamiento = dat.getFactorEscalamiento();
						
						
		}else if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATPRPOT)){
			/**
			 * VARIABLES ALEATORIAS
			 */
			for(int ib=0; ib<cantBloques; ib++){
				ProcesoEstocastico proc = actual.dameProcesoEstocastico(dat.getDatPrecio().get(ib).getProcSimulacion());
				ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(dat.getDatPrecio().get(ib).getProcOptimizacion());						
				String nombreVA = dat.getDatPrecio().get(ib).getNombre();
				nombresVAPrecio.add(nombreVA);
				if(proc.devuelveVADeNombre(nombreVA)==null){
					vaPrecio.add(proc.devuelveVADeNombre(nombreVA+"1"));
				}else{
					vaPrecio.add(proc.devuelveVADeNombre(nombreVA));
				}
				this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_OPT, procOptim);
				this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_SIM, proc);
			}
			for(int ib=0; ib<cantBloques; ib++){
				ProcesoEstocastico proc = actual.dameProcesoEstocastico(dat.getDatPotencia().get(ib).getProcSimulacion());
				ProcesoEstocastico procOptim = actual.dameProcesoEstocastico(dat.getDatPotencia().get(ib).getProcOptimizacion());						
				String nombreVA = dat.getDatPotencia().get(ib).getNombre();
				nombresVAPotencia.add(nombreVA);
				if(proc.devuelveVADeNombre(nombreVA)==null){
					vaPotencia.add(proc.devuelveVADeNombre(nombreVA+"1"));
				}else{
					vaPotencia.add(proc.devuelveVADeNombre(nombreVA));
				}
				this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_OPT, procOptim);
				this.agregarProcesoAParticipante(nombreVA, Constantes.FASE_SIM, proc);
			}
			
			// OJO QUE FALTA AGREGAR DATOS

			
		}else{
			System.out.println("Error en tipo de ImpoExpo");
			if (CorridaHandler.getInstance().isParalelo()){
				//PizarronRedis pp = new PizarronRedis();
			//	pp.matarServidores();
			}
			System.exit(1);
		}
		
		if(!tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATPRPOT)){
			String nombreD = "Unif-" + this.getNombre();
			SentidoTiempo st = new SentidoTiempo(1);
			PEDispSimple pedg = new PEDispSimple(nombreD);
			
			vaUniforme = new VariableAleatoria();
			vaUniforme.setNombre(this.getNombre() + "unif_disp");
			vaUniforme.setMuestreada(false);
			pedg.getVariablesAleatorias().add(vaUniforme);
			pedg.setCantVA(pedg.getVariablesAleatorias().size());
			pedg.setUnif(vaUniforme);
			pedg.getNombresVarsAleatorias().add(vaUniforme.getNombre());	
			pedg.setUsoOptimizacion(true);
			pedg.setUsoSimulacion(true);
			actual.agregarPE(pedg);
			pedg.setCantidadInnovaciones(1);		 
			vaUniforme.setPe(pedg);
			if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEEVOL)) {
				pedg.setPrioridadSorteo(1);	
			}else {
				pedg.setPrioridadSorteo(this.vaCMg.getPe().getPrioridadSorteo()+1);	
			}			
			this.agregarProcesoAParticipante(vaUniforme.getNombre(), Constantes.FASE_OPT, pedg);
			this.agregarProcesoAParticipante(vaUniforme.getNombre(), Constantes.FASE_SIM, pedg);
		}
		
	}
	

	////////////////
	
	
	@Override
	public void inicializarParaEscenario() {
		// Deliberadamente vacóo
		
	}
	
	@Override
	public void asignaVAOptim() {
		if(this.getProcesosDelParticipante()!=null){
			String nombreAux;
			for(String nombre : nombresVAPrecio){
				ProcesoEstocastico peOptim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombre +"1";
					
				} else {
					nombreAux = nombre;
				}					
				peOptim = this.devuelveProceso(nombre, Constantes.FASE_OPT);
				if (peOptim != null) {
					VariableAleatoria va = peOptim.devuelveVADeNombre(nombreAux);			
					vaPrecio.add(va);					
				}
			}
			for(String nombre : nombresVAPotencia){
				ProcesoEstocastico peOptim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombre +"1";
					
				} else {
					nombreAux = nombre;
				}				
				peOptim = this.devuelveProceso(nombre, Constantes.FASE_OPT);
				if (peOptim != null) {
					VariableAleatoria va = peOptim.devuelveVADeNombre(nombreAux);			
					vaPotencia.add(va);					
				}
			}	
			
			if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATFORMUL)){
				ProcesoEstocastico peOptim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombreVACMg +"1";					
				} else {
					nombreAux = nombreVACMg;
				}					
				peOptim = this.devuelveProceso(nombreVACMg, Constantes.FASE_OPT);
				if (peOptim != null) {
					VariableAleatoria va = peOptim.devuelveVADeNombre(nombreAux);			
					vaCMg = va;		
				}
			}	
		}	
	}
	
	@Override
	public void asignaVASimul() {
		if(this.getProcesosDelParticipante()!=null){
			String nombreAux;
			for(String nombre : nombresVAPrecio){
				ProcesoEstocastico peSim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombre +"1";					
				} else {
					nombreAux = nombre;
				}					
				peSim = this.devuelveProceso(nombre, Constantes.FASE_SIM);
				if (peSim != null) {
					VariableAleatoria va = peSim.devuelveVADeNombre(nombreAux);			
					vaPrecio.add(va);					
				}
			}
			for(String nombre : nombresVAPotencia){
				ProcesoEstocastico peSim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombre +"1";
					
				} else {
					nombreAux = nombre;
				}				
				peSim = this.devuelveProceso(nombre, Constantes.FASE_SIM);
				if (peSim != null) {
					VariableAleatoria va = peSim.devuelveVADeNombre(nombreAux);			
					vaPotencia.add(va);				
				}
			}		
			if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATFORMUL)){
				ProcesoEstocastico peSim;				
				if (this.getSimPaso().getValPostizador().isExterna()) {
					nombreAux = nombreVACMg +"1";
					
				} else {
					nombreAux = nombreVACMg;
				}
					
				peSim = this.devuelveProceso(nombreVACMg, Constantes.FASE_SIM);
				if (peSim != null) {
					VariableAleatoria va = peSim.devuelveVADeNombre(nombreAux);			
					vaCMg = va;			
				}
			}		
		}
	}
	
	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,	String proceso, long instante) {
		double [][] potencia = new double [this.getCantBloques()][this.getCantPostes()];
		double energiaTot = 0;
		double precioMed = 0;
		for(int ib=0; ib < this.getCantBloques();ib++){
			for (int ip = 0; ip < this.getCantPostes(); ip++) {
				potencia[ib][ip] = salidaUltimaIter.getSolucion().get(this.getCompD().generarNombre("pot",Integer.toString(ib) ,Integer.toString(ip)));
				energiaTot += potencia[ib][ip]*this.getDuracionPostes(ip)/Constantes.SEGUNDOSXHORA;
			//	System.out.println("POTENCIA: " + potencia[ib][ip]);
			}
		}
		double CMgPais = 0;
		if(this.getTipoImpoExpo().equalsIgnoreCase(utilitarios.Constantes.IEALEATFORMUL)) {
			CMgPais = this.getFactorEscalamiento().getValor(instante)*this.getVaCMg().getValor();
		}
		
		double costoTot = calculaCostoPaso(salidaUltimaIter);
		if(energiaTot>0) precioMed = costoTot/energiaTot;
		
		DatosImpoExpoSP dat = new DatosImpoExpoSP(this.getNombre(), this.getOperacionCompraVenta(), potencia, costoTot, CMgPais, precioMed);
		String nombarra = this.getBarra().getNombre();
		for (DatosBarraSP dbsp: resultadoPaso.getRed().getBarras()) {
			if (nombarra.equalsIgnoreCase(dbsp.getNombre()) || this.getBarra().getRedAsociada().getCompD().isUninodal()){
				dbsp.agregarImpoExpo(dat);
				break;
			}
		}
	}
	
	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		ArrayList<ProcesoEstocastico> ret = new ArrayList<ProcesoEstocastico>();		
		ProcesoEstocastico peOptim ;
		
		for(String nombre : nombresVAPrecio){
			peOptim = this.devuelveProceso(nombre, Constantes.FASE_OPT);
			if(!ret.contains(peOptim)) ret.add(peOptim);
		}
		
		for(String nombre : nombresVAPotencia){
			peOptim = this.devuelveProceso(nombre, Constantes.FASE_OPT);
			if(!ret.contains(peOptim)) ret.add(peOptim);
		}		
		
		if(tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATFORMUL)){
			peOptim = this.devuelveProceso(nombreVACMg, Constantes.FASE_OPT);
			ret.add(peOptim);
		}
		if(!tipoImpoExpo.equalsIgnoreCase(Constantes.IEALEATPRPOT)){
			peOptim = this.devuelveProceso(this.vaUniforme.getNombre(),Constantes.FASE_OPT);
			ret.add(peOptim);
		}
		
		return ret;
	}
	
	
	
	
	
	
	////////////////

	
	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		this.pais = pais;
	}
	public String getTipoImpoExpo() {
		return tipoImpoExpo;
	}
	public void setTipoImpoExpo(String tipoImpoExpo) {
		this.tipoImpoExpo = tipoImpoExpo;
	}
	public Barra getBarra() {
		return barra;
	}
	public void setBarra(Barra barra) {
		this.barra = barra;
	}
	public String getOperacionCompraVenta() {
		return operacionCompraVenta;
	}
	public void setOperacionCompraVenta(String operacionCompraVenta) {
		this.operacionCompraVenta = operacionCompraVenta;
	}
	public int getCantBloques() {
		return cantBloques;
	}
	public void setCantBloques(int cantBloques) {
		this.cantBloques = cantBloques;
	}

	public ImpoExpoCompDesp getCompD() {
		return compD;
	}
	public void setCompD(ImpoExpoCompDesp compD) {
		this.compD = compD;
	}
	public ImpoExpoCompSim getCompS() {
		return compS;
	}
	public void setCompS(ImpoExpoCompSim compS) {
		this.compS = compS;
	}
	public ImpoExpoComp getCompG() {
		return compG;
	}
	public void setCompG(ImpoExpoComp compG) {
		this.compG = compG;
	}


	public ArrayList<Evolucion<ArrayList<Double>>> getPotEvol() {
		return potEvol;
	}


	public void setPotEvol(ArrayList<Evolucion<ArrayList<Double>>> potEvol) {
		this.potEvol = potEvol;
	}


	public ArrayList<Evolucion<ArrayList<Double>>> getPreEvol() {
		return preEvol;
	}


	public void setPreEvol(ArrayList<Evolucion<ArrayList<Double>>> preEvol) {
		this.preEvol = preEvol;
	}


	public ArrayList<Evolucion<ArrayList<Double>>> getDispEvol() {
		return dispEvol;
	}


	public void setDispEvol(ArrayList<Evolucion<ArrayList<Double>>> dispEvol) {
		this.dispEvol = dispEvol;
	}


	public ArrayList<VariableAleatoria> getVaPrecio() {
		return vaPrecio;
	}


	public void setVaPrecio(ArrayList<VariableAleatoria> vaPrecio) {
		this.vaPrecio = vaPrecio;
	}


	public ArrayList<VariableAleatoria> getVaPotencia() {
		return vaPotencia;
	}


	public void setVaPotencia(ArrayList<VariableAleatoria> vaPotencia) {
		this.vaPotencia = vaPotencia;
	}


	public VariableAleatoria getVaCMg() {
		return vaCMg;
	}


	public void setVaCMg(VariableAleatoria vaCMg) {
		this.vaCMg = vaCMg;
	}


	
	public Evolucion<Double> getFactorEscalamiento() {
		return factorEscalamiento;
	}


	public void setFactorEscalamiento(Evolucion<Double> factorEscalamiento) {
		this.factorEscalamiento = factorEscalamiento;
	}


	public VariableAleatoria getVaUniforme() {
		return vaUniforme;
	}


	public ArrayList<Polinomio> getPoliPot() {
		return poliPot;
	}


	public void setPoliPot(ArrayList<Polinomio> poliPot) {
		this.poliPot = poliPot;
	}


	public ArrayList<Polinomio> getPoliPre() {
		return poliPre;
	}


	public void setPoliPre(ArrayList<Polinomio> poliPre) {
		this.poliPre = poliPre;
	}


	public ArrayList<Polinomio> getPoliDisp() {
		return poliDisp;
	}


	public void setPoliDisp(ArrayList<Polinomio> poliDisp) {
		this.poliDisp = poliDisp;
	}


	public void setVaUniforme(VariableAleatoria vaUniforme) {
		this.vaUniforme = vaUniforme;
	}

	public ArrayList<String> getNombresVAPrecio() {
		return nombresVAPrecio;
	}


	public void setNombresVAPrecio(ArrayList<String> nombresVAPrecio) {
		this.nombresVAPrecio = nombresVAPrecio;
	}


	public ArrayList<String> getNombresVAPotencia() {
		return nombresVAPotencia;
	}


	public void setNombresVAPotencia(ArrayList<String> nombresVAPotencia) {
		this.nombresVAPotencia = nombresVAPotencia;
	}


	public String getNombreVACMg() {
		return nombreVACMg;
	}


	public void setNombreVACMg(String nombreVACMg) {
		this.nombreVACMg = nombreVACMg;
	}


	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}
	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		ImpoExpo.atributosDetallados = atributosDetallados;
	}
	private static ArrayList<String> atributosDetallados;   // los de las salidas


	@Override
	public void aportarImpacto(Impacto i,DatosObjetivo costo ) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		
		return 0.0;
	}
	
	
	
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		return this.compD.cargarRestriccionesImpacto(impacto);
	}
	


	public boolean isMinTec() {
		return hayMinTec;
	}


	public void setMinTec(boolean minTec) {
		this.hayMinTec = minTec;
	}


	public Evolucion<Double> getMinTec() {
		return minTec;
	}


	public void setMinTec(Evolucion<Double> minTec) {
		this.minTec = minTec;
	}

	

	@Override
	public void crearCompDespPE() {
		ImpoExpoCompDespPE compDespPE = new ImpoExpoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}


}