/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TransBoxCox is part of MOP.
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

public class TransBoxCox extends TransformacionVA {
	
	private double lambda;
	private double media;
	private double desvio;
	private double traslacion;
	
	/**
	 * La transformación tiene cuatro parámetros, lambda, media, desvío y traslación
	 * se obtiene una variable y normal N(0,1) aplicando al valor original x
	 * la siguiente transformacion
	 * 
	 * x' = x + traslacion
	 * si lambda = 0    z = log(x')
	 * si lambda <> 0   z = [ x'**(lambda-1) / lamda] 
	 * con lo que z es normal pero no est�ndar
	 * 
	 * y = (z - media)/desvio
	 * 
	 * @param lambda
	 */
	
	


	public TransBoxCox(double lambda, double media, double desvio, double traslacion) {
		super();
		this.lambda = lambda;
		this.media = media;
		this.desvio = desvio;
		this.traslacion = traslacion;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public double getMedia() {
		return media;
	}

	public void setMedia(double media) {
		this.media = media;
	}

	public double getDesvio() {
		return desvio;
	}

	public void setDesvio(double desvio) {
		this.desvio = desvio;
	}
	

	public double getTraslacion() {
		return traslacion;
	}

	public void setTraslacion(double traslacion) {
		this.traslacion = traslacion;
	}

	@Override
	public double transformar(double x) {
		if(x + traslacion <=0){
			System.out.println("Error: Se pidió transformación Box-Cox de " + x);
		}
		double z;
		if(lambda==0){
			z = Math.log(x+traslacion);
		}else{
			z = (Math.pow(x+traslacion, lambda)-1)/lambda;
		}
		return (z-media)/desvio;
	}

	@Override
	public double inversa(double z) {
		double x = z*desvio + media;
		if(lambda>0){
			return Math.exp(x)-traslacion;
		}else{
			return (Math.pow(x, lambda)-1)/lambda-traslacion;
		}
	}

	@Override
	public ArrayList<Double> dameParametros(){
		ArrayList<Double> result = new ArrayList<Double>();
		result.add(this.getLambda());
		result.add(this.getMedia());
		result.add(this.getDesvio());
		result.add(this.getTraslacion());
		return result;
	}

}
