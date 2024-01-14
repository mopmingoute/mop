/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TablaHiperplanosMemoria is part of MOP.
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
import java.util.Iterator;
import java.util.Set;

import datatypesResOptim.DatosHiperplano;
import datatypesResOptim.DatosTablaHiperplanos;
import datatypesResOptim.DatosTablaVByValRec;

public class TablaHiperplanosMemoria extends TablaHiperplanos{
	
	/**
	 * PARA IMPLEMENTAR LOS MóTODOS DE TablaHiperplanos SE PRECISAN DOS Hashtables:
	 * tablaHiperplanos
	 * tablaHiperplanosPorPunto
	 */
	
	
	/*
	 * Es una lista con una tabla por paso de tiempo.
	 * Para cada paso de tiempo una tabla cuya clave es el código de enteros del punto de discretización
	 */
	private ArrayList<Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>> tablaHiperplanos;
	
	/**
	 * Es la tabla que a cada clave de todas las VE le asigna el hiperplano por el punto respectivo
	 */
	private ArrayList<Hashtable<ClaveDiscreta, Hiperplano>> tablaHiperplanosPorPunto;
	
	

	public TablaHiperplanosMemoria(int cantPasos) {
		this.setCantPasos(cantPasos);
		tablaHiperplanos = new ArrayList<Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>>();
		tablaHiperplanosPorPunto = new ArrayList<Hashtable<ClaveDiscreta, Hiperplano>>();
		for(int ip=0; ip<cantPasos; ip++){
			tablaHiperplanos.add(new Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>());
			tablaHiperplanosPorPunto.add(new Hashtable<ClaveDiscreta, Hiperplano>());
		}	
	}
	
	public TablaHiperplanosMemoria(DatosTablaHiperplanos dth){
		int cantPasos = dth.getCantPasos();
		this.setCantPasos(cantPasos);
		tablaHiperplanos = new ArrayList<Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>>();
		tablaHiperplanosPorPunto = new ArrayList<Hashtable<ClaveDiscreta, Hiperplano>>();
		for(int ip=0; ip<cantPasos; ip++){
			tablaHiperplanos.add(new Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>());
			tablaHiperplanosPorPunto.add(new Hashtable<ClaveDiscreta, Hiperplano>());
		}					
		for(int p=0; p<this.getCantPasos(); p++){
			Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>> dh1paso = dth.getTablaHiperplanos().get(p);
			// Crea la tablaHiperplanos
			Hashtable<ClaveDiscreta, ArrayList<Hiperplano>> h1paso = new Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>();
			Set<ClaveDiscreta> claves = dh1paso.keySet();
			Iterator<ClaveDiscreta> iter = claves.iterator();
			while(iter.hasNext()){
				ClaveDiscreta c = (ClaveDiscreta)iter;
				ArrayList<DatosHiperplano> aldh = dh1paso.get(c);
				ArrayList<Hiperplano> alh = new ArrayList<Hiperplano>();
				for(DatosHiperplano dh: aldh){
					Hiperplano h = new Hiperplano(dh);
					alh.add(h);
				}
				h1paso.put(c, alh);
			}
			this.getTablaHiperplanos().add(h1paso);
			// Crea la tablaHiperplanosPorPunto
			Hashtable<ClaveDiscreta, Hiperplano> h1pasoP = this.getTablaHiperplanosPorPunto().get(p);
			Hashtable<ClaveDiscreta, DatosHiperplano> dh1pasoP = new Hashtable<ClaveDiscreta, DatosHiperplano>();
			Set<ClaveDiscreta> clavesP = dh1pasoP.keySet();
			Iterator<ClaveDiscreta> iterP = claves.iterator();
			while(iterP.hasNext()){
				ClaveDiscreta c = (ClaveDiscreta)iterP;
				DatosHiperplano dh = dh1pasoP.get(c);
				Hiperplano h = new Hiperplano(dh);				
				h1pasoP.put(c, h);
			}
			this.getTablaHiperplanosPorPunto().add(h1pasoP);			
		}				
	}

	
	@Override
	public ArrayList<Hiperplano> devuelveHiperplanos(int paso, ClaveDiscreta clave) {
		Hashtable<ClaveDiscreta, ArrayList<Hiperplano>> tabla1Paso = tablaHiperplanos.get(paso);
		return tabla1Paso.get(clave);
	}


