/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ContratoCombustibleTopPasoFijo is part of MOP.
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

import compdespacho.ContratoCombCanioCompDesp;
import compdespacho.ContratoCombTopPasoFijoCompDesp;
import compgeneral.ContratoCombCanioComp;
import compsimulacion.ContratoCombCanioCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import datatypes.DatosContratoCombustibleCorrida;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import estado.VariableEstadoPar;
import logica.CorridaHandler;
import procesosEstocasticos.PEDisponibilidadGeometrica;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.VariableAleatoria;

public class ContratoCombustibleTopPasoFijo extends ContratoCombustible{
	
	
	private RedCombustible redAsociada;
	private ContratoCombCanioCompDesp compD;
	private ContratoCombCanioComp compG;
	private ContratoCombCanioCompSim compS;
	private ArrayList<VariableEstadoPar> prepagos; // el get(i) es el prepago de i peróodos anteriores al corriente 
	private ArrayList<VariableEstadoPar> prepagosOptim; //
	
	public ContratoCombustibleTopPasoFijo(DatosContratoCombustibleCorrida datos, Combustible comb,
			BarraCombustible ba) {
		super(datos, comb, ba);
		// TODO Auto-generated constructor stub
	}

	private TakeOrPayPasoFijo top;
	
	
	private String nCaudalComb;
	
	
	
	/**
	 * Devuelve el atributo estado de las VE prepagos del TOP de la simulación
	 * es decir los volómenes del paso corriente, paso anterior, etc.
	 * @return
	 */
	public double[] devuelvePrepagosEstado(){
		double[] volPrepagos = new double[top.getCantPeriodosMU()+1];
		int ip=0;
		for(VariableEstadoPar ve: prepagos){
			volPrepagos[ip]=prepagos.get(ip).getEstado();
			ip++;
		}
		return volPrepagos;
	}	
	
	
	/**
	 * Devuelve el atributo estado de las VE prepagos del TOP de la optimización
	 * es decir los volómenes del paso corriente, paso anterior, etc.
	 * @return
	 */
	public double[] devuelvePrepagosEstadoOptim(){
		double[] volPrepagos = new double[top.getCantPeriodosMU()+1];
		int ip=0;
		for(VariableEstadoPar ve: prepagosOptim){
			volPrepagos[ip]=prepagosOptim.get(ip).getEstado();
			ip++;
		}
		return volPrepagos;
	}
	
	/**
	 * Devuelve valores del recurso bien prepagado de la simulación
	 * @return
	 */
	public double[] devuelveValoresRecursoPrepago(){
		double[] valor = new double[top.getCantPeriodosMU()+1];
		int ip=0;
		for(VariableEstadoPar ve: prepagos){
			valor[ip]=prepagos.get(ip).getValorRecurso().get(0);
			ip++;
		}
		return valor;		
	}
	
	
	public double[] devuelveValoresRecursoPrepagoOptim(){
		double[] valor = new double[top.getCantPeriodosMU()+1];
		int ip=0;
		for(VariableEstadoPar ve: prepagosOptim){
			valor[ip]=prepagosOptim.get(ip).getValorRecurso().get(0);
			ip++;
		}
		return valor;
	}
	

	/**
	 * Carga los atributos estado de las VE de la simulación 
	 * @param prepagos
	 */
	public void cargaPrepagosEstado(double[] volPrepagos){
		for(int ip=0; ip<=top.getCantPeriodosMU();ip++){
			prepagos.get(ip).setEstado(volPrepagos[ip]);
			ip++;
		}
	}
	
	
	/**
	 * Carga los atributos estadoS0fint de las VE de la simulación
	 * @param prepagos
	 */
	public void cargaPrepagosEstadoS0fint(double[] volPrepagos){
		for(int ip=0; ip<=top.getCantPeriodosMU();ip++){
			prepagos.get(ip).setEstadoS0fint(volPrepagos[ip]);
			ip++;
		}
	}
	
	/**
	 * Carga los atributos estadoS0fint de las VE de la optimización
	 * @param prepagos
	 */
	public void cargaPrepagosEstadoS0fintOptim(double[] volPrepagos){
		for(int ip=0; ip<=top.getCantPeriodosMU();ip++){
			prepagosOptim.get(ip).setEstadoS0fint(volPrepagos[ip]);
			ip++;
		}
	}
	
	/**
	 * Carga los atributos estadoFinalOptim de las VE de la optimización
	 * @param prepagos
	 */
	public void cargaPrepagosEstadoFinalOptim(double[] volPrepagos){
		for(int ip=0; ip<=top.getCantPeriodosMU();ip++){
			prepagosOptim.get(ip).setEstadoFinalOptim(volPrepagos[ip]);
			ip++;
		}
	}	

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void asignaVAOptim() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void asignaVASimul() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String proceso, long instante) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNCaudalComb() {
		ContratoCombTopPasoFijoCompDesp cccd = (ContratoCombTopPasoFijoCompDesp) this.getCompDesp();
		return cccd .getNCaudalComb();
	}

	public TakeOrPayPasoFijo getTop() {
		return top;
	}

	public void setTop(TakeOrPayPasoFijo top) {
		this.top = top;
	}

	public String getnCaudalComb() {
		return nCaudalComb;
	}

	public void setnCaudalComb(String nCaudalComb) {
		this.nCaudalComb = nCaudalComb;
	}

	public RedCombustible getRedAsociada() {
		return redAsociada;
	}

	public void setRedAsociada(RedCombustible redAsociada) {
		this.redAsociada = redAsociada;
	}

	public ContratoCombCanioCompDesp getCompD() {
		return compD;
	}

	public void setCompD(ContratoCombCanioCompDesp compD) {
		this.compD = compD;
	}

	public ContratoCombCanioComp getCompG() {
		return compG;
	}

	public void setCompG(ContratoCombCanioComp compG) {
		this.compG = compG;
	}

	public ContratoCombCanioCompSim getCompS() {
		return compS;
	}

	public void setCompS(ContratoCombCanioCompSim compS) {
		this.compS = compS;
	}


	@Override
	public double costoMedio(long instante) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void aportarImpacto(Impacto i,DatosObjetivo costo ) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void crearCompDespPE() {
		// Deliberadamente en blanco
		
	}
}
