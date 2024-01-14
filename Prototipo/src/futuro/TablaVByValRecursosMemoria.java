/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaVByValRecursosMemoria is part of MOP.
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

import datatypesResOptim.DatosTablaVByValRec;
import estado.VariableEstado;
import optimizacion.ResOptim;
import pizarron.Paquete;
import utilitarios.EnumeradorLexicografico;

public class TablaVByValRecursosMemoria extends TablaVByValRecursos {
	
	
	/*
	 * Es una lista con una tabla por paso de tiempo.
	 * Para cada paso de tiempo una tabla cuya clave es el código de enteros del punto de discretización
	 */
	private ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>> tablaValores;
	

	
	
	public TablaVByValRecursosMemoria (){
		tablaValores = new ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>>();
	}
	
	public TablaVByValRecursosMemoria(DatosTablaVByValRec dtv){
		this.setCantPasos(dtv.getCantPasos());
		this.setTablaValores(dtv.getTablaValores());
	}
	
	/**
	 * Cantidad de pasos de la tabla
	 * @param cantPasos
	 */
	public TablaVByValRecursosMemoria(int cantPasos) {
		this.setCantPasos(cantPasos);
		tablaValores = new ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>>();
		for(int ip=0; ip<cantPasos; ip++){
			Hashtable<ClaveDiscreta, InformacionValorPunto> ht = new Hashtable<ClaveDiscreta, InformacionValorPunto>();
			tablaValores.add(ht);
		}		
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


	public ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>> getTablaValores() {
		return tablaValores;
	}


	public void setTablaValores(
			ArrayList<Hashtable<ClaveDiscreta, InformacionValorPunto>> tablaValores) {
		this.tablaValores = tablaValores;
	}

	
	

	@Override
	public void cargaInfoValoresPunto(int paso, ClaveDiscreta clave, InformacionValorPunto infvp) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> tabla1Paso = tablaValores.get(paso);
		if(tabla1Paso.containsKey(clave)) tabla1Paso.remove(clave);
		tabla1Paso.put(clave, infvp);			
	}


	
	public DatosTablaVByValRec creaDataType(){
		DatosTablaVByValRec dt = new DatosTablaVByValRec();
		dt.setCantPasos(this.getCantPasos());
		dt.setTablaValores(tablaValores);
		return dt;
		
	}

	@Override
	public void cargaInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave, InformacionValorPunto ivp) {
		cargaInfoValoresPunto(i,clave,ivp);
		
	}

	@Override
	public InformacionValorPunto devuelveInfoValoresPuntoAuxiliar(int i, ClaveDiscreta clave) {
		// TODO Auto-generated method stub
		return devuelveInfoValoresPunto(i,clave);
	}


	@Override
	public void devuelveTablaAuxiliar(int paso, int cantPasos, int cantEstados) {
		// TODO Auto-generated method stub
	}

	@Override
	public void devuelveTabla(int paso) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cargaTablaAuxiliar(int paso) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cargaTabla(int paso) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean compatiblePaquete(Paquete paq,ResOptim resoptim) {
		// TODO Auto-generated method stub
		return true;
	}

	
	

}
