/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ComparadorObjetos is part of MOP.
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

package utilitarios;

public class ComparadorObjetos implements Comparable{
	
	private double valorParaComparar;
	private Object objeto;
	
	
	public ComparadorObjetos(double valorParaComparar, Object objeto) {
		super();
		this.valorParaComparar = valorParaComparar;
		this.objeto = objeto;
	}



	public int compareTo(Object obj){
		ComparadorObjetos co = (ComparadorObjetos)obj;
		if(this.valorParaComparar < co.getValorParaComparar()) return -1;
		if(this.valorParaComparar > co.getValorParaComparar()) return 1;
		return 0;
		
	}
	
	

	public double getValorParaComparar() {
		return valorParaComparar;
	}

	public void setValorParaComparar(double valorParaComparar) {
		this.valorParaComparar = valorParaComparar;
	}

	public Object getObjeto() {
		return objeto;
	}

	public void setObjeto(Object objeto) {
		this.objeto = objeto;
	}
	
	

}
