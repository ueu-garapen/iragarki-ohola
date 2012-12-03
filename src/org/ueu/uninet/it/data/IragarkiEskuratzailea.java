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

package org.ueu.uninet.it.data;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Iragarkiak eskuratzeko motodoarekiko independentzia lortzeko klasea da. Abstrakzio maila bat gehitzen du.
 * 
 * @author ueu
 */
public class IragarkiEskuratzailea {

    /**
     * Unibertsitatea.net webgunetik iragarkiak erauzi.
     * Erroreak UIra pasatzen dira bertan dagokion trataera emateko
     * 
     * @return
     */
    
    /**
     * Kategoriari dagokion url-a pasatuta kategoria horretako iragrakiak lortu.
     * Konexio erroreak eta abar UI-ra bidali
     * @param url
     * @return
     * @throws IOException
     */
    
    public ArrayList<Iragarkia> getIragarkiak(String url) throws IOException{
        ArrayList<Iragarkia> iragarkiak = null;

        	//Jsoup klasea erabiltzen da webguneko informazioa erauzteko eta iragarkia osatzeko
        	htmlJsoupHelper hh = new htmlJsoupHelper(url);
        	iragarkiak = hh.getIragarkiZerrenda();
            return iragarkiak;        

    }
    
    /**
     * URLa emanda iragarki baten informazio oso aeskuraten da unibertsitatea.net webgunetik
     * JSOUP klasea erabilita html elementuak erauzteko.
     * @param url
     * @return
     */
    public Iragarkia getIragarkia(String url) {
        Iragarkia results = null;

        try {
        	htmlJsoupHelper hh = new htmlJsoupHelper(url);
            results = hh.getIragarkia();
            return results;
        } catch (Exception e) {
            return results;
        }

    }
}