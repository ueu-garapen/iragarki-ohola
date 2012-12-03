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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.tekle.oss.android.connectivity.NetworkConnectivity;
import com.tekle.oss.android.connectivity.NetworkMonitorListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class IragarkiaBidali extends Activity implements
NetworkMonitorListener {
	private static final int PICK_IMAGE = 1;

	private EditText izena;
	private EditText izenburua;
	private EditText eposta;
	private EditText telefonoa;
	private EditText mezua;
	private CheckBox ohar_legala;
	private EditText errobota;

	private ImageView imgView;
	private Button upload, bidali;
	private Bitmap bitmap;
	private ProgressDialog dialog;
	private Spinner atala;

	private IragarkiaBidali ekintza;
	private AlertDialog balidatuDialogoa;

	
	String filePath = null;

	/**
	 * Prestatu iragarki berria bidaltzeko formularioa
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iragarkia_bidali);

		this.ekintza = this;
		this.izena = (EditText) findViewById(R.id.editTextIzena);
		this.izenburua = (EditText) findViewById(R.id.editTextIzenburua);
		this.eposta = (EditText) findViewById(R.id.editTextEposta);
		this.telefonoa = (EditText) findViewById(R.id.editTextTelefonoa);
		this.mezua = (EditText) findViewById(R.id.editTextDeskribapena);
		this.ohar_legala = (CheckBox) findViewById(R.id.checkBoxlegalAdviceBidali);
		this.errobota = (EditText) findViewById(R.id.editTextErrobota);

		this.atala = (Spinner) findViewById(R.id.spinnerAtala);
		imgView = (ImageView) findViewById(R.id.ImageView);
		upload = (Button) findViewById(R.id.Upload);
		bidali = (Button) findViewById(R.id.iragarkiaBidali);

		// Iragarkiak irudirik badu galeriatik kargatu
		upload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(),
						"Aukeratu irudi bat.",
						Toast.LENGTH_SHORT).show();
				try {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(
							Intent.createChooser(intent, "Irudia aukeratu"),
							PICK_IMAGE);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// Formularioa bidali
		bidali.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (balidatu()) {
					dialog = ProgressDialog.show(IragarkiaBidali.this,
							"Fitxategia igotzen eta mezua bidaltzen",
							"Itxaron mesedez...", true);
					//Formularioa eta irudia bidali
					new ImageUploadTask().execute();
				}

			}
		});

		ArrayAdapter<String> iragarki_motak = new ArrayAdapter<String>(this,
				R.layout.spinner_view, getResources().getStringArray(
						R.array.iragarki_kategoriak));
		iragarki_motak.setDropDownViewResource(R.layout.spinner_view_dropdown);
		this.atala.setAdapter(iragarki_motak);
		
		// Sareko monitora prestatu
		NetworkConnectivity.sharedNetworkConnectivity().configure(this);
		NetworkConnectivity.sharedNetworkConnectivity()
				.addNetworkMonitorListener(this);
		NetworkConnectivity.sharedNetworkConnectivity().startNetworkMonitor();

	}
	
	@Override
	public void onPause(){
		super.onPause();
		//Atzeko planora pasatzean desaktibatu sareko monitorea 
		NetworkConnectivity.sharedNetworkConnectivity().stopNetworkMonitor();
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
		
		//itxi dialogo leiho guztiak
		if(dialog != null && dialog.isShowing()){
			dialog.dismiss();
		}
		if(balidatuDialogoa != null && balidatuDialogoa.isShowing()){
			balidatuDialogoa.dismiss();
		}
		this.finish();
	}

	// Irudia galeriatik kargatu
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImageUri = data.getData();
				

				try {
					// OI Fitxategi kudeatzailea
					String filemanagerstring = selectedImageUri.getPath();

					// MEDIA galeria
					String selectedImagePath = getPath(selectedImageUri);

					if (selectedImagePath != null) {
						filePath = selectedImagePath;
					} else if (filemanagerstring != null) {
						filePath = filemanagerstring;
					} else {
						Toast.makeText(getApplicationContext(), "Kokapen ezezaguna",
								Toast.LENGTH_LONG).show();
					}

					if (filePath != null) {
						Thread haria = new Thread() {
							@Override
							public void run() {
								decodeFile(filePath);
							}
						};
						haria.run();
						
					} else {
						bitmap = null;
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							"Barne-errore bat egon da.", Toast.LENGTH_LONG)
							.show();
				}
			}
			break;
		default:
		}
	}

	/**
	 * Fromularioa bidali unibertsitatea.net-era. Irudia ere zehaztu bada irudia bidali
	 * @author UEU
	 *
	 */
	class ImageUploadTask extends AsyncTask<Void, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(Void... unsued) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				// Retrieving error 417 at Post request zuzentzeko
				HttpParams params = httpClient.getParams();
				params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
						false);
				
				HttpContext localContext = new BasicHttpContext();
				
				//Prestatu HttpPost eskaera
				HttpPost httpPost = new HttpPost(
						Konstanteak.URL_IRAGARKIA_BIDALI);
				MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				byte[] data = {};
				//irudia badago bidali
				if (bitmap != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream(32);
					bitmap.compress(CompressFormat.JPEG, 100, bos);
					data = bos.toByteArray();
					entity.addPart("image", new ByteArrayBody(data,
							"myImage.jpg"));
				} else {// fitxategia hutsik bidali
					entity.addPart("image", new ByteArrayBody(data, ""));
				}
				
				//Formularioko gainontzeko eremuak bidaltzeko prestatu
				entity.addPart("atala", new StringBody(atala.getSelectedItem()
						.toString().toLowerCase()));
				entity.addPart("title", new StringBody(izenburua.getText()
						.toString()));
				entity.addPart("name", new StringBody(izena.getText()
						.toString()));
				entity.addPart("email", new StringBody(eposta.getText()
						.toString()));
				entity.addPart("phone", new StringBody(telefonoa.getText()
						.toString()));
				entity.addPart("description", new StringBody(mezua.getText()
						.toString()));
				entity.addPart("galdera", new StringBody("12"));
				entity.addPart("legalAdvice", new StringBody("accept"));
				entity.addPart("form.submitted", new StringBody("1"));

				httpPost.setEntity(entity);

				HttpResponse response = httpClient.execute(httpPost,
						localContext);
				return response;
			} catch (IOException e) {
				if (dialog.isShowing())
					dialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Sare-arazo bat egon da.", Toast.LENGTH_LONG).show();
				return null;
			} catch (Exception e) {
				if (dialog.isShowing())
					dialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Barne-errore bat egon da.", Toast.LENGTH_LONG).show();
				return null;
			}

		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		//Bidalketa bukatu ondoren horren berri eman edo errorea egon dela adierazi
		@Override
		protected void onPostExecute(HttpResponse sResponse) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();

				if (sResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

					Toast.makeText(getApplicationContext(),
							"Zure erantzuna ondo bidali da", Toast.LENGTH_SHORT)
							.show();
					// leihoa itxi eta hasierako pantailara joan
					ekintza.finish();
				} else {
					Toast.makeText(
							getApplicationContext(),
							"Errore bat gertatu da eta zure erantzuna ezin izan da ondo bidali. Saiatu berriro.",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	//irudiaren kokapena eskuratu
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	//Irudiaren tamaina eskalatu
	public void decodeFile(final String filePath) {
		// Decode image size
		if (bitmap != null) {
			bitmap.recycle();
		}

		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(filePath, o2);
		imgView.setImageBitmap(bitmap);
		imgView.setVisibility(View.VISIBLE);
	}

	//Formularioa balidatu
	private boolean balidatu() {
		boolean valid = true;

		StringBuilder validationText = new StringBuilder();

		if (!NetworkConnectivity.sharedNetworkConnectivity().isConnected()) {
			validationText.append("Sareko konexioa beharrezkoa da");
			balidatuDialogoa  = new AlertDialog.Builder(this)
					.setTitle("Errorea")
					.setMessage(validationText.toString())
					.setPositiveButton(
							"Segi",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int arg1) {
									IragarkiaBidali.this.finish();
								}
							}).show();
			validationText = null;
			valid = false;

		} else {
			if ((this.izena.getText() == null)
					|| this.izena.getText().toString().equals("")) { // bidaltzailearen
																		// izena
				validationText.append("Izena derrigorrezkoa da");
				valid = false;
			}
			if ((this.izenburua.getText() == null)
					|| this.izenburua.getText().toString().equals("")) { // bidaltzailearen
																			// izena
				validationText.append("\nIzenburua derrigorrezkoa da");
				valid = false;
			}
			if ((this.eposta.getText() == null)
					|| this.eposta.getText().toString().equals("")) { // bidaltzailearen
																		// eposta
				validationText.append("\nE-posta derrigorrezkoa da");
				valid = false;
			}
			if ((this.mezua.getText() == null)
					|| this.mezua.getText().toString().equals("")) {
				validationText.append("\nMezua derrigorrezkoa da");
				valid = false;
			}
			if (!this.ohar_legala.isChecked()) {
				validationText
						.append("\nOhar legala onartzea derrigorrezkoa da");
				valid = false;
			}
			if ((this.errobota.getText() == null)
					|| !this.errobota.getText().toString().equals("12")) {
				validationText
						.append("\nErrobota ez zarela egiaztatzea derrigorrezkoa da");
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
		}
		return valid;

	}

}