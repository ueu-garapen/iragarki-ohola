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

import android.app.Activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.Menu;

import com.tekle.oss.android.connectivity.NetworkConnectivity;
import com.tekle.oss.android.connectivity.NetworkMonitorListener;

/*
 * Iragarki-ohola aplikazioaren lehendabiziko pantaila da hau.
 * Hemendik iragarkiak bilatu daitezke modu librean bilaketa kutxatilaren bidez edo bestela
 * iragarkiak sailkapenaren arabera ikus daitezke.
 * Atal honetan iragarki berriak ere bidal daitezke
 */

public class IragarkiakBilatu extends Activity implements
		NetworkMonitorListener {

	ImageButton etxeak;
	ImageButton lana;
	ImageButton praktikak;
	ImageButton kotxeak;
	ImageButton salerosi;
	ImageButton bidaiak;

	Button bilatu, iragarki_berria;
	EditText iragazkia;
	AlertDialog balidazioDialogoa;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Gordetako iragarkiak ezabatu, sareko iragarki berriekin eguneratzeko.
		IragarkiOholaApplication application = (IragarkiOholaApplication) getApplication();
		application.hasieratuAplikazioa();
		setContentView(R.layout.iragarkia_bilatu);
		//kontrolatu sare konekxioa dagoela, beharrezkoa baita iragarkiekin lan egiteko: bidali, jaso...
		NetworkConnectivity.sharedNetworkConnectivity().configure(this);
		NetworkConnectivity.sharedNetworkConnectivity()
				.addNetworkMonitorListener(this);

		// Kategorien botoiak lortu layout-etik eta aginduarekin lotu
		// Etxeak kategoria
		this.etxeak = (ImageButton) findViewById(R.id.imageButtonEtxeak);
		this.etxeak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Kategoria honetako iragarkiak lortu 
				eskuratuIragarkiak(Konstanteak.ETXEAK);
			}
		});

		// Lana kategoria
		this.lana = (ImageButton) findViewById(R.id.imageButtonLana);
		this.lana.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.LANA);
			}
		});
		
		//Praktikak kategoria
		this.praktikak = (ImageButton) findViewById(R.id.imageButtonPraktikak);
		this.praktikak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.PRAKTIKAK);
			}
		});
		
		//Kotxeak kategoria
		this.kotxeak = (ImageButton) findViewById(R.id.imageButtonKotxeak);
		this.kotxeak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.KOTXEA);
			}
		});
		
		//Salerosi kategoria
		this.salerosi = (ImageButton) findViewById(R.id.imageButtonSalerosi);
		this.salerosi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.SALEROSI);
			}
		});
		
		//Bidaiak kategoria
		this.bidaiak = (ImageButton) findViewById(R.id.imageButtonBidaiak);
		this.bidaiak.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.BIDAIAK);
			}
		});

		// Bilaketa atala
		this.bilatu = (Button) findViewById(R.id.buttonBilatu);
		this.bilatu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				eskuratuIragarkiak(Konstanteak.BILAKETA);
			}
		});
		this.iragazkia = (EditText) findViewById(R.id.iragazkia);

		// Iragarki berria bidaltzeko atala
		this.iragarki_berria = (Button) findViewById(R.id.iragarkiBerria);
		this.iragarki_berria.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						IragarkiaBidali.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		// Sarearen egoera kontrolatzeko monitorea entzuten hasi
		NetworkConnectivity.sharedNetworkConnectivity().startNetworkMonitor();
		
	}
	
	// Menua XML fitxategia hasieratu (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menua, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        	case R.id.menu_laguntza:
				Intent intent_laguntza = new Intent(getApplicationContext(),
						IragarkiaLaguntza.class);
				startActivity(intent_laguntza);        		
        		return true;
 
        	case R.id.menu_honiburuz:
				Intent intent_honiburuz = new Intent(getApplicationContext(),
						IragarkiaHoniburuz.class);
				startActivity(intent_honiburuz);        		
        		return true;
  
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }        

	@Override
	public void onPause() {
		super.onPause();
		//Aurreko planoan exekutatzen ari denean aplikazioa gelditu sareko egoeraren monitorea
		NetworkConnectivity.sharedNetworkConnectivity().stopNetworkMonitor();
	}

	//Inplementatu NetworkMonitorListener interfazea
	@Override
	public void connectionCheckInProgress() {
		// networkTextView.setText("Checking connection");
	}

	//Sare-monitore 
	@Override
	public void connectionEstablished() {
		// networkTextView.setText("Connected");
	}

	@Override
	public void connectionLost() {
		//Sareko konexioa galduz gero errore mezua erakutsi eta aplikazioa itxi
		StringBuilder validationText = new StringBuilder();
		
		validationText.append("Sareko konexioa beharrezkoa da. Aplikazioa itxiko da.");
		new AlertDialog.Builder(this)
				.setTitle("Errorea")
				.setMessage(validationText.toString())
				.setPositiveButton("Segi",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								if (balidazioDialogoa != null && balidazioDialogoa.isShowing()){
									balidazioDialogoa.dismiss();
								}
								IragarkiakBilatu.this.finish();
							}
						}).show();
		validationText = null;
	}

	/*
	 * Iragarkiak eskuratzeko Intent-a prestatzen da. Kategoria parametro bezala
	 * pasatzen da
	 */
	private void eskuratuIragarkiak(int mota) {
		StringBuilder validationText = new StringBuilder();

		if(!NetworkConnectivity.sharedNetworkConnectivity().isConnected()){
			validationText.append("Sareko konexioa beharrezkoa da.");
			new AlertDialog.Builder(this)
					.setTitle("Errorea")
					.setMessage(validationText.toString())
					.setPositiveButton(
							"Segi",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int arg1) {
								}
							}).show();
			validationText = null;
		} else {
			// Hurrengo aktibitateari deitu, iragarki-zerrenda
			Intent intent = null;
			switch (mota) {
			case (Konstanteak.ETXEAK):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_ETXEAK);
				break;
			case (Konstanteak.BIDAIAK):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_BIDAIAK);
				break;
			case (Konstanteak.KOTXEA):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_KOTXEA);
				break;
			case (Konstanteak.LANA):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_LANA);
				break;
			case (Konstanteak.PRAKTIKAK):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_PRAKTIKAK);
				break;
			case (Konstanteak.SALEROSI):
				intent = new Intent(getApplicationContext(),
						IragarkiZerrenda.class);
				intent.putExtra(Konstanteak.URL_EXTRA,
						Konstanteak.URL_SALEROSI);
				break;
			// bilaketa egin nahi da
			case (Konstanteak.BILAKETA):
				if (balidatu()) {
					intent = new Intent(getApplicationContext(),
							IragarkiZerrenda.class);
					intent.putExtra(Konstanteak.URL_EXTRA,
							Konstanteak.URL_BILAKETA
									+ iragazkia.getText().toString());
				}
				break;
			default:
				break;
			}
			if (intent != null) { // bilaketaren kasua bilatzekoa ez bada zehaztu
				intent.putExtra("kategoria", mota);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	/*
	 * Bilaketaren kasuan egiaztatzen da bilaketa kutxatila hutsik ez egotea
	 */

	Boolean balidatu() {
		Boolean balekoa = true;

		StringBuilder validationText = new StringBuilder();

		if ((this.iragazkia.getText() == null)
				|| this.iragazkia.getText().toString().equals("")) {
			validationText.append("Bilaketarako irizpidea derrigorrezkoa da");
			balekoa = false;
		}
		if (!balekoa) {
			//Bilaketa irizpidea hutsik badago mezua erakutsi
			balidazioDialogoa = new AlertDialog.Builder(this)
					.setTitle("Errorea")
					.setMessage(validationText.toString())
					.setPositiveButton(
							"Segi",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int arg1) {
								}
							}).show();
			validationText = null;
		}
		return balekoa;
	}
}