/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaControlesDERedis is part of MOP.
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

import datatypesResOptim.DatosTablaControlesDE;
import datatypesResOptim.DatosTablaVByValRec;
import pizarron.PizarronRedis;

public class TablaControlesDERedis extends TablaControlesDE {

	/*
	 * Es una lista con una tabla por paso de tiempo. Para cada paso de tiempo una
	 * tabla cuya clave es el código de enteros del punto de discretización
	 */
	private ArrayList<Hashtable<ClaveDiscreta, int[]>> tablaControles;
	private PizarronRedis pizarron;

	/**
	 * 
	 * @param cantPasos Cantidad de pasos de la tabla
	 */
	public TablaControlesDERedis(int cantPasos) {
		this.setCantPasos(cantPasos);
		tablaControles = new ArrayList<Hashtable<ClaveDiscreta, int[]>>();
		for (int ip = 0; ip < cantPasos; ip++) {
			Hashtable<ClaveDiscreta, int[]> ht = new Hashtable<ClaveDiscreta, int[]>();
			tablaControles.add(ht);
		}
		pizarron = PizarronRedis.getInstance();
	}

	public ArrayList<Hashtable<ClaveDiscreta, int[]>> getTablaControles() {
		return tablaControles;
	}

	public void setTablaControles(ArrayList<Hashtable<ClaveDiscreta, int[]>> tablaControles) {
		this.tablaControles = tablaControles;
	}

	public TablaControlesDERedis(DatosTablaControlesDE dc) {
		this.setCantPasos(dc.getCantPasos());
		this.setTablaControles(dc.getTablaControles());
	}

	public TablaControlesDERedis() {
		tablaControles = new ArrayList<Hashtable<ClaveDiscreta, int[]>>();
	}

	@Override
	public int[] devuelveCodigoControlesDEOpt(int paso, int[] sInit) {
		return tablaControles.get(paso).get(new ClaveDiscreta(sInit));

	}

	@Override
	public void cargaCodigoControlesDEOpt(int paso, int[] sInit, int[] codigoOpt) {
		tablaControles.get(paso).put(new ClaveDiscreta(sInit), codigoOpt);

	}

	public DatosTablaControlesDE creaDataType() {
		DatosTablaControlesDE dt = new DatosTablaControlesDE();
		dt.setCantPasos(this.getCantPasos());
		dt.setTablaControles(tablaControles);
		return dt;
	}



	@Override
	public void cargaTabla(int paso) {
		Hashtable<ClaveDiscreta, int[]> aCargar = this.tablaControles.get(paso);
		pizarron.cargaTablaControles(paso,aCargar);		

	}

	
	@Override
	public void devuelveTabla(int paso,int cantPaquetes, int cantEstados) {		
		this.tablaControles.add(paso,pizarron.devuelveTablaControles(paso,cantPaquetes,cantEstados));		
	}

	@Override
	public void devuelveTablaEnteraDE(int paso) {		
		this.tablaControles.add(paso,pizarron.devuelveTablaControlesDE(paso));		
	}


}
