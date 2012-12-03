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

import org.ueu.uninet.it.data.FlowTextHelper;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ueu.uninet.it.data.IragarkiEskuratzailea;
import org.ueu.uninet.it.data.Iragarkia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tekle.oss.android.connectivity.NetworkConnectivity;
import com.tekle.oss.android.connectivity.NetworkMonitorListener;

/**
 * Iragarkiaren fitxa osoa aurkezten da eta iragarkia jarri duenarekin
 * harremanetan jartzeko formularioa eta modau.
 * 
 * @author UEU
 * 
 */
public class IragarkiOholaKontaktua extends Activity implements
		NetworkMonitorListener {

	// UIko elementuak
	private EditText izena;
	private EditText eposta;
	private EditText telefonoa;
	private EditText mezua;
	private Button bidali;
	private CheckBox ohar_legala;
	private EditText errobota;
	
	private AlertDialog balidatuDialogoa;

	// Iragarkiaren url unibertsitatea.net webgunean
	private String url = "Hutsik";

	// irudia balego bere propietateak
	private String image_src = "";
	private static Drawable drawable;
	private Boolean irudirik = false;

	// irudia eskuratzeko prozesuaren kudeatzailea
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			//Testua irudia inguratzeko
			FlowTextHelper fth = new FlowTextHelper();
			Display display = getWindowManager().getDefaultDisplay();
			fth.tryFlowText(text, img, laburpen_luzea, display);
			// Behin testua eta irudia kargatuta kendu dialogoa eta erakutsi
			// dena
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();
		}
	};

	private CountDownLatch latch;

	private final Handler handler_iragarkia = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			switch (msg.what) {
			// what horren errore kodea bidaltzen da, errorea egon den kasuetan,
			// edo bestela zerrendaren karga bukatu dela adierazi.
			case 0: // Sare egoera aztertzen duen zerbitzuak errorea bidali badu
				// Oraindik zerrenda kargatzen badago eta progresio mezua
				// martxan bada itxi.
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Iragarkia eskuratzean errore bate gertatu da.",
						Toast.LENGTH_LONG).show();
				// Errorea bistaratu eta leihoa itxi
				IragarkiOholaKontaktua.this.finish();
			case 1:
				// Iragarkia ondo jaso da, ezarri testua bere TextView-ean

				String data = msg.getData().getString("edukia");
				text = data;
				laburpen_luzea.setText(data);
				break;
			}
		}
	};
	// bidalketa prozesua/harirako kudeatzailea
	// Formularioa ondo bidali bada jakinarazi baita errorerik egon bada ere
	private final Handler handler_bidalketa = new Handler() {

		CharSequence text = "";
		int duration = Toast.LENGTH_SHORT;

		@Override
		public void handleMessage(final Message msg) {
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();
			if (msg.what == 1) {
				// Errorea egon da
				// IragarkiOholaKontaktua.this.finish();
				text = "Arazoren bat egon da eta ezin izan da zure erantzuna ondo bidali da";
				// toast.setText(text);
				// toast.show();
				Toast.makeText(getApplicationContext(), text, duration).show();

			} else {
				text = "Zure erantzuna ondo bidali da";
				// toast.setText(text);
				// toast.show();
				Toast.makeText(getApplicationContext(), text, duration).show();
				// mezua ondo bidali da
				IragarkiOholaKontaktua.this.finish();
			}
		}
	};

	// Iragarkiaren layouta osatuko duten elementuak erazagutu
	private static ImageView img;
	final static LayoutParams imageViewLayoutParams = new LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	//private RelativeLayout iragarkia_layout;
	private TextView laburpen_luzea;
	private ProgressDialog progressDialog;
	private static String text = "";

	/**
	 * Layout-a prestatu eta sareko monitorea prestatu
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iragarkia_erantzun);
		// Layout-eko elementuak kargatu
		this.izena = (EditText) findViewById(R.id.formIzena);
		this.eposta = (EditText) findViewById(R.id.formEposta);
		this.telefonoa = (EditText) findViewById(R.id.formTelefonoa);
		this.mezua = (EditText) findViewById(R.id.formMezua);
		this.ohar_legala = (CheckBox) findViewById(R.id.checkBoxlegalAdvice);
		this.errobota = (EditText) findViewById(R.id.formErrobota);

		this.bidali = (Button) findViewById(R.id.buttonBidali);
		this.bidali.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bidaliIragarkiErantzuna();
			}
		});

		// Iragarkiak irudia duen ala ez
		irudirik = getIntent().getBooleanExtra("irudia", false);

		// Eskuratu iragarkiaren layout eta oagaiak: testua eta irudia balego
		this.laburpen_luzea = (TextView) findViewById(R.id.laburpen_luzea);
		// this.img = new ImageView(this);
		IragarkiOholaKontaktua.img = (ImageView) findViewById(R.id.iragarki_irudia);
		//this.iragarkia_layout = (RelativeLayout) findViewById(R.id.iragarkia_layout);

		// Sareko monitora prestatu
		NetworkConnectivity.sharedNetworkConnectivity().configure(this);
		NetworkConnectivity.sharedNetworkConnectivity()
				.addNetworkMonitorListener(this);
		NetworkConnectivity.sharedNetworkConnectivity().startNetworkMonitor();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// hasieratu hariak sinkronizatzeko kontagailua, irudia eta testuaren
		// karga sinkronizatzeko
		latch = new CountDownLatch(1);
		StringBuilder validationText = new StringBuilder();
		// Egiaztatu sareko konekzioa dagoela bestela ohartarazi eta leihoa itxi
		if (!NetworkConnectivity.sharedNetworkConnectivity().isConnected()) {
			validationText.append("Sareko konexioa beharrezkoa da");
			balidatuDialogoa = new AlertDialog.Builder(this)
					.setTitle("Errorea")
					.setMessage(validationText.toString())
					.setPositiveButton(
							"Segi",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int arg1) {
									IragarkiOholaKontaktua.this.finish();
								}
							}).show();
			validationText = null;
		} else { // Sare konexioa badago
			this.progressDialog = ProgressDialog.show(this, " Lanean...",
					" Iragarkia eskuratzen", true, false);
			// ezarri iragarkiaren url-a unibertsitatea.net webgunean
			this.url = getIntent().getStringExtra("url");

			// TODO egiaztatu irudia duela getIrudi-ra deitu aurretik sare
			// atzipen bat
			// aurrezteko

			// irudia-ren url-a ezarri: formatua-> iragarkiaren_url +
			// /image_mini
			this.image_src = this.url + "/image_mini";

			// Testua eta irudia txertatzeko layout-a prestatu

			// Izenburua ezarri (parametro modua dator)
			TextView izenburua = (TextView) findViewById(R.id.izenburuaIragarkia);
			izenburua.setText(getIntent().getStringExtra("izenburua"));

			// Deskribapen laburra ezarri (parametro modua dator)
			TextView laburpena = (TextView) findViewById(R.id.laburpenaIragarkia);
			laburpena.setText(getIntent().getStringExtra("eduki_laburpen"));

			// Deskribapen luzea lortu hari berri batean
			getTestua();

			// Iragarkiak irudirik balu
			if (irudirik) {
				// eskuratu irudia saretik beste hari batean eta layout-ean
				// jarri
				// irudiaren kargak testuaren kargari itxaroten dio, hari biak
				// sinkronizatu gero testu inguratua egin ahal izateko
				irudiaDeskargatu task = new irudiaDeskargatu();
				task.execute(new String[] { this.image_src });
			}
			
			//Fokoa deskribaoen luzean jarri
			laburpen_luzea.setFocusable(true);
			laburpen_luzea.requestFocus();

		}
	}

	// NetworkMonitorListener interfazea inplementatu
	@Override
	public void connectionCheckInProgress() {
		// networkTextView.setText("Checking connection");
	}

	@Override
	public void connectionEstablished() {
		// networkTextView.setText("Connected");
	}

	@Override
	public void connectionLost() {
		// networkTextView.setText("No connection");

		Toast.makeText(getApplicationContext(),
				"Sareko konexioa beharrezkoa da.", Toast.LENGTH_LONG).show();
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		if(balidatuDialogoa != null && balidatuDialogoa.isShowing()){
			balidatuDialogoa.dismiss();
		}
		this.finish();
	}

	/**
	 * Erantzun-formularioa bidali iragarkiaren egileari
	 */
	private void bidaliIragarkiErantzuna() {
		// Balidatu formularioa
		if (!balidatu()) {
			return;
		}
		//Formularioa saretik bidali beste hari bat erabilita
		postData();
	}

	/**
	 * Erantzun-formularioa balidatu. Arazorik balego mezua erakutsi.
	 * 
	 * @return
	 */
	private boolean balidatu() {
		boolean valid = true;
		StringBuilder validationText = new StringBuilder();
		if ((this.izena.getText() == null)
				|| this.izena.getText().toString().equals("")) {
			validationText.append("Izena derrigorrezkoa da");
			valid = false;
		}
		if ((this.eposta.getText() == null)
				|| this.eposta.getText().toString().equals("")) {
			validationText.append("\nE-posta derrigorrezkoa da");
			valid = false;
		}
		if ((this.mezua.getText() == null)
				|| this.mezua.getText().toString().equals("")) {
			validationText.append("\nMezua derrigorrezkoa da");
			valid = false;
		}
		if (!this.ohar_legala.isChecked()) {
			validationText.append("\nOhar legala onartzea derrigorrezkoa da");
			valid = false;
		}
		if ((this.errobota.getText() == null)
				|| !this.errobota.getText().toString().equals("12")) {
			validationText.append("\nErrobota ez zarela egiaztatu");
			valid = false;
		}
		if (!valid) {
			balidatuDialogoa = new AlertDialog.Builder(this)
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
		return valid;
	}
	
	/**
	 * Deskribapen luzea lortzen da unibertsitatea.net webgunetik
	 * Horretarako hari berri bat erabiltzen da
	 * Prozesua bukatzen denean irudi kargatzaileari ematen zaio abisua layouta prestatzeko
	 */
	private void getTestua() {
		// TODO iragarkia saretik lortzean arazorik balego handler-era bidali
		// mezua erabiltzaileari oharra erakusteko
		// TODO bigeiratu iragarki zerrendan zelan egin den
		try {
			final IragarkiEskuratzailea ie = new IragarkiEskuratzailea();
			new Thread() {
				@Override
				public void run() {
					Iragarkia iragarkia = ie.getIragarkia(url);

					Message msg = new Message();
					Bundle bundle = new Bundle();

					bundle.putString("edukia", iragarkia.edukia);
					msg.setData(bundle);

					msg.what = 1;
					msg.setData(bundle);
					handler_iragarkia.sendMessage(msg);
					// Abisua eman irudi kargatzaile hariari testua lortu dela
					latch.countDown();
					// Bide batez progress dialogoa gelditu soilik irudirik ez
					// dagoenean
					// bestela irudia eta layout-a prestatu arte itxoingo da
					if (!irudirik && progressDialog != null &&  progressDialog.isShowing())
						progressDialog.dismiss();
				}
			}.start();
			// return iragarkia.edukia;
		} catch (IllegalThreadStateException e) {
			handler_iragarkia.sendEmptyMessage(0);
		} catch (Exception e) {
			// return "";
			handler_iragarkia.sendEmptyMessage(0);
		}

	}

	/**
	 * 
	 * @author UEU
	 * 
	 * Iragarkiko irudia kargatu eta ikusgai jarri
	 *
	 */
	static public class IrudiRunnable implements Runnable {

		public void run() {

			if (drawable != null) {
				// irudia
				img.setImageDrawable(drawable);
				img.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 
	 * @author UEU
	 *
	 * Irudia unibertsitatea.net webgunetik eskuratu beste hari bat erabiliz
	 * Emaitza eman aurretik testua kargatzeko hariarekin sinkronizatu
	 * Hari biak behin bukatutua erakutsi daiteke testua irudia inguratuta 
	 * android brestioak horrela ahalbidetzen badu
	 *
	 */
	private class irudiaDeskargatu extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... urls) {
			Drawable response = null;
			if (urls.length == 1) {
				try {
					// getTestua hariari itxaron
					latch.await();
				} catch (InterruptedException e) {
					handler_iragarkia.sendEmptyMessage(0);
				}
				response = this.getIrudia(urls[0]);
			}
			return response;
		}

		//Irudia Drawable batean prestatu
		private Drawable getIrudia(String src) {
			try {
				InputStream is = (InputStream) new URL(src).getContent();
				Drawable d = Drawable.createFromStream(is, "src name");
				return d;
			} catch (Exception e) {
				return null;
			}
		}

		//Behin bukatuta kerakutsi irudia
		@Override
		protected void onPostExecute(Drawable result) {
			drawable = result;
			handler.post(new IrudiRunnable());
			handler.sendEmptyMessage(0);
		}
	}

	private void postData() {
		// Sortu HttpClient berria eta goiburuak bidali
		final HttpClient httpclient = new DefaultHttpClient();
		// prestatu bidalketarako URLa
		final String url_bidalketa = this.url + Konstanteak.URL_IRAGARKIA_ERANTZUN;
		// Lokalean frogak egiteko
		//final String url_bidalketa = "http://10.0.2.2/uninet_zini/it_iragarki_ohola.php";
		this.progressDialog = ProgressDialog.show(this, " Lanean...",
				" Mezua bidaltzen", true, false);
		new Thread() {
			@Override
			public void run() {
				// hasi
				int what = 0;
				try {
					HttpPost httppost = new HttpPost(url_bidalketa);

					// Formularioko eremuak POST moduan kargatu
					List<NameValuePair> nameValuePairs;
					if (telefonoa.getText() != null) { // Telefonoa ez da derrigorrezkoa
						nameValuePairs = new ArrayList<NameValuePair>(6);
						nameValuePairs.add(new BasicNameValuePair("name", izena
								.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("eposta",
								eposta.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("telefonoa",
								telefonoa.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("text", mezua
								.getText().toString() + "\n" + url));
						nameValuePairs.add(new BasicNameValuePair("galdera",
								"12"));
						nameValuePairs.add(new BasicNameValuePair(
								"legalAdvice", "accept"));
					} else {
						nameValuePairs = new ArrayList<NameValuePair>(5);
						nameValuePairs.add(new BasicNameValuePair("name", izena
								.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("eposta",
								eposta.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("text", mezua
								.getText().toString() + "\n" + url));
						nameValuePairs.add(new BasicNameValuePair("galdera",
								"12"));
						nameValuePairs.add(new BasicNameValuePair(
								"legalAdvice", "accept"));
					}

					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					// Exekutatu HTTP Post eskaera
					HttpResponse response = httpclient.execute(httppost);

					//Dena ondo joanez gero
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

						// Handler-era mezua bidali bertan akatsaren berri
						// emateko
						what = 0;
					} else {
						// Errorea egon da
						what = 1;
					}
				} catch (IllegalArgumentException e) {
					// Handler-i mezua bidali
					// Errorea egon da
					what = 1;
				} catch (ClientProtocolException e) {
					what = 1;
				} catch (IOException e) {
					what = 1;
				} finally {
					//Edozelan konexioa itxi eta bukaera adierazi handler-i, errorea edo errorerik gabe
					httpclient.getConnectionManager().shutdown();
					handler_bidalketa.sendEmptyMessage(what); //what-en adierazten da errorea egon den edo dena ondo joan den
				}
			}
		}.start();
	}
}