	@Override
	public Hiperplano devuelveElHiperplanoDeUnPunto(int paso, ClaveDiscreta claveTotal) {
		Hashtable<ClaveDiscreta, Hiperplano> tabla1paso = tablaHiperplanosPorPunto.get(paso);
		return tabla1paso.get(claveTotal);
	}
	
	@Override
	public void cargaHiperplano(int paso, ClaveDiscreta claveVEDiscretas, ClaveDiscreta claveTotal, Hiperplano hiper) {
		if(claveVEDiscretas!=null){
			Hashtable<ClaveDiscreta, ArrayList<Hiperplano>> tabla1Paso = tablaHiperplanos.get(paso);
			if(tabla1Paso.get(claveVEDiscretas)==null){
				// Para esa clave no hay cargado ningón hiperplano, crea el ArrayList
				ArrayList<Hiperplano> al = new ArrayList<Hiperplano>();
				al.add(hiper);
				hiper.setNumeroId(0);
				tabla1Paso.put(claveVEDiscretas, al);
			}else{
				ArrayList<Hiperplano> al = tabla1Paso.get(claveVEDiscretas);
				al.add(hiper);
				hiper.setNumeroId(al.size()-1);
			}
		}
		if(claveTotal!=null){
			Hashtable<ClaveDiscreta, Hiperplano> tabla1PasoPorPunto = tablaHiperplanosPorPunto.get(paso);
			tabla1PasoPorPunto.put(claveTotal, hiper);
		}
	}	
	
	
	public DatosTablaHiperplanos creaDataType(){
		DatosTablaHiperplanos dt = new DatosTablaHiperplanos(this.getCantPasos());
		
		for(int p=0; p<this.getCantPasos(); p++){
			Hashtable<ClaveDiscreta, ArrayList<Hiperplano>> h1paso = this.getTablaHiperplanos().get(p);
			Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>> dh1paso = new Hashtable<ClaveDiscreta, ArrayList<DatosHiperplano>>();
			// Convierte la tablaHiperplanos
			Set<ClaveDiscreta> claves = h1paso.keySet();
			Iterator<ClaveDiscreta> iter = claves.iterator();			
			while(iter.hasNext()){
				ClaveDiscreta c = iter.next();
				ArrayList<Hiperplano> alh = h1paso.get(c);
				ArrayList<DatosHiperplano> aldh = new ArrayList<DatosHiperplano>();
				for(Hiperplano h: alh){
					DatosHiperplano dh = h.creaDataType();
					aldh.add(dh);
				}
				dh1paso.put(c, aldh);
			}
			dt.getTablaHiperplanos().set(p, dh1paso);
			// Convierte la tablaHiperplanosPorPunto
			Hashtable<ClaveDiscreta, Hiperplano> h1pasoP = this.getTablaHiperplanosPorPunto().get(p);
			Hashtable<ClaveDiscreta, DatosHiperplano> dh1pasoP = new Hashtable<ClaveDiscreta, DatosHiperplano>();
			Set<ClaveDiscreta> clavesP = h1pasoP.keySet();
			Iterator<ClaveDiscreta> iterP = clavesP.iterator();
			while(iterP.hasNext()){
				ClaveDiscreta c = iterP.next();
				Hiperplano h = h1pasoP.get(c);
				DatosHiperplano dh = h.creaDataType();
				dh1pasoP.put(c, dh);
			}
			dt.getTablaHiperplanosPorPunto().add(dh1pasoP);			
		}
		return dt;
		
	}

	public ArrayList<Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>> getTablaHiperplanos() {
		return tablaHiperplanos;
	}

	public void setTablaHiperplanos(ArrayList<Hashtable<ClaveDiscreta, ArrayList<Hiperplano>>> tablaHiperplanos) {
		this.tablaHiperplanos = tablaHiperplanos;
	}

	public ArrayList<Hashtable<ClaveDiscreta, Hiperplano>> getTablaHiperplanosPorPunto() {
		return tablaHiperplanosPorPunto;
	}

	public void setTablaHiperplanosPorPunto(ArrayList<Hashtable<ClaveDiscreta, Hiperplano>> tablaHiperplanosPorPunto) {
		this.tablaHiperplanosPorPunto = tablaHiperplanosPorPunto;
	}


	
}