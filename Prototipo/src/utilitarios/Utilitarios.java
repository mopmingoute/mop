/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Utilitarios is part of MOP.
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utilitarios {

	public static GregorianCalendar stringToGregorianCalendar(String fecha, String formato) {
		DateFormat df = new SimpleDateFormat(formato); // Se parsea un string con este formato a GregorianCalendar
		Date date = null;
		try {
			date = df.parse(fecha);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		GregorianCalendar greg = new GregorianCalendar();
		greg.setTime(date);
//		System.out.println(greg.get(Calendar.YEAR));
//		System.out.println(greg.get(Calendar.MONTH));
//		System.out.println(greg.get(Calendar.DAY_OF_MONTH));
//		System.out.println(greg.get(Calendar.HOUR_OF_DAY));
//		System.out.println(greg.get(Calendar.MINUTE));
//		System.out.println(greg.get(Calendar.SECOND));
		return greg; 
	}
	
	
	
}
 