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
import java.util.List;


import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.ueu.uninet.it.Konstanteak;

/**
 * Iragarkiak unibertsitatea.net-etik erauzteko klase laguntzailea.
 * Iragarkiak orrietan banatuta daude. Orriak banan banan errebisatu behar dira
 * iragarki guztiak eskuratzeko 
 * 
 * @author UEU
 * 
 */

public class htmlJsoupHelper {
	private Document doc;
	public ArrayList<Iragarkia> iragarkiak = new ArrayList<Iragarkia>();
	private final String url;

	/**
	 * Klase eraikitzailea. Iragarkien hasierako orria lortzen du, eta html dokumentua eskuratu
	 * @param url
	 * @param context
	 * @throws IOException
	 */
	public htmlJsoupHelper(String url) throws IOException {
		
		this.url = url;
		this.doc = Jsoup.connect(this.url).timeout(10 * 1000).get();
	}

	/**
	 * Orri kopuruaren arabera iragarki guztiak eskuratzen dira. Iragarki zerrenda bueltatzen da.
	 * Erroreak UIra bidaltzen dira erabiltzaileari dagokion mezua jakinarazteko 
	 * @return
	 * @throws IOException
	 */
	public ArrayList<Iragarkia> getIragarkiZerrenda() throws IOException {
		String url = "";
		int b_start = 0;

		int orriak = iragarkiOrriKopurua();

		int uneko_orria = 0;
		do {
			if (b_start != 0) {

				if (this.url.indexOf("?") >= 0) {
					url = this.url + "&b_start=" + b_start;
				} else {
					url = this.url + "?b_start=" + b_start;
				}
				try {
					this.doc = Jsoup.connect(url).timeout(10 * 1000).get();
				} catch (Exception e) {
					return this.iragarkiak;
				}
			}
			this.iragarkiak.addAll(this.iragarkiakEskuratu());
			uneko_orria++;
			b_start = uneko_orria * Konstanteak.iragarkiOrriko;
		}

		while (uneko_orria < orriak);

		return this.iragarkiak;
	}

	/**
	 * Orri jakin bateko iragarki guztiak eskuratu. Ez badago ezer zerrenda hutsa bueltatzen da.
	 * @return
	 */
	private List<Iragarkia> iragarkiakEskuratu() {
		List<Iragarkia> iragarkiak = new ArrayList<Iragarkia>();
		// Iragarkien div elementua eskuratu
		if (this.doc != null) { // konexioan errorerik egon ez bada
			Element iragarkiak_div = this.doc.getElementById("iragarkiak");

			if (iragarkiak_div != null) {
				Elements iragarkiaElements = iragarkiak_div
						.select("div.iragarkia");
				if (!iragarkiaElements.isEmpty()) { // iragarkiak atala topatu
													// bada
					for (Element iragarkiaElement : iragarkiaElements) {

						Iragarkia irag = gordeIragarkia(iragarkiaElement);
						iragarkiak.add(irag);
					}
				}
			}
		}
		return iragarkiak;
	}

	/**
	 * Iragarki zerrenda zenbat orritan banatuta dagoen bueltatzen digu
	 * @return
	 */
	private int iragarkiOrriKopurua() {
		int orrikop = 0;
		if (this.doc != null) { // HTML dokumentua lortu bada
			Elements nabigazio_barra = this.doc.getElementsByAttributeValue(
					"class", "listingBar");
			if (!nabigazio_barra.isEmpty()) { // nabigazio barra badago <div>
				// nabigazio estekak eskuratu
				Elements orriak = nabigazio_barra.select("a[href]");
				if (orriak.size() > 0) { // orri bakarra bada ez da erakusten
											// nabigazio
											// barrarik
					orrikop = Integer.parseInt(orriak.last().text());
				}
			}
		}
		return orrikop;
	}

	/**
	 * Orri bateko iragarki-zerrendako iragarki bat bat eskuratu eta iragarkia objektua bueltatu
	 * @param irag
	 * @return
	 */
	public Iragarkia gordeIragarkia(Element irag) {
		Iragarkia iragarkia = new Iragarkia();
		// badagoela, hutsik ez dagoela alegia.
		Element egilea_element = irag.getElementsByAttributeValue("class",
				"date").first();
		iragarkia.egilea = (egilea_element != null) ? egilea_element.text()
				.substring(egilea_element.text().indexOf("-")) : "";
		iragarkia.data = (egilea_element != null) ? egilea_element.text()
				.substring(0, egilea_element.text().indexOf("-")) : "";

		Element esteka = irag.getElementsByTag("a").first();
		iragarkia.edukia = (esteka != null) ? esteka.text() : "";
		iragarkia.url = esteka.attr("abs:href");
		Element atala_element = irag.getElementsByAttributeValue("class",
				"atala").first();
		iragarkia.atala = (atala_element != null) ? atala_element.text() : "";

		if (irag.hasClass("argazkiarekin")) {
			iragarkia.irudia = true;
		}

		return iragarkia;
	}

	public static void printIragarkia(Iragarkia irag) {
		System.out.println(irag.toString());
	}

	/**
	 * Iragarki jakin bateko eremuak eskuratu. Kasu honetan deskribapen luzea
	 * Gainontzekoa aurretik eskuratuko denez, orriz orri, parametro bezala pasatuko da dagokion tokian.
	 * @return
	 */
	public Iragarkia getIragarkia() {
		Iragarkia iragarkia = new Iragarkia();
		if (this.doc != null) {
			Elements deskribapenak = this.doc.getElementsByAttributeValue(
					"class", "documentDescription");
			if (!deskribapenak.isEmpty()) { // batzutan ez da deskribapenik
											// ematen
				String mezua = deskribapenak.first().text();
				iragarkia.edukia = mezua;
			}
		}
		return iragarkia;
	}

}
