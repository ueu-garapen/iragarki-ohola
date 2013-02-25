package org.ueu.uninet.it;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class IragarkiaLaguntza extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.laguntza);
		WebView webview_testua = (WebView) findViewById(R.id.laguntzatestua);
		webview_testua.loadData(getString(R.string.laguntza), "text/html", "utf-8");
	}

}
