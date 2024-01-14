/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * UtilStrings is part of MOP.
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

import java.util.ArrayList;

/**
 * utilitarios para manejar Strings
 * @author ut469262
 *
 */
public class UtilStrings {
	
	
	/**
	 * Reemplaza las letras del carácter por otras avance lugares adelante en el 
	 * alfabeto (con orden circular y agregando mayusculas). Los otros caracteres del String los deja igual
	 * @param s1
	 * @param avance
	 * @return
	 */
	public static String avanzaAlfabeto(String s1, int avance){
			
		String dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s1.length(); i++) {
		    char oldChar = s1.charAt(i);
		    int oldCharPositionInDictionary = dictionary.indexOf(oldChar);
		    if (oldCharPositionInDictionary >= 0) {
		        int newCharPositionInDictionary = 
		            (oldCharPositionInDictionary + avance) % dictionary.length();
		        sb.append(dictionary.charAt(newCharPositionInDictionary));
		    }else {
		        sb.append(oldChar);
		    }
		}
		String result = sb.toString();
		return result;
	}

	public static boolean esNumeroEntero(String numero) {

		try{
			Integer.parseInt(numero);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static boolean esNumeroDouble(String numero) {

		try{
			Double.parseDouble(numero);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static boolean esParDeNumeroEntero(String numero) {

		try{
			String[] par = numero.split(";");

			for (int j = 0; j < par.length; j++) {
				Double.parseDouble(par[j]);
			}
			boolean ret = (par.length == 2) ? true:false;
			return ret;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public static String arrayStringAtexto(ArrayList<String> arrayStrings, String separador){
		String texto = "";
		int cantPalabras=arrayStrings.size();
		for (int i = 0; i< cantPalabras -1; i ++)
		{
			texto += arrayStrings.get(i) + separador;
		}
		texto += arrayStrings.get(cantPalabras-1);
		return texto;
	}

	public static void main(String[] args){
		String s1 = "abcde-xyz";
		String s2 = avanzaAlfabeto(s1, 2);
		System.out.println(s2);
	}

	public static String separarCamelCase (String texto)
	{
		String ret = String.valueOf(texto.charAt(0)).toUpperCase();

		for (int i = 1; i < texto.length(); i++) {
			String upperChar = String.valueOf(texto.charAt(i)).toUpperCase();
			if (upperChar.equals(String.valueOf(texto.charAt(i)))) {
				//Separo
				ret += " "+ upperChar;
			}else {
				//TodoJunto
				ret +=texto.charAt(i);
			}
		}

		return ret;
	}

	public static int contarOcurrencias(ArrayList<String> lista, String palabra) {
		int contador = 0;
		for (String str : lista) {
			if (str.equals(palabra)) {
				contador++;
			}
		}
		return contador;
	}
}
