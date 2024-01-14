/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ClaveDiscreta is part of MOP.
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

import java.io.Serializable;
import java.util.Arrays;

public class ClaveDiscreta implements Serializable, Comparable{
	
	
	int[] enterosIndices; // lista de valores enteros
	
	
	
	public ClaveDiscreta(int[] enteros){
		this.enterosIndices = enteros;
	}
	
	public ClaveDiscreta(){};
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(enterosIndices);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClaveDiscreta other = (ClaveDiscreta) obj;
		if (!Arrays.equals(enterosIndices, other.enterosIndices))
			return false;
		return true;
	}
	
	public int[] getEnterosIndices() {
		return enterosIndices;
	}
	public void setEnterosIndices(int[] enterosIndices) {
		this.enterosIndices = enterosIndices;
	}

	@Override
	public int compareTo(Object clave) {
		ClaveDiscreta cd = (ClaveDiscreta)clave;
		for(int i=0; i<enterosIndices.length; i++){
			if(cd.getEnterosIndices()[i]<this.getEnterosIndices()[i]) return 1;
			if(cd.getEnterosIndices()[i]>this.getEnterosIndices()[i]) return -1;
		}
		return 0;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<enterosIndices.length; i++){
			sb.append(enterosIndices[i] + "\t");
		}
		return sb.toString();
	}

	public void imprimir() {
		System.out.println(this.toString());
		
	}
	
	
	
	
	

}
