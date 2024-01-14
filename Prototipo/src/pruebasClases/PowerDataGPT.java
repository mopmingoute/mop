/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PowerDataGPT is part of MOP.
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

package pruebasClases;


	
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public	class PowerDataGPT {
	
    private int year;
    private int month;
    private int day;
    private int hour;
    private double power;

    public PowerDataGPT(int year, int month, int day, int hour, double power) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.power = power;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public double getPower() {
        return power;
    }

    @Override
    public String toString() {
        return String.format("%d/%d/%d %d:00:00, %.2f MW", month, day, year, hour, power);
    }



    public static void main(String[] args) {
        String fileName = "D:/power_data.txt";
        ArrayList<PowerDataGPT> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\s+");
                int year = Integer.parseInt(fields[0]);
                int month = Integer.parseInt(fields[1]);
                int day = Integer.parseInt(fields[2]);
                int hour = Integer.parseInt(fields[3]);
                double power = Double.parseDouble(fields[4]);

                PowerDataGPT data = new PowerDataGPT(year, month, day, hour, power);
                dataList.add(data);
                System.out.println("Para acá");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (PowerDataGPT data : dataList) {
            System.out.println(data.toString());
        }
    }


}
