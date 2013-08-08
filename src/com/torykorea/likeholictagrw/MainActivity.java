package com.torykorea.likeholictagrw;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	boolean mWriteMode = false;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;
	private AlertDialog.Builder alertDialog;
	private AlertDialog resultDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((Button) findViewById(R.id.tagWriteBtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mNfcAdapter = NfcAdapter
								.getDefaultAdapter(MainActivity.this);
						mNfcPendingIntent = PendingIntent.getActivity(
								MainActivity.this,
								0,
								new Intent(MainActivity.this,
										MainActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
								0);

						enableTagWriteMode();

						alertDialog = new AlertDialog.Builder(MainActivity.this);
						alertDialog.setTitle("Touch tag to device");
						alertDialog
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										disableTagWriteMode();
									}
								});
						resultDialog = alertDialog.show();
					}
				});
	}

	private void enableTagWriteMode() {
		mWriteMode = true;
		IntentFilter tagDetected = new IntentFilter(
		// NfcAdapter.ACTION_TECH_DISCOVERED);
				NfcAdapter.ACTION_TAG_DISCOVERED);
		IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
		mWriteMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Tag writing mode
		if (mWriteMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			byte[] one = "pid=".getBytes();
			byte[] two = ((TextView) findViewById(R.id.pidEt)).getText()
					.toString().getBytes();
			byte[] three = "?index=".getBytes();
			byte[] four = ((TextView) findViewById(R.id.indexEt)).getText()
					.toString().getBytes();
			byte[] five = "?name=".getBytes();
			byte[] six = ((TextView) findViewById(R.id.shopNameEt)).getText()
					.toString().getBytes();
			byte[] combinedData = new byte[one.length + two.length
					+ three.length + four.length + five.length + six.length];

			System.arraycopy(one, 0, combinedData, 0, one.length);
			System.arraycopy(two, 0, combinedData, one.length, two.length);
			System.arraycopy(three, 0, combinedData, one.length + two.length,
					three.length);
			System.arraycopy(four, 0, combinedData, one.length + two.length
					+ three.length, four.length);
			System.arraycopy(five, 0, combinedData, one.length + two.length
					+ three.length + four.length, five.length);
			System.arraycopy(six, 0, combinedData, one.length + two.length
					+ three.length + four.length + five.length, six.length);

			// byte[] uriField =
			// "http://market.android.com/details?id=com.torykorea.likeholic"
			// .getBytes(Charset.forName("US-ASCII"));
			byte[] uriField = "market://details?id=com.torykorea.likeholic"
					.getBytes(Charset.forName("US-ASCII"));
			byte[] payload = new byte[uriField.length + 1]; // add 1 for the

//			System.arraycopy(uriField, 0, payload, 1, uriField.length);
//			// appends URI to payload
//			NdefRecord uriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
//					NdefRecord.RTD_URI, new byte[0], payload);
//
//			// NdefRecord idRecord = NdefRecord.createMime(
//			// "text/plain", combinedData);
//			NdefMessage idRecord = new NdefMessage(
//					new NdefRecord[] { NdefRecord
//							.createMime("application/com.torykorea.likeholic",
//									combinedData)

//					});

			NdefMessage message = new NdefMessage(
					new NdefRecord[] {
							NdefRecord.createMime("application/likeholic",
									combinedData)
							/**
							 * The Android Application Record (AAR) is commented
							 * out. When a device receives a push with an AAR in
							 * it, the application specified in the AAR is
							 * guaranteed to run. The AAR overrides the tag
							 * dispatch system. You can add it back in to
							 * guarantee that this activity starts when
							 * receiving a beamed message. For now, this code
							 * uses the tag dispatch system.
							 */
							,
							NdefRecord
									.createApplicationRecord("com.torykorea.likeholic") });

			// NdefMessage message = new NdefMessage(new NdefRecord[] {
			// NdefRecord.createMime(
			// "application/com.torykorea.likeholic", combinedData ) });

			if (writeTag(message, detectedTag)) {
				Toast.makeText(this, "Success: Wrote id to nfc tag",
						Toast.LENGTH_LONG).show();
				resultDialog.dismiss();
			}

		}
	}

	/*
	 * Writes an NdefMessage to a NFC tag
	 */
	public boolean writeTag(NdefMessage message, Tag tag) {

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					Toast.makeText(getApplicationContext(),
							"Error: tag not writable", Toast.LENGTH_SHORT)
							.show();
					return false;
				}
				if (ndef.getMaxSize() < 100) {
					Toast.makeText(getApplicationContext(),
							"Error: tag too small", Toast.LENGTH_SHORT).show();
					return false;
				}
				ndef.writeNdefMessage(message);
				ndef.makeReadOnly();
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
