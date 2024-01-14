/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaVByValRecursosRedis is part of MOP.
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

package futuro;


import java.util.ArrayList;
import java.util.Hashtable;

import optimizacion.Optimizador;
import optimizacion.ResOptim;
import optimizacion.ResOptimIncrementos;
import parque.Participante;
import pizarron.Paquete;
import pizarron.PizarronRedis;
import utilitarios.EnumeradorLexicografico;


public class TablaVByValRecursosRedis extends TablaVByValRecursos {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Es una lista con una tabla por paso de tiempo.
	 * Para cada paso de tiempo una tabla cuya clave es el código de enteros del punto de discretización
	 */
	private PizarronRedis pizarron;
	private ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>> tablaValores;
	
//	public TablaVByValRecursosRedis (){
//		setPizarron(new PizarronRedis());
//		tablaValores = new ArrayList<Hashtable<ClaveDiscreta,InformacionValorPunto>>();
//	}
	

	public TablaVByValRecursosRedis(int cantPasos) {
		setPizarron(PizarronRedis.getInstance());
		this.setCantPasos(cantPasos);
		tablaValores = new ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>>();
		for(int ip=0; ip<cantPasos; ip++){
			Hashtable<ClaveDiscreta, InformacionValorPunto> ht = new Hashtable<ClaveDiscreta, InformacionValorPunto>();
			tablaValores.add(ht);
		}		
	}


	public PizarronRedis getPizarron() {
		return pizarron;
	}

	public void setPizarron(PizarronRedis pizarron) {
		this.pizarron = pizarron;
	}


	@Override
	public InformacionValorPunto devuelveInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave) {
		return this.devuelveInfoValoresPunto(i, clave);
	}


	@Override
	public void cargaTablaAuxiliar(int paso) {
		pizarron.cargaTablaAuxiliar(paso,this.tablaValores.get(0));
	}


	@Override
	public void cargaTabla(int paso) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> aCargar = this.tablaValores.get(paso);
		pizarron.cargaTabla(paso,aCargar);		
	}


	@Override
	public void devuelveTablaAuxiliar(int paso, int cantPaquetes, int cantEstados) {		
		if(this.tablaValores.get(0)!=null) this.tablaValores.remove(0);
		this.tablaValores.add(0,pizarron.devuelveTablaAuxiliar(paso,cantPaquetes, cantEstados));
	}


	@Override
	public void devuelveTabla(int paso) {
		if(this.tablaValores.get(paso)!=null) this.tablaValores.remove(paso);
		this.tablaValores.add(paso,pizarron.devuelveTabla(paso));		
	}


	@Override
	public InformacionValorPunto devuelveInfoValoresPunto(int paso, ClaveDiscreta clave) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> tabla1Paso = tablaValores.get(paso);
		return tabla1Paso.get(clave);
	}
	

	@Override
	public double devuelveValorVBPunto(int paso, ClaveDiscreta clave) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> tabla1Paso = tablaValores.get(paso);
		InformacionValorPunto infoVP = tabla1Paso.get(clave);
		return infoVP.getValorVB();
	}
	

	@Override
	public void cargaInfoValoresPunto(int paso, ClaveDiscreta clave, InformacionValorPunto infvp) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> tabla1Paso = tablaValores.get(paso);
		if(tabla1Paso.containsKey(clave)) tabla1Paso.remove(clave);
		tabla1Paso.put(clave, infvp);			
	}


	@Override
	public void cargaInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave, InformacionValorPunto ivp) {
		cargaInfoValoresPunto(i,clave,ivp);
		
	}


	@Override
	public boolean compatiblePaquete(Paquete paq, ResOptim resoptim) {
		ResOptimIncrementos roptim = (ResOptimIncrementos)resoptim;
		
		for (int estado = paq.getEstadoIni(); estado < paq.getEstadoFin(); estado++) {
			int[] codigoEstado = roptim.getEnumLexEstados().devuelveVectorDeOrdinal(estado);
			ClaveDiscreta clave = new ClaveDiscreta(codigoEstado);
			InformacionValorPunto ivp = roptim.getTablaValores().devuelveInfoValoresPunto(paq.getPaso(), clave);
			if (ivp==null) return false;			
		}
		return true;
	}
	
	
	
	

}
