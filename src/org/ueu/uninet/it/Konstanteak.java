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


/**
 * Konfigurazio parametroak
 * 
 * @author UEU
 *
 */
public class Konstanteak {

	public static final String LOGTAG = "Uninet: iragarki bilaketak";
	public static final int iragarkiOrriko = 9;
	public static final String URL_EXTRA = "org.ueu.uninet.it.UrlExtra";
	
	public static final String URL_ETXEAK = "http://www.unibertsitatea.net/ikasleen-txokoa/etxeak";
	//kopia lokala, sarekoa ez dabilenerako
	//public static final String URL_ETXEAK ="http://10.0.2.2/uninet_zini/uninet_iragarki_ohola/index.php";
	public static final String URL_LANA = "http://www.unibertsitatea.net/ikasleen-txokoa/lana";
	public static final String URL_SALEROSI = "http://www.unibertsitatea.net/ikasleen-txokoa/salerosi";
	public static final String URL_PRAKTIKAK = "http://www.unibertsitatea.net/ikasleen-txokoa/praktikak";
	public static final String URL_KOTXEA = "http://www.unibertsitatea.net/ikasleen-txokoa/kotxea";
	public static final String URL_BIDAIAK = "http://www.unibertsitatea.net/ikasleen-txokoa/bidaiak";
	public static final String URL_BILAKETA = "http://www.unibertsitatea.net/ikasleen-txokoa/bilaketa?SearchableText=";
	public static final String URL_IRAGARKIA_ERANTZUN = "/apuntea_comment_send_form";
	public static final String URL_IRAGARKIA_BIDALI = "http://www.unibertsitatea.net/ikasleen-txokoa/sailkatua_send_form";
	//public static final String URL_IRAGARKIA_BIDALI = "http://10.0.2.2/uninet_zini/irudia.php";
	
	public static final int ETXEAK = 0;
	public static final int LANA = 1;
	public static final int SALEROSI = 2;
	public static final int PRAKTIKAK = 3;
	public static final int KOTXEA = 4;
	public static final int BIDAIAK = 5;
	public static final int BILAKETA = 6;
	
	public static final Boolean DEBUG = false;
	public static final String[] KATEGORIAK = {"Etxeak","Lana","Salerosi","Praktikak","Kotxea","Bidaiak","Bilaketa"};
}