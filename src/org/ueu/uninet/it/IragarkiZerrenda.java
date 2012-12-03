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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import android.widget.TextView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.ueu.uninet.it.data.IragarkiEskuratzailea;
//import org.ueu.uninet.it.Konstanteak;
import org.ueu.uninet.it.data.Iragarkia;

import com.tekle.oss.android.connectivity.NetworkConnectivity;
import com.tekle.oss.android.connectivity.NetworkMonitorListener;

/*
 * Iragarki zerrenda bat aurkezten digu eta zerrenda hori iragazteko
 * testu-kutxa.
 * Testu-kutxaren bidez deskribapeneko testuan bilatzen da eta zerrenda edukia egokitzen da bilaketara
 */

public class IragarkiZerrenda extends ListActivity implements
		NetworkMonitorListener {
//TODO ezarri fokoa zerrendan eta ez iragazkia, bestela teklatua agetzen da eta ezin da zerrenda ikusi

	private ProgressDialog progressDialog;
	private ArrayList<Iragarkia> iragarkiak;

	//Kategoria bakoitzeko zeernda bat
	private ArrayList<Iragarkia> iragarkiakLana;
	private ArrayList<Iragarkia> iragarkiakEtxeak;
	private ArrayList<Iragarkia> iragarkiakBidaiak;
	private ArrayList<Iragarkia> iragarkiakSalerosi;
	private ArrayList<Iragarkia> iragarkiakPraktikak;
	private ArrayList<Iragarkia> iragarkiakKotxea;

	private TextView empty;

	//iragazkia
	private EditText filterText = null;
	IragarkiAdapter iragarkiadapter = null;

	//Atzeko planoan dabiltzaten prozesuak sinkronizatzeko kudeatzailea
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {

			switch (msg.what) {
			//what horren errore kodea bidaltzen da, errorea egon den kasuetan, edo bestela zerrendaren karga bukatu dela adierazi.
			case 0: //Sare egoera aztertzen duen zerbitzuak errorea bidali badu
				// Oraindik zerrenda kargatzen badago eta progresio mezua martxan bada itxi.
				
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Sareko konexioa beharrezkoa da.", Toast.LENGTH_LONG).show();
				//Errorea bistaratu eta leihoa itxi
				IragarkiZerrenda.this.finish();
				break;
			case 1:
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						"Errorea sare konexioan: Host ezezaguna.",
						Toast.LENGTH_LONG).show();
				IragarkiZerrenda.this.finish();
				break;
			case 2:
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						"Errorea sare konexioan: Konexioa denboraz kanpo.",
						Toast.LENGTH_LONG).show();
				IragarkiZerrenda.this.finish();
				break;
			case 3:
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						"Errorea sare konexioan: Konexioa-salbuespena.",
						Toast.LENGTH_LONG).show();				
				IragarkiZerrenda.this.finish();
				break;
			case 4:
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						"Errore bat gertatu da iragarkiak eskuratzean.",
						Toast.LENGTH_LONG).show();
				IragarkiZerrenda.this.finish();
				break;
			case 5: //zerrendaren karga bukatu denean errorerik gabe
				// orduan sortu adaptadorea eta ListActivity-rekin lotu
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				if ((iragarkiak == null) || (iragarkiak.size() == 0)) {
					empty.setText(R.string.bilaketa_hutsa);
				} else {
					iragarkiadapter = new IragarkiAdapter(
							IragarkiZerrenda.this, R.layout.zerrenda_item,
							iragarkiak);
					setListAdapter(iragarkiadapter);

				}
				break;
			case 6: //Bestelako errorea	
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						"Errore bat gertatu da iragarkiak eskuratzean.",
						Toast.LENGTH_LONG).show();
				IragarkiZerrenda.this.finish();
				break;
			}

		}
	};
	
	//Iragazkia testu-kutxan idaztea kudeatzeko begiralea
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			//Testua aldatzean zerrenda-adaptadorearen iragazkiari deitu
			if(iragarkiadapter != null){ //Zerrenda hutsik badago ez da adaptadorea sortzen.
				iragarkiadapter.getFilter().filter(s);
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Layouta prestatu
		this.setContentView(R.layout.iragarki_zerrenda);
		//Zerrenda hutsa erakusteko
		this.empty = (TextView) findViewById(R.id.empty);
		//Sareko konexioa beharrezkoa da, zerbitzu honekin uneoro egiaztatzen da sarearen egoera.
		NetworkConnectivity.sharedNetworkConnectivity().configure(this);
		NetworkConnectivity.sharedNetworkConnectivity()
				.addNetworkMonitorListener(this);
		int kategoria = getIntent().getIntExtra("kategoria",
				Konstanteak.BILAKETA);
		String izenburua = (String) this.getTitle() + " -- " + Konstanteak.KATEGORIAK[kategoria];
		this.setTitle(izenburua);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//zerrenda iragazkia kendu
		filterText.removeTextChangedListener(filterTextWatcher);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Sareko monitorea aktibatu
		NetworkConnectivity.sharedNetworkConnectivity().startNetworkMonitor();
		// Zerrendako propietateak ezarri
		final ListView listView = getListView();
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		//Iragazkia prestatu. Iragazki modua erabiltzen den testu-kutxan testua aldatzen denerako Listenerra ezarri
		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);

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
			// Eskuratu iragarkiak aurretik kargatu izan badira
			IragarkiOholaApplication application = (IragarkiOholaApplication) getApplication();
			iragarkiakLana = application.getIragarkiakLana();
			iragarkiakKotxea = application.getIragarkiakKotxea();
			iragarkiakEtxeak = application.getIragarkiakEtxeak();
			iragarkiakSalerosi = application.getIragarkiakSalerosi();
			iragarkiakPraktikak = application.getIragarkiakPraktikak();
			iragarkiakBidaiak = application.getIragarkiakBidaiak();

			//Eskuratu kategoria eta kategoria horren url aurreko leihotik
			String url = getIntent()
					.getStringExtra(Konstanteak.URL_EXTRA);
			int kategoria = getIntent().getIntExtra("kategoria",
					Konstanteak.BILAKETA);
			//Kategoriaren arabera dagokion iragarki-multzoa kargatu
			kargatuIragarkiak(url, kategoria);

			// Fitxa ikustetik, berriro bueltatzean zerrendara, zerrendan ez dago iragazkia aplikatuta
			// Honela berrezartzen da iragazki testua eta horrek berriro deitzen
			// dio textchanged-eri iragazkia aplikatzeko
			if (filterText.getText().length() > 0) {
				filterText.setText(filterText.getText().toString());
			}
		}

	}
	
	@Override
	public void onPause(){
		super.onPause();
		//Atzeko planora pasatzean desaktibatu sareko monitorea 
		NetworkConnectivity.sharedNetworkConnectivity().stopNetworkMonitor();
	}

	// Inplementatu NetworkMonitorListener interfazea
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
		//Ez dago konexioa eta kudeatzaileari pasatzen zaio abisua, egin beharrekoa egiteko
		handler.sendEmptyMessage(0);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 * Zerrendako elementu batean klikatzean iragarki horre fitxara joango gara
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			Intent intent = new Intent(getApplicationContext(),
					IragarkiOholaKontaktua.class);
			//iragazita egon daitekeenez iragarkiak adaptadoretik jasoko ditugu
			Iragarkia iragarkia = (Iragarkia) iragarkiadapter.getItem(position);
			//Parametroak prestatu
			
			//Iragarkiaren URL-a
			intent.putExtra("url", iragarkia.url);
			//Iragarkiaren goiburukoa
			intent.putExtra("izenburua", iragarkia.data + " - "
					+ iragarkia.egilea);
			//Deskibapen laburra
			intent.putExtra("eduki_laburpen", iragarkia.edukia);
			//Irudia duen ala ez
			intent.putExtra("irudia", iragarkia.irudia);
			startActivity(intent);
		} catch (Exception e) {
			// iragarkiak url-rik ez badu edo egilea, data edukia falta baditu
			// Saretik ekartzean erroreren bat egon bada, edo iragarkia gaizki eratuta egon bada jatorrizko webgunean
			handler.sendEmptyMessage(6);
		}
	}

	/**
	 * Kategoriaren arabera saretik (unibertsitatea.net webgunetik) iragarki zerrenda lortu 
	 * @param url
	 * @param kategoria
	 */
	private void kargatuIragarkiak(final String url, int kategoria) {
		// kategoriarik zehaztu ez bada bilaketa dela kontsideratzen da

		//Saretik iragarkiak eskuratzeko klase laguntzailea. Abstrakzioa ahalbidetzen digu.
		//horrela iragarkiak beste edozein modutan lortu ditzakegu etorkizunean. API aldatzen bada adibidez.
		//final IragarkiEskuratzailea ie = new IragarkiEskuratzailea(getApplicationContext(), url);
		final IragarkiEskuratzailea ie = new IragarkiEskuratzailea();
		
		// Kategoriaren arabera iragarki zerrenda lortu eta dagokion propietatean ezarri
		try {
			switch (kategoria) {
			case (Konstanteak.ETXEAK):
				ezarriIragakiZerrenda(iragarkiakEtxeak, url, ie, "Etxeak");
				break;
			case (Konstanteak.BIDAIAK):
				ezarriIragakiZerrenda(iragarkiakBidaiak, url, ie, "Bidaiak");
				break;
			case (Konstanteak.KOTXEA):
				ezarriIragakiZerrenda(iragarkiakKotxea, url, ie, "Kotxea");
				break;
			case (Konstanteak.LANA):
				ezarriIragakiZerrenda(iragarkiakLana, url, ie, "Lana");
				break;
			case (Konstanteak.PRAKTIKAK):
				ezarriIragakiZerrenda(iragarkiakPraktikak, url, ie, "Praktikak");
				break;
			case (Konstanteak.SALEROSI):
				ezarriIragakiZerrenda(iragarkiakSalerosi, url, ie, "Salerosi");
				break;
			// bilaketa egin nahi da
			case (Konstanteak.BILAKETA):
				if (iragarkiak == null) {
					this.progressDialog = ProgressDialog
							.show(this, " Lanean...", " Iragarkiak eskuratzen",
									true, false);
					//Beste hari batean exekutatu iragarkian eskuratzea
					new Thread() {
						@Override
						public void run() {
							try {
								iragarkiak = ie.getIragarkiak(url);
								handler.sendEmptyMessage(5);
							} catch (UnknownHostException e) {
								handler.sendEmptyMessage(1); // error UnknownHostException
							} catch (SocketTimeoutException e) {
								handler.sendEmptyMessage(2); // error SocketTimeoutException
							} catch (ConnectException e) {
								handler.sendEmptyMessage(3); // error ConnectException
							} catch (Exception e) {
								handler.sendEmptyMessage(4); // error Exception (IOException)
							}
							
						}
					}.start();
				}
			}
		} catch (IllegalThreadStateException e) {
			// Erroren bat gertatu da hariekin
			handler.sendEmptyMessage(6);
		}

	}

	/**
	 * Gordetako iragarki zerrenda (kategoria) pasatu, kategoriaren url, iragarki-eskuratzailea eta kategoria
	 * pasatuta, kategoria jakin horretako iragarki zerrenda eskuratu iragarki-eskuratzailea erabilita
	 * @param IragarkiakKategoria
	 * @param url
	 * @param ie
	 * @param kategoria
	 */
	private void ezarriIragakiZerrenda(
			ArrayList<Iragarkia> IragarkiakKategoria, final String url,
			final IragarkiEskuratzailea ie, final String kategoria) {
		// final IragarkiEskuratzailea ie = new IragarkiEskuratzailea(url);
		final IragarkiOholaApplication aplikazioa = (IragarkiOholaApplication) getApplication();
		if (IragarkiakKategoria == null) {
			this.progressDialog = ProgressDialog.show(this, " Lanean...",
					" Iragarkiak eskuratzen", true, false);
			new Thread() {
				@Override
				public void run() {
					try {
						iragarkiak = ie.getIragarkiak(url);
						handler.sendEmptyMessage(5);
					} catch (UnknownHostException e) {
						handler.sendEmptyMessage(1);
					} catch (SocketTimeoutException e) {
						handler.sendEmptyMessage(2);
					} catch (ConnectException e) {
						handler.sendEmptyMessage(3);
					} catch (Exception e) {
						handler.sendEmptyMessage(4);
					}
					if (iragarkiak != null) {
						Class<?> c;
						try {
							// Java erreflexioa kategoria bakoitzari dagokion
							// metodoa deitzeko
							// printzipioz catch ataleko erroreak ez lirateke
							// gertatu beharko
							// kontrolatutako klase bat delako eta metodoak ziur
							// definituta daudela
							
							/**
							 * honekin iraunkortasuna lantzen da.
							 * Lehenego aldian iragarki zerrenda saretik ekartzen da eta gero
							 * aplikazioan bertan gordetzen da eta ez da beharrezkoa berriro iragarkia ekartzea
							 * 
							 */
							Class<?> partypes[] = new Class[1];
							// parametro motak prestatu
							partypes[0] = ArrayList.class;
							c = aplikazioa.getClass();
							Method method = c.getMethod("setIragarkiak"
									+ kategoria, partypes);
							// parametroak prestatu
							Object arg[] = new Object[1];
							arg[0] = iragarkiak;
							// metodoari deitu, gorde informazioa modu
							// iraunkorrean berriro saretik hartu behar ez
							// izateko
							
							// aplikazioaren set-errati deitzeko
							//aplikazioa.setIragarkiak____() erakoa
							method.invoke(aplikazioa, arg);
						} catch (SecurityException e) {
							handler.sendEmptyMessage(6);
						} catch (NoSuchMethodException e) {
							handler.sendEmptyMessage(6);
						} catch (IllegalArgumentException e) {
							handler.sendEmptyMessage(6);
						} catch (IllegalAccessException e) {
							handler.sendEmptyMessage(6);
						} catch (InvocationTargetException e) {
							handler.sendEmptyMessage(6);
						}
					}
				}
			}.start();
		} else { //Aldez aurretik kargatuta egon denez iragarki-zerrenda, ez da beharrezkoa berriro saretik eskuratzea
			if (IragarkiakKategoria.size() == 0) {
				empty.setText(R.string.bilaketa_hutsa);
			} else {
				//Zerrenda adaptadorearekin lotu.
				iragarkiak = IragarkiakKategoria;
				iragarkiadapter = new IragarkiAdapter(IragarkiZerrenda.this,
						R.layout.zerrenda_item, IragarkiakKategoria);
				setListAdapter(iragarkiadapter);
			}
		}
	}
	
	/**
	 * Iragarki zerrendarako adaptadore egokitua
	 * Item bakoitzaren elementuak definitzen dira baita iragazkia ere
	 * @author kudeatzailea
	 *
	 */
	private class IragarkiAdapter extends BaseAdapter implements Filterable {

		ArrayList<Iragarkia> iragarkiak_zerrendan;
		private IragarkiFilter mFilter;

		public IragarkiAdapter(Context context, int textViewResourceId,
				ArrayList<Iragarkia> iragarkiak) {

			this.iragarkiak_zerrendan = iragarkiak;
		}

		/**
		 * Aukeratu egokitutako iragazkia: IragarkiFilter
		 */
		@Override
		public Filter getFilter() {
			if (null == mFilter) {
				mFilter = new IragarkiFilter();
			}
			return mFilter;
		}

		//Bista prestatu
		// @Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			if (v == null) { // Bista ez badago baliabideetatik eskuratu
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.zerrenda_item, null);
			}
			
			// Iragarkia irudia izanez gero zerrendan irudiaren ikonoa erakutsi
			ImageView irudi = (ImageView) v.findViewById(R.id.backgroundItem);

			if (iragarkiak_zerrendan.get(position).irudia) {
				irudi.setVisibility(View.VISIBLE);
			} else {
				irudi.setVisibility(View.INVISIBLE);
			}

			// Eremuak osatu
			
			//Goiburua: iragarkia argitaratu deneko data eta egilea
			TextView data = (TextView) v.findViewById(R.id.data);
			data.setText(iragarkiak_zerrendan.get(position).data + " - "
					+ iragarkiak_zerrendan.get(position).egilea);
			//Deskribapen laburra
			TextView edukia = (TextView) v.findViewById(R.id.edukia);
			//edukia.setText(Html.fromHtml(iragarkiak_zerrendan.get(position).edukia));
			edukia.setText(iragarkiak_zerrendan.get(position).edukia);
			
			//Kategoria
			TextView atala = (TextView) v.findViewById(R.id.atala);
			atala.setText(iragarkiak_zerrendan.get(position).atala);

			return v;
		}

		public int getCount() {
			return iragarkiak_zerrendan.size();
		}

		public Object getItem(int position) {
			return iragarkiak_zerrendan.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		/**
		 * Iragazkia. Hizki bakoitzeko errebisatzen da deskribapen laburrean dagoen eta horren arabera
		 * zerrendako osagaiak murrizten doaz.
		 * Hasierako zerrenda bikoizten da. Honela bi zerrendekin egingo da lan, osao eta iragazitakoa
		 * @author kudeatzailea
		 *
		 */
		private class IragarkiFilter extends Filter {
			@Override
			// TODO clone() metodoak ez ditu generikoak onartzen.
			@SuppressWarnings("unchecked")
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();
				//iragazkia minuskuletara pasa gero alderaketa egiteko
				String prefixString = null == prefix ? null : prefix.toString()
						.toLowerCase();
				ArrayList<Iragarkia> iragarkiak_filter;

				if (null != prefixString && prefixString.length() > 0) {
					// Iragarkien kopia sinplea. Honek funtzionatzen du
					// zerrendako osagaiak ez direlako aldatze. Ezaugarri hau aldatuko balitz 
					// beste sinkronizazio estategia bat erabili beharko litzateke
					synchronized (iragarkiak_zerrendan) {
						iragarkiak_filter = (ArrayList<Iragarkia>) iragarkiak
								.clone();
					}
					
					//Osagaiak atzetik aurrera korritzen dira zerrendatik elementuak ezabatzen joango garelako eta
					//indizeetan ez eragiteko

					for (int i = iragarkiak_filter.size() - 1; i >= 0; --i) {
						Iragarkia titulazioa = iragarkiak_filter.get(i);
						String deskribapena = titulazioa.edukia.toLowerCase();
						// if (!description.startsWith(prefixString)) {
						//zerrendan ez badago gako horrekin elementurik ezabatu. Deskribapen laburrean begiratzen da
						if (deskribapena.indexOf(prefixString) < 0) {
							iragarkiak_filter.remove(i);
						}else{
							//TODO ordezkatu testua nabarmenduta agertzeko topatutakoa
							//http://stackoverflow.com/questions/5754363/android-how-to-replace-part-of-a-string-by-another-string
						
						/*String text2 = text + CepVizyon.getPhoneCode() + "\n\n"
						            + getText(R.string.currentversion) + CepVizyon.getLicenseText();
						Spannable WordtoSpan = new SpannableString(text2);
						WordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), text.length, (text + CepVizyon.getPhoneCode()).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						myTextView.setText(WordtoSpan);
						*/
							//titulazioa.edukia=deskribapena.replaceAll(prefixString, "<font color=\"#000000\" bgcolor=\"#ffffff\">" + prefixString + "</font>");
							
							//iragarkiak_filter.set(i,titulazioa);
						}
					}

					results.values = iragarkiak_filter;
					results.count = iragarkiak_filter.size();
				} else {
					// Ez denez gakorik zehaztu zerrenda osoa bueltatu. Hasierakoa, ezer iragazki barik duena.
					
					synchronized (iragarkiak) {
						results.values = iragarkiak;
						results.count = iragarkiak.size();
					}
				}

				return results;

			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence prefix,
					FilterResults results) {
				// ArrayList<Iragarkia> iragarkiak_filter;
				// Oharra: Funtzio hau beti deitzen da UI haritik
				iragarkiak_zerrendan = (ArrayList<Iragarkia>) results.values;

				//Iragazkia aplikatu bada eta apdatadorearen zerrenda aldatu bada
				//jakinarazi zerrndako bista freskatzeko
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					empty.setText(R.string.bilaketa_hutsa);
					notifyDataSetInvalidated();
				}

			}
		}
	}

}