/**
 * Copyright (C) 2012  Udako Euskal Unibertsitatea informatikaria@ueu.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.ueu.uninet.it;

import java.util.ArrayList;

import org.ueu.uninet.it.data.Iragarkia;

import android.app.Application;

public class IragarkiOholaApplication extends Application {

	private ArrayList<Iragarkia> iragarkiakLana;
	private ArrayList<Iragarkia> iragarkiakEtxeak;
	private ArrayList<Iragarkia> iragarkiakBidaiak;
	private ArrayList<Iragarkia> iragarkiakSalerosi;
	private ArrayList<Iragarkia> iragarkiakPraktikak;
	private ArrayList<Iragarkia> iragarkiakKotxea;

	public IragarkiOholaApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public ArrayList<Iragarkia> getIragarkiakLana() {
		return this.iragarkiakLana;
	}

	public ArrayList<Iragarkia> getIragarkiakEtxeak() {
		return this.iragarkiakEtxeak;
	}

	public ArrayList<Iragarkia> getIragarkiakBidaiak() {
		return this.iragarkiakBidaiak;
	}

	public ArrayList<Iragarkia> getIragarkiakSalerosi() {
		return this.iragarkiakSalerosi;
	}

	public ArrayList<Iragarkia> getIragarkiakPraktikak() {
		return this.iragarkiakPraktikak;
	}

	public ArrayList<Iragarkia> getIragarkiakKotxea() {
		return this.iragarkiakKotxea;
	}

	public void setIragarkiakEtxeak(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakEtxeak = unekoIragarkia;
	}

	public void setIragarkiakLana(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakLana = unekoIragarkia;
	}

	public void setIragarkiakBidaiak(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakBidaiak = unekoIragarkia;
	}

	public void setIragarkiakSalerosi(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakSalerosi = unekoIragarkia;
	}

	public void setIragarkiakPraktikak(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakPraktikak = unekoIragarkia;
	}

	public void setIragarkiakKotxea(ArrayList<Iragarkia> unekoIragarkia) {
		this.iragarkiakKotxea = unekoIragarkia;
	}
	
	public void hasieratuAplikazioa(){
		this.iragarkiakLana = null;
		this.iragarkiakEtxeak = null;
		this.iragarkiakBidaiak = null;
		this.iragarkiakSalerosi = null;
		this.iragarkiakPraktikak = null;
		this.iragarkiakKotxea = null;
	}

}
